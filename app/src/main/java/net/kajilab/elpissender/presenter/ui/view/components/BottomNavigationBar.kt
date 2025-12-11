package net.kajilab.elpissender.presenter.ui.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.kajilab.elpissender.R
import net.kajilab.elpissender.entity.BottomNavigationBarRoute
import net.kajilab.elpissender.entity.BottomNavigationItem
import net.kajilab.elpissender.presenter.ui.theme.EstimatingLocationUsingRadioWavesTheme

@Composable
fun BottomNavigationBar(
    items: List<BottomNavigationItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    NavigationBar {
        items.forEachIndexed { index, screen ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    BadgeIcon(
                        badgeCount = screen.badgeCount,
                        hasNews = screen.hasNews,
                        selectedIcon = screen.selectedIcon,
                        unselectedIcon = screen.unselectedIcon,
                        index = index,
                        selectedItemIndex = selectedItemIndex,
                        contentDescription = screen.title,
                    )
                },
                label = { Text(screen.title) },
            )
        }
    }
}

@Composable
private fun BadgeIcon(
    badgeCount: Int?,
    hasNews: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    index: Int,
    selectedItemIndex: Int,
    contentDescription: String,
) {
    BadgedBox(
        badge = {
            when {
                badgeCount != null -> {
                    Badge {
                        Text(text = badgeCount.toString())
                    }
                }
                hasNews -> {
                    Badge()
                }
            }
        },
    ) {
        Icon(
            imageVector = if (index == selectedItemIndex) selectedIcon else unselectedIcon,
            contentDescription = contentDescription,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    EstimatingLocationUsingRadioWavesTheme {
        BottomNavigationBar(
            items =
                listOf(
                    BottomNavigationItem(
                        title = stringResource(id = R.string.home),
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Filled.Home,
                        hasNews = false,
                        badgeCount = null,
                        path = BottomNavigationBarRoute.HOME,
                    ),
                    BottomNavigationItem(
                        title = stringResource(id = R.string.user),
                        selectedIcon = Icons.Filled.AccountCircle,
                        unselectedIcon = Icons.Filled.AccountCircle,
                        hasNews = false,
                        badgeCount = null,
                        path = BottomNavigationBarRoute.USER,
                    ),
                ),
            selectedItemIndex = 0,
            onItemSelected = {},
        )
    }
}
