/**
 * Dynamic test-data seed helpers.
 *
 * Instead of hardcoding IDs, each spec calls the relevant list operation
 * during beforeAll to discover real IDs present in the live environment.
 * This file provides typed builders for mutation payloads so every spec
 * generates unique, traceable test records.
 */

// ─────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────

/** Returns an ISO timestamp string used to make test records unique. */
export const testTimestamp = () => new Date().toISOString().replace(/[:.]/g, '-');

/** Generates a clearly identifiable test prefix so artefacts are easy to find. */
export const testPrefix = () => `[AUTO-TEST-${Date.now()}]`;

// ─────────────────────────────────────────────────────────────
// Device test data
// ─────────────────────────────────────────────────────────────

export const deviceStatuses = ['Online', 'Offline', 'Maintenance'] as const;
export type DeviceStatus = (typeof deviceStatuses)[number];

export function newDevicePayload(overrides: Partial<Record<string, unknown>> = {}) {
  const ts = testTimestamp();
  return {
    deviceName: `${testPrefix()} Device-${ts}`,
    serialNumber: `SN-AUTO-${ts}`,
    model: 'XR-5000',
    status: 'Offline' as DeviceStatus,
    location: 'Test Lab',
    customerId: 'test-customer-001',
    firmwareVersion: 'v1.0.0',
    health: 80,
    ...overrides,
  };
}

// ─────────────────────────────────────────────────────────────
// Firmware test data
// ─────────────────────────────────────────────────────────────

export const firmwareStatuses = ['Pending', 'Approved', 'Deprecated', 'Rejected'] as const;
export type FirmwareStatus = (typeof firmwareStatuses)[number];

export function newFirmwarePayload(overrides: Partial<Record<string, unknown>> = {}) {
  const ts = testTimestamp();
  return {
    name: `${testPrefix()} FW-${ts}`,
    version: `v0.0.${Date.now() % 1000}`,
    deviceModel: 'XR-5000',
    releaseDate: new Date().toISOString().split('T')[0],
    fileSize: 1536000,
    checksum: `sha256:auto${ts.replace(/-/g, '')}`,
    ...overrides,
  };
}

// ─────────────────────────────────────────────────────────────
// Service Order test data
// ─────────────────────────────────────────────────────────────

export const serviceOrderStatuses = ['Pending', 'In Progress', 'Completed', 'Cancelled'] as const;
export type ServiceOrderStatus = (typeof serviceOrderStatuses)[number];

export function newServiceOrderPayload(overrides: Partial<Record<string, unknown>> = {}) {
  const ts = testTimestamp();
  return {
    title: `${testPrefix()} Service-${ts}`,
    description: 'Automated test service order — safe to delete',
    priority: 'Low',
    scheduledDate: new Date(Date.now() + 86_400_000).toISOString().split('T')[0],
    ...overrides,
  };
}

// ─────────────────────────────────────────────────────────────
// Compliance test data
// ─────────────────────────────────────────────────────────────

export const certifications = ['HIPAA', 'ISO 27001', 'FCC', 'WiFi Alliance', 'CE'] as const;
export type Certification = (typeof certifications)[number];

export function newCompliancePayload(
  firmwareId: string,
  overrides: Partial<Record<string, unknown>> = {},
) {
  return {
    firmwareId,
    firmwareVersion: 'v0.0.1',
    deviceModel: 'XR-5000',
    certifications: ['ISO 27001'],
    vulnerabilities: 0,
    description: 'Automated test compliance submission — safe to delete',
    ...overrides,
  };
}

// ─────────────────────────────────────────────────────────────
// Audit log date ranges
// ─────────────────────────────────────────────────────────────

export const auditLogRanges = {
  /** Last 24 hours (matches Dashboard usage) */
  last24h: () => ({
    startDate: new Date(Date.now() - 86_400_000).toISOString(),
    endDate: new Date().toISOString(),
  }),
  /** Last 7 days */
  last7Days: () => ({
    startDate: new Date(Date.now() - 7 * 86_400_000).toISOString(),
    endDate: new Date().toISOString(),
  }),
  /** Last 30 days */
  last30Days: () => ({
    startDate: new Date(Date.now() - 30 * 86_400_000).toISOString(),
    endDate: new Date().toISOString(),
  }),
  /** Future range — should return empty results */
  future: () => ({
    startDate: new Date(Date.now() + 86_400_000).toISOString(),
    endDate: new Date(Date.now() + 2 * 86_400_000).toISOString(),
  }),
};

// ─────────────────────────────────────────────────────────────
// Invalid / negative test values
// ─────────────────────────────────────────────────────────────

export const INVALID_ID = 'non-existent-id-00000000-0000-0000-0000-000000000000';
export const INVALID_EMAIL = 'not-a-real-user@invalid-domain-xyz.com';
export const INVALID_STATUS = 'INVALID_STATUS_VALUE';
export const EMPTY_STRING = '';
