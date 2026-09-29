@echo off
REM Windows (cmd / PowerShell).  Pakai:  .\build_run.bat
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -cp "lib\cloudsim-3.0.3.jar" -d out src\config\*.java src\data\*.java src\infra\*.java src\scheduler\*.java src\metrics\*.java src\app\*.java
if errorlevel 1 goto gagal
echo ^>^>^> Build OK
echo ^>^>^> [1/2] Uji penjadwal (tanpa CloudSim)
java -cp out app.SchedulerSelfTest
if errorlevel 1 goto gagal
echo ^>^>^> [2/2] Simulasi CloudSim
java -cp "out;lib\cloudsim-3.0.3.jar" app.MainMinMin %*
goto selesai
:gagal
echo GAGAL - baca pesan error di atas.
:selesai
