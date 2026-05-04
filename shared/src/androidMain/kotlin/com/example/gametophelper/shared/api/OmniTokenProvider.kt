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
    private var webViewRef: WebView? = null

    private fun safeDestroy() {
        Handler(Looper.getMainLooper()).post {
            println("💀 Уничтожаем WebView")
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    suspend fun getScheduleFromWebView(login: String, password: String): String? =
        withTimeoutOrNull(60_000L) { // Увеличили таймаут до 60 секунд
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
                                    println("📄 URL=$url | loginStarted=$loginStarted | scheduleRequested=$scheduleRequested")

                                    // ФИНИШ: Расписание
                                    if (url?.contains("schedulePage") == true && !url.contains("login")) {
                                        println("🔍 РАСПИСАНИЕ НАЙДЕНО! Ждём 6 сек...")
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            println("⏰ Извлекаем HTML...")
                                            webViewRef?.evaluateJavascript("""
                                                var html = document.documentElement.outerHTML;
                                                OmniBridge.onHtmlReceived(html);
                                            """.trimIndent(), null)
                                        }, 6000)
                                        return
                                    }

                                    // ШАГ 2: Новости → кликаем на Расписание в меню
                                    if (url?.contains("news") == true && !scheduleRequested) {
                                        scheduleRequested = true
                                        println("📰 НОВОСТИ! Ищем ссылку на расписание...")
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            println("🔍 Выполняем JS для поиска ссылки...")
                                            webViewRef?.evaluateJavascript("""
                                                (function() {
                                                    OmniBridge.onLog('=== ПОИСК РАСПИСАНИЯ ===');
                                                    
                                                    var allLinks = document.querySelectorAll('a');
                                                    OmniBridge.onLog('Всего ссылок: ' + allLinks.length);
                                                    
                                                    var scheduleLinks = document.querySelectorAll('a[href*="schedulePage"]');
                                                    OmniBridge.onLog('Ссылок на schedulePage: ' + scheduleLinks.length);
                                                    
                                                    for (var i = 0; i < scheduleLinks.length; i++) {
                                                        OmniBridge.onLog('  [' + i + '] href=' + scheduleLinks[i].href + ' text=' + (scheduleLinks[i].textContent||'').trim());
                                                    }
                                                    
                                                    var liItems = document.querySelectorAll('li.schedulePage a, li[class*="schedule"] a');
                                                    OmniBridge.onLog('li.schedulePage a: ' + liItems.length);
                                                    
                                                    var targetLink = scheduleLinks[0] || liItems[0];
                                                    if (targetLink) {
                                                        OmniBridge.onLog('КЛИКАЕМ: ' + targetLink.href);
                                                        
                                                        // Способ 1: обычный клик
                                                        targetLink.click();
                                                        OmniBridge.onLog('Способ 1: click()');
                                                        
                                                        // Способ 2: MouseEvent
                                                        var event = new MouseEvent('click', {
                                                            view: window,
                                                            bubbles: true,
                                                            cancelable: true
                                                        });
                                                        targetLink.dispatchEvent(event);
                                                        OmniBridge.onLog('Способ 2: MouseEvent');
                                                        
                                                        // Способ 3: focus + Enter
                                                        targetLink.focus();
                                                        var ke = new KeyboardEvent('keydown', { key: 'Enter', keyCode: 13, bubbles: true });
                                                        targetLink.dispatchEvent(ke);
                                                        OmniBridge.onLog('Способ 3: Enter');
                                                        
                                                        // Способ 4: запасной переход
                                                        setTimeout(function() {
                                                            OmniBridge.onLog('Способ 4: location.href');
                                                            window.location.href = 'https://omni.top-academy.ru/#/schedulePage';
                                                        }, 1000);
                                                    } else {
                                                        OmniBridge.onLog('Ссылка не найдена! Пробуем хеш...');
                                                        window.location.hash = '#/schedulePage';
                                                    }
                                                })();
                                            """.trimIndent(), null)
                                        }, 2000)
                                        return
                                    }

                                    // ШАГ 1: Логин
                                    if (url?.contains("login") == true && !loginStarted) {
                                        loginStarted = true
                                        println("🔐 ЛОГИН! Ждём 4 сек для рендеринга...")

                                        Handler(Looper.getMainLooper()).postDelayed({
                                            println("🔑 Заполняем поля и кликаем Войти...")
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
                                                    OmniBridge.onLog('Полей ввода: ' + inputs.length);
                                                    for (var i = 0; i < inputs.length; i++) {
                                                        OmniBridge.onLog('  [' + i + '] type=' + inputs[i].type + ' ng-model=' + (inputs[i].getAttribute('ng-model')||'нет'));
                                                    }
                                                    
                                                    try {
                                                        var scope = angular.element(document.body).scope();
                                                        OmniBridge.onLog('Angular scope найден');
                                                        scope['${'$'}apply'](function() {
                                                            if (scope.form) {
                                                                scope.form.username = '$login';
                                                                scope.form.password = '$password';
                                                                OmniBridge.onLog('scope.form.username = ' + scope.form.username);
                                                            } else {
                                                                OmniBridge.onLog('scope.form не найден');
                                                            }
                                                        });
                                                    } catch(e) {
                                                        OmniBridge.onLog('Angular error: ' + e.message);
                                                    }
                                                    
                                                    setTimeout(function() {
                                                        OmniBridge.onLog('--- nativeSetter ---');
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
                                                            OmniBridge.onLog('Поля заполнены через setter');
                                                        }
                                                        
                                                        var btns = document.querySelectorAll('button');
                                                        OmniBridge.onLog('Кнопок: ' + btns.length);
                                                        for (var j = 0; j < btns.length; j++) {
                                                            OmniBridge.onLog('  [' + j + '] ' + (btns[j].textContent||'').trim());
                                                            if ((btns[j].textContent||'').trim() === 'Войти') {
                                                                btns[j].click();
                                                                OmniBridge.onLog('КЛИК по Войти!');
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