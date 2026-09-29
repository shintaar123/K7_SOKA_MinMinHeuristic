# Panduan Lengkap: Simulasi Min-Min di CloudSim (Kelompok 7 · SOKA Kelas B)

Panduan ini dari nol sampai hasil akhir: setup, kode lengkap, tes, grafik, dan tugas tiap orang. **Orang 1 mulai dari Bagian 2.**

Kode Java sudah lengkap di repo ini (`src/`), jadi tidak perlu mengetik ulang. Peta file ada di Bagian 4.

---

## 0. Status Pengujian

Sudah dijalankan dengan **CloudSim 3.0.3 asli** dan **dataset GoCJ asli (300 task)**:

| Lingkungan | Hasil |
|---|---|
| Windows + JDK 11 (laptop Orang 1) | Lulus: build, uji penjadwal, simulasi, validasi V2–V9 |
| Linux + JDK 21 | Lulus, metrik identik |
| Kompilasi `--release 8` | Lulus (belum dijalankan di Java 8) |

Belum diuji: macOS, langkah IDE (Eclipse/IntelliJ, Langkah 8), run di Java 8/17.

Nilai bertanda **[USULAN]** di `SimConfig.java` (pemetaan RAM host, bandwidth, storage, ukuran image VM) tidak ada di PPT. Samakan dengan tugas minggu lalu, lalu ubah **hanya** di `SimConfig.java`.

---

## 1. Gambaran Besar

```
 GoCJ 300 task ─▶ Min-Min (kode murni) ─▶ order + taskToVm ─▶ CloudSim (5 host, 15 VM) ─▶ metrik + CSV + grafik
   GoCJLoader       MinMinScheduler         CloudletFactory      DatacenterFactory/VmFactory    MetricsCalculator
                                            bindCloudletToVm     MainMinMin                     ResultWriter
```

| Peran | Isi | File yang dipegang |
|---|---|---|
| **A · Koordinator (Orang 1)** | setup, repo, integrasi, jalankan, validasi | `SimConfig`, `MainMinMin`, `build_run.*` |
| **B · Infrastruktur** | datacenter 5 host, 15 VM | `DatacenterFactory`, `VmFactory`, `MappedVmAllocationPolicy` |
| **C · Algoritma** | Min-Min + uji | `MinMinScheduler`, `SchedulerSelfTest` |
| **D · Dataset & Cloudlet** | GoCJ + cloudlet | `GoCJLoader`, `CloudletFactory`, `make_dummy_gocj.py` |
| **E · Metrik & Laporan** | metrik, grafik, analisis, revisi draft/PPT | `MetricsCalculator`, `Validator`, `ResultWriter`, `plot_results.py` |

Kalau anggota hanya 4 orang, gabungkan D dan E. Kalau 3 orang, gabungkan A+D dan C+E.

---

## 2. ORANG 1 (Koordinator) — Langkah dari Nol

Tujuan: **semua anggota bisa menjalankan program di laptop masing-masing dan melihat "SEMUA VALIDASI LULUS".**

### Langkah 1 · Pasang JDK (bukan hanya JRE)

Butuh `javac`. Cek dulu:

```bash
java -version
javac -version
```

Kalau `javac` tidak dikenal:

- **Windows:** unduh JDK 17 dari adoptium.net, atau lewat terminal `winget install EclipseAdoptium.Temurin.17.JDK`. Tutup dan buka lagi terminal setelahnya.
- **WSL / Ubuntu:** `sudo apt update && sudo apt install -y openjdk-17-jdk`
- **Mac:** `brew install openjdk@17`

Berhasil kalau `javac -version` menampilkan angka versi.

### Langkah 2 · Ambil proyek

Clone repo (`git clone <url-repo>`), lalu masuk ke foldernya. **Semua perintah selalu dijalankan dari folder `minmin-cloudsim` ini** (kalau tidak, dataset dan folder `results` tidak ketemu).

