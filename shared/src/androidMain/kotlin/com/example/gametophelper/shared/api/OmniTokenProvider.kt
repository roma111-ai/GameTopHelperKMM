package com.example.gametophelper.shared.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class OmniTokenProvider(private val context: Context) {

    private var loginStarted = false
    private var scheduleRequested = false
    private var collegeSwitched = false
    private var webViewRef: WebView? = null

    private fun safeDestroy() {
        Handler(Looper.getMainLooper()).post {
            println("💀 Уничтожаем WebView")
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    suspend fun getScheduleFromWebView(login: String, password: String): String? =
        withTimeoutOrNull(60_000L) {
            suspendCancellableCoroutine { continuation ->

                Handler(Looper.getMainLooper()).post {
                    try {
                        println("🚀 Создаём WebView...")
                        CookieManager.getInstance().setAcceptCookie(true)

                        val webView = WebView(context.applicationContext).apply {
                            webViewRef = this

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                javaScriptCanOpenWindowsAutomatically = true
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0"
                            }

                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onHtmlReceived(html: String) {
                                    println("📦 HTML получен (${html.length} символов)")
                                    safeDestroy()
                                    if (continuation.isActive) continuation.resume(html)
                                }

                                @JavascriptInterface
                                fun onError(error: String) {
                                    println("❌ JS: $error")
                                    safeDestroy()
                                    if (continuation.isActive) continuation.resume(null)
                                }

                                @JavascriptInterface
                                fun onLog(msg: String) { println("📜 $msg") }
                            }, "OmniBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    println("📄 URL=$url | login=$loginStarted | schedule=$scheduleRequested | college=$collegeSwitched")

                                    // ФИНИШ: Расписание
                                    if (url?.contains("schedulePage") == true && !url.contains("login")) {
                                        println("🔍 РАСПИСАНИЕ! Ждём 6 сек...")
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            println("⏰ Извлекаем HTML...")
                                            webViewRef?.evaluateJavascript("""
                                                var html = document.documentElement.outerHTML;
                                                OmniBridge.onHtmlReceived(html);
                                            """.trimIndent(), null)
                                        }, 6000)
                                        return
                                    }

                                    // ШАГ 2b: Новости после смены города → сразу к расписанию
                                    if (url?.contains("news") == true && collegeSwitched) {
                                        println("📰 Город сменён! Загружаем расписание через loadUrl...")
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            webViewRef?.loadUrl("https://omni.top-academy.ru/#/schedulePage")
                                        }, 1500)
                                        return
                                    }

                                    // ШАГ 2a: Новости (первый раз) → переключаем на КОЛЛЕДЖ
                                    if (url?.contains("news") == true && !scheduleRequested) {
                                        scheduleRequested = true
                                        collegeSwitched = true
                                        println("📰 НОВОСТИ! Переключаем на КОЛЛЕДЖ...")
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            webViewRef?.evaluateJavascript("""
                                                (function() {
                                                    OmniBridge.onLog('=== ПЕРЕКЛЮЧАЕМ НА КОЛЛЕДЖ ===');
                                                    
                                                    var cityButtons = document.querySelectorAll('button.changeUser, md-menu button, [ng-click*="changeCity"], [ng-click*="city"]');
                                                    OmniBridge.onLog('Кнопок: ' + cityButtons.length);
                                                    for (var i = 0; i < cityButtons.length; i++) {
                                                        OmniBridge.onLog('  [' + i + '] ' + (cityButtons[i].textContent||'').trim());
                                                    }
                                                    
                                                    // Способ 1: Angular
                                                    try {
                                                        var scope = angular.element(document.body).scope();
                                                        scope['${'$'}apply'](function() {
                                                            scope.changeCity(402);
                                                        });
                                                        OmniBridge.onLog('changeCity(402) вызван');
                                                    } catch(e) {
                                                        OmniBridge.onLog('Angular: ' + e.message);
                                                    }
                                                    
                                                    // Способ 2: клик по "Колледж"
                                                    setTimeout(function() {
                                                        var btns = document.querySelectorAll('button, a, md-menu-item');
                                                        for (var j = 0; j < btns.length; j++) {
                                                            var txt = (btns[j].textContent||'').trim();
                                                            if (txt.indexOf('Колледж') > -1 && txt.indexOf('ВУЗ') === -1) {
                                                                OmniBridge.onLog('Кликаем: ' + txt);
                                                                btns[j].click();
                                                                break;
                                                            }
                                                        }
                                                    }, 500);
                                                })();
                                            """.trimIndent(), null)
                                        }, 2000)
                                        return
                                    }

                                    // ШАГ 1: Логин
                                    if (url?.contains("login") == true && !loginStarted) {
                                        loginStarted = true
                                        println("🔐 ЛОГИН! Ждём 4 сек...")

                                        Handler(Looper.getMainLooper()).postDelayed({
                                            println("🔑 Заполняем поля...")
                                            webViewRef?.evaluateJavascript("""
                                                (function() {
                                                    OmniBridge.onLog('=== СТАРТ ЛОГИНА ===');
                                                    
                                                    var oldSend = XMLHttpRequest.prototype.send;
                                                    XMLHttpRequest.prototype.send = function(body) {
                                                        this.addEventListener('load', function() {
                                                            OmniBridge.onLog('XHR: ' + this.status + ' ' + this.responseText.substring(0, 200));
                                                        });
                                                        oldSend.call(this, body);
                                                    };
                                                    
                                                    var inputs = document.querySelectorAll('input');
                                                    for (var i = 0; i < inputs.length; i++) {
                                                        OmniBridge.onLog('  [' + i + '] type=' + inputs[i].type + ' ng-model=' + (inputs[i].getAttribute('ng-model')||'нет'));
                                                    }
                                                    
                                                    try {
                                                        var scope = angular.element(document.body).scope();
                                                        scope['${'$'}apply'](function() {
                                                            if (scope.form) {
                                                                scope.form.username = '$login';
                                                                scope.form.password = '$password';
                                                            }
                                                        });
                                                    } catch(e) {}
                                                    
                                                    setTimeout(function() {
                                                        var inp2 = document.querySelectorAll('input');
                                                        var lf = null, pf = null;
                                                        for (var k = 0; k < inp2.length; k++) {
                                                            if (inp2[k].type === 'text' || inp2[k].type === 'email') lf = inp2[k];
                                                            if (inp2[k].type === 'password') pf = inp2[k];
                                                        }
                                                        
                                                        if (lf && pf) {
                                                            var setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                                                            setter.call(lf, '$login');
                                                            lf.dispatchEvent(new Event('input', {bubbles: true}));
                                                            setter.call(pf, '$password');
                                                            pf.dispatchEvent(new Event('input', {bubbles: true}));
                                                        }
                                                        
                                                        var btns = document.querySelectorAll('button');
                                                        for (var j = 0; j < btns.length; j++) {
                                                            if ((btns[j].textContent||'').trim() === 'Войти') {
                                                                btns[j].click();
                                                                OmniBridge.onLog('КЛИК!');
                                                                break;
                                                            }
                                                        }
                                                    }, 800);
                                                })();
                                            """.trimIndent(), null)
                                        }, 4000)
                                        return
                                    }

                                    println("📍 Другая страница: $url")
                                }
                            }
                        }

                        println("🚀 Загружаем https://omni.top-academy.ru/")
                        webView.loadUrl("https://omni.top-academy.ru/")

                    } catch (e: Exception) {
                        println("❌ Exception: ${e.message}")
                        e.printStackTrace()
                        safeDestroy()
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }
        }
}