import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './specs',
  testMatch: '**/*.spec.ts',
  timeout: 120000,
  globalSetup: './specs/api/global-setup.ts',
  use: {
    headless: false,
    baseURL: 'https://main.dddsig2mih3hw.amplifyapp.com',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1280, height: 720 },
      },
    },
  ],
});