```
minmin-cloudsim/
├── lib/cloudsim-3.0.3.jar        ← CloudSim asli (sudah termasuk)
├── data/                         ← taruh dataset GoCJ di sini
├── src/{config,data,infra,scheduler,metrics,app}/
├── analysis/plot_results.py
├── tools/make_dummy_gocj.py
├── build_run.sh   build_run.bat
└── results/                      ← dibuat otomatis
```

Jar CloudSim yang sama bisa diunduh dari `https://github.com/Cloudslab/cloudsim/releases/download/cloudsim-3.0.3/cloudsim-3.0.3.zip` (folder `jars/`), berguna untuk membaca dokumentasi dan contoh bawaannya.

### Langkah 3 · Dataset

Dataset asli sudah ada di `data/GoCJ_Dataset_300.txt` (GoCJ, Hussain & Aleem 2018; satu angka MI per baris; ringkasan di `data/README.md`). Tidak perlu diunduh lagi.
`tools/make_dummy_gocj.py` hanya untuk data uji. Jangan dipakai untuk hasil final.

### Langkah 4 · Build + tes + jalankan (satu perintah)

```bash
bash build_run.sh          # Linux / WSL / Mac / Git Bash
build_run.bat              # Windows cmd / PowerShell
```

Skrip ini melakukan tiga hal berurutan: kompilasi, uji penjadwal (tanpa CloudSim), lalu simulasi CloudSim penuh.

**Yang harus muncul:**

1. `>>> Build OK`
2. Delapan baris `[PASS]` lalu `SEMUA UJI LULUS`
3. Blok `HASIL SIMULASI MIN-MIN`, diakhiri `SEMUA VALIDASI LULUS`

Kalau ada `[FAIL]` atau error, lihat Bagian 6.

### Langkah 5 · Baca outputnya

Output dengan dataset GoCJ asli (sama dengan `results/console_output.txt`):

