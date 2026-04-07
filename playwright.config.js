// @ts-check
/** @type {import('@playwright/test').PlaywrightTestConfig} */
const config = {
  testDir: './specs',
  testMatch: '**/geo-dom-inspect.spec.ts',
  timeout: 120000,
  use: {
    headless: false,
    baseURL: 'https://main.dddsig2mih3hw.amplifyapp.com',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        browserName: 'chromium',
        viewport: { width: 1280, height: 720 },
      },
    },
  ],
};

module.exports = config;
