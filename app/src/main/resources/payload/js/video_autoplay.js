(function () {
  if (!window.AmznKiller) window.AmznKiller = {};
  if (window.AmznKiller.disableVideoAutoplay) return;

  window.AmznKiller.disableVideoAutoplay = function (args) {
    var enabled = !!(args && args.enabled);
    if (!enabled) {
      window.AmznKiller._autoplayOff = false;
      return null;
    }
    if (window.AmznKiller._autoplayOff) return null;
    window.AmznKiller._autoplayOff = true;

    var lastGesture = 0;
    ['pointerdown', 'keydown', 'touchstart'].forEach(function (t) {
      document.addEventListener(t, function () { lastGesture = Date.now(); }, true);
    });
    function userInitiated() { return Date.now() - lastGesture < 1000; }

    function tame(v) {
      try {
        v.autoplay = false;
        v.removeAttribute('autoplay');
        if (!v.paused && !v.__amznkillerUserPlayed) v.pause();
      } catch (e) {}
    }
    function scan() {
      var vids = document.querySelectorAll('video');
      for (var i = 0; i < vids.length; i++) tame(vids[i]);
    }

    document.addEventListener('play', function (e) {
      var v = e.target;
      if (!v || v.tagName !== 'VIDEO') return;
      if (userInitiated()) v.__amznkillerUserPlayed = true;
      else if (!v.__amznkillerUserPlayed) v.pause();
    }, true);

    scan();
    new MutationObserver(scan).observe(
      document.body || document.documentElement,
      { childList: true, subtree: true },
    );
    return null;
  };
})();
