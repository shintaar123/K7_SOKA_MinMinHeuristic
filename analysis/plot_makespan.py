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
from matplotlib.ticker import FuncFormatter

R = "results"


def fmt_int_id(x, pos=None):
    # 10000 -> "10.000"
    return f"{int(round(x)):,}".replace(",", ".")


def fmt_1des_id(x):
    # 4428.6 -> "4.428,6"
    s = f"{x:,.1f}"  # "4,428.6"
    return s.replace(",", "X").replace(".", ",").replace("X", ".")


def fmt_0des_id(x):
    # 26812 -> "26.812"
    return f"{x:,.0f}".replace(",", ".")
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
    ax1.annotate(f"{fmt_1des_id(row['makespan_simulasi_detik'])} s",
                 (row["jumlah_task"], row["makespan_simulasi_detik"]),
                 textcoords="offset points", xytext=(0, 7), ha='center', fontsize=7.5)

ax1.set_title("GoCJ Dataset (100 – 1.000 Task)", fontsize=13, fontweight="bold", pad=12)
ax1.set_xlabel("Jumlah Task", fontsize=11)
ax1.set_ylabel("Makespan (detik)", fontsize=11)
ax1.set_xticks(df_gocj["jumlah_task"])
ax1.set_xticklabels([fmt_int_id(v) for v in df_gocj["jumlah_task"]])
ax1.yaxis.set_major_formatter(FuncFormatter(fmt_int_id))
ax1.grid(True, linestyle=":", alpha=0.6)
ax1.legend(loc="upper left")

# Subplot 2: Synthetic Dataset (1.000 - 10.000 Task)
ax2.plot(df_synth["jumlah_task"], df_synth["makespan_simulasi_detik"],
         marker="s", color="#16a34a", linewidth=2.2, markersize=6, label="Makespan Simulasi")
ax2.plot(df_synth["jumlah_task"], df_synth["makespan_prediksi_detik"],
         linestyle="--", color="#dc2626", linewidth=1.8, label="Prediksi Min-Min")

for _, row in df_synth.iterrows():
    ax2.annotate(f"{fmt_0des_id(row['makespan_simulasi_detik'])} s",
                 (row["jumlah_task"], row["makespan_simulasi_detik"]),
                 textcoords="offset points", xytext=(0, 7), ha='center', fontsize=7.5)

ax2.set_title("Synthetic Dataset (1.000 – 10.000 Task)", fontsize=13, fontweight="bold", pad=12)
ax2.set_xlabel("Jumlah Task", fontsize=11)
ax2.set_ylabel("Makespan (detik)", fontsize=11)
ax2.set_xticks(df_synth["jumlah_task"])
ax2.set_xticklabels([fmt_int_id(v) for v in df_synth["jumlah_task"]])
ax2.yaxis.set_major_formatter(FuncFormatter(fmt_int_id))

plt.suptitle("Evaluasi Algoritma Min-Min: Makespan vs Jumlah Task", fontsize=15, fontweight="bold", y=1.00)
fig.tight_layout()

out_path = os.path.join(R, "grafik_makespan.png")
fig.savefig(out_path, dpi=200, bbox_inches="tight")
plt.close(fig)
print(f"[OK] Grafik berhasil disimpan ke: {out_path}")

# ---- Grafik gabungan (skala log X, format Indonesia) ----
fig2, ax = plt.subplots(figsize=(10, 6))
ax.plot(df_gocj["jumlah_task"], df_gocj["makespan_simulasi_detik"],
        marker="o", color="#0284c7", linewidth=2.2, label="GoCJ (100 - 1.000 task)")
ax.plot(df_synth["jumlah_task"], df_synth["makespan_simulasi_detik"],
        marker="s", color="#16a34a", linewidth=2.2, label="Synthetic (1.000 - 10.000 task)")
ax.set_xscale("log")
ax.set_xticks(list(df_gocj["jumlah_task"]) + list(df_synth["jumlah_task"]))
ax.set_xticklabels([fmt_int_id(v) for v in list(df_gocj["jumlah_task"]) + list(df_synth["jumlah_task"])],
                   rotation=45)
ax.yaxis.set_major_formatter(FuncFormatter(fmt_int_id))
ax.set_title("Min-Min: Makespan vs Jumlah Task (GoCJ vs Synthetic)", fontsize=13, fontweight="bold")
ax.set_xlabel("Jumlah Task (skala log)", fontsize=11)
ax.set_ylabel("Makespan (detik)", fontsize=11)
ax.grid(True, linestyle=":", alpha=0.6, which="both")
ax.legend()
fig2.tight_layout()
out2 = os.path.join(R, "grafik_makespan_gabungan.png")
fig2.savefig(out2, dpi=200, bbox_inches="tight")
plt.close(fig2)
print(f"[OK] Grafik gabungan disimpan ke: {out2}")
