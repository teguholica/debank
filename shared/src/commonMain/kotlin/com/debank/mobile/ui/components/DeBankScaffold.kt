package com.debank.mobile.ui.components

import androidx.compose.foundation.layout.Box
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
import androidx.navigation3.runtime.NavKey
import com.debank.mobile.domain.DashboardRoute
import com.debank.mobile.domain.HistoryRoute
import com.debank.mobile.domain.Route
import com.debank.mobile.domain.SendRoute
import com.debank.mobile.domain.SettingsRoute

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: NavKey
) {
    Dashboard("Dashboard", Icons.Default.Home, DashboardRoute),
    Send("Kirim", Icons.Default.Send, SendRoute()),
    History("Riwayat", Icons.Default.History, HistoryRoute),
    Settings("Pengaturan", Icons.Default.Settings, SettingsRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeBankScaffold(
    currentRoute: NavKey,
    onNavigate: (NavKey) -> Unit,
    showBottomNav: Boolean,
    snackbarHostState: SnackbarHostState? = null,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val showTitle = currentRoute is DashboardRoute

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
                SnackbarHost(snackbarHostState) { data ->
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
                    currentRoute = currentRoute,
                    onNavigate = onNavigate
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

@Composable
private fun DeBankBottomNav(
    currentRoute: NavKey,
    onNavigate: (NavKey) -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = when {
                currentRoute is DashboardRoute && item == BottomNavItem.Dashboard -> true
                currentRoute is SendRoute && item == BottomNavItem.Send -> true
                currentRoute is HistoryRoute && item == BottomNavItem.History -> true
                currentRoute is SettingsRoute && item == BottomNavItem.Settings -> true
                else -> false
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
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
