# Bagian Orang 4 - Dataset & Cloudlet

## Identitas Peran

**Peran:** D · Dataset & Cloudlet  
**Tanggung jawab utama:** pengolahan dataset GoCJ dan pembentukan Cloudlet untuk simulasi CloudSim.

---

## 1. Ruang Lingkup Pekerjaan

Bagian ini bertanggung jawab pada dua komponen utama:

- `src/data/GoCJLoader.java` — membaca, memvalidasi, dan menghitung statistik dataset GoCJ.
- `src/infra/CloudletFactory.java` — mengubah data panjang task (`lengthMI`) menjadi Cloudlet sesuai urutan yang diberikan oleh scheduler.

Alur data yang digunakan:

```text
GoCJ_Dataset_300.txt
        ↓
   GoCJLoader
        ↓
 double[] lengthMI
        ↓
 MinMinScheduler
        ↓
   r.order + taskToVm
        ↓
 CloudletFactory
        ↓
      Cloudlet
        ↓
    CloudSim
```

Panduan proyek menetapkan bahwa D menangani **GoCJ + Cloudlet**, termasuk pengujian pengaruh urutan pengiriman Cloudlet. 

---

## 2. Dataset GoCJ

Dataset yang digunakan untuk hasil final adalah dataset GoCJ asli dengan **300 task**:

```text
data/GoCJ_Dataset_300.txt
```

Format dataset:

- satu nilai MI per baris;
- minimal 300 baris diperlukan;
- nilai yang dibaca harus berupa angka dan bernilai positif.

Dataset uji `make_dummy_gocj.py` tidak digunakan untuk hasil final.

---

## 3. Statistik Dataset Final

Hasil pembacaan dataset pada run final:

| Statistik | Nilai |
|---|---:|
| Jumlah task | 300 |
| Minimum MI | 15,000 |
| Maksimum MI | 900,000 |
| Rata-rata MI | 138,420.0 |
| Total MI | 41,526,000 |

Output terminal:

```text
Dataset OK  : GoCJ | n=300  min=15000  max=900000  rata-rata=138420.0  total=41526000 MI
```

---

## 4. `GoCJLoader`

`GoCJLoader` bertugas membaca dataset menjadi array:

```java
double[] lengthMI
```

Fungsi utamanya:

1. Membaca file dataset.
2. Mengambil 300 task pertama sesuai konfigurasi.
3. Memastikan nilai MI valid dan lebih besar dari 0.
4. Menerapkan skala MI dari konfigurasi.
5. Menghasilkan statistik dataset untuk laporan.

Dengan dataset final, loader berhasil menghasilkan **300 nilai `lengthMI`**.

---

## 5. `CloudletFactory`

`CloudletFactory` mengubah setiap nilai pada `lengthMI` menjadi objek `Cloudlet`.

Konsep pemetaan:

```text
lengthMI[id]
    ↓
Cloudlet dengan cloudletId = id
```

Cloudlet dibuat dengan parameter yang sudah digunakan proyek, kemudian `userId` diatur ke ID broker.

Yang penting untuk simulasi adalah **urutan list Cloudlet**. Pada kondisi final, Cloudlet dibuat menggunakan:

```java
List<Cloudlet> cloudlets = CloudletFactory.create(
    broker.getId(), lengthMI, r.order
);
```

Dengan demikian, broker menerima Cloudlet dalam **urutan hasil Min-Min**.

---

## 6. Eksperimen Pengaruh Urutan Cloudlet

Untuk membuktikan bahwa urutan pengiriman Cloudlet penting, dilakukan eksperimen dengan mengganti `r.order` menjadi urutan natural `0..299`.

Kode eksperimen:

```java
int[] naturalOrder = new int[lengthMI.length];

for (int i = 0; i < naturalOrder.length; i++) {
    naturalOrder[i] = i;
}

List<Cloudlet> cloudlets = CloudletFactory.create(
    broker.getId(), lengthMI, naturalOrder
);
```

### Bagian yang tidak diubah

Pemetaan Cloudlet ke VM tetap menggunakan hasil Min-Min:

```java
for (Cloudlet c : cloudlets) {
    broker.bindCloudletToVm(
        c.getCloudletId(),
        r.taskToVm[c.getCloudletId()]
    );
}
```

