import { createServer } from "node:http";
import { neon } from "@neondatabase/serverless";

const port = Number(process.env.PORT || 8080);
const databaseUrl = process.env.DATABASE_URL;

function send(res, status, payload, extraHeaders = {}) {
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    ...extraHeaders,
  });
  res.end(JSON.stringify(payload));
}

const server = createServer(async (req, res) => {
  const url = new URL(req.url || "/", "http://localhost");
  if (req.method === "OPTIONS") {
    res.writeHead(204, { Allow: "GET, HEAD, OPTIONS" });
    return res.end();
  }
  if (req.method !== "GET" && req.method !== "HEAD") {
    return send(res, 405, { error: "Metode tidak diizinkan." }, { Allow: "GET, HEAD, OPTIONS" });
  }
  if (url.pathname === "/health") return send(res, 200, { ok: true, service: "lamongan-bekas-api" });
  if (!databaseUrl) return send(res, 503, { error: "Konfigurasi database belum tersedia." });

  try {
    const sql = neon(databaseUrl);
    if (url.pathname === "/categories") {
      const data = await sql`SELECT id, nama, slug FROM public.categories ORDER BY id`;
      return send(res, 200, { data }, { "Cache-Control": "public, max-age=60, stale-while-revalidate=300" });
    }
    if (url.pathname === "/listings") {
      const search = (url.searchParams.get("q") || "").trim().slice(0, 80);
      const category = (url.searchParams.get("kategori") || "").trim().slice(0, 80);
      const parsedLimit = Number.parseInt(url.searchParams.get("limit") || "24", 10);
      const limit = Math.min(Math.max(Number.isFinite(parsedLimit) ? parsedLimit : 24, 1), 48);
      const data = await sql.query(
        "SELECT l.id, l.judul, l.deskripsi, l.harga, l.kondisi, l.kecamatan, l.dibuat_pada, c.nama AS kategori, c.slug AS kategori_slug, (SELECT p.url FROM public.listing_photos p WHERE p.listing_id = l.id ORDER BY p.urutan LIMIT 1) AS foto FROM public.listings l JOIN public.categories c ON c.id = l.kategori_id WHERE l.status = 'aktif' AND (l.kedaluwarsa_pada IS NULL OR l.kedaluwarsa_pada > now()) AND ($1 = '' OR l.judul ILIKE '%' || $1 || '%' OR l.deskripsi ILIKE '%' || $1 || '%' OR l.kecamatan ILIKE '%' || $1 || '%') AND ($2 = '' OR c.slug = $2) ORDER BY l.dibuat_pada DESC LIMIT $3",
        [search, category, limit]
      );
      return send(res, 200, { data }, { "Cache-Control": "public, max-age=30, stale-while-revalidate=120" });
    }
    return send(res, 404, { error: "Endpoint tidak ditemukan." });
  } catch (error) {
    console.error("API request failed", { path: url.pathname, message: error?.message });
    return send(res, 500, { error: url.pathname === "/categories" ? "Kategori belum dapat dimuat." : "Barang belum dapat dimuat." });
  }
});

server.listen(port, "0.0.0.0", () => {
  console.log(`Lamongan Bekas API listening on ${port}`);
});