```
================= HASIL SIMULASI MIN-MIN =================
Dataset        : GoCJ | n=300  min=15000  max=900000  rata-rata=138420.0  total=41526000 MI
Datacenter     : 1 datacenter, 5 host, 15 VM (Small/Medium/Large x5)
Algoritma      : Min-Min (batch, independent, non-preemptive)

--- Penempatan VM ---
VM  0 (Small, 1000 MIPS) -> Host 1
VM  1 (Medium, 2000 MIPS) -> Host 1
VM  2 (Large, 3000 MIPS) -> Host 1
VM  3 (Small, 1000 MIPS) -> Host 2
VM  4 (Medium, 2000 MIPS) -> Host 2
VM  5 (Large, 3000 MIPS) -> Host 2
VM  6 (Small, 1000 MIPS) -> Host 3
VM  7 (Medium, 2000 MIPS) -> Host 3
VM  8 (Large, 3000 MIPS) -> Host 3
VM  9 (Small, 1000 MIPS) -> Host 4
VM 10 (Medium, 2000 MIPS) -> Host 4
VM 11 (Large, 3000 MIPS) -> Host 4
VM 12 (Small, 1000 MIPS) -> Host 5
VM 13 (Medium, 2000 MIPS) -> Host 5
VM 14 (Large, 3000 MIPS) -> Host 5

--- Metrik ---
Makespan (simulasi)       : 1594.00 detik
Makespan (prediksi MinMin): 1594.00 detik
Batas bawah teoritis      : 1384.20 detik
Resource Utilization      : 86.87 %
Average Waiting Time      : 339.79 detik
Throughput                : 0.1882 task/detik
Task sukses               : 300 / 300

--- Beban per VM ---
VM | tipe   | MIPS | jumlah task | busy time (s) | utilisasi VM (%)
 0 | Small  | 1000 |           9 |       1572.00 |    98.62
 1 | Medium | 2000 |          20 |       1160.75 |    72.82
 2 | Large  | 3000 |          32 |       1527.00 |    95.80
 3 | Small  | 1000 |           9 |       1574.00 |    98.75
 4 | Medium | 2000 |          20 |       1278.27 |    80.19
 5 | Large  | 3000 |          31 |       1299.00 |    81.49
 6 | Small  | 1000 |           9 |       1576.00 |    98.87
 7 | Medium | 2000 |          20 |       1372.02 |    86.07
 8 | Large  | 3000 |          31 |       1363.50 |    85.54
 9 | Small  | 1000 |           9 |       1594.00 |   100.00
10 | Medium | 2000 |          20 |       1469.77 |    92.21
11 | Large  | 3000 |          31 |       1371.83 |    86.06
12 | Small  | 1000 |           8 |        700.00 |    43.91
13 | Medium | 2000 |          20 |       1472.75 |    92.39
14 | Large  | 3000 |          31 |       1439.66 |    90.32

--- Validasi otomatis ---
[PASS] V2  300 cloudlet berstatus SUCCESS  -> 300 / 300
[PASS] V3  jumlah task seluruh VM = 300  -> total = 300
[PASS] V4a makespan CloudSim ~ prediksi Min-Min  -> CloudSim=1593.9955  prediksi=1594.0000  selisih=0.0045 dtk (toleransi 0.5)
[PASS] V4b waktu mulai tiap task ~ prediksi (urutan submit benar)  -> selisih terbesar = 0.0247 dtk
[PASS] V4c setiap task jalan di VM hasil Min-Min
[PASS] V5  makespan >= batas bawah teoritis  -> makespan=1594.00  batas bawah=1384.20
[PASS] V6  Resource Utilization <= 100%  -> 86.87 %
[PASS] V7  tidak ada task tumpang tindih di satu VM
[PASS] V9  VM 0-2 -> Host1, VM 3-5 -> Host2, ... VM 12-14 -> Host5  -> 15 VM terpasang = 15
[PASS] V8  penjadwal deterministik (dua kali jalan identik)
SEMUA VALIDASI LULUS
==========================================================
```

Cara membaca:

| Bagian | Artinya | Cek apa |
|---|---|---|
| Penempatan VM | VM 0–2 di Host 1, dst. | Sama dengan slide 13 |
| Makespan simulasi vs prediksi | Hasil CloudSim vs hitungan Min-Min | Selisih sangat kecil (<0,03 detik) |
| Batas bawah teoritis | Σ MI ÷ 30.000 | Makespan tidak boleh lebih kecil |
| Beban per VM | Task dan busy time tiap VM | Untuk analisis load balance |
| Validasi otomatis | V2–V9 | Semua `[PASS]` |

Selisih simulasi vs prediksi sangat kecil (0,0045 detik dari 1594 pada data asli; selisih waktu mulai terbesar 0,0247 detik). Penyebab pastinya belum diverifikasi ke kode CloudSim, jadi jawab ke dosen: "selisihnya di bawah 0,03 detik". Toleransi validasi diset 0,5 detik.

### Langkah 6 · Buat grafik

```bash
pip install pandas matplotlib
python analysis/plot_results.py
```

Hasilnya tiga PNG di `results/` plus beberapa angka pendukung analisis di terminal.

### Langkah 7 · Kerja bersama lewat GitHub

Aturan kerja ada di `README.md` (satu orang satu bagian file, `git pull` dulu, commit kecil). Pastikan **setiap anggota** berhasil menjalankan `build_run` sebelum lanjut. Ini yang paling sering macet.

### Langkah 8 · Kalau pakai IDE (opsional, belum kuuji)

- **IntelliJ:** New Project → Java, buka folder `minmin-cloudsim`. Klik kanan `src` → *Mark Directory as* → *Sources Root*. File → Project Structure → Libraries → tambah `lib/cloudsim-3.0.3.jar`. Di Run Configuration `app.MainMinMin`, set **Working directory** ke folder `minmin-cloudsim`.
- **Eclipse:** New Java Project, tambahkan `src` sebagai source folder, Build Path → Add JARs → `lib/cloudsim-3.0.3.jar`. Run `app.MainMinMin`, working directory = folder proyek.

