package jp.hotdrop.weeb.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.webkit.WebResourceError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import jp.hotdrop.weeb.model.BookMarkCategory
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUiState,
    effects: SharedFlow<MainEffect>,
    onAddressChange: (String) -> Unit,
    onSubmitAddress: () -> Unit,
    onLoadHome: () -> Unit,
    onTogglePcMode: () -> Unit,
    onOpenBookmarkList: () -> Unit,
    onOpenBookmarkDialog: () -> Unit,
    onPageUpdated: (String, String?, Boolean) -> Unit,
    onSelectCategory: (Long) -> Unit,
    onBookmarkTitleChange: (String) -> Unit,
    onSaveBookmark: () -> Unit,
    onCloseBookmarkDialog: () -> Unit
) {
    val context = LocalContext.current
    val webView = remember { createConfiguredWebView(context) }
    var popupWebView by remember { mutableStateOf<WebView?>(null) }
    var isPopupVisible by remember { mutableStateOf(false) }
    val mobileUserAgent = remember { webView.settings.userAgentString }
    var hasAppliedUserAgent by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val closePopup: () -> Unit = {
        popupWebView?.apply {
            stopLoading()
            destroy()
        }
        popupWebView = null
        isPopupVisible = false
    }

    DisposableEffect(Unit) {
        val client = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return !isAllowedScheme(request.url)
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                if (url != null) {
                    onPageUpdated(url, view.title, view.canGoBack())
                }
            }

            override fun onPageFinished(view: WebView, url: String?) {
                if (url != null) {
                    onPageUpdated(url, view.title, view.canGoBack())
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    view.stopLoading()
                    view.loadUrl("about:blank")
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
                handler?.cancel()
            }
        }
        val chromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                view?.let { web ->
                    onPageUpdated(web.url.orEmpty(), title, web.canGoBack())
                }
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                if (popupWebView != null) {
                    closePopup()
                }
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val popupClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean = !isAllowedScheme(request.url)

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        if (request.isForMainFrame) {
                            view.stopLoading()
                            view.loadUrl("about:blank")
                        }
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: android.net.http.SslError?
                    ) {
                        handler?.cancel()
                    }
                }
                val popupChromeClient = object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView?) {
                        closePopup()
                    }
                }
                val targetWebView = createConfiguredWebView(context).apply {
                    webViewClient = popupClient
                    webChromeClient = popupChromeClient
                }
                applyUserAgent(targetWebView.settings, state.isPcMode, mobileUserAgent)
                popupWebView = targetWebView
                isPopupVisible = true
                transport.webView = targetWebView
                resultMsg?.sendToTarget()
                return true
            }
        }
        webView.webViewClient = client
        webView.webChromeClient = chromeClient
        onDispose {
            webView.stopLoading()
            webView.destroy()
            popupWebView?.destroy()
        }
    }

    LaunchedEffect(state.isPcMode) {
        applyUserAgent(webView.settings, state.isPcMode, mobileUserAgent)
        if (hasAppliedUserAgent && !webView.url.isNullOrBlank()) {
            webView.reload()
        }
        hasAppliedUserAgent = true
    }

    LaunchedEffect(effects) {
        effects.collectLatest { effect ->
            when (effect) {
                is MainEffect.LoadUrl -> {
                    if (effect.reload) {
                        webView.reload()
                    } else {
                        val currentUrl = webView.url
                        if (currentUrl.isNullOrEmpty() || currentUrl != effect.url) {
                            webView.loadUrl(effect.url)
                        } else {
                            webView.reload()
                        }
                    }
                }
                MainEffect.Reload -> {
                    webView.reload()
                }
            }
        }
    }

    BackHandler {
        if (isPopupVisible) {
            val popup = popupWebView
            if (popup != null && popup.canGoBack()) {
                popup.goBack()
            } else {
                closePopup()
            }
            return@BackHandler
        }
        if (webView.canGoBack()) {
            webView.goBack()
            onPageUpdated(webView.url.orEmpty(), webView.title, webView.canGoBack())
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.addressText,
                            onValueChange = onAddressChange,
                            modifier = Modifier.weight(1f).height(46.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onSearch = {
                                    onSubmitAddress()
                                }
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (webView.canGoBack()) {
                            webView.goBack()
                            onPageUpdated(webView.url.orEmpty(), webView.title, webView.canGoBack())
                        } else {
                            (context as? Activity)?.finish()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                actions = {
                    IconButton(onClick = onLoadHome) {
                        Icon(Icons.Default.Home, contentDescription = "home")
                    }
                    IconButton(onClick = onOpenBookmarkDialog) {
                        Icon(Icons.Default.Bookmarks, contentDescription = "bookmark")
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "ブックマーク一覧") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenBookmarkList()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Computer, contentDescription = null)
                                        Text(text = if (state.isPcMode) "PCモード: ON" else "PCモード: OFF")
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    onTogglePcMode()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { webView },
                    update = { view ->
                        if (view.url.isNullOrEmpty() && state.currentUrl.isNotBlank()) {
                            view.loadUrl(state.currentUrl)
                        }
                    }
                )
            }
            if (isPopupVisible && popupWebView != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { popupWebView!! }
                    )
                    IconButton(
                        onClick = closePopup,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "close popup", tint = Color.White)
                    }
                }
            }
        }
    }

    if (state.bookmarkDialog.isVisible) {
        BookmarkSaveDialog(
            dialogState = state.bookmarkDialog,
            categories = state.categories,
            onTitleChange = onBookmarkTitleChange,
            onCategorySelect = onSelectCategory,
            onSave = onSaveBookmark,
            onDismiss = onCloseBookmarkDialog
        )
    }
}

@Composable
private fun BookmarkSaveDialog(
    dialogState: BookmarkDialogState,
    categories: List<BookMarkCategory>,
    onTitleChange: (String) -> Unit,
    onCategorySelect: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = dialogState.message == null && dialogState.selectedCategoryId != null && !dialogState.isSaving
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "閉じる")
            }
        },
        title = { Text(text = "ブックマーク登録") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (dialogState.message != null) {
                    Text(text = dialogState.message, color = Color.Red)
                }
                OutlinedTextField(
                    value = dialogState.titleInput,
                    onValueChange = onTitleChange,
                    label = { Text(text = "タイトル") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = dialogState.url,
                    onValueChange = {},
                    enabled = false,
                    label = { Text(text = "URL") },
                    singleLine = true
                )
                Text(text = "カテゴリ")
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = category.name, modifier = Modifier.weight(1f))
                        androidx.compose.material3.RadioButton(
                            selected = dialogState.selectedCategoryId == category.id,
                            onClick = { onCategorySelect(category.id) }
                        )
                    }
                }
            }
        }
    )
}

private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36"

private fun createConfiguredWebView(context: Context): WebView {
    return WebView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        with(settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            setSupportMultipleWindows(true)
        }
        setDownloadListener { _, _, _, _, _ -> }
    }
}

private fun applyUserAgent(settings: WebSettings, isPcMode: Boolean, mobileUserAgent: String) {
    if (isPcMode) {
        settings.userAgentString = DESKTOP_USER_AGENT
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
    } else {
        settings.userAgentString = mobileUserAgent
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = false
    }
}

private fun isAllowedScheme(uri: Uri): Boolean {
    val scheme = uri.scheme ?: return false
    return scheme == "http" || scheme == "https"
}
