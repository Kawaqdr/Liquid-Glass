package com.mdrakibjs.liquidglass.ui

import android.app.WallpaperManager
import android.content.Intent
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mdrakibjs.liquidglass.data.AppRepository
import com.mdrakibjs.liquidglass.data.LauncherApp
import kotlinx.coroutines.launch
import kotlin.math.ceil

private const val DOCK_LIMIT = 5
private const val APPS_PER_PAGE = 20
private const val PREFS_NAME = "liquid_glass_launcher"
private const val PREF_DOCK_APPS = "dock_packages"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassApp() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    val preferences = remember { context.getSharedPreferences(PREFS_NAME, 0) }
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
    var dockPackages by remember { mutableStateOf<List<String>>(emptyList()) }
    var drawerOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    BackHandler(enabled = drawerOpen || settingsOpen) {
        if (drawerOpen) { drawerOpen = false; query = "" } else settingsOpen = false
    }

    LaunchedEffect(Unit) {
        val loaded = repository.loadApps()
        apps = loaded
        val saved = preferences.getStringSet(PREF_DOCK_APPS, null)
        val initial = (saved?.toList() ?: loaded.take(DOCK_LIMIT).map { it.packageName })
            .filter { pkg -> loaded.any { it.packageName == pkg } }
            .take(DOCK_LIMIT)
        dockPackages = initial
        if (saved == null) preferences.edit().putStringSet(PREF_DOCK_APPS, initial.toSet()).apply()
    }

    fun saveDock(value: List<String>) {
        val safe = value.distinct().take(DOCK_LIMIT)
        dockPackages = safe
        preferences.edit().putStringSet(PREF_DOCK_APPS, safe.toSet()).apply()
    }

    fun toggleDock(app: LauncherApp) {
        val next = if (app.packageName in dockPackages) dockPackages - app.packageName
        else if (dockPackages.size < DOCK_LIMIT) dockPackages + app.packageName
        else {
            Toast.makeText(context, "Dock holds up to 5 apps", Toast.LENGTH_SHORT).show()
            return
        }
        saveDock(next)
        Toast.makeText(context, if (app.packageName in next) "Added to Dock" else "Removed from Dock", Toast.LENGTH_SHORT).show()
    }

    val dockApps = dockPackages.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    val homeApps = remember(apps, dockPackages) { apps.filterNot { it.packageName in dockPackages } }
    val filteredApps = remember(apps, query) { apps.filter { it.label.contains(query, true) } }
    val pageCount = maxOf(1, ceil(homeApps.size / APPS_PER_PAGE.toFloat()).toInt())
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(Modifier.fillMaxSize().background(Color(0xFF08101F))) {
        AndroidView(
            factory = { viewContext ->
                ImageView(viewContext).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    try { setImageDrawable(WallpaperManager.getInstance(viewContext).drawable) }
                    catch (_: Exception) { setBackgroundColor(android.graphics.Color.rgb(8, 16, 31)) }
        } },
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color(0x18030A18)))

        if (settingsOpen) {
            Column(
                Modifier.fillMaxSize().padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                    start = 22.dp, end = 22.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Settings", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                SettingRow("Refresh installed apps", "Refresh") {
                    scope.launch { apps = repository.loadApps() }
                }
                SettingRow("Manage Dock", "${dockPackages.size}/5") { settingsOpen = false; drawerOpen = true }
                SettingRow("Android Home settings", "Default launcher") {
                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
                SettingRow("Done", "Close") { settingsOpen = false }
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
                    start = 14.dp, end = 14.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    pageSpacing = 0.dp,
                    key = { it }
                ) { page ->
                    val pageApps = homeApps.drop(page * APPS_PER_PAGE).take(APPS_PER_PAGE)
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                        pageApps.chunked(4).forEach { rowApps ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowApps.forEach { app ->
                                    HomeAppIcon(app = app, onClick = { repository.launch(app) }, onLongPress = { toggleDock(app) })
                                }
                                repeat(4 - rowApps.size) { Spacer(Modifier.width(76.dp)) }
                            }
                        }
                        repeat((5 - ceil(pageApps.size / 4f).toInt()).coerceAtLeast(0)) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }

                if (pageCount > 1) {
                    Row(
                        Modifier.padding(top = 5.dp, bottom = 17.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pageCount) { index ->
                            Box(
                                Modifier.size(if (pagerState.currentPage == index) 8.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = .42f))
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(30.dp))
                }

                DockBar(apps = dockApps, repository = repository, onRemove = { toggleDock(it) })
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .size(width = 34.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = .68f))
                        .clickable { drawerOpen = true; query = "" }
                        .pointerInput(Unit) {
                            var total = 0f
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, drag -> total += drag },
                                onDragEnd = { if (total < -18f) { drawerOpen = true; query = "" }; total = 0f }
                            )
                        }
                )
            }
        }

        AnimatedVisibility(
            visible = drawerOpen && !settingsOpen,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(.88f),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Column(
                Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Color(0xE8172235))
                    .border(1.dp, Color.White.copy(alpha = .20f), RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 34.dp, height = 4.dp)
                    .clip(CircleShape).background(Color.White.copy(alpha = .45f)))
                Row(Modifier.fillMaxWidth().padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("App Library", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                        Text("${dockApps.size} of 5 dock spaces used", color = Color.White.copy(alpha = .58f), fontSize = 11.sp)
                    }
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White,
                        modifier = Modifier.clip(CircleShape).clickable { drawerOpen = false; query = "" }.padding(9.dp))
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color.White.copy(alpha = .13f))
                        .padding(horizontal = 13.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(9.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) Text("Search", color = Color.White.copy(alpha = .5f), fontSize = 14.sp)
                        BasicTextField(
                            value = query, onValueChange = { query = it }, singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp)
                        )
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f).padding(top = 10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(17.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        HomeAppIcon(app = app, onClick = { repository.launch(app) }, onLongPress = { toggleDock(app) }, compact = true,
                            pinned = app.packageName in dockPackages)
                    }
                }
                DockBar(apps = dockApps, repository = repository, onRemove = { toggleDock(it) })
            }
        }
 

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeAppIcon(
    app: LauncherApp,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    compact: Boolean = false,
    pinned: Boolean = false
) {
    Column(
        modifier = Modifier.width(if (compact) 76.dp else 76.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { viewContext ->
                ImageView(viewContext).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageDrawable(viewContext.packageManager.getApplicationIcon(app.packageName))
        } },
            modifier = Modifier.size(if (compact) 48.dp else 57.dp)
        )
        Spacer(Modifier.height(5.dp))
        Text(app.label, color = Color.White, fontSize = if (compact) 10.sp else 11.sp,
            maxLines = 1, fontWeight = FontWeight.Normal)
        if (compact && pinned) Text("In Dock", color = Color(0xFFBDE6FF), fontSize = 8.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockBar(apps: List<LauncherApp>, repository: AppRepository, onRemove: (LauncherApp) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(29.dp))
            .background(Color(0x3DFFFFFF))
            .border(1.dp, Color.White.copy(alpha = .32f), RoundedCornerShape(29.dp))
            .padding(horizontal = 7.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            AndroidView(
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageDrawable(viewContext.packageManager.getApplicationIcon(app.packageName))
            } },
                modifier = Modifier.size(51.dp).clip(RoundedCornerShape(13.dp))
                    .combinedClickable(onClick = { repository.launch(app) }, onLongClick = { onRemove(app) })
            )
        }
        repeat((DOCK_LIMIT - apps.size).coerceAtLeast(0)) {
            Box(Modifier.size(51.dp).clip(RoundedCornerShape(13.dp))
                .background(Color.White.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Text("+", color = Color.White.copy(alpha = .55f), fontSize = 21.sp)
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = .14f)).clickable(onClick = onClick).padding(17.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Medium)
        Text(value, color = Color.White.copy(alpha = .68f), fontSize = 12.sp)
    }
}
