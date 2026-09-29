"""
Membuat 3 grafik dari CSV hasil simulasi. Jalankan dari folder root proyek:
    python analysis/plot_results.py
Butuh:  pip install pandas matplotlib
Output: results/grafik_1_beban_vm.png, grafik_2_urutan_vs_panjang.png, grafik_3_waiting_time.png
"""
import os
import pandas as pd
import matplotlib
matplotlib.use("Agg")          # aman di laptop tanpa layar / WSL
import matplotlib.pyplot as plt

R = "results"
cl = pd.read_csv(os.path.join(R, "cloudlet_result.csv"))
vm = pd.read_csv(os.path.join(R, "vm_load.csv"))
sm = pd.read_csv(os.path.join(R, "summary.csv")).set_index("metrik")["nilai"]

# ---- 1. Beban per VM (busy time) -> apakah load seimbang? ----
fig, ax = plt.subplots(figsize=(10, 4.5))
colors = {"Small": "#8ecae6", "Medium": "#219ebc", "Large": "#023047"}
ax.bar(vm["vm_id"], vm["busy_time"], color=[colors[t] for t in vm["tipe"]])
ax.axhline(sm["makespan_simulasi_detik"], color="red", ls="--", lw=1, label="Makespan")
for x, n in zip(vm["vm_id"], vm["jumlah_task"]):
    ax.text(x, 5, str(n), ha="center", va="bottom", color="white", fontsize=8)
ax.set_xticks(vm["vm_id"]); ax.set_xlabel("VM id (angka di batang = jumlah task)")
ax.set_ylabel("Busy time (detik)"); ax.set_title("Beban per VM - Min-Min")
handles = [plt.Rectangle((0, 0), 1, 1, color=c) for c in colors.values()]
ax.set_ylim(0, sm["makespan_simulasi_detik"] * 1.18)
ax.legend(handles + [ax.lines[0]], list(colors.keys()) + ["Makespan"], loc="upper center", ncol=4)
fig.tight_layout(); fig.savefig(os.path.join(R, "grafik_1_beban_vm.png"), dpi=150); plt.close(fig)

# ---- 2. Urutan penjadwalan vs panjang task -> apakah task kecil duluan? ----
fig, ax = plt.subplots(figsize=(8, 4.5))
ax.scatter(cl["urutan_dijadwalkan"], cl["panjang_MI"], s=12, alpha=0.7)
ax.set_xlabel("Urutan dijadwalkan (1 = pertama)"); ax.set_ylabel("Panjang task (MI)")
ax.set_title("Urutan penjadwalan vs panjang task")
fig.tight_layout(); fig.savefig(os.path.join(R, "grafik_2_urutan_vs_panjang.png"), dpi=150); plt.close(fig)

# ---- 3. Waiting time per task, diurutkan menurut panjang task ----
s = cl.sort_values("panjang_MI").reset_index(drop=True)
fig, ax = plt.subplots(figsize=(8, 4.5))
ax.bar(s.index, s["waiting_time"], width=1.0, color="#219ebc")
ax.axhline(sm["average_waiting_time_detik"], color="red", ls="--", lw=1, label="Rata-rata")
ax.set_xlabel("Task (diurutkan dari terpendek ke terpanjang)"); ax.set_ylabel("Waiting time (detik)")
ax.set_title("Waiting time per task"); ax.legend()
fig.tight_layout(); fig.savefig(os.path.join(R, "grafik_3_waiting_time.png"), dpi=150); plt.close(fig)

# ---- Angka pendukung untuk analisis dugaan slide 10 ----
q = cl["panjang_MI"].quantile([0.25, 0.75])
kecil = cl[cl["panjang_MI"] <= q[0.25]]; besar = cl[cl["panjang_MI"] >= q[0.75]]
print("Rata-rata waiting task terpendek (25%%): %.2f dtk" % kecil["waiting_time"].mean())
print("Rata-rata waiting task terpanjang (25%%): %.2f dtk" % besar["waiting_time"].mean())
print("Korelasi urutan vs panjang task (Spearman): %.3f" % cl["urutan_dijadwalkan"].corr(cl["panjang_MI"], method="spearman"))
print("Utilisasi VM Small/Medium/Large (rata-rata %):")
print(vm.groupby("tipe")["utilisasi_vm"].mean().round(2).to_string())
print("Grafik tersimpan di folder results/")
