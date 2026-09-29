package app;

import java.util.Random;
import scheduler.MinMinScheduler;

/** Uji penjadwal SAJA (tanpa CloudSim). Harus PASS semua sebelum lanjut ke integrasi. */
public class SchedulerSelfTest {
    static int fail = 0;
    static void check(String name, boolean ok) {
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + name);
        if (!ok) fail++;
    }

    public static void main(String[] args) {
        // ---- Uji 1: contoh mini slide 8 ----
        double[] len  = {4000, 8000, 2000, 6000};   // T1..T4
        double[] mips = {1000, 2000};               // VM1, VM2
        MinMinScheduler.Result r = MinMinScheduler.schedule(len, mips);
        // urutan benar: T3, T1, T4, T2 -> indeks 2, 0, 3, 1
        check("Slide 8: urutan = T3,T1,T4,T2", r.order[0] == 2 && r.order[1] == 0 && r.order[2] == 3 && r.order[3] == 1);
        // T3->VM2, T1->VM2, T4->VM1, T2->VM2
        check("Slide 8: pemetaan T1->VM2, T2->VM2, T3->VM2, T4->VM1",
              r.taskToVm[0] == 1 && r.taskToVm[1] == 1 && r.taskToVm[2] == 1 && r.taskToVm[3] == 0);
        check("Slide 8: CT = 3, 7, 1, 6 detik",
              r.finishTime[0] == 3 && r.finishTime[1] == 7 && r.finishTime[2] == 1 && r.finishTime[3] == 6);
        check("Slide 8: makespan = 7 detik (" + r.makespan() + ")", r.makespan() == 7.0);

        // ---- Uji 2: sifat umum pada 300 task acak, 15 VM ----
        Random rnd = new Random(42);
        double[] L = new double[300];
        double sum = 0;
        for (int i = 0; i < L.length; i++) { L[i] = 15000 + rnd.nextInt(900000); sum += L[i]; }
        double[] M = new double[15];
        for (int i = 0; i < 15; i++) M[i] = new double[]{1000, 2000, 3000}[i % 3];
        MinMinScheduler.Result R = MinMinScheduler.schedule(L, M);

        boolean allAssigned = true; boolean[] seen = new boolean[L.length];
        for (int k = 0; k < L.length; k++) seen[R.order[k]] = true;
        for (boolean s : seen) allAssigned &= s;
        check("300 task tepat sekali masing-masing di order", allAssigned);

        double lowerBound = sum / 30000.0;   // 5 x (1000+2000+3000)
        check(String.format("makespan (%.2f) >= batas bawah teoritis (%.2f)", R.makespan(), lowerBound), R.makespan() >= lowerBound - 1e-9);

        boolean noOverlap = true;
        double[] lastFinish = new double[15];
        for (int k = 0; k < L.length; k++) {                  // menurut urutan penjadwalan
            int t = R.order[k], v = R.taskToVm[t];
            if (R.startTime[t] < lastFinish[v] - 1e-9) noOverlap = false;
            lastFinish[v] = R.finishTime[t];
        }
        check("Tidak ada task tumpang tindih di satu VM", noOverlap);

        MinMinScheduler.Result R2 = MinMinScheduler.schedule(L, M);
        check("Deterministik: dua kali jalan hasil sama", java.util.Arrays.equals(R.taskToVm, R2.taskToVm) && R.makespan() == R2.makespan());

        System.out.println(fail == 0 ? "\nSEMUA UJI LULUS" : "\nADA UJI GAGAL: " + fail);
        System.exit(fail == 0 ? 0 : 1);
    }
}
