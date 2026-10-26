package id.lamonganbekas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

private val Ink = Color(0xFF172B4D)
private val Blue = Color(0xFF315FEA)
private val Paper = Color(0xFFF5F7FC)
private val Muted = Color(0xFF667085)

private data class Category(val id: String, val nama: String, val slug: String)
private data class Listing(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val harga: Long,
    val kondisi: String,
    val kecamatan: String,
    val kategori: String
)

private suspend fun apiGet(path: String): JSONObject = withContext(Dispatchers.IO) {
    if (BuildConfig.API_BASE_URL.isBlank()) {
        throw IllegalStateException("Alamat API belum dikonfigurasi. Backend perlu diaktifkan terlebih dahulu.")
    }
    val connection = (URL(BuildConfig.API_BASE_URL + path).openConnection() as HttpURLConnection)
    connection.requestMethod = "GET"
    connection.connectTimeout = 10_000
    connection.readTimeout = 12_000
    connection.setRequestProperty("Accept", "application/json")
    try {
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (status !in 200..299) {
            val message = runCatching { JSONObject(body).optString("error") }.getOrNull()
            throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: "Server merespons HTTP $status.")
        }
        JSONObject(body)
    } finally {
        connection.disconnect()
    }
}

private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
private fun rupiah(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value).replace(",00", "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LamonganBekasApp() }
    }
}

@Composable
private fun LamonganBekasApp() {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("Beranda") }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var listings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var categoriesLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var categoriesError by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(retryToken) {
        categoriesLoading = true
        categoriesError = null
        try {
            val response = apiGet("/categories")
            val array = response.optJSONArray("data")
            categories = buildList {
                if (array != null) for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val slug = item.optString("slug")
                    val name = item.optString("nama")
                    if (slug.isNotBlank() && name.isNotBlank()) {
                        add(Category(item.optString("id"), name, slug))
                    }
                }
            }
        } catch (e: Exception) {
            categoriesError = e.message ?: "Kategori gagal dimuat."
        } finally {
            categoriesLoading = false
        }
    }

    LaunchedEffect(query, selectedCategory, retryToken) {
        delay(300)
        loading = true
        error = null
        try {
            val params = mutableListOf("limit=24")
            if (query.isNotBlank()) params += "q=${encode(query.trim())}"
            if (selectedCategory.isNotBlank()) params += "kategori=${encode(selectedCategory)}"
            val response = apiGet("/listings?${params.joinToString("&")}")
            val array = response.optJSONArray("data")
            listings = buildList {
                if (array != null) for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(
                        Listing(
                            id = item.optString("id"),
                            judul = item.optString("judul", "Barang tanpa judul"),
                            deskripsi = item.optString("deskripsi"),
                            harga = item.optLong("harga"),
                            kondisi = item.optString("kondisi").replace('_', ' '),
                            kecamatan = item.optString("kecamatan"),
                            kategori = item.optString("kategori", "Lainnya")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            listings = emptyList()
            error = e.message ?: "Barang gagal dimuat."
        } finally {
            loading = false
        }
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, background = Paper, surface = Color.White)) {
        Scaffold(
            containerColor = Paper,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf("Beranda", "Favorit", "Pesan", "Akun").forEach { label ->
                        NavigationBarItem(
                            selected = tab == label,
                            onClick = { tab = label },
                            icon = {
                                Text(
                                    when (label) {
                                        "Beranda" -> "⌂"
                                        "Favorit" -> "♡"
                                        "Pesan" -> "☏"
                                        else -> "●"
                                    },
                                    fontSize = 20.sp
                                )
                            },
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
                                BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp)
                                )
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
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (selectedCategory.isBlank()) Blue else Color.White,
                                shape = CircleShape,
                                modifier = Modifier.clickable { selectedCategory = "" }
                            ) {
                                Text("Semua", Modifier.padding(horizontal = 13.dp, vertical = 9.dp), color = if (selectedCategory.isBlank()) Color.White else Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            categories.forEach { category ->
                                Surface(
                                    color = if (selectedCategory == category.slug) Blue else Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.clickable { selectedCategory = category.slug }
                                ) {
                                    Text(category.nama, Modifier.padding(horizontal = 13.dp, vertical = 9.dp), color = if (selectedCategory == category.slug) Color.White else Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        if (categoriesLoading) {
                            Spacer(Modifier.height(8.dp))
                            Text("Memuat kategori…", color = Muted, fontSize = 12.sp)
                        }
                        categoriesError?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Kategori: $it", color = Color(0xFFB42318), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(22.dp))
                        when {
                            loading -> {
                                Box(Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Blue)
                                }
                            }
                            error != null -> {
                                Surface(color = Color.White, shape = RoundedCornerShape(20.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Barang belum dapat dimuat", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                        Text(error.orEmpty(), color = Muted, fontSize = 13.sp)
                                        Spacer(Modifier.height(12.dp))
                                        Button(onClick = { retryToken++ }) { Text("Coba lagi") }
                                    }
                                }
                            }
                            listings.isEmpty() -> {
                                Surface(color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 1.dp) {
                                    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(Modifier.size(62.dp).background(Color(0xFFEAF0FF), CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                                        }
                                        Spacer(Modifier.height(15.dp))
                                        Text("Belum ada barang aktif", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Barang akan tampil di sini setelah warga memasang iklan yang aktif.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                                    }
                                }
                            }
                            else -> {
                                Text("${listings.size} barang ditemukan", color = Muted, fontSize = 12.sp)
                                Spacer(Modifier.height(10.dp))
                                listings.forEach { listing ->
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(18.dp),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    ) {
                                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                            Text(listing.kategori.uppercase(Locale("id", "ID")), color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                                            Spacer(Modifier.height(5.dp))
                                            Text(listing.judul, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(6.dp))
                                            Text(rupiah(listing.harga), color = Blue, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                            if (listing.deskripsi.isNotBlank()) {
                                                Spacer(Modifier.height(6.dp))
                                                Text(listing.deskripsi.take(140), color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Text("${listing.kondisi.replaceFirstChar { it.uppercase() }} • ${listing.kecamatan}", color = Muted, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
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
