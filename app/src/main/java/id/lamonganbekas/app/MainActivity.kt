package id.lamonganbekas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF172B4D)
private val Blue = Color(0xFF315FEA)
private val Paper = Color(0xFFF5F7FC)
private val Muted = Color(0xFF667085)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LamonganBekasApp() }
    }
}

@Composable
private fun LamonganBekasApp() {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Semua") }
    var tab by remember { mutableStateOf("Beranda") }
    val categories = listOf("Semua", "Elektronik", "Kendaraan", "Rumah Tangga", "Fashion")

    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, background = Paper, surface = Color.White)) {
        Scaffold(
            containerColor = Paper,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf("Beranda", "Favorit", "Pesan", "Akun").forEach { label ->
                        NavigationBarItem(
                            selected = tab == label,
                            onClick = { tab = label },
                            icon = { Text(when (label) { "Beranda" -> "⌂"; "Favorit" -> "♡"; "Pesan" -> "☏"; else -> "●" }, fontSize = 20.sp) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color.White)
                            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(46.dp).background(Blue, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                                Text("LB", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("lamonganbekas", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Pasar barang bekas lokal", color = Muted, fontSize = 12.sp)
                            }
                            Text("LAMONGAN", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(28.dp))
                        Text("Barang lama,", color = Ink, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp)
                        Text("manfaat baru.", color = Blue, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Temukan barang menarik dari warga Lamongan. Lebih dekat, lebih praktis, lebih bermanfaat.", color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
                        Spacer(Modifier.height(18.dp))
                        Row(
                            Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = Muted)
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.weight(1f)) {
                                if (query.isEmpty()) Text("Cari HP, sepeda, meja…", color = Muted, fontSize = 14.sp)
                                BasicTextField(query, onValueChange = { query = it }, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp))
                            }
                        }
                        Spacer(Modifier.height(13.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Fokus warga Kabupaten Lamongan", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                        Text("JELAJAHI PASAR", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("Cari yang kamu butuhkan", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { category ->
                                Surface(color = if (selected == category) Blue else Color.White, shape = CircleShape, modifier = Modifier.clickable { selected = category }) {
                                    Text(category, Modifier.padding(horizontal = 13.dp, vertical = 9.dp), color = if (selected == category) Color.White else Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Surface(color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 1.dp) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(62.dp).background(Color(0xFFEAF0FF), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                                }
                                Spacer(Modifier.height(15.dp))
                                Text(if (query.isNotBlank() || selected != "Semua") "Belum ada barang yang cocok" else "Pasar sedang menunggu barang pertamanya", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Daftar barang akan muncul setelah layanan data terhubung dan iklan aktif tersedia.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                                Spacer(Modifier.height(14.dp))
                                Text("API belum dikonfigurasi", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Surface(color = Color(0xFFEAF6EF), shape = RoundedCornerShape(16.dp)) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Text("✓", color = Color(0xFF19734A), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Transaksi nyaman dimulai dari waspada", color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(Modifier.height(5.dp))
                                    Text("Periksa kondisi barang, bertemu di tempat aman, dan jangan transfer sebelum yakin.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
