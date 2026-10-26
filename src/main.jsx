import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import "./style.css";
const rupiah = (n) => new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 }).format(Number(n || 0));
function App() {
  const [categories, setCategories] = useState([]), [listings, setListings] = useState([]);
  const [selected, setSelected] = useState(""), [draft, setDraft] = useState(""), [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true), [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => { fetch("/api/categories").then(r => { if (!r.ok) throw Error(); return r.json(); }).then(v => setCategories(v.data || [])).catch(() => setError("Kategori belum dapat dimuat.")); }, []);
  useEffect(() => {
    let alive = true; setLoading(true); setError("");
    const p = new URLSearchParams(); if (search) p.set("q", search); if (selected) p.set("kategori", selected);
    fetch(`/api/listings?${p}`).then(r => { if (!r.ok) throw Error(); return r.json(); }).then(v => { if (alive) setListings(v.data || []); }).catch(() => { if (alive) setError("Barang belum dapat dimuat. Periksa konfigurasi DATABASE_URL di Netlify."); }).finally(() => { if (alive) setLoading(false); });
    return () => { alive = false; };
  }, [search, selected, reload]);
  function submit(e) { e.preventDefault(); setSearch(draft.trim()); }
  return <div className="shell">
    <header className="topbar"><a className="brand" href="#top"><span className="brand-mark">LB</span><span>lamongan<span className="brand-light">bekas</span><small>Pasar barang bekas lokal</small></span></a><a className="safe-link" href="#aman">Tips aman ↗</a></header>
    <main id="top"><section className="hero"><div className="hero-copy"><span className="eyebrow">DARI WARGA, UNTUK WARGA</span><h1>Barang lama,<br/><em>manfaat baru.</em></h1><p>Temukan barang menarik dari warga Lamongan. Lebih dekat, lebih praktis, lebih bermanfaat.</p><form className="search" onSubmit={submit}><span aria-hidden="true">⌕</span><input aria-label="Cari barang" value={draft} onChange={e => setDraft(e.target.value)} placeholder="Cari HP, sepeda, meja…"/><button>Cari barang</button></form><div className="note"><i/> Fokus warga Kabupaten Lamongan</div></div><div className="hero-art" aria-hidden="true"><div className="sun"/><div className="floating float-a"><span>♧</span><b>Dipakai lagi</b><small>Hemat, bermanfaat</small></div><div className="floating float-b"><span>↻</span><b>Putar manfaat</b><small>Jual · Beli · Pakai</small></div><div className="box-art"><div className="handle"/><div className="box-label">BARANG BAIK<br/>LAYAK PAKAI</div></div><i className="leaf leaf-a"/><i className="leaf leaf-b"/></div></section>
    <section className="catalog"><div className="section-heading"><div><span className="eyebrow">JELAJAHI PASAR</span><h2>Cari yang kamu butuhkan</h2></div><span className="count">{loading ? "Memuat…" : `${listings.length} barang`}</span></div><div className="categories"><button className={!selected ? "chip active" : "chip"} onClick={() => setSelected("")}>Semua</button>{categories.map(c => <button key={c.id} className={selected === c.slug ? "chip active" : "chip"} onClick={() => setSelected(c.slug)}>{c.nama}</button>)}</div>
    {loading ? <div className="state"><div className="spinner"/><p>Sedang mencari barang…</p></div> : error ? <div className="state"><h3>Ada kendala sebentar</h3><p>{error}</p><button className="retry" onClick={() => setReload(x => x + 1)}>Coba lagi</button></div> : listings.length === 0 ? <div className="state empty"><div className="empty-mark">⌕</div><h3>{search || selected ? "Belum ada barang yang cocok" : "Pasar sedang menunggu barang pertamanya"}</h3><p>{search || selected ? "Coba kata kunci atau kategori lain." : "Belum ada iklan aktif. Cek lagi nanti untuk menemukan barang dari warga sekitar."}</p>{(search || selected) && <button className="retry" onClick={() => { setSearch(""); setDraft(""); setSelected(""); }}>Lihat semua barang</button>}</div> : <div className="grid">{listings.map(item => <article className="listing" key={item.id}><div className="photo">{item.foto ? <img src={item.foto} alt={item.judul} loading="lazy"/> : <div className="placeholder"><b>LB</b><small>Barang pilihan warga</small></div>}<span className="condition">{String(item.kondisi || "").replaceAll("_", " ")}</span></div><div className="listing-info"><span className="category-name">{item.kategori}</span><h3>{item.judul}</h3><p className="price">{rupiah(item.harga)}</p><p className="location">⌖ {item.kecamatan}, Lamongan</p></div></article>)}</div>}
    </section><section className="safety" id="aman"><div className="check">✓</div><div><h2>Transaksi nyaman dimulai dari waspada</h2><p>Periksa kondisi barang, bertemu di tempat aman, dan jangan transfer sebelum yakin dengan penjual serta barangnya.</p></div></section></main>
    <footer><a href="#top">Lamongan Bekas</a><span>Pasar lokal yang menghidupkan kembali manfaat barang.</span><small>© {new Date().getFullYear()} Lamongan Bekas</small></footer>
  </div>;
}
createRoot(document.getElementById("root")).render(<React.StrictMode><App/></React.StrictMode>);
