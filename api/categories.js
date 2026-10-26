import { neon } from "@neondatabase/serverless";

export default async function handler(req, res) {
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    return res.status(405).send("Method not allowed");
  }
  if (!process.env.DATABASE_URL) {
    return res.status(503).json({ error: "Konfigurasi database belum tersedia." });
  }
  try {
    const sql = neon(process.env.DATABASE_URL);
    const data = await sql`SELECT id, nama, slug FROM public.categories ORDER BY id`;
    res.setHeader("Cache-Control", "public, max-age=60, stale-while-revalidate=300");
    return res.status(200).json({ data });
  } catch (error) {
    console.error("categories query failed", error?.message);
    return res.status(500).json({ error: "Kategori belum dapat dimuat." });
  }
}
