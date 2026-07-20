package eu.hxreborn.amznkiller.xposed.runtime

import android.webkit.WebView
import eu.hxreborn.amznkiller.xposed.hook.forceDarkWebview
import eu.hxreborn.amznkiller.xposed.injector.CssInjector
import eu.hxreborn.amznkiller.xposed.injector.DarkModeInjector
import eu.hxreborn.amznkiller.xposed.injector.PriceChartsInjector
import eu.hxreborn.amznkiller.xposed.injector.VideoAutoplayInjector
import eu.hxreborn.amznkiller.xposed.injector.WebViewDebuggingGate

object PageRuntime {
    fun onPageStarted(webView: WebView) {
        if (!forceDarkWebview) return
        DarkModeInjector.inject(webView)
    }

    fun onPageLoaded(
        webView: WebView,
        url: String,
    ) {
        val amazon = AmazonUrlParser.parse(url)
        if (!amazon.isAmazon) return
        WebViewDebuggingGate.tryEnable()
        DarkModeInjector.inject(webView)
        CssInjector.inject(webView, url)
        VideoAutoplayInjector.inject(webView)
        PriceChartsInjector.inject(webView, amazon)
    }
}
