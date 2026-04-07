import { test, expect } from '@playwright/test';

test('KPI card DOM inspection - verify locators', async ({ page }) => {
  // ── Login ────────────────────────────────────────────────────────────────────
  await page.goto('https://main.dddsig2mih3hw.amplifyapp.com');
  await page.waitForLoadState('domcontentloaded');

  // Wait for login form
  await page.waitForSelector('input[type="email"], input[name="username"], input[placeholder*="email" i]', {
    timeout: 20000,
  });
  await page.locator('input[type="email"], input[name="username"], input[placeholder*="email" i]').first().fill('ajaykumar.yadav@3pillarglobal.com');
  await page.locator('input[type="password"]').fill('Secure@12345');
  await page.locator('button[type="submit"], button:has-text("Sign in"), button:has-text("Login")').first().click();

  // Wait for dashboard to load
  await page.waitForSelector('text=Dashboard', { timeout: 30000 });
  await page.waitForTimeout(5000); // let KPI data load

  console.log('[LOGIN] URL after login:', page.url());

  // ── Inspect KPI card structure ───────────────────────────────────────────────
  const kpiStructure = await page.evaluate(() => {
    // Find all div.bg-card elements
    const bgCards = Array.from(document.querySelectorAll('div.bg-card'));
    const results: any[] = [];

    for (const card of bgCards.slice(0, 10)) {
      const textContent = card.textContent?.trim().substring(0, 200);
      const classList = card.className;

      // Look for text-sm elements (labels)
      const textSmDivs = Array.from(card.querySelectorAll('div[class*="text-sm"]'));
      const text3xlDivs = Array.from(card.querySelectorAll('div[class*="text-3xl"]'));

      // Also check for p and span with text-sm
      const allTextSmEls = Array.from(card.querySelectorAll('[class*="text-sm"]'));

      results.push({
        cardClass: classList,
        cardTextSummary: textContent?.substring(0, 100),
        textSmDivs: textSmDivs.map(el => ({
          tag: el.tagName,
          class: el.className,
          text: el.textContent?.trim(),
        })),
        allTextSmEls: allTextSmEls.map(el => ({
          tag: el.tagName,
          class: el.className,
          text: el.textContent?.trim(),
        })).slice(0, 5),
        text3xlDivs: text3xlDivs.map(el => ({
          tag: el.tagName,
          class: el.className,
          text: el.textContent?.trim(),
        })),
      });
    }
    return results;
  });

  console.log('[KPI STRUCTURE]:', JSON.stringify(kpiStructure, null, 2));

  // ── Test with Playwright's native locator API ─────────────────────────────────
  // These use Playwright's own CSS engine which supports :text-is()
  const playwrightSelectors = [
    "div.bg-card:has(div.text-sm:text-is('Total Devices')) div[class*='text-3xl']",
    "div.bg-card:has(div.text-sm:text-is('Active Deployments')) div[class*='text-3xl']",
    "div.bg-card:has(div.text-sm:text-is('Pending Approvals')) div[class*='text-3xl']",
    "div.bg-card:has(div.text-sm:text-is('Health Score')) div[class*='text-3xl']",
  ];

  for (const sel of playwrightSelectors) {
    const count = await page.locator(sel).count();
    const texts: any[] = [];
    for (let i = 0; i < count; i++) {
      texts.push(await page.locator(sel).nth(i).textContent());
    }
    console.log(`[PLAYWRIGHT] "${sel}" => count=${count}, texts=${JSON.stringify(texts)}`);
  }

  // ── Also try broader selectors to find the label text ────────────────────────
  const altSelectors = [
    // Broader: any element with 'Total Devices' text
    "div.bg-card:has(:text-is('Total Devices')) div[class*='text-3xl']",
    // Using p tag instead of div
    "div.bg-card:has(p.text-sm:text-is('Total Devices')) div[class*='text-3xl']",
    // Using span
    "div.bg-card:has(span[class*='text-sm']:text-is('Total Devices')) div[class*='text-3xl']",
    // Using div with text-muted class (label)
    "div.bg-card:has(div[class*='text-muted']:text-is('Total Devices')) div[class*='text-3xl']",
    // Using p with text-muted class
    "div.bg-card:has(p[class*='text-muted']:text-is('Total Devices')) div[class*='text-3xl']",
  ];

  for (const sel of altSelectors) {
    const count = await page.locator(sel).count();
    const texts: any[] = [];
    for (let i = 0; i < count; i++) {
      texts.push(await page.locator(sel).nth(i).textContent());
    }
    console.log(`[ALT PLAYWRIGHT] "${sel}" => count=${count}, texts=${JSON.stringify(texts)}`);
  }

  // ── Look at raw HTML of a KPI card ───────────────────────────────────────────
  const kpiHtml = await page.evaluate(() => {
    // Find a card that might be a KPI card - looking for text-3xl elements
    const cards = Array.from(document.querySelectorAll('div.bg-card'));
    const kpiCards = cards.filter(c => c.querySelector('[class*="text-3xl"]'));
    if (kpiCards.length === 0) return 'No KPI cards found (no text-3xl element)';
    // Return first 2 KPI cards' HTML
    return kpiCards.slice(0, 2).map(c => c.outerHTML.substring(0, 800)).join('\n\n---\n\n');
  });
  console.log('[KPI CARD HTML]:', kpiHtml);

  // ── Check Health Score icon ───────────────────────────────────────────────────
  const healthScoreCardHtml = await page.evaluate(() => {
    const cards = Array.from(document.querySelectorAll('div.bg-card'));
    const healthCard = cards.find(c => c.textContent?.includes('Health Score'));
    if (!healthCard) return 'Not found';
    return healthCard.outerHTML;
  });
  console.log('[HEALTH SCORE CARD HTML]:', healthScoreCardHtml);

  // ── Check LOADING_PLACEHOLDER and ZERO_FALLBACK ───────────────────────────────
  const specialLocators = [
    "div[class*='text-3xl']:text-is('\u2014')",  // LOADING_PLACEHOLDER
    "div[class*='text-3xl']:text-is('0')",        // ZERO_FALLBACK
    "div[class*='text-3xl']",                      // all value divs
  ];
  for (const sel of specialLocators) {
    const count = await page.locator(sel).count();
    const texts: any[] = [];
    for (let i = 0; i < count; i++) {
      texts.push(await page.locator(sel).nth(i).textContent());
    }
    console.log(`[SPECIAL] "${sel}" => count=${count}, texts=${JSON.stringify(texts)}`);
  }

  // ── Test fixed selectors with text-xs ────────────────────────────────────────
  const fixedSelectors = [
    "div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')) div[class*='text-3xl']",
    "div.bg-card:has(div[class*='text-xs']:text-is('Active Deployments')) div[class*='text-3xl']",
    "div.bg-card:has(div[class*='text-xs']:text-is('Pending Approvals')) div[class*='text-3xl']",
    "div.bg-card:has(div[class*='text-xs']:text-is('Health Score')) div[class*='text-3xl']",
    // Icon selectors
    "div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')) svg[class*='text-blue']",
    "div.bg-card:has(div[class*='text-xs']:text-is('Active Deployments')) svg[class*='text-green']",
    "div.bg-card:has(div[class*='text-xs']:text-is('Pending Approvals')) svg[class*='text-orange']",
    "div.bg-card:has(div[class*='text-xs']:text-is('Health Score')) svg[class*='text-green']",
    // KPI grid container
    "div[class*='grid-cols-4']:has(div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')))",
    // Label locators
    "div.bg-card div[class*='text-xs']",
  ];

  for (const sel of fixedSelectors) {
    const count = await page.locator(sel).count();
    const texts: any[] = [];
    for (let i = 0; i < count; i++) {
      texts.push(await page.locator(sel).nth(i).textContent());
    }
    console.log(`[FIXED] "${sel}" => count=${count}, texts=${JSON.stringify(texts)}`);
  }

  // ── Inspect the first KPI card's complete HTML ───────────────────────────────
  const firstKpiCardHtml = await page.evaluate(() => {
    const cards = Array.from(document.querySelectorAll('div.bg-card'));
    const kpiCards = cards.filter(c => c.querySelector('[class*="text-3xl"]'));
    if (kpiCards.length === 0) return 'No KPI cards found';
    return kpiCards[0].outerHTML;
  });
  console.log('[FIRST KPI CARD FULL HTML]:', firstKpiCardHtml);

  // ── Check what element holds the "Total Devices" label ────────────────────────
  const labelElementInfo = await page.evaluate(() => {
    // Look for any element containing "Total Devices" text in the KPI card
    const allCards = Array.from(document.querySelectorAll('div.bg-card'));
    const kpiCard = allCards.find(c => c.textContent?.includes('Total Devices'));
    if (!kpiCard) return 'KPI card not found';

    // Walk all descendant elements
    const allEls = Array.from(kpiCard.querySelectorAll('*'));
    const totalDevEls = allEls.filter(el =>
      el.textContent?.trim() === 'Total Devices' ||
      el.childNodes.length === 1 && el.textContent?.includes('Total Devices')
    );
    return totalDevEls.map(el => ({
      tag: el.tagName,
      class: el.className,
      text: el.textContent?.trim(),
      outerHTML: el.outerHTML.substring(0, 200),
    }));
  });
  console.log('[TOTAL DEVICES LABEL ELEMENT]:', JSON.stringify(labelElementInfo, null, 2));

  // Always pass — diagnostic test
  expect(true).toBe(true);
});