### Tugas Orang 1 selanjutnya

Setelah tim jalan: kumpulkan hasil dari B–E, jalankan validasi final, pastikan `results/` berasal dari **dataset asli**, dan atur latihan demo (Bagian 5).

---

## 3. Tugas Tiap Peran (setelah Bagian 2 beres)

Setiap peran punya file sendiri, cara tes, dan poin yang harus bisa dijelaskan ke dosen.

### Peran B · Infrastruktur CloudSim

**Tugas:**

1. Cocokkan `SimConfig.java` dengan tugas minggu lalu: jumlah PE, MIPS, RAM host, bandwidth, storage. Ubah hanya bagian **[USULAN]** yang berbeda.
2. Pastikan tabel *Penempatan VM* di output sama dengan slide 13 (V9 harus PASS).

**Penjelasan singkat:**
- `DatacenterFactory`: membuat 5 `Host`. Tiap host punya daftar `Pe` (prosesor + MIPS), RAM, bandwidth, storage, dan `VmSchedulerTimeShared`. Lalu dibungkus jadi 1 `Datacenter`.
- `VmFactory`: membuat 15 VM, masing-masing 1 PE, dengan `CloudletSchedulerSpaceShared` (task jalan satu-satu sampai selesai = non-preemptive).
- `MappedVmAllocationPolicy`: kebijakan bawaan CloudSim menaruh VM di host paling longgar, sehingga VM 0–2 tidak otomatis ke Host 1. Kelas ini memaksa `vmId / 3 → host`, supaya sama dengan gambar slide 13.

**Harus bisa dijawab:** kenapa perlu policy sendiri? kenapa 1 VM = 1 PE?

### Peran C · Algoritma Min-Min

**Tugas:** jalankan `java -cp out app.SchedulerSelfTest`, hitung tangan contoh slide 8, lalu cocokkan tiap bagian kode dengan pseudocode slide 7.

**Penjelasan singkat, `MinMinScheduler.schedule`:**
1. Untuk tiap task belum terjadwal, hitung CT di tiap VM: `CT = ready[VM] + panjang/MIPS`, ambil VM dengan CT terkecil.
2. Dari semua task, pilih yang CT minimumnya paling kecil.
3. Jadwalkan task itu ke VM-nya, update `ready[VM]`, keluarkan dari daftar.
4. Ulangi sampai habis.

Aturan seri: antar VM ambil indeks terkecil (pakai `<` murni), antar task ambil indeks terkecil. Kompleksitas ± O(n²·m).

**Harus bisa dijawab:** kenapa Min-Min cenderung menjadwalkan task pendek dulu? apa kelemahannya? (jawab dari slide 9–10)

### Peran D · Dataset & Cloudlet

**Tugas:**

1. Unduh GoCJ asli (Langkah 3), taruh di `data/`, pastikan barisnya ≥ 300 dan program mencetak `Dataset OK`.
2. Catat statistik dataset (min, max, rata-rata) dari terminal untuk laporan.
3. **Eksperimen pembuktian:** ganti `r.order` menjadi urutan `0..299` di pemanggilan `CloudletFactory.create` pada `MainMinMin`. Dengan data asli, hasilnya **makespan tetap 1594,00 detik, tetapi Average Waiting Time naik dari 339,79 menjadi 714,07 detik dan validasi V4b GAGAL**. Kembalikan setelah eksperimen. Ini alasan cloudlet harus dikirim menurut urutan Min-Min.

