import { chromium } from '@playwright/test';
import path from 'path';

export const STORAGE_STATE_PATH = path.resolve(__dirname, '../../.auth/storageState.json');

/**
 * Global setup — runs once before the entire API test suite.
 * Logs into the HLM Platform via browser and persists the Cognito tokens
 * (idToken / accessToken) to storageState.json so that API fixture tests
 * can extract them without re-authenticating each spec.
 */
export default async function globalSetup() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  await page.goto('https://main.dddsig2mih3hw.amplifyapp.com/');

  // Wait for login form — the app redirects unauthenticated users to auth UI
  await page.waitForSelector('input[type="email"], input[name="username"], input[placeholder*="email" i]', {
    timeout: 20_000,
  });

  // Fill credentials
  await page.locator('input[type="email"], input[name="username"], input[placeholder*="email" i]').first().fill('ajaykumar.yadav@3pillarglobal.com');
  await page.locator('input[type="password"]').fill('Secure@12345');
  await page.locator('button[type="submit"], button:has-text("Sign in"), button:has-text("Login")').first().click();

  // Wait until the authenticated dashboard is loaded
  await page.waitForSelector('text=Dashboard', { timeout: 20_000 });

  // Persist cookies + localStorage (Cognito tokens live in localStorage)
  await context.storageState({ path: STORAGE_STATE_PATH });

  await browser.close();
}
