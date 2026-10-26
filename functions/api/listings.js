import { neon } from "@neondatabase/serverless";

export async function onRequest(context) {
  if (context.request.method !== "GET") {
    return new Response("Method not allowed", { status: 405, headers: { Allow: "GET" } });
  }
  const databaseUrl = context.env.DATABASE_URL;
  if (!databaseUrl) {
    return Response.json({ error: "Konfigurasi database belum tersedia." }, { status: 503 });
  }
  const url = new URL(context.request.url);
  const search = (url.searchParams.get("q") || "").trim().slice(0, 80);
  const category = (url.searchParams.get("kategori") || "").trim().slice(0, 80);
  const limit = Math.min(Math.max(Number.parseInt(url.searchParams.get("limit") || "24", 10) || 24, 1), 48);
  try {
    const sql = neon(databaseUrl);
    const data = await sql.query(
      "SELECT l.id, l.judul, l.deskripsi, l.harga, l.kondisi, l.kecamatan, l.dibuat_pada, c.nama AS kategori, c.slug AS kategori_slug, (SELECT p.url FROM public.listing_photos p WHERE p.listing_id = l.id ORDER BY p.urutan LIMIT 1) AS foto FROM public.listings l JOIN public.categories c ON c.id = l.kategori_id WHERE l.status = 'aktif' AND l.kedaluwarsa_pada > now() AND ($1 = '' OR l.judul ILIKE '%' || $1 || '%' OR l.deskripsi ILIKE '%' || $1 || '%' OR l.kecamatan ILIKE '%' || $1 || '%') AND ($2 = '' OR c.slug = $2) ORDER BY l.dibuat_pada DESC LIMIT $3",
      [search, category, limit]
    );
    return Response.json({ data }, {
      headers: { "Cache-Control": "public, max-age=30, stale-while-revalidate=120" }
    });
  } catch (error) {
    console.error("listings query failed", error?.message);
    return Response.json({ error: "Iklan belum dapat dimuat." }, { status: 500 });
  }
}
