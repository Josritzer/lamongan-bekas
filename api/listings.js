import { neon } from "@neondatabase/serverless";

export default async function handler(req, res) {
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    return res.status(405).send("Method not allowed");
  }
  if (!process.env.DATABASE_URL) {
    return res.status(503).json({ error: "Konfigurasi database belum tersedia." });
  }
  const search = String(req.query.q || "").trim().slice(0, 80);
  const category = String(req.query.kategori || "").trim().slice(0, 80);
  const limit = Math.min(Math.max(Number.parseInt(req.query.limit || "24", 10) || 24, 1), 48);
  try {
    const sql = neon(process.env.DATABASE_URL);
    const data = await sql.query(
      "SELECT l.id, l.judul, l.deskripsi, l.harga, l.kondisi, l.kecamatan, l.dibuat_pada, c.nama AS kategori, c.slug AS kategori_slug, (SELECT p.url FROM public.listing_photos p WHERE p.listing_id = l.id ORDER BY p.urutan LIMIT 1) AS foto FROM public.listings l JOIN public.categories c ON c.id = l.kategori_id WHERE l.status = 'aktif' AND l.kedaluwarsa_pada > now() AND ($1 = '' OR l.judul ILIKE '%' || $1 || '%' OR l.deskripsi ILIKE '%' || $1 || '%' OR l.kecamatan ILIKE '%' || $1 || '%') AND ($2 = '' OR c.slug = $2) ORDER BY l.dibuat_pada DESC LIMIT $3",
      [search, category, limit]
    );
    res.setHeader("Cache-Control", "public, max-age=30, stale-while-revalidate=120");
    return res.status(200).json({ data });
  } catch (error) {
    console.error("listings query failed", error?.message);
    return res.status(500).json({ error: "Iklan belum dapat dimuat." });
  }
}
