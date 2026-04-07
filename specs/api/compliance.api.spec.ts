/**
 * Firmware Compliance API Tests
 * Operations: getCompliance · listComplianceByStatus · getComplianceByCertification
 *
 * NOTE: PaginatedResponse.items is AWSJSON scalar — no sub-field selection allowed.
 *       The `totalCount` field is used (not `total`).
 */

import { test, expect, parseItems } from './fixtures/graphql';
import { certifications, INVALID_ID } from './test-data/seed';

// ─── GraphQL documents ────────────────────────────────────────

const LIST_COMPLIANCE_BY_STATUS = `
  query listComplianceByStatus($status: String!) {
    listComplianceByStatus(status: $status) {
      items
      totalCount
    }
  }
`;

const GET_COMPLIANCE = `
  query getCompliance($id: String!) {
    getCompliance(id: $id) {
      id
      firmwareId
      firmwareVersion
      deviceModel
      status
      certifications
      vulnerabilities
    }
  }
`;

const GET_COMPLIANCE_BY_CERTIFICATION = `
  query getComplianceByCertification($certification: String!) {
    getComplianceByCertification(certification: $certification) {
      items
      totalCount
    }
  }
`;

// ─── Seeded state ─────────────────────────────────────────────

let firstComplianceId = '';
let foundCertification = '';

test.describe('Compliance API', () => {
  test.beforeAll(async ({ gql }) => {
    const res = await gql.query('listComplianceByStatus', LIST_COMPLIANCE_BY_STATUS, {
      status: 'Approved',
    });
    const items = parseItems(res.data?.listComplianceByStatus?.items);
    if (items.length > 0) {
      firstComplianceId = items[0].id;
      const certs = items[0].certifications;
      foundCertification = Array.isArray(certs)
        ? certs[0]
        : typeof certs === 'string'
          ? JSON.parse(certs)[0]
          : '';
    }
  });

  // ─────────── listComplianceByStatus — positive ────────────

  test('listComplianceByStatus Approved returns only approved records', async ({ gql }) => {
    const res = await gql.query('listComplianceByStatus', LIST_COMPLIANCE_BY_STATUS, {
      status: 'Approved',
    });
    expect(res.statusCode).toBe(200);
    expect(res.errors).toBeUndefined();
    const items = parseItems(res.data?.listComplianceByStatus?.items);
    items.forEach((c: any) => expect(c.status).toBe('Approved'));
  });

  test('listComplianceByStatus Pending returns only pending records', async ({ gql }) => {
    const res = await gql.query('listComplianceByStatus', LIST_COMPLIANCE_BY_STATUS, {
      status: 'Pending',
    });
    expect(res.statusCode).toBe(200);
    expect(res.errors).toBeUndefined();
    const items = parseItems(res.data?.listComplianceByStatus?.items);
    items.forEach((c: any) => expect(c.status).toBe('Pending'));
  });

  test('listComplianceByStatus Deprecated returns only deprecated records', async ({ gql }) => {
    const res = await gql.query('listComplianceByStatus', LIST_COMPLIANCE_BY_STATUS, {
      status: 'Deprecated',
    });
    expect(res.statusCode).toBe(200);
    const items = parseItems(res.data?.listComplianceByStatus?.items);
    items.forEach((c: any) => expect(c.status).toBe('Deprecated'));
  });

  test('listComplianceByStatus response items have required fields', async ({ gql }) => {
    const res = await gql.query('listComplianceByStatus', LIST_COMPLIANCE_BY_STATUS, {
      status: 'Approved',
    });
    const items = parseItems(res.data?.listComplianceByStatus?.items);
    const item = items[0];
    if (!item) return;
    expect(item).toMatchObject({
      id: expect.any(String),
      firmwareId: expect.any(String),
      status: 'Approved',
    });
  });

  // ─────────── getCompliance — positive ─────────────────────

  test('getCompliance returns record by valid ID', async ({ gql }) => {
    test.skip(!firstComplianceId, 'No compliance ID seeded');
    const res = await gql.query('getCompliance', GET_COMPLIANCE, { id: firstComplianceId });
    expect(res.statusCode).toBe(200);
    expect(res.errors).toBeUndefined();

    const record = res.data?.getCompliance;
    expect(record?.id).toBe(firstComplianceId);
    expect(record?.firmwareId).toBeTruthy();
    expect(record?.deviceModel).toBeTruthy();
  });

  // ─────────── getComplianceByCertification — positive ──────

  for (const cert of certifications) {
    test(`getComplianceByCertification returns records for ${cert} certification`, async ({ gql }) => {
      const res = await gql.query('getComplianceByCertification', GET_COMPLIANCE_BY_CERTIFICATION, {
        certification: cert,
      });
      expect(res.statusCode).toBe(200);
      expect(res.errors).toBeUndefined();
      expect(res.data?.getComplianceByCertification).toBeDefined();
    });
  }

  test('getComplianceByCertification returned items contain the requested cert', async ({
    gql,
  }) => {
    test.skip(!foundCertification, 'No certification found in seeded data');
    const res = await gql.query('getComplianceByCertification', GET_COMPLIANCE_BY_CERTIFICATION, {
      certification: foundCertification,
    });
    expect(res.statusCode).toBe(200);
    const items = parseItems(res.data?.getComplianceByCertification?.items);
    items.forEach((c: any) => {
      const certs = Array.isArray(c.certifications) ? c.certifications : JSON.parse(c.certifications ?? '[]');
      expect(certs).toContain(foundCertification);
    });
  });

  // ─────────── negative tests ───────────────────────────────

  test('getCompliance with non-existent ID returns null or error', async ({ gql }) => {
    const res = await gql.query('getCompliance', GET_COMPLIANCE, { id: INVALID_ID });
    expect(res.statusCode).toBe(200);
    const isNull = res.data?.getCompliance === null || res.data?.getCompliance === undefined;
    const hasErrors = Array.isArray(res.errors) && res.errors.length > 0;
    expect(isNull || hasErrors).toBe(true);
  });

  test('listComplianceByStatus with invalid status returns empty or error', async ({ gql }) => {
    const res = await gql.query('listComplianceByStatus', LIST_COMPLIANCE_BY_STATUS, {
      status: 'INVALID_XYZ',
    });
    expect(res.statusCode).toBe(200);
    const items = parseItems(res.data?.listComplianceByStatus?.items);
    const emptyOrError = items.length === 0 || (Array.isArray(res.errors) && res.errors.length > 0);
    expect(emptyOrError).toBe(true);
  });

  test('getComplianceByCertification with unknown certification returns empty', async ({ gql }) => {
    const res = await gql.query('getComplianceByCertification', GET_COMPLIANCE_BY_CERTIFICATION, {
      certification: 'UNKNOWN-CERT-XYZ',
    });
    expect(res.statusCode).toBe(200);
    const items = parseItems(res.data?.getComplianceByCertification?.items);
    expect(items.length).toBe(0);
  });
});
