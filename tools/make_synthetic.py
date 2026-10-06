"""
Generator Synthetic Dataset untuk penjadwalan CloudSim Min-Min.
Menghasilkan task berukuran 1.000 hingga 10.000 (kelipatan 1.000):
Synthetic_1000.txt ... Synthetic_10000.txt

Distribusi Task:
Heterogen / Multimodal (Realistic Cloud Workload):
- 60% Task Kecil   : 10,000 - 50,000 MI   (Web request / microservice)
- 30% Task Sedang  : 50,000 - 150,000 MI  (Data processing / query)
- 10% Task Besar   : 150,000 - 500,000 MI (Batch ETL / ML training)

Penggunaan:
    python tools/make_synthetic.py
"""
import os
import random

SEED = 2024
random.seed(SEED)

DATA_DIR = "data"
os.makedirs(DATA_DIR, exist_ok=True)

TASK_COUNTS = list(range(1000, 10001, 1000))  # 1000, 2000, ..., 10000

DISTRIBUTION = [
    (10000, 50000, 60),    # Small tasks (60%)
    (50000, 150000, 30),   # Medium tasks (30%)
    (150000, 500000, 10),  # Large tasks (10%)
]

weights = [d[2] for d in DISTRIBUTION]

def generate_dataset(n):
    filepath = os.path.join(DATA_DIR, f"Synthetic_{n}.txt")
    with open(filepath, "w") as f:
        for _ in range(n):
            chosen_group = random.choices(DISTRIBUTION, weights=weights)[0]
            low, high, _ = chosen_group
            # Step 500 MI agar representatif
            length = random.randrange(low, high + 1, 500)
            f.write(f"{length}\n")
    print(f"[OK] Generated: {filepath} ({n} tasks)")

def main():
    print("=== Generating Synthetic Datasets (1.000 - 10.000 Tasks) ===")
    for n in TASK_COUNTS:
        generate_dataset(n)
    print("Selesai semua Synthetic Datasets!")

if __name__ == "__main__":
    main()
