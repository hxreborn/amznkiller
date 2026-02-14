(function () {
  if (!window.AmznKiller) window.AmznKiller = {};
  if (window.AmznKiller.setDarkMode) return;

  // CSS fixes for Android's native force dark algorithm
  var DARK_FIX_CSS =
    // Kill mix-blend-mode that makes product images and metadata unreadable
    '[class*=image-container] img{mix-blend-mode:normal!important}' +
    'img[style*="mix-blend-mode"]{mix-blend-mode:normal!important}' +
    '[class*=asin-metadata]{mix-blend-mode:normal!important}' +
    // Prevent white flash on navigation
    'html{background-color:#1a1a1a!important}' +
    'body{background-color:#1a1a1a!important}' +
    // Deal cards: override #f7f7f7/#fff so HW renderer doesn't auto-darken unevenly
//    '[class*=asin-container],[class*=asin-image-wrapper]{background-color:#cbcbcb!important}' +
    '[class*=asin-container],[class*=asin-image-wrapper]{background-color:white!important}' +
    // Match card grid parent only when it contains deal cards to avoids product pages WIP
//    '.a-cardui-body:has([class*=asin-container]){background-color:#e0e0e0!important}' +
    // Deal badges: transparent bg so HW renderer has nothing to darken
    '[class*=badgeMessage]{background-color:transparent!important}' +
    // Buy buttons
    '.a-button-primary,.a-button-oneclick{color-scheme:only light!important}';

  window.AmznKiller.setDarkMode = function (args) {
    var enabled = !!(args && args.enabled);
    var s = document.getElementById('amznkiller-dark');
    if (enabled) {
      if (!s) {
        s = document.createElement('style');
        s.id = 'amznkiller-dark';
        (document.head || document.documentElement).appendChild(s);
      }
      s.textContent = DARK_FIX_CSS;
    } else {
      if (s) s.remove();
    }
    return null;
  };
})();