**Penjelasan singkat:**
- `GoCJLoader`: baca satu angka per baris, ambil 300 pertama, validasi > 0, cetak statistik. Kalau file hilang atau ada baris bukan angka, muncul pesan yang jelas.
- `CloudletFactory`: id cloudlet = indeks task. List diisi **menurut urutan Min-Min** karena broker mengirim cloudlet sesuai urutan list.

### Peran E · Metrik, Analisis & Laporan

**Tugas:**

1. Jalankan `plot_results.py` dengan data asli, lalu tulis analisis untuk tiap dugaan di slide 10: **didukung / tidak didukung oleh data**, disertai angkanya. Jangan memaksakan hasil.
2. Revisi draft B.1–B.5 (tabel perbaikan ada di file rencana sebelumnya, bagian 1: hapus semua rujukan ke Max-Min sebagai algoritma yang diimplementasikan) dan isi dengan angka spesifikasi dari `SimConfig`.
3. Tambah 2–3 slide hasil ke PPT (tabel 4 metrik, dua grafik utama) dan satu slide "kesesuaian rancangan dengan simulasi".

**Penjelasan singkat, metrik:**

| Metrik | Rumus di kode |
|---|---|
| Makespan | `max(finish) − t0` (t0 = 0.1 dtk, waktu submit awal internal CloudSim) |
| Resource Utilization | `Σ busy time ÷ (makespan × 15) × 100` |
| Average Waiting Time | rata-rata `(waktu mulai − waktu submit)` |
| Throughput | `task sukses ÷ makespan` |

`Validator` menjalankan V2–V9 otomatis dan `ResultWriter` menulis CSV. Angka dari data asli untuk analisis: korelasi Spearman urutan-vs-panjang = 1,000 (task pendek selalu dijadwalkan lebih dulu); rata-rata waiting task terpendek 25% = 42,73 detik vs terpanjang 25% = 728,12 detik; task terakhir (900.000 MI) jatuh ke VM 9 (Small) dan menentukan makespan, sementara VM 12 hanya terpakai 43,9%.

---

## 4. Peta Kode

| File | Peran | Fungsi |
|---|---|---|
| `src/config/SimConfig.java` | A | Semua parameter (host, VM, dataset, output) |
| `src/app/MainMinMin.java` | A | Program utama, 5 langkah alur |
| `build_run.sh` / `build_run.bat` | A | Build + uji + simulasi satu perintah |
| `src/infra/DatacenterFactory.java` | B | 5 host heterogen + Datacenter |
| `src/infra/VmFactory.java` | B | 15 VM (Small/Medium/Large ×5), space-shared |
| `src/infra/MappedVmAllocationPolicy.java` | B | Penempatan VM 0–2 → Host 1, dst. |
| `src/scheduler/MinMinScheduler.java` | C | Algoritma Min-Min (tanpa CloudSim) |
| `src/app/SchedulerSelfTest.java` | C | Uji contoh slide 8 + sifat umum |
| `src/data/GoCJLoader.java` | D | Baca dataset + validasi + statistik |
| `src/infra/CloudletFactory.java` | D | Cloudlet menurut urutan Min-Min |
| `src/metrics/MetricsCalculator.java` | E | 4 metrik + beban per VM |
| `src/metrics/Validator.java` | E | Validasi otomatis V2–V9 |
| `src/metrics/ResultWriter.java` | E | Ringkasan terminal + CSV |
| `analysis/plot_results.py` | E | 3 grafik + angka analisis |
| `tools/make_dummy_gocj.py` | D | Data uji saja |

---

## 5. Demo 1: Peta Kode ke Poin Demo

| Poin demo | Tunjukkan | Pelaksana |
|---|---|---|
| 1. Jelaskan algoritma | Slide 2–11 | C (+ A) |
| 2. Jalankan simulasi | `build_run` → blok HASIL + SEMUA VALIDASI LULUS. Siapkan **video rekaman cadangan** | A |
| 3. Tunjukkan implementasi | `MinMinScheduler.java`, lalu bagian `bindCloudletToVm` di `MainMinMin.java` | C |
| 4. Kesesuaian proposal vs simulasi | Bandingkan slide 12–14 dan draft B.1–B.5 dengan parameter di awal output + `SimConfig.java` | B + D |
| 5. Hasil dan analisis | Tabel metrik, 3 grafik | E |
| 6. Rekam | Zoom Record dari sebelum bicara sampai selesai, upload YouTube Unlisted, link ke Sheet Demo | Operator Zoom |

