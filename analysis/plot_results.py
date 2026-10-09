"""
Membuat 3 grafik dari CSV hasil simulasi. Jalankan dari folder root proyek:
    python analysis/plot_results.py
Butuh:  pip install pandas matplotlib
Output: results/grafik_1_beban_vm.png, grafik_2_urutan_vs_panjang.png, grafik_3_waiting_time.png
Format angka Indonesia: titik = ribuan, koma = desimal.
"""
import os
import pandas as pd
import numpy as np
import matplotlib
matplotlib.use("Agg")          # aman di laptop tanpa layar / WSL
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter

R = "results"
cl = pd.read_csv(os.path.join(R, "cloudlet_result.csv"))
vm = pd.read_csv(os.path.join(R, "vm_load.csv"))
sm = pd.read_csv(os.path.join(R, "summary.csv")).set_index("metrik")["nilai"]


def fmt_int_id(x, pos=None):
    try:
        v = float(x)
    except (TypeError, ValueError):
        return ""
    return f"{int(round(v)):,}".replace(",", ".")


def fmt_des_id(x, nd=1):
    s = f"{x:,.{nd}f}"
    return s.replace(",", "X").replace(".", ",").replace("X", ".")


n = len(cl)
makespan = float(sm["makespan_simulasi_detik"])
avg_wait = float(sm["average_waiting_time_detik"])

# ---- 1. Beban per VM (busy time) ----
fig, ax = plt.subplots(figsize=(10, 4.5))
colors = {"Small": "#8ecae6", "Medium": "#219ebc", "Large": "#023047"}
bars = ax.bar(vm["vm_id"], vm["busy_time"], color=[colors[t] for t in vm["tipe"]])
ax.axhline(makespan, color="red", ls="--", lw=1,
           label=f"Makespan {fmt_des_id(makespan)} s")
# angka jumlah task di ATAS batang (hitam, terbaca)
ymax = max(vm["busy_time"].max(), makespan)
for rect, nt in zip(bars, vm["jumlah_task"]):
    ax.text(rect.get_x() + rect.get_width() / 2, rect.get_height() + ymax * 0.015,
            fmt_int_id(nt), ha="center", va="bottom", color="black", fontsize=8)
ax.set_xticks(vm["vm_id"])
ax.set_xticklabels([f"{i}\n{t[0]}" for i, t in zip(vm["vm_id"], vm["tipe"])])
ax.set_xlabel("VM id (huruf = tipe S/M/L, angka di atas batang = jumlah task)")
ax.set_ylabel("Busy time (detik)")
ax.set_title("Beban per VM - Min-Min")
handles = [plt.Rectangle((0, 0), 1, 1, color=c) for c in colors.values()]
ax.yaxis.set_major_formatter(FuncFormatter(fmt_int_id))
ax.set_ylim(0, ymax * 1.22)
ax.legend(handles + [ax.lines[0]], list(colors.keys()) + [f"Makespan {fmt_des_id(makespan)} s"],
          loc="upper center", ncol=4, fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(R, "grafik_1_beban_vm.png"), dpi=150); plt.close(fig)

# ---- 2. Urutan penjadwalan vs panjang task ----
fig, ax = plt.subplots(figsize=(8, 4.5))
ax.scatter(cl["urutan_dijadwalkan"], cl["panjang_MI"], s=12, alpha=0.6)
ax.set_xlabel("Urutan dijadwalkan (1 = pertama)")
ax.set_ylabel("Panjang task (MI)")
ax.set_title("Urutan penjadwalan vs panjang task")
ax.yaxis.set_major_formatter(FuncFormatter(fmt_int_id))
ax.xaxis.set_major_formatter(FuncFormatter(fmt_int_id))
ax.grid(True, linestyle=":", alpha=0.5)
fig.tight_layout(); fig.savefig(os.path.join(R, "grafik_2_urutan_vs_panjang.png"), dpi=150); plt.close(fig)

# ---- 3. Waiting time: rata-rata per bin (diurutkan menurut panjang task) ----
s = cl.sort_values("panjang_MI").reset_index(drop=True)
n_bins = 20 if n >= 200 else max(5, n // 10)
s["bin"] = pd.qcut(s.index, q=n_bins, duplicates="drop")
g = s.groupby("bin", observed=True).agg(
    mi_min=("panjang_MI", "min"), mi_max=("panjang_MI", "max"),
    wait_mean=("waiting_time", "mean"), jml=("waiting_time", "size"))
g = g.reset_index(drop=True)
x = np.arange(len(g))
labels = [f"{fmt_int_id(a)}–{fmt_int_id(b)}" for a, b in zip(g["mi_min"], g["mi_max"])]

fig, ax = plt.subplots(figsize=(10, 4.5))
bars = ax.bar(x, g["wait_mean"], color="#219ebc", edgecolor="white")
ax.axhline(avg_wait, color="red", ls="--", lw=1.2,
           label=f"Rata-rata global {fmt_des_id(avg_wait)} s")
for i, v in enumerate(g["wait_mean"]):
    if i % max(1, len(g) // 10) == 0 or i == len(g) - 1:
        ax.text(i, v, fmt_des_id(v, 0) + " s", ha="center", va="bottom", fontsize=7)
ax.set_xticks(x)
step = max(1, len(g) // 10)
ax.set_xticklabels([lb if (i % step == 0 or i == len(g) - 1) else "" for i, lb in enumerate(labels)],
                   rotation=30, ha="right", fontsize=7)
ax.set_xlabel(f"Kelompok task per panjang MI (n={fmt_int_id(n)} task, {fmt_int_id(g['jml'].iloc[0])} task/bin)")
ax.set_ylabel("Rata-rata waiting time (detik)")
ax.set_title("Rata-rata waiting time per kelompok panjang task")
ax.yaxis.set_major_formatter(FuncFormatter(fmt_int_id))
ax.legend(fontsize=9)
fig.tight_layout(); fig.savefig(os.path.join(R, "grafik_3_waiting_time.png"), dpi=150); plt.close(fig)

# ---- Angka pendukung ----
q = cl["panjang_MI"].quantile([0.25, 0.75])
kecil = cl[cl["panjang_MI"] <= q[0.25]]; besar = cl[cl["panjang_MI"] >= q[0.75]]
print("Rata-rata waiting task terpendek (25%%): %s dtk" % fmt_des_id(kecil["waiting_time"].mean(), 2))
print("Rata-rata waiting task terpanjang (25%%): %s dtk" % fmt_des_id(besar["waiting_time"].mean(), 2))
try:
    rs = cl["urutan_dijadwalkan"].corr(cl["panjang_MI"], method="spearman")
except ImportError:
    # ponytail: tanpa scipy, Spearman = Pearson dari rank
    rs = cl["urutan_dijadwalkan"].rank().corr(cl["panjang_MI"].rank())
print("Korelasi urutan vs panjang task (Spearman): %.3f" % rs)
print("Utilisasi VM Small/Medium/Large (rata-rata %%):")
print(vm.groupby("tipe")["utilisasi_vm"].mean().round(2).to_string())
print("Grafik tersimpan di folder results/")
