package com.debank.mobile.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.debank.mobile.domain.AppScreen

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: AppScreen
) {
    Dashboard("Dashboard", Icons.Default.Home, AppScreen.Dashboard),
    Send("Kirim", Icons.Default.Send, AppScreen.Send()),
    History("Riwayat", Icons.Default.History, AppScreen.History),
    Settings("Pengaturan", Icons.Default.Settings, AppScreen.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeBankScaffold(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    showBottomNav: Boolean,
    snackbarHostState: SnackbarHostState? = null,
    topBar: @Composable () -> Unit = {},
    content: @Composable (AppScreen) -> Unit
) {
    val showTitle = currentScreen == AppScreen.Dashboard

    Scaffold(
        topBar = {
            if (showTitle) {
                TopAppBar(title = { Text("DeBank") })
            } else {
                topBar()
            }
        },
        snackbarHost = if (snackbarHostState != null) {
            {
                val state = snackbarHostState!!
                SnackbarHost(state) { data ->
                    val isSuccess = data.visuals.message.startsWith("✓")
                    val displayMessage = data.visuals.message.removePrefix("✓").removePrefix("✗")
                    Snackbar(
                        shape = RoundedCornerShape(12.dp),
                        containerColor = if (isSuccess) Color(0xFF43A047) else MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(displayMessage)
                        }
                    }
                }
            }
        } else {
            { SnackbarHost(SnackbarHostState()) }
        },
        bottomBar = {
            if (showBottomNav) {
                DeBankBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isForward = when {
                    targetState is AppScreen.Send && initialState is AppScreen.Dashboard -> true
                    targetState is AppScreen.History && initialState is AppScreen.Dashboard -> true
                    targetState is AppScreen.Settings && initialState is AppScreen.Dashboard -> true
                    targetState is AppScreen.Dashboard -> true
                    else -> false
                }
                if (isForward) {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { it / 4 }
                    ) togetherWith
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { -it / 4 }
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { -it / 4 }
                    ) togetherWith
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { it / 4 }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "screenTransition"
        ) { screen ->
            content(screen)
        }
    }
}

@Composable
private fun DeBankBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = when (currentScreen) {
                is AppScreen.Dashboard -> item == BottomNavItem.Dashboard
                is AppScreen.Send -> item == BottomNavItem.Send
                is AppScreen.History -> item == BottomNavItem.History
                is AppScreen.Settings -> item == BottomNavItem.Settings
                else -> false
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                label = {
                    Text(
                        item.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}

