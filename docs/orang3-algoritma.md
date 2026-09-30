# Bagian Orang 3 — Algoritma Min-Min (Peran C)

**Tanggung jawab:** logika penjadwalan `MinMinScheduler.java`, verifikasi teoritis `SchedulerSelfTest.java`, pencocokan pseudocode dan contoh manual (Slide 6–7), penjelasan algoritma saat demo (Slide 2–11).

## Metodologi Validasi Dua Tingkat (Two-Level Verification)
Untuk memastikan keabsahan hasil, pengujian dilakukan dalam dua tingkat yang saling melengkapi:
1. **Tingkat 1 - Verifikasi Matematis Mandiri (`SchedulerSelfTest.java`):**
   Menguji kebenaran algoritma secara terisolasi dari simulator untuk memastikan rumus matematika, penanganan seri (*tie-break*), dan invarian beban kerja (deterministik, zero-overlap, batas bawah teoritis) bekerja 100% sesuai teori Braun et al. (2001).
2. **Tingkat 2 - Validasi Simulasi CloudSim (`MainMinMin.java`):**
   Menguji eksekusi jadwal Min-Min pada lingkungan cloud berbasis *event-driven* dengan 5 Host dan 15 VM.
3. **Esensi Komparasi:**
   Membandingkan nilai prediksi teoritis (Tingkat 1) terhadap hasil eksekusi runtime CloudSim (Tingkat 2). Akurasi terbukti presisi tinggi dengan selisih waktu penyelesaian hanya **0,0045 detik** (Toleransi sistem: 0,5 detik).

## Hasil Eksekusi & Bukti Komparasi (Log Terminal)
Kedua tingkat pengujian dijalankan berurutan melalui satu perintah:
```bash
bash build_run.sh
```

### 1. Log Verifikasi Teoritis (Tingkat 1)
```text
>>> [1/2] Uji penjadwal (tanpa CloudSim)
[PASS] Slide 8: urutan = T3,T1,T4,T2
[PASS] Slide 8: pemetaan T1->VM2, T2->VM2, T3->VM2, T4->VM1
[PASS] Slide 8: CT = 3, 7, 1, 6 detik
[PASS] Slide 8: makespan = 7 detik (7.0)
[PASS] 300 task tepat sekali masing-masing di order
[PASS] makespan (4600.19) >= batas bawah teoritis (4409.26)
[PASS] Tidak ada task tumpang tindih di satu VM
[PASS] Deterministik: dua kali jalan hasil sama

SEMUA UJI LULUS
```

### 2. Log Validasi Simulasi CloudSim & Komparasi Prediksi (Tingkat 2)
```text
>>> [2/2] Simulasi CloudSim
Dataset OK  : GoCJ | n=300  min=15000  max=900000  rata-rata=138420.0  total=41526000 MI
Min-Min OK  : prediksi makespan = 1594.00 detik

--- Komparasi Metrik Utama ---
Makespan (simulasi CloudSim) : 1594.00 detik (1593.9955)
Makespan (prediksi Min-Min)  : 1594.00 detik (1594.0000)
Selisih Simulasi vs Teori    : 0.0045 detik (Validasi V4a: PASS)
Resource Utilization         : 86.87 %
Average Waiting Time         : 339.79 detik
Throughput                   : 0.1882 task/detik
Task sukses                  : 300 / 300 (100%)

--- Validasi Otomatis ---
[PASS] V2  300 cloudlet berstatus SUCCESS  -> 300 / 300
[PASS] V3  jumlah task seluruh VM = 300  -> total = 300
[PASS] V4a makespan CloudSim ~ prediksi Min-Min  -> selisih = 0.0045 dtk
[PASS] V4b waktu mulai tiap task ~ prediksi  -> selisih = 0.0247 dtk
[PASS] V4c setiap task jalan di VM hasil Min-Min (100% presisi)
[PASS] V5  makespan >= batas bawah teoritis  -> makespan=1594.00  batas bawah=1384.20
[PASS] V6  Resource Utilization <= 100%  -> 86.87 %
[PASS] V7  tidak ada task tumpang tindih di satu VM
[PASS] V9  VM 0-2 -> Host1, VM 3-5 -> Host2, ... VM 12-14 -> Host5
[PASS] V8  penjadwal deterministik (dua kali jalan identik)
SEMUA VALIDASI LULUS
```

## Logika Kode `MinMinScheduler.java` (Bahan Presentasi)
Kodenya dirancang modular dengan tahapan eksekusi:
1. **Looping (Baris 36):** Berjalan sebanyak $n$ kali sampai seluruh task terjadwal.
2. **Min Pertama (Baris 44–49):** Untuk setiap task $i$, hitung $CT = \text{readyTime}[VM] + (\text{MI} / \text{MIPS})$ di seluruh VM, lalu pilih VM tercepat. Aturan seri (*tie-break*) memilih indeks VM terkecil secara deterministik (operator `<`).
3. **Min Kedua (Baris 51):** Dari seluruh kandidat task, pilih task dengan waktu selesai ($CT$) paling kecil secara global.
4. **Update State (Baris 55–60):** Jadwalkan task terpilih ke VM-nya, perbarui `readyTime` VM, catat urutan ke `order`, dan tandai task selesai (`done[i] = true`).
5. **Kompleksitas:** $\mathcal{O}(n^2 \times m)$ untuk $n$ task dan $m$ VM.

## Integrasi ke CloudSim (`MainMinMin.java`)
- Hasil penjadwalan dari `MinMinScheduler` (`r.taskToVm` dan `r.order`) dihubungkan ke CloudSim di baris 63–65 menggunakan perintah:
  `broker.bindCloudletToVm(cloudletId, r.taskToVm[cloudletId])`
- Cloudlet dikirim ke broker mengikuti urutan `r.order`.
- Keberhasilan integrasi dibuktikan oleh validasi **V4c** (100% task jalan di VM hasil Min-Min) dan **V4a** (Makespan CloudSim = 1593.9955 detik ~ prediksi Min-Min = 1594.0000 detik).

## Perintah Jalankan
```bash
bash build_run.sh
```
Troubleshooting dan penjelasan detail metrik ada di [PANDUAN_LENGKAP.md](PANDUAN_LENGKAP.md).
