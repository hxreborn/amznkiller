(function () {
  if (!window.AmznKiller) window.AmznKiller = {};
  if (window.AmznKiller.injectCharts) return;

  var KEEPA_DOMAINS = {
    'amazon.com': 1, 'amazon.co.uk': 2, 'amazon.de': 3,
    'amazon.fr': 4, 'amazon.co.jp': 5, 'amazon.ca': 6,
    'amazon.it': 8, 'amazon.es': 9, 'amazon.in': 10,
    'amazon.com.mx': 11, 'amazon.com.br': 12, 'amazon.com.au': 13
  };

  var CAMEL_LOCALES = {
    'amazon.com': 'us', 'amazon.co.uk': 'uk', 'amazon.de': 'de',
    'amazon.fr': 'fr', 'amazon.co.jp': 'jp', 'amazon.ca': 'ca',
    'amazon.it': 'it', 'amazon.es': 'es', 'amazon.com.au': 'au'
  };

  window.AmznKiller.injectCharts = function (args) {
    if (document.getElementById('amznkiller-charts')) return null;

    var asin = args.asin;
    var domain = args.domain || window.location.hostname.replace(/^www\./, '');
    var keepaId = args.keepaId || KEEPA_DOMAINS[domain] || 1;
    var camelLocale = args.camelLocale || CAMEL_LOCALES[domain] || 'us';
    var dark = !!(args && args.dark);

    var keepaUrl = 'https://graph.keepa.com/pricehistory.png?used=1&amazon=1&new=1&domain=' + keepaId + '&asin=' + asin;

    var camelUrl = 'https://charts.camelcamelcamel.com/' + camelLocale + '/' + asin + '/amazon-new-used.png?force=1&legend=1&tp=all&w=725&h=400';

    var borderColor = dark ? '#333' : '#ddd';
    var bgColor = dark ? '#1a1a1a' : '#fafafa';
    var titleColor = dark ? '#e0e0e0' : '#333';
    var labelColor = dark ? '#999' : '#666';

    var c = document.createElement('div');
    c.id = 'amznkiller-charts';
    c.style.cssText = 'margin:16px 0;padding:12px;border:1px solid ' + borderColor + ';border-radius:8px;background:' + bgColor;

    var title = document.createElement('div');
    title.style.cssText = 'font-weight:bold;font-size:14px;margin-bottom:8px;color:' + titleColor;
    title.textContent = 'Price History';
    c.appendChild(title);

    function addChart(url, label, linkBase, imgStyle) {
      var w = document.createElement('div');
      w.style.cssText = 'margin-bottom:8px';
      var lbl = document.createElement('div');
      lbl.style.cssText = 'font-size:12px;color:' + labelColor + ';margin-bottom:4px';
      lbl.textContent = label;
      w.appendChild(lbl);
      var a = document.createElement('a');
      a.href = linkBase;
      a.target = '_blank';
      a.rel = 'noopener';
      var img = document.createElement('img');
      img.src = url;
      img.style.cssText = 'width:100%;height:auto;border-radius:4px' + (imgStyle || '');
      img.alt = label + ' for ' + asin;
      img.onerror = function () { w.style.display = 'none'; };
      a.appendChild(img);
      w.appendChild(a);
      c.appendChild(w);
    }

    var keepaLink = 'https://keepa.com/#!product/' + keepaId + '-' + asin;
    var camelLink = 'https://' + (camelLocale === 'us' ? '' : camelLocale + '.') + 'camelcamelcamel.com/product/' + asin;
    var darkFilter = dark ? ';filter:invert(1) hue-rotate(180deg) saturate(2) brightness(0.9)' : '';
    addChart(keepaUrl, 'Keepa', keepaLink, darkFilter);
    addChart(camelUrl, 'CamelCamelCamel', camelLink, darkFilter);

    var targets = [
      '#olpLinkWidget_feature_div > div.a-section.olp-link-widget > div > div.a-cardui.olp-link-widget-card-padding.olp-widget-bottomPadding',
      '#corePriceDisplay_desktop_feature_div',
      '#corePrice_feature_div',
      '#unifiedPrice_feature_div',
      '#price_feature_div',
      '#desktop_buybox',
      '#buybox',
      '#buyBoxAccordion',
      '#newAccordionRow',
      '#productOverview_feature_div',
      '#centerCol',
      '#mobileapp_accordion_feature_div'
    ];

    function tryInsert() {
      for (var i = 0; i < targets.length; i++) {
        var el = document.querySelector(targets[i]);
        if (el && el.parentNode) {
          el.parentNode.insertBefore(c, el.nextSibling);
          return targets[i];
        }
      }
      return null;
    }

    var hit = tryInsert();
    if (!hit) {
      var obs = new MutationObserver(function (_, o) {
        var h = tryInsert();
        if (h) o.disconnect();
      });
      obs.observe(document.body || document.documentElement, { childList: true, subtree: true });
      setTimeout(function () { obs.disconnect(); }, 10000);
    }
    return null;
  };
})();
