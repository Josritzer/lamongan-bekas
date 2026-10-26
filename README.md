# Lamongan Bekas

Marketplace barang bekas lokal untuk wilayah Kabupaten Lamongan.

## Stack
- React + Vite
- Netlify Functions
- Neon PostgreSQL (`@neondatabase/serverless`)

## Jalankan lokal
1. `npm install`
2. Salin `.env.example` menjadi `.env`, lalu isi `DATABASE_URL` dari Neon. Jangan commit `.env`.
3. `npm run dev` untuk frontend saja.
4. `npm run dev:netlify` untuk frontend beserta Netlify Functions.

## Deploy ke Netlify
- Hubungkan repository ini di Netlify.
- Build command: `npm run build`
- Publish directory: `dist`
- Tambahkan `DATABASE_URL` pada environment variables Netlify.

## Keamanan dan batas fitur
- Kredensial database hanya dibaca Netlify Functions; jangan gunakan `VITE_DATABASE_URL`.
- API awal hanya menyediakan pembacaan kategori dan iklan aktif, dengan query SQL berparameter.
- Belum ada fitur membuat akun, memasang iklan, mengunggah foto, atau mengirim laporan. Alur Neon Auth dan pemetaan identitas ke `public.users` perlu diverifikasi sebelum endpoint tulis diaktifkan.
- Database belum memiliki iklan; halaman menampilkan empty state.
