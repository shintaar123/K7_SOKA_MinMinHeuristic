"""
Membuat DATA UJI mirip-GoCJ (BUKAN dataset asli) supaya program bisa dites sebelum file asli
dari Mendeley Data diunduh. Hasil: data/GoCJ_Dataset_300.txt (1 angka MI per baris).
Untuk hasil FINAL / demo, GANTI file ini dengan GoCJ_Dataset_300.txt asli.

Pemakaian:  python tools/make_dummy_gocj.py            (300 task, seed 42)
"""
import random, sys, os

n    = int(sys.argv[1]) if len(sys.argv) > 1 else 300
seed = int(sys.argv[2]) if len(sys.argv) > 2 else 42
random.seed(seed)

# (batas bawah MI, batas atas MI, bobot) - perkiraan pola panjang-pendek (mayoritas task pendek, ekor panjang)
groups = [(15000, 55000, 20), (59000, 99000, 40), (101000, 135000, 30), (150000, 337500, 4), (525000, 900000, 6)]
weights = [g[2] for g in groups]

os.makedirs("data", exist_ok=True)
path = os.path.join("data", "GoCJ_Dataset_%d.txt" % n)
with open(path, "w") as f:
    for _ in range(n):
        lo, hi, _w = random.choices(groups, weights=weights)[0]
        f.write("%d\n" % (random.randrange(lo, hi + 1, 1000)))
print("ditulis:", path)
