import { test, expect } from '@playwright/test';

test('geo location DOM inspection - map markers and selectors', async ({ page }) => {
  // ── Step 1: Navigate and Login ──────────────────────────────────────────────
  await page.goto('/');
  await page.waitForLoadState('domcontentloaded');

  // Fill login credentials
  await page.fill('input[type="email"], input[name="email"], input[placeholder*="email" i]', 'ajaykumar.yadav@3pillarglobal.com');
  await page.fill('input[type="password"], input[name="password"], input[placeholder*="password" i]', 'Secure@12345');
  await page.click('button[type="submit"]');

  // Wait for redirect after login
  await page.waitForURL(/dashboard|inventory|home/i, { timeout: 20000 }).catch(() => {});
  await page.waitForLoadState('domcontentloaded');
  console.log('[LOGIN] URL after login:', page.url());

  // ── Step 2: Navigate to Inventory & Assets ──────────────────────────────────
  const inventorySelectors = [
    'a:has-text("Inventory & Assets")',
    'a:has-text("Inventory")',
    'nav a:has-text("Inventory")',
    '[role="link"]:has-text("Inventory")',
  ];

  let navigated = false;
  for (const sel of inventorySelectors) {
    const el = page.locator(sel).first();
    if (await el.isVisible({ timeout: 3000 }).catch(() => false)) {
      console.log('[NAV] Clicking:', sel);
      await el.click();
      navigated = true;
      break;
    }
  }
  if (!navigated) {
    await page.goto('/inventory');
  }
  await page.waitForLoadState('domcontentloaded');
  await page.waitForTimeout(2000);
  console.log('[NAV] Inventory URL:', page.url());

  // ── Step 3: Click the Geo Location tab ──────────────────────────────────────
  const geoTabSelectors = [
    'button:has-text("Geo Location")',
    '[role="tab"]:has-text("Geo Location")',
    'button:has-text("Geo")',
    '[role="tab"]:has-text("Geo")',
    'li:has-text("Geo Location")',
  ];

  for (const sel of geoTabSelectors) {
    try {
      const el = page.locator(sel).first();
      if (await el.isVisible({ timeout: 3000 }).catch(() => false)) {
        console.log('[GEO TAB] Clicking:', sel, '| text:', await el.textContent());
        await el.click();
        break;
      }
    } catch {}
  }

  // ── Step 4: Wait for map canvas to appear ───────────────────────────────────
  console.log('[MAP] Waiting for maplibregl canvas...');
  try {
    await page.waitForSelector('.maplibregl-map', { timeout: 20000 });
    console.log('[MAP] maplibregl-map container found');
  } catch {
    console.log('[MAP] maplibregl-map NOT found. Checking for canvas...');
    try {
      await page.waitForSelector('canvas', { timeout: 10000 });
      console.log('[MAP] canvas found via fallback');
    } catch {
      console.log('[MAP] No canvas found');
    }
  }

  // ── Step 5: Wait for markers to appear (poll up to 30 seconds) ─────────────
  console.log('[MARKERS] Polling for markers (up to 30s)...');
  let markerCount = 0;
  for (let i = 0; i < 30; i++) {
    markerCount = await page.evaluate(() =>
      document.querySelectorAll('.maplibregl-marker').length
    );
    if (markerCount > 0) {
      console.log(`[MARKERS] Found ${markerCount} markers after ${i + 1}s`);
      break;
    }
    await page.waitForTimeout(1000);
  }
  if (markerCount === 0) {
    console.log('[MARKERS] No .maplibregl-marker found after 30s polling');
  }

  // ── Step 6: Comprehensive DOM inspection ────────────────────────────────────
  const domInspection = await page.evaluate(() => {
    const results: Record<string, any> = {};

    // Test all candidate selectors
    const selectors = [
      '.maplibregl-marker',
      '.maplibregl-marker-anchor-center',
      '.maplibregl-marker.maplibregl-marker-anchor-center',
      '.mapboxgl-marker',
      '.mapboxgl-marker.mapboxgl-marker-anchor-center',
      '[aria-label="Map marker"]',
      '[role="button"]',
      '[class*="marker"]',
      '[class*="pin"]',
      '[class*="Pin"]',
      '[class*="Marker"]',
      'div[style*="position: absolute"][style*="transform"]',
    ];

    results.selectorCounts = {};
    for (const sel of selectors) {
      try {
        results.selectorCounts[sel] = document.querySelectorAll(sel).length;
      } catch (e) {
        results.selectorCounts[sel] = 'ERROR: ' + (e as Error).message;
      }
    }

    // Get full outerHTML of first marker
    const firstMarker = document.querySelector('.maplibregl-marker');
    if (firstMarker) {
      results.firstMarkerOuterHTML = firstMarker.outerHTML;
      results.firstMarkerClasses = firstMarker.className?.toString();
      results.firstMarkerAttributes = Array.from(firstMarker.attributes).map(a => `${a.name}="${a.value}"`);
      results.firstMarkerChildren = Array.from(firstMarker.children).map(c => ({
        tag: c.tagName,
        classes: c.className?.toString(),
        html: c.outerHTML.substring(0, 200),
      }));
    }

    // Get outerHTML of first 3 markers
    const allMarkers = document.querySelectorAll('.maplibregl-marker');
    results.allMarkersCount = allMarkers.length;
    results.firstThreeMarkersHTML = Array.from(allMarkers).slice(0, 3).map(m => ({
      outerHTML: m.outerHTML,
      classes: m.className?.toString(),
    }));

    // Check the maplibregl-map container structure
    const mapContainer = document.querySelector('.maplibregl-map');
    if (mapContainer) {
      results.mapContainerChildren = Array.from(mapContainer.children).map(c => ({
        tag: c.tagName,
        classes: c.className?.toString().substring(0, 100),
        childCount: c.children.length,
      }));
    }

    // Look for the markers layer / overlay
    const markerLayer = document.querySelector('.maplibregl-marker-container, .maplibregl-canvas-container, [class*="marker-container"]');
    results.markerLayerFound = !!markerLayer;
    if (markerLayer) {
      results.markerLayerHTML = markerLayer.outerHTML.substring(0, 500);
    }

    return results;
  });

  console.log('[DOM INSPECTION]:', JSON.stringify(domInspection, null, 2));

  // ── Step 7: Take snapshot ───────────────────────────────────────────────────
  // (Snapshot taken via MCP tool separately)

  // ── Step 8: Click "Online" filter and recount ───────────────────────────────
  const onlineButton = page.locator('button:has-text("Online")').first();
  const onlineVisible = await onlineButton.isVisible({ timeout: 5000 }).catch(() => false);

  if (onlineVisible) {
    const onlineText = await onlineButton.textContent();
    console.log('[FILTER] Online button text:', onlineText);

    const beforeFilterCount = await page.evaluate(() =>
      document.querySelectorAll('.maplibregl-marker').length
    );
    console.log('[FILTER] Marker count BEFORE Online click:', beforeFilterCount);

    await onlineButton.click();
    await page.waitForTimeout(2000);

    const afterFilterCount = await page.evaluate(() =>
      document.querySelectorAll('.maplibregl-marker').length
    );
    console.log('[FILTER] Marker count AFTER Online click:', afterFilterCount);
  } else {
    console.log('[FILTER] Online button not found');
    // Scan all buttons
    const allButtons = await page.evaluate(() =>
      Array.from(document.querySelectorAll('button')).map(b => ({
        text: b.textContent?.trim().substring(0, 60),
        classes: b.className?.toString().substring(0, 100),
      }))
    );
    console.log('[ALL BUTTONS]:', JSON.stringify(allButtons, null, 2));
  }

  // ── Step 9: Try JS click() on first marker to check detail card ────────────
  const jsClickResult = await page.evaluate(() => {
    const markers = document.querySelectorAll('.maplibregl-marker');
    if (markers.length === 0) return { found: false, message: 'No markers found' };

    const firstMarker = markers[0] as HTMLElement;
    firstMarker.click();
    return {
      found: true,
      message: 'Clicked first marker via JS click()',
      markerHTML: firstMarker.outerHTML.substring(0, 300),
    };
  });
  console.log('[JS CLICK]:', JSON.stringify(jsClickResult));

  await page.waitForTimeout(2000);

  // ── Step 10: Check for detail card after click ──────────────────────────────
  const detailCardCheck = await page.evaluate(() => {
    // Look for detail card with various selectors
    const cardSelectors = [
      '[class*="detail"]',
      '[class*="Detail"]',
      '[class*="card"]',
      '[class*="Card"]',
      '[class*="popup"]',
      '[class*="Popup"]',
      '[class*="modal"]',
      '[class*="Modal"]',
      '[class*="panel"]',
      '[class*="Panel"]',
      '[role="dialog"]',
    ];

    const found: Array<{ selector: string; count: number; firstHTML: string }> = [];
    for (const sel of cardSelectors) {
      try {
        const els = document.querySelectorAll(sel);
        if (els.length > 0) {
          found.push({
            selector: sel,
            count: els.length,
            firstHTML: (els[0] as HTMLElement).outerHTML.substring(0, 400),
          });
        }
      } catch {}
    }
    return found;
  });
  console.log('[DETAIL CARD CHECK]:', JSON.stringify(detailCardCheck, null, 2));

  // ── Step 11: Check close button ────────────────────────────────────────────
  const closeButtonCheck = await page.evaluate(() => {
    const closeSelectors = [
      'button[aria-label*="close" i]',
      'button[aria-label*="Close" i]',
      'button[class*="close"]',
      'button[class*="Close"]',
      '[class*="close-btn"]',
      '[class*="closeBtn"]',
      'button:has-text("×")',
      'button:has-text("✕")',
      'button:has-text("X")',
    ];

    const found: Array<{ selector: string; count: number; firstOuterHTML: string }> = [];
    for (const sel of closeSelectors) {
      try {
        const els = document.querySelectorAll(sel);
        if (els.length > 0) {
          found.push({
            selector: sel,
            count: els.length,
            firstOuterHTML: (els[0] as HTMLElement).outerHTML,
          });
        }
      } catch {}
    }
    return found;
  });
  console.log('[CLOSE BUTTON CHECK]:', JSON.stringify(closeButtonCheck, null, 2));

  // ── Step 12: Final comprehensive marker scan ────────────────────────────────
  const finalMarkerScan = await page.evaluate(() => {
    const allMarkers = document.querySelectorAll('.maplibregl-marker');
    const withAnchorCenter = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center');

    // Check what anchor classes exist on markers
    const anchorClasses = Array.from(allMarkers).map(m => {
      const classes = Array.from(m.classList);
      return classes.filter(c => c.includes('anchor'));
    });

    return {
      totalMarkers: allMarkers.length,
      withAnchorCenter: withAnchorCenter.length,
      uniqueAnchorClasses: [...new Set(anchorClasses.flat())],
      markerClassList: Array.from(allMarkers).slice(0, 3).map(m => Array.from(m.classList)),
    };
  });
  console.log('[FINAL MARKER SCAN]:', JSON.stringify(finalMarkerScan, null, 2));

  // Always pass - this is an inspection test
  expect(true).toBe(true);
});
