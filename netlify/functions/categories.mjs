import { neon } from "@neondatabase/serverless";

export default async () => {
  if (!process.env.DATABASE_URL) return Response.json({ error: "Konfigurasi database belum tersedia." }, { status: 503 });
  try {
    const sql = neon(process.env.DATABASE_URL);
    const data = await sql`SELECT id, nama, slug FROM public.categories ORDER BY id`;
    return Response.json({ data }, { headers: { "Cache-Control": "public, max-age=60, stale-while-revalidate=300" } });
  } catch (error) {
    console.error("categories query failed", error?.message);
    return Response.json({ error: "Kategori belum dapat dimuat." }, { status: 500 });
  }
};
