# Simulasi Min-Min di CloudSim

Tugas SOKA (Semester 5, Kelas B), Kelompok 7. Kami menjalankan algoritma **Min-Min** untuk penjadwalan task di **CloudSim 3.0.3**, pakai dataset **GoCJ** (300 task).

## Anggota

| NRP | Nama |
|---|---|
| 5027241021 | Mochkamad Maulana Syafaat |
| 5027241052 | Salsa Bil Ulla |
| 5027241049 | Khumaidi Kharis Az-zacky |
| 5027241016 | Shinta Alya Ramadani |
| 5027241041 | Raya Ahmad Syarif |

## Cara jalankan

Butuh **JDK 11 ke atas**. Cek dulu dengan `javac -version`.

```
.\build_run.bat                    # Windows
bash build_run.sh                  # Linux / WSL / Mac
python analysis/plot_results.py    # grafik (pip install pandas matplotlib)
```

Kalau berhasil, muncul `SEMUA UJI LULUS` lalu `SEMUA VALIDASI LULUS`. Hasilnya ada di `results/`.

## Hasil (dataset GoCJ asli)

| Metrik | Nilai |
|---|---|
| Makespan | 1594,00 detik (prediksi Min-Min: 1594,00) |
| Resource Utilization | 86,87% |
| Average Waiting Time | 339,79 detik |
| Throughput | 0,1882 task/detik |
| Task sukses | 300 dari 300 |

Task terbesar (525 ribu sampai 900 ribu MI) dijadwalkan paling akhir. Task terakhir (900 ribu MI) jatuh ke VM 9 (Small) dan itu yang menentukan makespan. VM 12 cuma terpakai 43,9%. Ini cocok dengan kelemahan Min-Min di slide 10.

## Isi folder

```
src/        kode Java
  config/     SimConfig.java (semua parameter ada di sini)
  data/       GoCJLoader
  scheduler/  MinMinScheduler
  infra/      DatacenterFactory, VmFactory, CloudletFactory, MappedVmAllocationPolicy
  metrics/    MetricsCalculator, Validator, ResultWriter
  app/        MainMinMin (program utama), SchedulerSelfTest
data/       dataset GoCJ_Dataset_300.txt
results/    hasil final (CSV, grafik, console)
analysis/   plot_results.py
tools/      generator data uji (bukan untuk hasil final)
lib/        cloudsim-3.0.3.jar
docs/       panduan dan catatan
```"# K7_SOKA_MinMinHeuristic" 
