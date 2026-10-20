(function () {
  if (!window.AmznKiller) window.AmznKiller = {};
  if (window.AmznKiller.setDarkMode) return;

  // CSS fixes for Android's native force dark algorithm
  var DARK_FIX_CSS =
    // Kill mix-blend-mode that makes product images and metadata unreadable
    '*{mix-blend-mode:normal!important}' +
    // TODO:
    // Prevent white flash on navigation(WIP)
    'html{background-color:#1a1a1a!important}' +
    'body{background-color:#1a1a1a!important}' +
    // Deal cards: override #f7f7f7/#fff so HW renderer doesn't auto-darken unevenly
//    '[class*=asin-container],[class*=asin-image-wrapper]{background-color:#cbcbcb!important}' +
    '[class*=asin-container],[class*=asin-image-wrapper]{background-color:white!important}' +
    // Match card grid parent only when it contains deal cards to avoids product pages WIP
//    '.a-cardui-body:has([class*=asin-container]){background-color:#e0e0e0!important}' +
    // Deal badges: transparent bg so HW renderer has nothing to darken
    '[class*=badgeMessage]{background-color:transparent!important}' +
    '[data-csa-c-painter=single-creative-card]{color-scheme:only light!important}' +
    '.new-dot-t2{color-scheme:only light!important;background-color:#6f7373!important;border-color:#6f7373!important}' +
    '.new-dot-t2.dot-selected-t2{background-color:#fff!important;border-color:#fff!important}' +
    '.s-color-swatch-outer-circle-selected{color-scheme:only light!important;background-color:transparent!important;' +
    'box-shadow:0 0 0 2px #e8e8e8!important}' +
    '#wd-color-image-backdrop,#wd-backdrop-overscroll{filter:brightness(.3)!important}' +
    '#wd-backdrop-gradient{background-image:linear-gradient(to top,#1a1a1a 10%,rgba(26,26,26,.5) 24%,' +
    'rgba(26,26,26,.33) 75%,rgba(26,26,26,0) 98%)!important}' +
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