**Pertanyaan yang mungkin muncul:**

1. *Bagaimana memastikan hasil Min-Min benar-benar dipakai CloudSim?* → `bindCloudletToVm` + urutan submit sesuai `order`; validasi V4b dan V4c membuktikannya.
2. *Kenapa makespan simulasi beda tipis dari prediksi?* → selisihnya di bawah 0,03 detik dari 1594 detik; penyebab pastinya belum diverifikasi.
3. *Kenapa VM ditempatkan dengan policy sendiri?* → agar sesuai gambar slide 13.
4. *Apakah beban VM seimbang?* → `vm_load.csv` dan grafik 1.

Latihan wajib: satu kali dry run penuh sambil Zoom Record, termasuk uji upload ke YouTube.

---

## 6. Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| `javac: command not found` / `'javac' is not recognized` | Hanya JRE atau PATH belum terbaca | Pasang JDK (Langkah 1), buka ulang terminal |
| `Could not find or load main class app.MainMinMin` | Dijalankan bukan dari folder proyek, atau belum build | `cd` ke `minmin-cloudsim`, jalankan `build_run` |
| `NoClassDefFoundError: org/cloudbus/...` | Jar CloudSim tidak ada di classpath | Windows pakai `;` dan Linux pakai `:` antar path. Gunakan `build_run.*` |
| `Dataset tidak ditemukan` | File belum ada di `data/` | Langkah 3 |
| `Dataset hanya punya X baris` | File kurang dari 300 baris | Pakai file 300 task, atau ambil dari file yang lebih besar |
| `Ada baris bukan angka` | Header/teks atau desimal koma | Bersihkan file: satu angka per baris |
| `[FAIL] V4b` | Urutan submit cloudlet tidak sesuai `order` | Pastikan `CloudletFactory.create(..., r.order)` |
| `[FAIL] V9` | Penempatan VM bukan `vmId/3` | Pastikan `DatacenterFactory` memakai `MappedVmAllocationPolicy` |
| `[FAIL] V2` / "Failed to create VM" | RAM, MIPS, atau PE host kurang setelah `SimConfig` diubah | Total RAM VM per host (7168 MB) harus ≤ RAM host. MIPS VM ≤ MIPS per PE |
| Log CloudSim ratusan baris | Mode verbose aktif | `VERBOSE_CLOUDSIM = false` di `SimConfig` |
| `python: command not found` | Python belum terpasang | Coba `python3`, atau pasang Python |
| `ModuleNotFoundError: pandas` | Belum di-install | `pip install pandas matplotlib` |

---

## 7. Checklist Akhir

- [ ] Semua anggota berhasil menjalankan dan melihat `SEMUA VALIDASI LULUS`
- [ ] Dataset **asli** GoCJ dipakai untuk hasil final (bukan data uji)
- [ ] Nilai **[USULAN]** di `SimConfig` sudah dicocokkan dengan tugas minggu lalu
- [ ] `results/` berisi `summary.csv`, `cloudlet_result.csv`, `vm_load.csv`, `console_output.txt`, 3 grafik
- [ ] Draft B.1–B.5 direvisi (tanpa Max-Min sebagai algoritma yang diimplementasikan)
- [ ] PPT ditambah slide hasil dan slide kesesuaian
- [ ] Dry run + Zoom Record, video cadangan simulasi siap
- [ ] Upload YouTube (Unlisted) → link ke Sheet Demo → kumpulkan di its.id/2026SOKA
