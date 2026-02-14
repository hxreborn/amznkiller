package eu.hxreborn.amznkiller.xposed.runtime

import android.webkit.WebView
import eu.hxreborn.amznkiller.prefs.PrefsManager
import eu.hxreborn.amznkiller.xposed.injector.CssInjector
import eu.hxreborn.amznkiller.xposed.injector.DarkModeInjector
import eu.hxreborn.amznkiller.xposed.injector.PriceChartsInjector
import eu.hxreborn.amznkiller.xposed.injector.WebViewDebuggingGate

object PageRuntime {
    fun onPageStarted(webView: WebView) {
        val prefs = PrefsManager.snapshot()
        if (!prefs.forceDarkWebview) return
        DarkModeInjector.inject(webView, prefs)
    }

    fun onPageLoaded(
        webView: WebView,
        url: String,
    ) {
        val amazon = AmazonUrlParser.parse(url)
        if (!amazon.isAmazon) return
        val prefs = PrefsManager.snapshot()
        WebViewDebuggingGate.tryEnable(prefs)
        DarkModeInjector.inject(webView, prefs)
        CssInjector.inject(webView, url, prefs)
        PriceChartsInjector.inject(webView, prefs, amazon)
    }
}
