import { test as base, request, APIRequestContext } from '@playwright/test';
import fs from 'fs';
import { STORAGE_STATE_PATH } from '../global-setup';

// ─────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────

export const APPSYNC_URL =
  'https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql';

// ─────────────────────────────────────────────────────────────
// Token helpers
// ─────────────────────────────────────────────────────────────

/** Extract the Cognito idToken from a Playwright storageState file. */
export function getIdTokenFromStorageState(path: string): string {
  const state = JSON.parse(fs.readFileSync(path, 'utf-8'));
  for (const origin of state.origins ?? []) {
    for (const entry of origin.localStorage ?? []) {
      if (entry.name.endsWith('.idToken')) return entry.value;
    }
  }
  throw new Error(`No idToken found in storageState at ${path}`);
}

// ─────────────────────────────────────────────────────────────
// GraphQL response type
// ─────────────────────────────────────────────────────────────

export interface GqlResponse<T = Record<string, unknown>> {
  data?: T;
  errors?: { message: string; locations?: unknown[]; path?: unknown[] }[];
  statusCode: number;
}

// ─────────────────────────────────────────────────────────────
// GraphQL helper factory
// ─────────────────────────────────────────────────────────────

export type GraphQLClient = {
  query<T = Record<string, unknown>>(
    operationName: string,
    query: string,
    variables?: Record<string, unknown>,
  ): Promise<GqlResponse<T>>;
  mutate<T = Record<string, unknown>>(
    operationName: string,
    mutation: string,
    variables?: Record<string, unknown>,
  ): Promise<GqlResponse<T>>;
};

function createGraphQLClient(apiContext: APIRequestContext, idToken: string): GraphQLClient {
  async function send<T>(
    operationName: string,
    document: string,
    variables: Record<string, unknown> = {},
  ): Promise<GqlResponse<T>> {
    const response = await apiContext.post(APPSYNC_URL, {
      headers: {
        'Content-Type': 'application/json',
        Authorization: idToken, // AppSync Cognito User Pool auth — no "Bearer" prefix
      },
      data: { operationName, query: document, variables },
    });
    const body = await response.json();
    return { ...body, statusCode: response.status() };
  }

  return {
    query: (name, doc, vars) => send(name, doc, vars),
    mutate: (name, doc, vars) => send(name, doc, vars),
  };
}

// ─────────────────────────────────────────────────────────────
// Unauthenticated helper (for negative auth tests)
// ─────────────────────────────────────────────────────────────

export async function unauthenticatedGql(
  operationName: string,
  query: string,
  variables: Record<string, unknown> = {},
): Promise<{ statusCode: number; body: unknown }> {
  const apiContext = await request.newContext();
  const response = await apiContext.post(APPSYNC_URL, {
    headers: { 'Content-Type': 'application/json' },
    data: { operationName, query, variables },
  });
  const body = await response.json();
  await apiContext.dispose();
  return { statusCode: response.status(), body };
}

// ─────────────────────────────────────────────────────────────
// Playwright fixture
// ─────────────────────────────────────────────────────────────

type ApiFixtures = {
  gql: GraphQLClient;
  idToken: string;
};

export const test = base.extend<ApiFixtures>({
  idToken: async ({}, use) => {
    const token = getIdTokenFromStorageState(STORAGE_STATE_PATH);
    await use(token);
  },

  gql: async ({ idToken }, use) => {
    const apiContext = await request.newContext();
    const client = createGraphQLClient(apiContext, idToken);
    await use(client);
    await apiContext.dispose();
  },
});

export { expect } from '@playwright/test';

/**
 * Parse AWSJSON items field from AppSync PaginatedResponse.
 * AppSync may return items as a JSON string or a pre-parsed array depending on
 * the client. This helper handles both cases transparently.
 */
export function parseItems(raw: unknown): any[] {
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try { return JSON.parse(raw); } catch { return []; }
  }
  return [];
}
