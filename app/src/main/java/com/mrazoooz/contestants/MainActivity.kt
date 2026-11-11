package com.mrazoooz.contestants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private data class Contestant(val id: Long, val name: String, val score: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ContestantsApp() }
    }
}

private val Ink = Color(0xFF17231F)
private val Green = Color(0xFF167A5A)
private val Mint = Color(0xFFE5F4ED)
private val Canvas = Color(0xFFF5F7F5)
private val Muted = Color(0xFF78847E)

@Composable
private fun ContestantsApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("contestants_store", 0) }
    val people = remember {
        mutableStateListOf<Contestant>().apply {
            val raw = prefs.getString("people", "[]") ?: "[]"
            runCatching {
                val array = JSONArray(raw)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(Contestant(item.getLong("id"), item.getString("name"), item.getInt("score")))
                }
            }
        }
    }
    var query by remember { mutableStateOf("") }
    var sortHigh by remember { mutableStateOf(true) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Contestant?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var limited by remember { mutableStateOf(prefs.getBoolean("limited", false)) }
    var maxScore by remember { mutableIntStateOf(prefs.getInt("maxScore", 100)) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }

    LaunchedEffect(people.toList()) {
        val array = JSONArray()
        people.forEach { p ->
            array.put(JSONObject().put("id", p.id).put("name", p.name).put("score", p.score))
        }
        prefs.edit().putString("people", array.toString()).apply()
    }
    LaunchedEffect(limited, maxScore, dark) {
        prefs.edit().putBoolean("limited", limited).putInt("maxScore", maxScore).putBoolean("dark", dark).apply()
    }

    val sorted = people.filter { it.name.contains(query.trim(), ignoreCase = true) }
        .sortedWith(if (sortHigh) compareByDescending<Contestant> { it.score }.thenBy { it.name } else compareBy<Contestant> { it.name })

    val scheme = if (dark) darkColorScheme(
        primary = Color(0xFF73D5AC), background = Color(0xFF111A16),
        surface = Color(0xFF1B2721), onSurface = Color(0xFFE8F0EB),
        onBackground = Color(0xFFE8F0EB)
    ) else lightColorScheme(
        primary = Green, background = Canvas, surface = Color.White,
        onSurface = Ink, onBackground = Ink
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { editing = null; showEditor = true },
                        containerColor = Green,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(18.dp)
                    ) { Icon(Icons.Default.Add, contentDescription = "إضافة متسابق") }
                }
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("كشف المتسابقين", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Text("تابع النتائج والنقاط بسهولة", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "الإعدادات", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryCard("المتسابقون", people.size.toString(), Icons.Default.Groups, Modifier.weight(1f))
                        SummaryCard("أعلى نتيجة", (people.maxOfOrNull { it.score } ?: 0).toString(), Icons.Default.EmojiEvents, Modifier.weight(1f))
                        SummaryCard("مجموع النقاط", people.sumOf { it.score }.toString(), Icons.Default.Star, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ابحث عن متسابق...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("قائمة المتسابقين", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { sortHigh = !sortHigh }) {
                            Icon(if (sortHigh) Icons.Default.ExpandMore else Icons.Default.ExpandLess, null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (sortHigh) "الأعلى نقاطاً" else "حسب الاسم")
                        }
                    }

                    if (sorted.isEmpty()) {
                        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Groups, null, tint = Muted, modifier = Modifier.size(44.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(if (people.isEmpty()) "ابدأ بإضافة أول متسابق" else "لا توجد نتائج مطابقة", color = Muted)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(sorted, key = { it.id }) { person ->
                                ContestantCard(
                                    person = person,
                                    onIncrease = {
                                        val index = people.indexOfFirst { it.id == person.id }
                                        if (index >= 0 && (!limited || people[index].score < maxScore))
                                            people[index] = people[index].copy(score = people[index].score + 1)
                                    },
                                    onDecrease = {
                                        val index = people.indexOfFirst { it.id == person.id }
                                        if (index >= 0 && people[index].score > 0)
                                            people[index] = people[index].copy(score = people[index].score - 1)
                                    },
                                    onEdit = { editing = person; showEditor = true },
                                    onDelete = { people.removeAll { it.id == person.id } },
                                    maxScore = if (limited) maxScore else null
                                )
                            }
                        }
                    }
                }
            }

            if (showEditor) {
                EditorDialog(
                    initial = editing,
                    onDismiss = { showEditor = false },
                    onSave = { name, score ->
                        val old = editing
                        if (old == null) people.add(Contestant(System.currentTimeMillis(), name, score.coerceAtLeast(0)))
                        else {
                            val index = people.indexOfFirst { it.id == old.id }
                            if (index >= 0) people[index] = old.copy(name = name, score = score.coerceAtLeast(0))
                        }
                        showEditor = false
                    }
                )
            }

            if (showSettings) {
                SettingsDialog(
                    limited = limited, maxScore = maxScore, dark = dark,
                    onLimited = { limited = it }, onMax = { maxScore = it },
                    onDark = { dark = it }, onDismiss = { showSettings = false }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, null, tint = Green, modifier = Modifier.size(19.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
        }
    }
}

@Composable
private fun ContestantCard(
    person: Contestant,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    maxScore: Int?
) {
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp).background(Mint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(person.name.take(1).uppercase(), color = Green, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(person.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(if (maxScore == null) "النقاط المسجلة" else "من أصل $maxScore", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(person.score.toString(), color = Green, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                Text("نقطة", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledTonalIconButton(onClick = onIncrease, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(4.dp))
                FilledTonalIconButton(onClick = onDecrease, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.ExpandMore, null, modifier = Modifier.size(18.dp))
                }
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "خيارات") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("تعديل") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(text = { Text("حذف") }, onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun EditorDialog(
    initial: Contestant?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var score by remember(initial) { mutableStateOf(initial?.score?.toString() ?: "0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "إضافة متسابق" else "تعديل المتسابق", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المتسابق") }, singleLine = true)
                OutlinedTextField(value = score, onValueChange = { score = it.filter(Char::isDigit).take(7) }, label = { Text("النقاط") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name.trim(), score.toIntOrNull() ?: 0) }, enabled = name.isNotBlank()) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun SettingsDialog(
    limited: Boolean,
    maxScore: Int,
    dark: Boolean,
    onLimited: (Boolean) -> Unit,
    onMax: (Int) -> Unit,
    onDark: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var maxText by remember(maxScore) { mutableStateOf(maxScore.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("نظام النقاط المحدود")
                    Switch(checked = limited, onCheckedChange = onLimited)
                }
                if (limited) {
                    OutlinedTextField(value = maxText, onValueChange = { maxText = it.filter(Char::isDigit).take(7) }, label = { Text("الحد الأعلى للنقاط") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("الوضع الداكن")
                    Switch(checked = dark, onCheckedChange = onDark)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onMax((maxText.toIntOrNull() ?: 100).coerceAtLeast(1)); onDismiss() }) { Text("تم") }
        }
    )
}