Jadi eksperimen hanya mengubah **urutan submit Cloudlet**, bukan hasil pemetaan task ke VM.

---

## 7. Hasil Eksperimen

### Kondisi normal — urutan `r.order`

| Metrik | Hasil |
|---|---:|
| Makespan | 1594.00 detik |
| Average Waiting Time | 339.79 detik |
| Task sukses | 300 / 300 |
| V4b | PASS |

### Eksperimen — urutan `0..299`

| Metrik | Hasil |
|---|---:|
| Makespan | 1594.00 detik |
| Average Waiting Time | 714.07 detik |
| Task sukses | 300 / 300 |
| V4b | FAIL |
| Selisih waktu mulai terbesar | 1473.9967 detik |

Output eksperimen menunjukkan:

```text
Average Waiting Time : 714.07 detik

[FAIL] V4b waktu mulai tiap task ~ prediksi (urutan submit benar)
       selisih terbesar = 1473.9967 dtk
```

Eksperimen ini digunakan sebagai bukti bahwa Cloudlet perlu dikirim mengikuti `r.order` hasil Min-Min agar urutan eksekusi CloudSim sesuai jadwal teoritis.

---

## 8. Kondisi Kode Final

Setelah eksperimen selesai, kode harus **dikembalikan** ke kondisi final:

```java
List<Cloudlet> cloudlets = CloudletFactory.create(
    broker.getId(), lengthMI, r.order
);
```

Kode `naturalOrder` hanya digunakan untuk eksperimen dan tidak digunakan pada hasil final.

---

## 9. Verifikasi Run Final

Run menggunakan:

### Windows

```powershell
.\build_run.bat
```

### Linux / WSL / Mac / Git Bash

```bash
bash build_run.sh
```

Kondisi final harus menghasilkan:

```text
SEMUA UJI LULUS
...
SEMUA VALIDASI LULUS
```

Pada hasil run final yang digunakan dalam proyek:

```text
Dataset OK  : GoCJ | n=300  min=15000  max=900000  rata-rata=138420.0  total=41526000 MI
Min-Min OK  : prediksi makespan = 1594.00 detik

Makespan (simulasi)       : 1594.00 detik
Makespan (prediksi MinMin): 1594.00 detik
Resource Utilization      : 86.87 %
Average Waiting Time      : 339.79 detik
Throughput                : 0.1882 task/detik
Task sukses               : 300 / 300
```

Validasi utama:

```text
[PASS] V2  300 cloudlet berstatus SUCCESS
[PASS] V3  jumlah task seluruh VM = 300
[PASS] V4a makespan CloudSim ~ prediksi Min-Min
[PASS] V4b waktu mulai tiap task ~ prediksi
[PASS] V4c setiap task jalan di VM hasil Min-Min
[PASS] V5  makespan >= batas bawah teoritis
[PASS] V6  Resource Utilization <= 100%
[PASS] V7  tidak ada task tumpang tindih di satu VM
[PASS] V9  penempatan VM sesuai rancangan
[PASS] V8  penjadwal deterministik
SEMUA VALIDASI LULUS
```

---

## 10. Ringkasan Kontribusi Orang 4

Kontribusi bagian D dapat diringkas sebagai berikut:

> Menyiapkan dataset GoCJ asli 300 task, memastikan dataset berhasil dibaca menjadi `double[] lengthMI`, mencatat statistik dataset, serta menangani pembentukan Cloudlet melalui `CloudletFactory`. Dilakukan eksperimen dengan mengirim Cloudlet menggunakan urutan natural `0..299` untuk membuktikan pengaruh urutan submit terhadap waktu tunggu dan kesesuaian waktu mulai. Setelah eksperimen, urutan dikembalikan ke `r.order` sebagai konfigurasi final.

---

## 11. File Terkait

```text
src/data/GoCJLoader.java
src/infra/CloudletFactory.java
data/GoCJ_Dataset_300.txt
tools/make_dummy_gocj.py       # hanya untuk data uji
```

---

## 12. Referensi Internal Proyek

Panduan lengkap proyek menjelaskan pembagian tugas D, format dataset, fungsi `GoCJLoader`, fungsi `CloudletFactory`, serta eksperimen perubahan urutan Cloudlet. 
