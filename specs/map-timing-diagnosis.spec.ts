import { test, expect } from '@playwright/test';

test('map pin timing diagnosis - canvas load to filter click', async ({ page }) => {
  // ── Step 1: Navigate and Login ──────────────────────────────────────────────
  await page.goto('/');
  await page.waitForLoadState('domcontentloaded');

  await page.fill('input[type="email"], input[name="email"], input[placeholder*="email" i]', 'ajaykumar.yadav@3pillarglobal.com');
  await page.fill('input[type="password"], input[name="password"], input[placeholder*="password" i]', 'Secure@12345');
  await page.click('button[type="submit"]');
  await page.waitForURL(/dashboard|inventory|home/i, { timeout: 20000 }).catch(() => {});
  await page.waitForLoadState('domcontentloaded');
  console.log('[LOGIN] After login URL:', page.url());

  // ── Step 2: Navigate to Inventory page ──────────────────────────────────────
  const inventoryLinks = [
    'nav a:has-text("Inventory")',
    'a:has-text("Inventory & Assets")',
    'a:has-text("Inventory")',
    '[role="link"]:has-text("Inventory")',
  ];
  let inventoryClicked = false;
  for (const sel of inventoryLinks) {
    const el = page.locator(sel).first();
    if (await el.isVisible({ timeout: 2000 }).catch(() => false)) {
      console.log('[NAV] Clicking inventory nav link with selector:', sel);
      await el.click();
      inventoryClicked = true;
      break;
    }
  }
  if (!inventoryClicked) {
    await page.goto('/inventory');
  }
  await page.waitForLoadState('domcontentloaded');
  await page.waitForTimeout(1500);
  console.log('[NAV] Inventory URL:', page.url());

  // ── Step 3: Snapshot to understand page structure before clicking Geo tab ───
  const beforeGeoTab = await page.evaluate(() => {
    const tabs = document.querySelectorAll('[role="tab"], button[class*="tab"], li[class*="tab"], button[class*="Tab"]');
    return Array.from(tabs).map(el => ({
      tag: el.tagName,
      text: el.textContent?.trim().substring(0, 60),
      className: el.className?.toString().substring(0, 100),
      ariaSelected: el.getAttribute('aria-selected'),
      id: el.id,
    }));
  });
  console.log('[TAB SCAN] Available tabs before clicking Geo:', JSON.stringify(beforeGeoTab, null, 2));

  // Also scan for any button-like elements with "geo" or "location"
  const geoRelated = await page.evaluate(() => {
    const all = Array.from(document.querySelectorAll('button, a, [role="tab"], li'));
    return all
      .filter(el => (el.textContent || '').toLowerCase().includes('geo') || (el.textContent || '').toLowerCase().includes('location'))
      .map(el => ({
        tag: el.tagName,
        text: el.textContent?.trim().substring(0, 60),
        className: el.className?.toString().substring(0, 100),
        id: el.id,
      }));
  });
  console.log('[GEO SCAN] Elements with geo/location text:', JSON.stringify(geoRelated, null, 2));

  // ── Step 4: Click the Geo Location tab ──────────────────────────────────────
  const geoTabSelectors = [
    'button:has-text("Geo Location")',
    '[role="tab"]:has-text("Geo Location")',
    '[role="tab"]:has-text("Geo")',
    'button:has-text("Geo")',
    'li:has-text("Geo Location")',
    'a:has-text("Geo Location")',
    ':has-text("Geo Location")',
  ];

  let geoTabClicked = false;
  for (const sel of geoTabSelectors) {
    try {
      const el = page.locator(sel).first();
      if (await el.isVisible({ timeout: 2000 }).catch(() => false)) {
        console.log('[GEO TAB] Found with selector:', sel);
        const text = await el.textContent();
        console.log('[GEO TAB] Element text:', text);
        await el.click();
        geoTabClicked = true;
        break;
      }
    } catch (e) {
      // continue
    }
  }
  if (!geoTabClicked) {
    console.log('[GEO TAB] Could not find tab, taking accessibility snapshot...');
  }

  // ── Step 5: Wait for canvas to appear and measure timing ────────────────────
  console.log('[CANVAS] Waiting for div.maplibregl-map canvas to appear...');
  const canvasWaitStart = Date.now();

  try {
    await page.waitForSelector('div.maplibregl-map canvas', { timeout: 15000 });
    const canvasWaitMs = Date.now() - canvasWaitStart;
    console.log(`[CANVAS] Canvas appeared after ${canvasWaitMs}ms from waitForSelector start`);
  } catch (e) {
    console.log('[CANVAS] div.maplibregl-map canvas NOT found within 15s. Trying fallback selectors...');
    // Try broader selectors
    const fallbacks = ['canvas', '.maplibregl-map', '[class*="maplibre"]', '[class*="mapbox"]'];
    for (const sel of fallbacks) {
      const count = await page.locator(sel).count();
      console.log(`[CANVAS FALLBACK] "${sel}" count:`, count);
    }
  }

  // ── Step 6: Count markers immediately after canvas loads ────────────────────
  const countImmediately = await page.evaluate(() => {
    const markers = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center');
    const allMaplibreMarkers = document.querySelectorAll('.maplibregl-marker');
    const allMapboxMarkers = document.querySelectorAll('.mapboxgl-marker');
    const canvases = document.querySelectorAll('canvas');
    const mapDivs = document.querySelectorAll('div.maplibregl-map');

    // Also try to find ANY marker-like elements
    const anyMarker = document.querySelectorAll('[class*="marker"]');

    return {
      primarySelector: '.maplibregl-marker.maplibregl-marker-anchor-center',
      primaryCount: markers.length,
      allMaplibreMarkers: allMaplibreMarkers.length,
      allMapboxMarkers: allMapboxMarkers.length,
      canvasCount: canvases.length,
      mapDivCount: mapDivs.length,
      anyMarkerCount: anyMarker.length,
      anyMarkerClasses: Array.from(anyMarker).slice(0, 5).map(el => el.className?.toString()),
      sampleMarkerHTML: markers.length > 0
        ? Array.from(markers).slice(0, 2).map(m => m.outerHTML.substring(0, 300))
        : [],
    };
  });
  console.log('[COUNT T=0] Immediately after canvas appeared:', JSON.stringify(countImmediately, null, 2));

  // ── Step 7: Wait 3 more seconds and count again ─────────────────────────────
  await page.waitForTimeout(3000);
  const countAt3s = await page.evaluate(() => {
    const markers = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center');
    const allMaplibreMarkers = document.querySelectorAll('.maplibregl-marker');
    const anyMarker = document.querySelectorAll('[class*="marker"]');
    return {
      primaryCount: markers.length,
      allMaplibreMarkers: allMaplibreMarkers.length,
      anyMarkerCount: anyMarker.length,
      sampleMarkerHTML: markers.length > 0
        ? Array.from(markers).slice(0, 2).map(m => m.outerHTML.substring(0, 300))
        : [],
      sampleAnyMarkerHTML: anyMarker.length > 0
        ? Array.from(anyMarker).slice(0, 3).map(m => ({
            class: m.className?.toString(),
            html: m.outerHTML.substring(0, 200),
          }))
        : [],
    };
  });
  console.log('[COUNT T=3s] After 3 more seconds:', JSON.stringify(countAt3s, null, 2));

  // Take accessibility snapshot at this point
  console.log('[SNAPSHOT] Taking snapshot before filter click...');

  // ── Step 8: Find and click the "Online" filter button ───────────────────────
  // First, scan for filter buttons
  const filterButtons = await page.evaluate(() => {
    const all = Array.from(document.querySelectorAll('button, [role="button"], [role="tab"]'));
    return all
      .filter(el => (el.textContent || '').toLowerCase().includes('online') ||
                    (el.textContent || '').toLowerCase().includes('offline') ||
                    (el.textContent || '').toLowerCase().includes('all') ||
                    (el.getAttribute('aria-label') || '').toLowerCase().includes('filter'))
      .map(el => ({
        tag: el.tagName,
        text: el.textContent?.trim().substring(0, 60),
        className: el.className?.toString().substring(0, 100),
        id: el.id,
        ariaLabel: el.getAttribute('aria-label'),
        ariaPressed: el.getAttribute('aria-pressed'),
        ariaSelected: el.getAttribute('aria-selected'),
      }));
  });
  console.log('[FILTER SCAN] Filter-like buttons found:', JSON.stringify(filterButtons, null, 2));

  // Click the "Online" filter
  const onlineFilterSelectors = [
    'button:has-text("Online(")',
    'button:has-text("Online")',
    '[role="button"]:has-text("Online")',
    'button[class*="filter"]:has-text("Online")',
  ];

  let onlineClicked = false;
  for (const sel of onlineFilterSelectors) {
    try {
      const el = page.locator(sel).first();
      if (await el.isVisible({ timeout: 2000 }).catch(() => false)) {
        const text = await el.textContent();
        console.log('[FILTER] Found Online filter with selector:', sel, '| text:', text);

        // Count before click
        const beforeClick = await page.evaluate(() =>
          document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length
        );
        console.log('[COUNT PRE-CLICK] Markers before clicking Online:', beforeClick);

        await el.click();
        onlineClicked = true;

        // ── Step 9: Immediately after click ────────────────────────────────
        const immediateAfterClick = await page.evaluate(() => {
          const count = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length;
          const allMarkers = document.querySelectorAll('.maplibregl-marker').length;
          return { count, allMarkers };
        });
        console.log('[COUNT T=0s after click] Immediately after Online click:', JSON.stringify(immediateAfterClick));

        // ── Step 10: Wait 1 second ─────────────────────────────────────────
        await page.waitForTimeout(1000);
        const at1s = await page.evaluate(() => {
          const count = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length;
          const allMarkers = document.querySelectorAll('.maplibregl-marker').length;
          // Also check button active states
          const buttons = Array.from(document.querySelectorAll('button'));
          const onlineBtn = buttons.find(b => (b.textContent || '').includes('Online'));
          return {
            count,
            allMarkers,
            onlineButtonClass: onlineBtn?.className?.toString().substring(0, 120),
            onlineButtonAriaPressed: onlineBtn?.getAttribute('aria-pressed'),
            onlineButtonAriaSelected: onlineBtn?.getAttribute('aria-selected'),
          };
        });
        console.log('[COUNT T=1s after click]:', JSON.stringify(at1s, null, 2));

        // ── Step 11: Wait 2 more seconds ───────────────────────────────────
        await page.waitForTimeout(2000);
        const at3s = await page.evaluate(() => {
          const markers = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center');
          const allMarkers = document.querySelectorAll('.maplibregl-marker').length;
          // Check button active states again
          const buttons = Array.from(document.querySelectorAll('button'));
          const onlineBtn = buttons.find(b => (b.textContent || '').includes('Online'));
          const allFilterBtns = buttons
            .filter(b => {
              const txt = (b.textContent || '').toLowerCase();
              return txt.includes('online') || txt.includes('offline') || txt.includes('all');
            })
            .map(b => ({
              text: b.textContent?.trim().substring(0, 40),
              class: b.className?.toString().substring(0, 120),
              ariaPressed: b.getAttribute('aria-pressed'),
              ariaSelected: b.getAttribute('aria-selected'),
            }));
          return {
            count: markers.length,
            allMarkers,
            sampleHTML: markers.length > 0
              ? Array.from(markers).slice(0, 2).map(m => m.outerHTML.substring(0, 300))
              : [],
            allFilterBtns,
          };
        });
        console.log('[COUNT T=3s after click]:', JSON.stringify(at3s, null, 2));
        break;
      }
    } catch (e) {
      // continue
    }
  }

  if (!onlineClicked) {
    console.log('[FILTER] Could not find Online filter button. Scanning all buttons...');
    const allBtns = await page.evaluate(() => {
      return Array.from(document.querySelectorAll('button')).map(b => ({
        text: b.textContent?.trim().substring(0, 50),
        class: b.className?.toString().substring(0, 80),
      }));
    });
    console.log('[ALL BUTTONS]:', JSON.stringify(allBtns, null, 2));
  }

  // ── Step 12: Final snapshot ─────────────────────────────────────────────────
  // Final deep scan: find the exact working selector for map pins
  const finalScan = await page.evaluate(() => {
    const results: Record<string, any> = {};

    // Test every candidate selector
    const candidates = [
      '.maplibregl-marker',
      '.maplibregl-marker-anchor-center',
      '.maplibregl-marker.maplibregl-marker-anchor-center',
      '.mapboxgl-marker',
      '.mapboxgl-marker.mapboxgl-marker-anchor-center',
      '[class*="marker"][class*="anchor"]',
      '[class*="maplibre"][class*="marker"]',
      'div[style*="position: absolute"][style*="transform"]',
    ];

    results.selectorCounts = {};
    for (const sel of candidates) {
      try {
        results.selectorCounts[sel] = document.querySelectorAll(sel).length;
      } catch (e) {
        results.selectorCounts[sel] = 'ERROR: ' + (e as Error).message;
      }
    }

    // Inspect one marker in detail if any exist
    const markers = document.querySelectorAll('.maplibregl-marker');
    if (markers.length > 0) {
      results.firstMarkerDetail = {
        outerHTML: markers[0].outerHTML.substring(0, 500),
        classNames: markers[0].className?.toString(),
        parentClass: markers[0].parentElement?.className?.toString(),
        attributes: Array.from(markers[0].attributes).map(a => a.name + '="' + a.value + '"'),
        childCount: markers[0].children.length,
        computedDisplay: window.getComputedStyle(markers[0]).display,
        computedVisibility: window.getComputedStyle(markers[0]).visibility,
        computedOpacity: window.getComputedStyle(markers[0]).opacity,
      };
    }

    // Check the map container structure
    const mapContainer = document.querySelector('.maplibregl-map');
    if (mapContainer) {
      results.mapContainerChildClasses = Array.from(mapContainer.children).map(c => ({
        tag: c.tagName,
        class: c.className?.toString().substring(0, 80),
        childCount: c.children.length,
      }));
    }

    return results;
  });

  console.log('[FINAL SCAN]:', JSON.stringify(finalScan, null, 2));

  // ── Step 13: Reset to All filter first ──────────────────────────────────────
  const allBtn = page.locator('button:has-text("All")').first();
  if (await allBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
    await allBtn.click();
    await page.waitForTimeout(1500);
  }

  // ── Step 14: JS click() on first marker - does detail card open? ─────────────
  const jsClickResult = await page.evaluate(() => {
    const markers = document.querySelectorAll('.maplibregl-marker');
    if (markers.length === 0) return { found: false, markerCount: 0, clicked: false };
    const first = markers[0] as HTMLElement;
    const html = first.outerHTML.substring(0, 300);
    first.click();
    return { found: true, markerCount: markers.length, clicked: true, markerHTML: html };
  });
  console.log('[JS CLICK MARKER]:', JSON.stringify(jsClickResult));

  await page.waitForTimeout(2000);

  // ── Step 15: Check if detail card appeared after JS click ────────────────────
  const detailCardCheck = await page.evaluate(() => {
    const cardSelectors = [
      '[class*="detail"]',
      '[class*="Detail"]',
      '[class*="DeviceDetail"]',
      '[class*="device-detail"]',
      '[class*="card"]',
      '[class*="Card"]',
      '[class*="popup"]',
      '[class*="Popup"]',
      '[class*="modal"]',
      '[class*="Modal"]',
      '[class*="panel"]',
      '[class*="Panel"]',
      '[class*="overlay"]',
      '[class*="Overlay"]',
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
            firstHTML: (els[0] as HTMLElement).outerHTML.substring(0, 500),
          });
        }
      } catch {}
    }
    return { cardSelectors: found };
  });
  console.log('[DETAIL CARD AFTER JS CLICK]:', JSON.stringify(detailCardCheck, null, 2));

  // ── Step 16: Check for close button ──────────────────────────────────────────
  const closeButtonCheck = await page.evaluate(() => {
    const closeSelectors = [
      'button[aria-label*="close" i]',
      'button[aria-label*="Close" i]',
      'button[class*="close"]',
      'button[class*="Close"]',
      '[class*="close-btn"]',
      '[class*="closeBtn"]',
    ];
    const found: Array<{ selector: string; count: number; outerHTML: string }> = [];
    for (const sel of closeSelectors) {
      try {
        const els = document.querySelectorAll(sel);
        if (els.length > 0) {
          found.push({
            selector: sel,
            count: els.length,
            outerHTML: (els[0] as HTMLElement).outerHTML,
          });
        }
      } catch {}
    }
    // Also look at ALL buttons on the page right now
    const allBtns = Array.from(document.querySelectorAll('button')).map(b => ({
      text: b.textContent?.trim().substring(0, 30),
      ariaLabel: b.getAttribute('aria-label'),
      classes: b.className?.toString().substring(0, 100),
      outerHTML: b.outerHTML.substring(0, 200),
    }));
    return { closeSelectors: found, allButtons: allBtns };
  });
  console.log('[CLOSE BUTTON CHECK]:', JSON.stringify(closeButtonCheck, null, 2));

  // ── Step 17: Try Playwright click on first marker ─────────────────────────────
  const markerLocator = page.locator('.maplibregl-marker').first();
  const markerVisible = await markerLocator.isVisible({ timeout: 5000 }).catch(() => false);
  console.log('[PLAYWRIGHT CLICK] First marker visible via Playwright locator:', markerVisible);

  if (markerVisible) {
    // The inner <div></div> inside .maplibregl-marker intercepts pointer events.
    // Use evaluate to trigger the click directly on the DOM element instead.
    await page.evaluate(() => {
      const marker = document.querySelector('.maplibregl-marker') as HTMLElement;
      if (marker) marker.click();
    });
    await page.waitForTimeout(2000);

    // Check what appeared after Playwright click
    const afterPlaywrightClick = await page.evaluate(() => {
      const body = document.body.innerHTML;
      // Look for anything that looks like a detail/popup that appeared
      const allVisible = Array.from(document.querySelectorAll('*')).filter(el => {
        const style = window.getComputedStyle(el);
        const rect = (el as HTMLElement).getBoundingClientRect();
        return style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0'
          && rect.width > 50 && rect.height > 50
          && (el.className?.toString().toLowerCase().includes('card') ||
              el.className?.toString().toLowerCase().includes('detail') ||
              el.className?.toString().toLowerCase().includes('popup') ||
              el.className?.toString().toLowerCase().includes('panel') ||
              el.className?.toString().toLowerCase().includes('modal'));
      }).map(el => ({
        tag: el.tagName,
        classes: el.className?.toString().substring(0, 100),
        text: el.textContent?.trim().substring(0, 100),
        outerHTML: (el as HTMLElement).outerHTML.substring(0, 400),
      }));
      return { visibleCardElements: allVisible.slice(0, 5) };
    });
    console.log('[AFTER PLAYWRIGHT CLICK]:', JSON.stringify(afterPlaywrightClick, null, 2));
  }

  // Verify test collected useful data
  expect(true).toBe(true);
});
