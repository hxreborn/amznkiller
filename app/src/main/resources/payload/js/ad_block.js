(function () {
  if (!window.AmznKiller) window.AmznKiller = {};
  if (window.AmznKiller.blockAds) return;

  window.AmznKiller.blockAds = function (args) {
    var css = args.css || '';
    var expected = args.expectedRules || 0;
    var hash = String(args.hash || 0);
    var validate = !!args.validate;

    var s = document.getElementById('amznkiller');
    if (!s) {
      s = document.createElement('style');
      s.id = 'amznkiller';
      (document.head || document.documentElement).appendChild(s);
    }
    var prev = s.getAttribute('data-amznkiller-hash');
    if (prev !== hash) {
      s.setAttribute('data-amznkiller-hash', hash);
      s.textContent = css;
    }

    if (!validate) return null;

    try {
      var parsed = 0;
      if (s.sheet && s.sheet.cssRules) parsed = s.sheet.cssRules.length;
      return {
        ok: parsed === expected,
        expected: expected,
        parsed: parsed,
        delta: expected - parsed
      };
    } catch (e) {
      return { ok: false, expected: expected, error: String(e) };
    }
  };
})();
