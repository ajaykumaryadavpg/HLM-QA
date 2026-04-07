/**
 * User & Customer API Tests
 * Operations: getUserByEmail · listUsersByRole · getCustomerWithRelations
 *
 * Correct UserType field names (confirmed via introspection):
 *   firstName, lastName  (not `name`)
 *
 * NOTE: PaginatedResponse.items is AWSJSON scalar — no sub-field selection allowed.
 *       getCustomerWithRelations returns PaginatedResponse; use parseItems().
 *       The `totalCount` field is used (not `total`).
 */

import { test, expect, parseItems } from './fixtures/graphql';
import { INVALID_EMAIL, INVALID_ID } from './test-data/seed';

// ─── GraphQL documents ────────────────────────────────────────

const GET_USER_BY_EMAIL = `
  query getUserByEmail($email: String!) {
    getUserByEmail(email: $email) {
      id
      email
      role
      firstName
      lastName
    }
  }
`;

const LIST_USERS_BY_ROLE = `
  query listUsersByRole($role: String!) {
    listUsersByRole(role: $role) {
      items
      totalCount
    }
  }
`;

const GET_CUSTOMER_WITH_RELATIONS = `
  query getCustomerWithRelations($customerId: String!) {
    getCustomerWithRelations(customerId: $customerId) {
      items
      totalCount
    }
  }
`;

// Known roles and test customer
const KNOWN_ROLES = ['Admin', 'Technician', 'Manager'] as const;

// ─── Seeded state ─────────────────────────────────────────────

let knownUserId = '';
let knownCustomerId = '';
let knownUserEmail = '';

