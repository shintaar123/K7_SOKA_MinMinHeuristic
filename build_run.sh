#!/usr/bin/env bash
# Linux / macOS / WSL / Git Bash.  Pakai:  bash build_run.sh          (build + tes + jalankan)
set -e
cd "$(dirname "$0")"
CP="lib/cloudsim-3.0.3.jar"
rm -rf out && mkdir out
javac -encoding UTF-8 -cp "$CP" -d out $(find src -name "*.java")
echo ">>> Build OK"
echo ">>> [1/2] Uji penjadwal (tanpa CloudSim)"
java -cp out app.SchedulerSelfTest
echo ">>> [2/2] Simulasi CloudSim"
java -cp "out:$CP" app.MainMinMin "$@"
