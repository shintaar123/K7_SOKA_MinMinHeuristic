# Bagian Orang 1 — Koordinator & Integrator

**Tanggung jawab:** setup lingkungan, `SimConfig.java`, `MainMinMin.java`, skrip `build_run.*`, integrasi kode semua peran, validasi hasil, koordinasi demo.

## Yang sudah selesai
- Setup dan uji di Windows + JDK 11 (lulus). Angka identik dengan uji di Linux + JDK 21.
- Program berjalan end-to-end memakai CloudSim 3.0.3 asli dan **dataset GoCJ asli** (`data/GoCJ_Dataset_300.txt`).
- Validasi otomatis V2–V9 lulus. Penjelasan singkat V1 dan V10: V1 = `SchedulerSelfTest`, V10 = parameter yang dicetak di awal run.
- Hasil final ada di `results/`: `summary.csv`, `cloudlet_result.csv`, `vm_load.csv`, `console_output.txt`, 3 grafik.

## Alur `MainMinMin` (5 langkah, ini yang dijelaskan saat demo)
1. `GoCJLoader` baca 300 task → `double[] lengthMI`.
2. `MinMinScheduler.schedule` menghitung `taskToVm` dan `order` (di luar CloudSim).
3. `CloudSim.init` → `DatacenterFactory` (5 host) → `DatacenterBroker` → `VmFactory` (15 VM).
4. `CloudletFactory` membuat cloudlet **menurut `order` Min-Min**, lalu `bindCloudletToVm` mengikat tiap cloudlet ke VM hasil Min-Min, baru `startSimulation`.
5. `MetricsCalculator` + `Validator` + `ResultWriter` → metrik, validasi, CSV.

Catatan penting: cloudlet harus dikirim menurut urutan Min-Min. Kalau dikirim urut id, makespan tetap sama tetapi Average Waiting Time naik dari 339,79 ke 714,07 detik dan validasi V4b gagal (sudah dicoba).

## Yang masih perlu dilakukan
- [ ] Pastikan setiap anggota sudah berhasil menjalankan `build_run` di laptopnya.
- [ ] Cocokkan nilai `[USULAN]` di `SimConfig.java` (RAM per host, bandwidth, storage, image VM) dengan tugas minggu lalu. Setelah itu jalankan ulang dan commit `results/`.
- [ ] Kumpulkan analisis dari Peran E dan slide hasil untuk PPT.
- [ ] Dry run demo dengan Zoom Record, siapkan video cadangan, upload YouTube (Unlisted), isi Sheet Demo, kumpulkan di its.id/2026SOKA.

## Perintah cepat
```
.\build_run.bat                       # build + uji + simulasi
python analysis\plot_results.py       # grafik dari results/*.csv
```
Error umum (JDK tidak ketemu, dataset hilang, `[FAIL] V4b/V9`): tabel troubleshooting di [PANDUAN_LENGKAP.md](PANDUAN_LENGKAP.md), bagian 6.
