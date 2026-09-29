# Cara Push ke GitHub

Ada dua bagian. **Bagian A** cuma dikerjakan satu orang (yang bikin repo). **Bagian B** untuk semua anggota.

Pastikan Git sudah terpasang: `git --version`. Kalau belum, unduh dari git-scm.com.

## A. Yang bikin repo (sekali saja)

**1. Bikin repo kosong di GitHub**
- Buka github.com, klik **New repository**.
- Isi nama, misalnya `soka-minmin-cloudsim`.
- Pilih **Private** dulu (lihat catatan lisensi dataset di bawah).
- **Jangan** centang "Add a README", `.gitignore`, atau license. Repo harus kosong.
- Klik **Create repository**, lalu salin URL-nya (`https://github.com/USERNAME/soka-minmin-cloudsim.git`).

**2. Atur nama dan email Git** (sekali per laptop)
```
git config --global user.name "Nama Kamu"
git config --global user.email "email-github@contoh.com"
```

**3. Push dari folder proyek**

Buka PowerShell **di dalam** folder `soka-minmin-cloudsim` (yang ada `README.md` dan `src/`). Jangan di folder atasnya.

```
git init
git add .
git commit -m "first commit: simulasi Min-Min di CloudSim"
git branch -M main
git remote add origin https://github.com/USERNAME/soka-minmin-cloudsim.git
git push -u origin main
```

Waktu push pertama, biasanya muncul jendela login GitHub di browser. Login saja. Kalau diminta password lewat terminal, pakai **Personal Access Token** (GitHub, Settings, Developer settings, Personal access tokens), bukan password akun.

**4. Undang anggota lain**

Di repo GitHub: **Settings**, **Collaborators**, **Add people**. Masukkan username GitHub tiap anggota. Mereka harus terima undangan lewat email.

## B. Semua anggota

**Pertama kali (ambil proyek)**
```
git clone https://github.com/USERNAME/soka-minmin-cloudsim.git
cd soka-minmin-cloudsim
```
Lalu coba `.\build_run.bat` sampai muncul `SEMUA VALIDASI LULUS`.

**Setiap kali kerja**
```
git pull                          # ambil kerjaan terbaru dulu
# ... edit file bagianmu ...
git status                        # cek apa yang berubah
git add .
git commit -m "pesan singkat yang jelas"
git push
```

Contoh pesan commit yang enak dibaca:
- `metrics: tambah analisis dugaan slide 10`
- `docs: isi hasil eksperimen urutan submit`
- `infra: ubah RAM host sesuai tugas minggu lalu`

## Kalau ada masalah

| Masalah | Solusi |
|---|---|
| `fatal: not a git repository` | Kamu belum di folder proyek. `cd` ke `soka-minmin-cloudsim`. |
| `remote origin already exists` | Sudah pernah ditambah. Cek `git remote -v`. Salah URL? `git remote set-url origin URL_BARU`. |
| `rejected ... fetch first` atau `non-fast-forward` | Ada yang push duluan. Jalankan `git pull`, lalu `git push` lagi. |
| Konflik saat `git pull` | Buka file yang ditandai `<<<<<<<`, pilih isi yang benar, hapus tandanya. Lalu `git add .`, `git commit`, `git push`. Kalau bingung, tanya grup dulu. |
| `Permission denied` / 403 | Akunmu belum jadi collaborator, atau login pakai akun yang salah. |
| Salah commit file | Sebelum push: `git reset --soft HEAD~1`. Sesudah push: tanya grup, jangan force push. |

## Catatan

- Jangan `git push --force`. Itu bisa menimpa kerjaan orang lain.
- Folder `out/` sengaja tidak ikut ke repo (ada di `.gitignore`). Isinya hasil compile, akan dibuat ulang tiap `build_run`.
- Dataset GoCJ berasal dari Mendeley Data. Cek lisensinya dulu sebelum repo dibuat **Public**.
- Kalau ada perubahan di `results/`, commit sekalian supaya hasil di repo selalu sama dengan kode terakhir.
