"""
Membuat grafik Makespan vs Jumlah Task:
- Memisahkan hasil GoCJ Dataset (100 - 1.000 task) dan Synthetic Dataset (1.000 - 10.000 task).
- Menghasilkan 2 subplot berdampingan (atau panel terpisah) agar skala jumlah task & makespan terlihat jelas,
  serta 1 perbandingan gabungan.

Input:
  results/batch_gocj.csv
  results/batch_synthetic.csv

Output:
  results/grafik_makespan.png
"""
import os
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

R = "results"
gocj_csv = os.path.join(R, "batch_gocj.csv")
synth_csv = os.path.join(R, "batch_synthetic.csv")

if not os.path.exists(gocj_csv) or not os.path.exists(synth_csv):
    print(f"Error: File CSV batch tidak ditemukan di {R}/. Jalankan run_batch.sh terlebih dahulu.")
    exit(1)

df_gocj = pd.read_csv(gocj_csv).sort_values("jumlah_task")
df_synth = pd.read_csv(synth_csv).sort_values("jumlah_task")

# Buat Figure dengan 2 Subplot berdampingan (GoCJ di kiri, Synthetic di kanan)
# Sesuai revisi dosen: "pisahkan hasil GOCJ dan Synthetic Dataset"
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5.5))

# Subplot 1: GoCJ Dataset (100 - 1000 Task)
ax1.plot(df_gocj["jumlah_task"], df_gocj["makespan_simulasi_detik"],
         marker="o", color="#0284c7", linewidth=2.2, markersize=6, label="Makespan Simulasi")
ax1.plot(df_gocj["jumlah_task"], df_gocj["makespan_prediksi_detik"],
         linestyle="--", color="#f97316", linewidth=1.8, label="Prediksi Min-Min")

for _, row in df_gocj.iterrows():
    ax1.annotate(f"{row['makespan_simulasi_detik']:.1f}s",
                 (row["jumlah_task"], row["makespan_simulasi_detik"]),
                 textcoords="offset points", xytext=(0, 7), ha='center', fontsize=7.5)

ax1.set_title("GoCJ Dataset (100 – 1.000 Task)", fontsize=13, fontweight="bold", pad=12)
ax1.set_xlabel("Jumlah Task", fontsize=11)
ax1.set_ylabel("Makespan (detik)", fontsize=11)
ax1.set_xticks(df_gocj["jumlah_task"])
ax1.grid(True, linestyle=":", alpha=0.6)
ax1.legend(loc="upper left")

# Subplot 2: Synthetic Dataset (1.000 - 10.000 Task)
ax2.plot(df_synth["jumlah_task"], df_synth["makespan_simulasi_detik"],
         marker="s", color="#16a34a", linewidth=2.2, markersize=6, label="Makespan Simulasi")
ax2.plot(df_synth["jumlah_task"], df_synth["makespan_prediksi_detik"],
         linestyle="--", color="#dc2626", linewidth=1.8, label="Prediksi Min-Min")

for _, row in df_synth.iterrows():
    ax2.annotate(f"{row['makespan_simulasi_detik']:.0f}s",
                 (row["jumlah_task"], row["makespan_simulasi_detik"]),
                 textcoords="offset points", xytext=(0, 7), ha='center', fontsize=7.5)

ax2.set_title("Synthetic Dataset (1.000 – 10.000 Task)", fontsize=13, fontweight="bold", pad=12)
ax2.set_xlabel("Jumlah Task", fontsize=11)
ax2.set_ylabel("Makespan (detik)", fontsize=11)
ax2.set_xticks(df_synth["jumlah_task"])
ax2.tick_params(axis='x', rotation=30)
ax2.grid(True, linestyle=":", alpha=0.6)
ax2.legend(loc="upper left")

plt.suptitle("Evaluasi Algoritma Min-Min: Makespan vs Jumlah Task", fontsize=15, fontweight="bold", y=1.00)
fig.tight_layout()

out_path = os.path.join(R, "grafik_makespan.png")
fig.savefig(out_path, dpi=200, bbox_inches="tight")
plt.close(fig)
print(f"[OK] Grafik berhasil disimpan ke: {out_path}")
