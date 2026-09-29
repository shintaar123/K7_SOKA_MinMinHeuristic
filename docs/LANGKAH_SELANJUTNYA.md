# Langkah Selanjutnya

Kode dan hasil dasar sudah jadi. Yang tersisa tinggal mencocokkan parameter, analisis, laporan, dan demo. Kerjakan sesuai peran (rinciannya di [PANDUAN_LENGKAP.md](PANDUAN_LENGKAP.md)).

## Mulai dari sini

1. Clone repo (lihat [GIT_PUSH.md](GIT_PUSH.md)).
2. Pasang JDK 11 ke atas.
3. Jalankan `.\build_run.bat` (Windows) atau `bash build_run.sh` (Linux/WSL/Mac).
4. Harus muncul `SEMUA UJI LULUS` dan `SEMUA VALIDASI LULUS`. Kalau gagal, cek tabel troubleshooting di PANDUAN_LENGKAP bagian 6.
5. Untuk grafik: `pip install pandas matplotlib`, lalu `python analysis/plot_results.py`.

## Yang harus dikerjakan

**Semua anggota**
- [ ] Berhasil menjalankan `build_run` di laptop sendiri.

**Infrastruktur (B)**
- [ ] Samakan nilai `[USULAN]` di `src/config/SimConfig.java` dengan tugas minggu lalu: RAM per host, bandwidth, storage, ukuran image VM.
- [ ] Setelah diubah: jalankan ulang, pastikan validasi lulus, commit `results/` yang baru.

**Algoritma (C)**
- [ ] Jalankan `java -cp out app.SchedulerSelfTest`, hitung tangan contoh slide 8.
- [ ] Cocokkan kode `MinMinScheduler` dengan pseudocode slide 7.
- [ ] Siap jelaskan: kenapa Min-Min menjadwalkan task pendek dulu, dan apa kelemahannya.

**Dataset dan Cloudlet (D)**
- [ ] Catat statistik dataset untuk laporan (n=300, min=15.000, max=900.000, rata-rata=138.420, total=41.526.000 MI).
- [ ] Eksperimen urutan submit: di `MainMinMin`, ganti `r.order` jadi urutan `0..299`. Hasil yang diharapkan: makespan tetap 1594,00, tapi waiting time naik ke 714,07 dan V4b gagal. Kembalikan kodenya setelah eksperimen.
- [ ] Cek lisensi dataset di Mendeley Data.

**Metrik dan Laporan (E)**
- [ ] Analisis tiap dugaan di slide 10: didukung atau tidak didukung data, sertakan angkanya.
- [ ] Revisi draft B.1 sampai B.5. Hapus rujukan ke Max-Min sebagai algoritma yang diimplementasikan. Isi angka spesifikasi dari `SimConfig`.
- [ ] Tambah 2 sampai 3 slide hasil ke PPT (tabel 4 metrik, dua grafik utama) dan satu slide "kesesuaian rancangan dengan simulasi".

**Demo**
- [ ] Bagi tugas bicara per poin demo (tabel di PANDUAN_LENGKAP bagian 5).
- [ ] Dry run penuh sambil Zoom Record.
- [ ] Siapkan video cadangan simulasi.
- [ ] Upload YouTube (Unlisted), isi Sheet Demo, kumpulkan di its.id/2026SOKA.

## Angka yang bisa dipakai untuk analisis

- Korelasi Spearman urutan vs panjang task: 1,000 (task pendek selalu dijadwalkan lebih dulu).
- Rata-rata waiting task terpendek 25%: 42,73 detik. Task terpanjang 25%: 728,12 detik.
- Task terakhir (900.000 MI) jatuh ke VM 9 (Small) dan menentukan makespan.
- VM 12 hanya terpakai 43,9%.
- Selisih makespan simulasi vs prediksi di bawah 0,03 detik. Penyebab pastinya belum diverifikasi, jadi jangan dijawab dengan dugaan.

## Sebelum selesai

- [ ] Jalankan ulang `build_run` dengan kode terakhir, lalu commit `results/`.
- [ ] `git pull` dan `git push` terakhir. Pastikan repo sama dengan yang dipakai untuk demo.
