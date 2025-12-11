package net.kajilab.elpissender.presenter.ui.view.home

import androidx.compose.runtime.Composable
import net.kajilab.elpissender.presenter.ui.view.components.WebViewComponent

@Composable
fun HomeScreen(topAppBarActions: (List<@Composable () -> Unit>) -> Unit) {
    WebViewComponent(
        topAppBarActions = topAppBarActions,
        url = "https://elpis.kajilab.dev/",
    )
}
