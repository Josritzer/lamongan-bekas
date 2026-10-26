import { neon } from "@neondatabase/serverless";

export async function onRequest(context) {
  if (context.request.method !== "GET") {
    return new Response("Method not allowed", { status: 405, headers: { Allow: "GET" } });
  }
  const databaseUrl = context.env.DATABASE_URL;
  if (!databaseUrl) {
    return Response.json({ error: "Konfigurasi database belum tersedia." }, { status: 503 });
  }
  try {
    const sql = neon(databaseUrl);
    const data = await sql`SELECT id, nama, slug FROM public.categories ORDER BY id`;
    return Response.json({ data }, {
      headers: { "Cache-Control": "public, max-age=60, stale-while-revalidate=300" }
    });
  } catch (error) {
    console.error("categories query failed", error?.message);
    return Response.json({ error: "Kategori belum dapat dimuat." }, { status: 500 });
  }
}
