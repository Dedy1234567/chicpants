# 👖 ChicPants Ecommerce

**ChicPants Ecommerce** adalah aplikasi berbasis web untuk penjualan pakaian secara online.  
Dengan aplikasi ini, admin dapat menambahkan produk yang ingin dijual, sementara pengguna dapat melihat katalog produk dan melakukan pembelian secara langsung melalui website.

---

## 📝 Deskripsi
ChicPants Ecommerce dibangun untuk mempermudah proses jual-beli pakaian dengan tampilan yang sederhana, responsif, dan mudah digunakan.  
Fitur utama yang tersedia meliputi:
- Manajemen produk oleh admin (tambah, ubah, hapus).
- Katalog produk yang dapat diakses oleh pengguna.
- Fitur pembelian produk secara online.
- Integrasi dengan database untuk menyimpan data produk dan transaksi.

Aplikasi ini menggunakan **Java (Spring Boot)** sebagai backend dan **Thymeleaf** sebagai template engine untuk menampilkan halaman web.

---

## 🛠️ Teknologi yang Digunakan
- **Java (Spring Boot)** – Backend utama aplikasi.  
- **Thymeleaf** – Template engine untuk membangun tampilan web.  
- **Maven** – Build dan dependency management.  
- **MySQL / PostgreSQL** – Database penyimpanan data (contoh database: `db_chic`).  
- **HTML, CSS, JavaScript** – Untuk tampilan front-end.

---

## ⚙️ Persiapan Database
Sebelum menjalankan aplikasi, pastikan database sudah disiapkan.  

1. Buat database dengan nama **`db_chic`**.  
   Jika menggunakan MySQL, jalankan perintah berikut:
   ```sql
   CREATE DATABASE db_chic;
   
2. Jalan kan project dan akses **localhost:8080/**