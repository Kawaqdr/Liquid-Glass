package com.kawaqdr.liquidglass.ui

import android.content.Intent
import android.provider.Settings
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kawaqdr.liquidglass.data.AppRepository
import com.kawaqdr.liquidglass.data.LauncherApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassApp() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
    var query by remember { mutableStateOf("") }
    var drawerOpen by remember { mutableStateOf(false) }
    var horizontalDrawer by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { apps = repository.loadApps() }

    val visibleApps = apps.filter { it.label.contains(query, ignoreCase = true) }
    val pagerState = rememberPagerState(pageCount = { ((visibleApps.size + 23) / 24).coerceAtLeast(1) })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF172337))
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Box(
            Modifier.fillMaxSize().background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(0xFF668DAA), Color(0xFF293D5A), Color(0xFF171D31))
                )
            )
        )

        if (settingsOpen) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Liquid Glass", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Launcher settings", color = Color.White.copy(alpha = .7f))
                GlassSettingRow("App drawer layout", if (horizontalDrawer) "Horizontal pages" else "Vertical scroll") {
                    horizontalDrawer = !horizontalDrawer
                }
                GlassSettingRow("Refresh installed apps", "Reload") { apps = repository.loadApps() }
                GlassSettingRow("Android Home settings", "Choose default") {
                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
                GlassSettingRow("Back to Home", "Done") { settingsOpen = false }
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(22.dp))
                Text("LIQUID GLASS", color = Color.White.copy(alpha = .8f), fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Home", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(24.dp))
                if (drawerOpen) {
                    Row(
                        Modifier.fillMaxWidth().glassSurface().padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                            decorationBox = { inner ->
                                Box {
                                    if (query.isEmpty()) Text("Search apps", color = Color.White.copy(alpha = .55f))
                                    inner()
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    if (horizontalDrawer) {
                        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                            AppGrid(visibleApps.drop(page * 24).take(24), repository)
                        }
                    } else {
                        AppGrid(visibleApps, repository, modifier = Modifier.weight(1f))
                    }
                } else {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Bottom) {
                        Text("Your space.", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
                        Text("A calmer way to get things done.", color = Color.White.copy(alpha = .75f), fontSize = 15.sp)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().glassSurface(RoundedCornerShape(36.dp)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DockAction("Home", Icons.Default.Apps) { drawerOpen = false; query = "" }
                    DockAction(if (drawerOpen) "Close" else "Apps", Icons.Default.Search) {
                        drawerOpen = !drawerOpen
                        query = ""
                    }
                    DockAction("Settings", Icons.Default.Settings) { settingsOpen = true; drawerOpen = false }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<LauncherApp>,
    repository: AppRepository,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { repository.launch(app) }
            ) {
                AndroidView(
                    factory = { viewContext ->
                        ImageView(viewContext).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setImageDrawable(viewContext.packageManager.getApplicationIcon(app.packageName))
                        }
                    },
                    modifier = Modifier.size(54.dp)
                )
                Spacer(Modifier.height(7.dp))
                Text(app.label, color = Color.White, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DockAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(23.dp))
        Text(label, color = Color.White.copy(alpha = .85f), fontSize = 10.sp)
    }
}

@Composable
private fun GlassSettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().glassSurface().clickable(onClick = onClick).padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Medium)
        Text(value, color = Color(0xFFC7E7FF), fontSize = 12.sp)
    }
}
