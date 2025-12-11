package net.kajilab.elpissender.presenter.ui.view.components

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewComponent(
    url: String,
    topAppBarActions: (List<@Composable () -> Unit>) -> Unit,
) {
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val webViewState = remember { mutableStateOf<WebView?>(null) }

    topAppBarActions(
        listOf {
            IconButton(onClick = {
                webViewState.value?.reload()
            }) {
                Icon(Icons.Filled.Refresh, "Trigger Refresh")
            }
        },
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // ローディング表示
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        // エラー表示
        if (hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "エラーが発生しました")
                    Text(text = errorMessage)
                    Button(onClick = {
                        hasError = false
                        isLoading = true
                    }) {
                        Text("再読み込み")
                    }
                }
            }
        }

        // WebView
        if (!hasError) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        // WebViewの初期設定
                        webViewState.value = this

                        // セキュリティ設定
                        settings.apply {
                            // JavaScriptは必要な場合のみ有効化
                            javaScriptEnabled = true

                            // ファイルアクセスの制限
                            allowFileAccess = false
                            allowContentAccess = false
                            allowFileAccessFromFileURLs = false
                            allowUniversalAccessFromFileURLs = false
                            domStorageEnabled = true

                            // キャッシュモードの設定
                            cacheMode = WebSettings.LOAD_DEFAULT

                            // mixed contentの制御
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                            // セキュリティ設定
                            setSupportMultipleWindows(false)
                            setGeolocationEnabled(false)

                            // Safe Browsing の有効化
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = true
                            }
                        }

                        // WebViewClientの設定
                        webViewClient =
                            object : WebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?,
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                }

                                override fun onPageFinished(
                                    view: WebView?,
                                    url: String?,
                                ) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?,
                                ) {
                                    super.onReceivedError(view, request, error)
                                    isLoading = false
                                    hasError = true
                                    errorMessage = "ページの読み込みに失敗しました: ${error?.description}"
                                }

                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?,
                                ) {
                                    // SSL/TLSエラーの処理
                                    handler?.cancel()
                                    isLoading = false
                                    hasError = true
                                    errorMessage = "セキュリティエラー: SSL証明書が無効です"
                                }
                            }

                        // WebChromeClientの設定
                        webChromeClient =
                            object : WebChromeClient() {
                                override fun onProgressChanged(
                                    view: WebView?,
                                    newProgress: Int,
                                ) {
                                    super.onProgressChanged(view, newProgress)
                                    // プログレスバーの更新などが必要な場合はここで処理
                                }
                            }

                        // URLの読み込み
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
