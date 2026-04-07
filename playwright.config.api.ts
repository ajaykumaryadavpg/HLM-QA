import { defineConfig } from '@playwright/test';

/**
 * Playwright configuration for HLM GraphQL API test suite.
 *
 * Run:  npx playwright test --config playwright.config.api.ts
 * HTML report is written to api-test-results/
 */
export default defineConfig({
  testDir: './specs/api',
  testMatch: '**/*.api.spec.ts',
  timeout: 30_000,
  globalSetup: './specs/api/global-setup.ts',

  use: {
    baseURL: 'https://main.dddsig2mih3hw.amplifyapp.com',
  },

  projects: [{ name: 'hlm-api' }],

  reporter: [
    ['list'],
    ['html', { outputFolder: 'api-test-results', open: 'never' }],
  ],
});
