# Bagian Orang 2 — Infrastruktur CloudSim (Peran B)

**File yang dipegang:** `src/infra/DatacenterFactory.java`, `src/infra/VmFactory.java`, `src/infra/MappedVmAllocationPolicy.java`. Parameternya terpusat di `src/config/SimConfig.java`.

**Tujuan:** Memastikan spesifikasi datacenter di simulator identik dengan Slide 12–14 dan tugas minggu lalu, serta memastikan seluruh VM teralokasi tepat ke Host yang dituju (Validasi V9 PASS).

## 1. Spesifikasi Infrastruktur (Slide 12–13)
Konfigurasi terpusat pada `SimConfig.java`:

| Komponen | Spesifikasi & Nilai | Catatan & Sumber |
|---|---|---|
| **Jumlah Host / VM** | 5 Host fisik, 15 VM (3 VM per host) | Sesuai Slide 13 |
| **PE per Host** | Host 1-2: 4 PE \| Host 3-4: 6 PE \| Host 5: 8 PE | Heterogen (Slide 13) |
| **MIPS per PE Host** | Host 1-2: 3000 \| Host 3-4: 3500 \| Host 5: 4000 | Heterogen (Slide 13) |
| **Tipe VM** | Small: 1000 MIPS \| Medium: 2000 MIPS \| Large: 3000 MIPS | 1 PE per VM (Slide 13) |
| **RAM VM** | Small: 1024 MB \| Medium: 2048 MB \| Large: 4096 MB | Slide 13 |
| **RAM Host** | Host 1-2: 16 GB \| Host 3-4: 24 GB \| Host 5: 32 GB | [USULAN] disamakan tugas m3 |
| **Bandwidth Host / VM** | Host: 10.000 Mbps \| VM: 1.000 Mbps | [USULAN] |
| **Storage Host / VM** | Host Storage: 1.000.000 MB \| VM Image: 10.000 MB | [USULAN] |

## 2. Batasan Teknis & Keamanan Alokasi
- Total kebutuhan RAM untuk 3 VM per host adalah $1024 + 2048 + 4096 = 7168\text{ MB}$. RAM host terkecil (16 GB / 16384 MB) jauh mencukupi sehingga tidak terjadi *Out of Memory*.
- MIPS VM tertinggi (3000 MIPS pada VM Large) $\le$ MIPS PE host terendah (3000 MIPS).
- Tiap host membutuhkan minimal 3 PE untuk menampung 3 VM (1 VM = 1 PE).

## 3. Logika Kode Infrastruktur (Bahan Demo)
- **`DatacenterFactory.java`:** Membangun 5 Host fisik heterogen dengan karakteristik Linux/Xen dan penjadwal CPU `VmSchedulerTimeShared`.
- **`VmFactory.java`:** Membuat 15 VM (pola Small, Medium, Large diulang 5 kali) menggunakan penjadwal cloudlet `CloudletSchedulerSpaceShared` (non-preemptive, 1 task berjalan sampai selesai di core VM).
- **`MappedVmAllocationPolicy.java`:** Kebijakan kustom yang memaksa pemetaan `vmId / 3 -> host`. Bawaan CloudSim (`VmAllocationPolicySimple`) menaruh VM di host paling longgar sehingga urutannya acak. Custom policy ini menjamin VM 0–2 di Host 1, VM 3–5 di Host 2, dst., tepat sesuai gambar Slide 13.

## 4. Bukti Hasil Eksekusi Penempatan VM (Log Output)
Ketika dijalankan via `bash build_run.sh`, penempatan VM terbukti 100% presisi:
```text
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

[PASS] V9  VM 0-2 -> Host1, VM 3-5 -> Host2, ... VM 12-14 -> Host5  -> 15 VM terpasang = 15
```

## 5. Pertanyaan Dosen yang Kerap Muncul
1. **Kenapa perlu alokasi VM sendiri (`MappedVmAllocationPolicy`)?**  
   *Jawab:* Kebijakan bawaan CloudSim menempatkan VM ke host mana saja yang paling longgar. Agar alokasi persis dengan desain proposal di slide 13 (VM 0-2 di Host 1, VM 3-5 di Host 2, dst.), kita membuat policy turunan sendiri.
2. **Kenapa 1 VM = 1 PE?**  
   *Jawab:* Agar kapasitas MIPS per VM terdefinisi jelas (1000, 2000, 3000 MIPS) sehingga perhitungan waktu eksekusi task menjadi deterministik: $\text{Panjang Task (MI)} / \text{MIPS VM}$.
3. **Kenapa penjadwal task di VM menggunakan `SpaceShared`?**  
   *Jawab:* Min-Min mengasumsikan model komputasi non-preemptive di mana satu task diproses tuntas di core tersebut sebelum task berikutnya dikerjakan.

## 6. Perintah Jalankan
```bash
bash build_run.sh
```
Troubleshooting dan penjelasan detail metrik ada di [PANDUAN_LENGKAP.md](PANDUAN_LENGKAP.md).
