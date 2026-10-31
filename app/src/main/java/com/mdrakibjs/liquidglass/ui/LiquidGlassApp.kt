package com.mdrakibjs.liquidglass.ui

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mdrakibjs.liquidglass.data.AppRepository
import com.mdrakibjs.liquidglass.data.LauncherApp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DOCK_LIMIT = 5
private const val PREFS_NAME = "liquid_glass_launcher"
private const val PREF_DOCK_APPS = "dock_packages"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassApp() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    val scope = rememberCoroutineScope()
    val preferences = remember { context.getSharedPreferences(PREFS_NAME, 0) }
    var apps by remember { mutableStateOf(emptyList<LauncherApp>()) }
    var dockPackages by remember { mutableStateOf<List<String>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var drawerOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerOpen || settingsOpen) {
        if (drawerOpen) { drawerOpen = false; query = "" } else settingsOpen = false
    }

    LaunchedEffect(Unit) {
        val loaded = repository.loadApps()
        apps = loaded
        val saved = preferences.getStringSet(PREF_DOCK_APPS, null)
        val initialDock = (saved?.toList() ?: loaded.take(DOCK_LIMIT).map { it.packageName })
            .filter { pkg -> loaded.any { it.packageName == pkg } }
            .take(DOCK_LIMIT)
        dockPackages = initialDock
        if (saved == null) {
            preferences.edit().putStringSet(PREF_DOCK_APPS, initialDock.toSet()).apply()
        }
    }

    fun persistDock(packages: List<String>) {
        val safePackages = packages.distinct().take(DOCK_LIMIT)
        dockPackages = safePackages
        preferences.edit().putStringSet(PREF_DOCK_APPS, safePackages.toSet()).apply()
    }

    fun toggleDock(app: LauncherApp) {
        val updated = if (app.packageName in dockPackages) {
            dockPackages - app.packageName
        } else if (dockPackages.size < DOCK_LIMIT) {
            dockPackages + app.packageName
        } else {
            Toast.makeText(context, "Dock holds up to 5 apps", Toast.LENGTH_SHORT).show()
            return
        }
        persistDock(updated)
        Toast.makeText(
            context,
            if (app.packageName in updated) "Added to Dock" else "Removed from Dock",
            Toast.LENGTH_SHORT
        ).show()
    }

    val dockApps = dockPackages.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    val filteredApps = remember(apps, query) {
        apps.filter { it.label.contains(query, ignoreCase = true) }
    }
    val homeApps = remember(apps, dockPackages) {
        apps.filterNot { it.packageName in dockPackages }.take(12)
    }
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF8AA7BA),
                        .34f to Color(0xFF415B76),
                        .70f to Color(0xFF202D45),
                        1f to Color(0xFF111827)
                    )
                )
            )
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 65.dp, end = 22.dp)
                .size(210.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x5578D8FF), Color.Transparent)))
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 90.dp)
                .size(250.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x445F8FFF), Color.Transparent)))
        )

        if (settingsOpen) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Settings", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                Text("Make your space yours", color = Color.White.copy(alpha = .65f), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                GlassSettingRow("Refresh installed apps", "Refresh") {
                    scope.launch {
                        val refreshed = repository.loadApps()
                        apps = refreshed
                        persistDock(dockPackages.filter { pkg -> refreshed.any { it.packageName == pkg } })
                    }
                }
                GlassSettingRow("Manage Dock", "${dockPackages.size}/5 apps") {
                    settingsOpen = false
                    drawerOpen = true
                }
                GlassSettingRow("Android Home settings", "Default launcher") {
                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
                GlassSettingRow("Done", "Back to Home") { settingsOpen = false }
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.height(22.dp))
                Text(dateLabel.uppercase(), color = Color.White.copy(alpha = .72f), fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(5.dp))
                Text("Good day.", color = Color.White, fontSize = 39.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp)
                Spacer(Modifier.height(25.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Favourites", color = Color.White.copy(alpha = .92f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "EDIT DOCK",
                        color = Color.White.copy(alpha = .65f),
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { drawerOpen = true; query = "" }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
                Spacer(Modifier.height(15.dp))

                if (homeApps.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        items(homeApps, key = { it.packageName }) { app ->
                            AppTile(
                                app = app,
                                repository = repository,
                                isPinned = app.packageName in dockPackages,
                                onLongPress = { toggleDock(app) }
                            )
                        }
                    }
                } else {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Your apps will appear here", color = Color.White.copy(alpha = .7f))
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = .13f))
                        .combinedClickable(
                            onClick = { drawerOpen = true; query = "" },
                            onLongClick = { drawerOpen = true; query = "" }
                        )
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search apps", tint = Color.White.copy(alpha = .9f), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Search your apps", color = Color.White.copy(alpha = .75f), fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Menu, contentDescription = "App library", tint = Color.White.copy(alpha = .75f), modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(13.dp))
                LiquidDock(
                    apps = dockApps,
                    repository = repository,
                    onRemove = { toggleDock(it) }
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        "SETTINGS",
                        color = Color.White.copy(alpha = .62f),
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        modifier = Modifier.clickable { settingsOpen = true }.padding(8.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        AnimatedVisibility(
            visible = drawerOpen && !settingsOpen,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 5 })
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xD9142032))
                    .padding(
                        top = 20.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 12.dp
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("App Library", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
                        Text("Long-press an app to add it to your Dock", color = Color.White.copy(alpha = .65f), fontSize = 12.sp)
                    }
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close app library",
                        tint = Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { drawerOpen = false; query = "" }
                            .padding(10.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth().glassSurface(RoundedCornerShape(22.dp)).padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = .8f), modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) Text("Search apps", color = Color.White.copy(alpha = .55f), fontSize = 15.sp)
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ALL APPS", color = Color.White.copy(alpha = .58f), fontSize = 10.sp, letterSpacing = 1.6.sp)
                    Text("${dockApps.size}/5 IN DOCK", color = Color.White.copy(alpha = .58f), fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppTile(
                            app = app,
                            repository = repository,
                            isPinned = app.packageName in dockPackages,
                            onLongPress = { toggleDock(app) }
                        )
                    }
                }
                LiquidDock(
                    apps = dockApps,
                    repository = repository,
                    onRemove = { toggleDock(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: LauncherApp,
    repository: AppRepository,
    isPinned: Boolean,
    onLongPress: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { repository.launch(app) },
                onLongClick = onLongPress
            )
            .padding(vertical = 2.dp)
    ) {
        Box {
            AndroidView(
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageDrawable(viewContext.packageManager.getApplicationIcon(app.packageName))
                    }
                },
                modifier = Modifier.size(54.dp)
            )
            if (isPinned) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBCE7FF))
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(app.label, color = Color.White.copy(alpha = .96f), fontSize = 10.sp, maxLines = 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LiquidDock(
    apps: List<LauncherApp>,
    repository: AppRepository,
    onRemove: (LauncherApp) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .glassSurface(RoundedCornerShape(34.dp), Color(0x33263850))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            AndroidView(
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageDrawable(viewContext.packageManager.getApplicationIcon(app.packageName))
                    }
                },
                modifier = Modifier
                    .size(49.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = { repository.launch(app) },
                        onLongClick = { onRemove(app) }
                    )
            )
        }
        repeat((DOCK_LIMIT - apps.size).coerceAtLeast(0)) {
            Box(
                Modifier
                    .size(49.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White.copy(alpha = .055f)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White.copy(alpha = .55f), fontSize = 23.sp, fontWeight = FontWeight.Light)
            }
        }
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