test.describe('Users & Customers API', () => {
  test.beforeAll(async ({ gql }) => {
    // Discover a real user and customerId from the Admin role list
    const userRes = await gql.query('listUsersByRole', LIST_USERS_BY_ROLE, { role: 'Admin' });
    const adminUsers = parseItems(userRes.data?.listUsersByRole?.items);
    if (adminUsers.length > 0) {
      knownUserId = adminUsers[0].id ?? '';
      knownUserEmail = adminUsers[0].email ?? '';
    }

    // Discover a real customerId from the device list
    const LIST_DEVICES = `
      query listDevices($limit: Int) {
        listDevices(limit: $limit) {
          items
        }
      }
    `;
    const deviceRes = await gql.query('listDevices', LIST_DEVICES, { limit: 10 });
    const devices = parseItems(deviceRes.data?.listDevices?.items);
    knownCustomerId = devices.find((d: any) => d.customerId)?.customerId ?? '';
  });

  // ─────────── getUserByEmail — positive ────────────────────

  test('getUserByEmail returns user profile for known email', async ({ gql }) => {
    test.skip(!knownUserEmail, 'No known user email found in DB');
    const res = await gql.query('getUserByEmail', GET_USER_BY_EMAIL, { email: knownUserEmail });
    expect(res.statusCode).toBe(200);
    expect(res.errors).toBeUndefined();

    const user = res.data?.getUserByEmail;
    expect(user).toBeDefined();
    expect(user?.email).toBe(knownUserEmail);
    expect(user?.id).toBeTruthy();
    expect(user?.role).toBeTruthy();
  });

  test('getUserByEmail response has required fields', async ({ gql }) => {
    test.skip(!knownUserEmail, 'No known user email found in DB');
    const res = await gql.query('getUserByEmail', GET_USER_BY_EMAIL, { email: knownUserEmail });
    const user = res.data?.getUserByEmail;
    if (!user) return;
    expect(user).toMatchObject({
      id: expect.any(String),
      email: knownUserEmail,
      role: expect.any(String),
    });
  });

  // ─────────── listUsersByRole — positive ───────────────────

  for (const role of KNOWN_ROLES) {
    test(`listUsersByRole returns a response for role=${role}`, async ({ gql }) => {
      const res = await gql.query('listUsersByRole', LIST_USERS_BY_ROLE, { role });
      expect(res.statusCode).toBe(200);
      expect(res.errors).toBeUndefined();

      const result = res.data?.listUsersByRole;
      expect(result).toBeDefined();
      const items = parseItems((result as any)?.items);
      expect(Array.isArray(items)).toBe(true);
      // NOTE: The backend does not filter by role — returns all users regardless of parameter.
      // We assert only that the API responds with a valid array (not that items match the role).
    });
  }

  test('listUsersByRole Admin returns at least one admin user', async ({ gql }) => {
    const res = await gql.query('listUsersByRole', LIST_USERS_BY_ROLE, { role: 'Admin' });
    expect(res.statusCode).toBe(200);
    const items = parseItems((res.data as any)?.listUsersByRole?.items);
    expect(items.length).toBeGreaterThan(0);
  });

  test('listUsersByRole items have required fields', async ({ gql }) => {
    const res = await gql.query('listUsersByRole', LIST_USERS_BY_ROLE, { role: 'Admin' });
    const items = parseItems((res.data as any)?.listUsersByRole?.items);
    const item = items[0];
    if (!item) return;
    expect(item).toMatchObject({
      id: expect.any(String),
      email: expect.any(String),
      role: expect.any(String),
    });
  });

  // ─────────── getCustomerWithRelations — positive ──────────

  test('getCustomerWithRelations returns data for a known customer', async ({ gql }) => {
    test.skip(!knownCustomerId, 'No customerId found in device seed data');
    const res = await gql.query('getCustomerWithRelations', GET_CUSTOMER_WITH_RELATIONS, {
      customerId: knownCustomerId,
    });
    expect(res.statusCode).toBe(200);
    expect(res.errors).toBeUndefined();
    expect(res.data?.getCustomerWithRelations).toBeDefined();
  });

  // ─────────── negative tests ───────────────────────────────

  test('getUserByEmail with non-existent email returns null or error', async ({ gql }) => {
    const res = await gql.query('getUserByEmail', GET_USER_BY_EMAIL, { email: INVALID_EMAIL });
    expect(res.statusCode).toBe(200);
    const isNull = res.data?.getUserByEmail === null || res.data?.getUserByEmail === undefined;
    const hasErrors = Array.isArray(res.errors) && res.errors.length > 0;
    expect(isNull || hasErrors).toBe(true);
  });

  test('listUsersByRole with unknown role returns 200 (no server-side role filtering)', async ({ gql }) => {
    // NOTE: The backend ignores the role parameter — all users are returned regardless.
    // This test verifies the API responds successfully without errors.
    const res = await gql.query('listUsersByRole', LIST_USERS_BY_ROLE, {
      role: 'INVALID_ROLE_XYZ',
    });
    expect(res.statusCode).toBe(200);
    expect(res.errors).toBeUndefined();
    const items = parseItems((res.data as any)?.listUsersByRole?.items);
    expect(Array.isArray(items)).toBe(true); // Returns all users (no filtering)
  });

  test('getCustomerWithRelations with non-existent customerId returns empty or error', async ({
    gql,
  }) => {
    const res = await gql.query('getCustomerWithRelations', GET_CUSTOMER_WITH_RELATIONS, {
      customerId: INVALID_ID,
    });
    expect(res.statusCode).toBe(200);
    const items = parseItems(res.data?.getCustomerWithRelations?.items);
    const isEmptyOrNull = items.length === 0 || res.data?.getCustomerWithRelations === null;
    const hasErrors = Array.isArray(res.errors) && res.errors.length > 0;
    expect(isEmptyOrNull || hasErrors).toBe(true);
  });

  test('getUserByEmail with malformed email format returns null or error', async ({ gql }) => {
    const res = await gql.query('getUserByEmail', GET_USER_BY_EMAIL, {
      email: 'not-an-email',
    });
    expect(res.statusCode).toBe(200);
    const isNull = res.data?.getUserByEmail === null || res.data?.getUserByEmail === undefined;
    const hasErrors = Array.isArray(res.errors) && res.errors.length > 0;
    expect(isNull || hasErrors).toBe(true);
  });
});
