#!/usr/bin/env bash
# Script untuk menjalankan simulasi batch:
# 1. Compile Java code
# 2. Batch GoCJ: 100 - 1.000 task (kelipatan 100) -> results/batch_gocj.csv
# 3. Batch Synthetic: 1.000 - 10.000 task (kelipatan 1.000) -> results/batch_synthetic.csv
# 4. Generate Plot Makespan vs Jumlah Task -> results/grafik_makespan.png
set -e
cd "$(dirname "$0")"

CP="lib/cloudsim-3.0.3.jar"
mkdir -p out results

echo ">>> 1. Compiling Java sources..."
javac -encoding UTF-8 -cp "$CP" -d out $(find src -name "*.java")
echo ">>> Compile berhasil!"
echo ""

echo ">>> 2. Menjalankan Batch GoCJ Dataset (100 - 1.000 task, kelipatan 100)..."
GOCJ_FILES=(
    "data/GoCJ_Dataset_100.txt"
    "data/GoCJ_Dataset_200.txt"
    "data/GoCJ_Dataset_300.txt"
    "data/GoCJ_Dataset_400.txt"
    "data/GoCJ_Dataset_500.txt"
    "data/GoCJ_Dataset_600.txt"
    "data/GoCJ_Dataset_700.txt"
    "data/GoCJ_Dataset_800.txt"
    "data/GoCJ_Dataset_900.txt"
    "data/GoCJ_Dataset_1000.txt"
)
java -cp "out:$CP" app.BatchRunner "GoCJ" "results/batch_gocj.csv" "${GOCJ_FILES[@]}"
echo ""

echo ">>> 3. Menjalankan Batch Synthetic Dataset (1.000 - 10.000 task)..."
SYNTH_FILES=(
    "data/Synthetic_1000.txt"
    "data/Synthetic_2000.txt"
    "data/Synthetic_3000.txt"
    "data/Synthetic_4000.txt"
    "data/Synthetic_5000.txt"
    "data/Synthetic_6000.txt"
    "data/Synthetic_7000.txt"
    "data/Synthetic_8000.txt"
    "data/Synthetic_9000.txt"
    "data/Synthetic_10000.txt"
)
java -cp "out:$CP" app.BatchRunner "Synthetic" "results/batch_synthetic.csv" "${SYNTH_FILES[@]}"
echo ""

echo ">>> 4. Menghasilkan Grafik Makespan vs Jumlah Task..."
if [ -f ".venv/bin/python3" ]; then
    .venv/bin/python3 analysis/plot_makespan.py
else
    python3 analysis/plot_makespan.py
fi
echo ""
echo ">>> SEMUA PROSES BATCH DAN VISUALISASI SELESAI!"
