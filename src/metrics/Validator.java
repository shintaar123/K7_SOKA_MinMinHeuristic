package metrics;

import config.SimConfig;
import infra.MappedVmAllocationPolicy;
import scheduler.MinMinScheduler;

/** Validasi V2-V9 secara otomatis. (V1 = SchedulerSelfTest, V10 = cetak parameter di awal run.) */
public class Validator {
    public final StringBuilder report = new StringBuilder();
    public int failed = 0;

    private void check(String name, boolean ok, String detail) {
        report.append(ok ? "[PASS] " : "[FAIL] ").append(name);
        if (detail != null && !detail.isEmpty()) report.append("  -> ").append(detail);
        report.append("\n");
        if (!ok) failed++;
    }

    public Validator(MetricsCalculator m, MinMinScheduler.Result r, double[] lengthMI) {
        int n = m.n;
        // V2: semua cloudlet SUCCESS
        check("V2  " + n + " cloudlet berstatus SUCCESS", m.success == n, m.success + " / " + n);

        // V3: total task seluruh VM = n
        int sum = 0; for (int c : m.vmCount) sum += c;
        check("V3  jumlah task seluruh VM = " + n, sum == n, "total = " + sum);

        // V4: hasil CloudSim sama dengan prediksi Min-Min (makespan & waktu mulai per task)
        // CloudSim mengakumulasi floating point internal clock saat ribuan task dieksekusi beruntun.
        double tol = Math.max(0.5, n * 0.0003);    // detik; toleransi proporsional terhadap jumlah task
        double dMake = Math.abs(m.makespan - r.makespan());
        double maxDStart = 0;
        for (int i = 0; i < n; i++) if (!Double.isNaN(m.start[i])) maxDStart = Math.max(maxDStart, Math.abs(m.start[i] - r.startTime[i]));
        check("V4a makespan CloudSim ~ prediksi Min-Min", dMake <= tol,
              String.format("CloudSim=%.4f  prediksi=%.4f  selisih=%.4f dtk (toleransi %.2f)", m.makespan, r.makespan(), dMake, tol));
        check("V4b waktu mulai tiap task ~ prediksi (urutan submit benar)", maxDStart <= tol,
              String.format("selisih terbesar = %.4f dtk (toleransi %.2f)", maxDStart, tol));

        // V4c: VM yang dipakai cloudlet = VM hasil Min-Min
        boolean vmSame = true;
        for (int i = 0; i < n; i++) if (m.vmOf[i] != r.taskToVm[i]) vmSame = false;
        check("V4c setiap task jalan di VM hasil Min-Min", vmSame, null);

        // V5: makespan >= batas bawah teoritis
        double total = 0; for (double v : lengthMI) total += v;
        double totalMips = 0; for (double v : SimConfig.vmMips()) totalMips += v;
        double lb = total / totalMips;
        check("V5  makespan >= batas bawah teoritis", m.makespan >= lb - 1e-6,
              String.format("makespan=%.2f  batas bawah=%.2f", m.makespan, lb));

        // V6: utilisasi <= 100%
        check("V6  Resource Utilization <= 100%", m.utilization <= 100.0 + 1e-6, String.format("%.2f %%", m.utilization));

        // V7: tidak ada overlap di satu VM (urut waktu mulai)
        boolean noOverlap = true;
        for (int v = 0; v < SimConfig.NUM_VM && noOverlap; v++) {
            java.util.List<double[]> iv = new java.util.ArrayList<double[]>();
            for (int i = 0; i < n; i++) if (m.vmOf[i] == v && !Double.isNaN(m.start[i])) iv.add(new double[]{m.start[i], m.finish[i]});
            java.util.Collections.sort(iv, new java.util.Comparator<double[]>() {
                public int compare(double[] a, double[] b) { return Double.compare(a[0], b[0]); }
            });
            for (int k = 1; k < iv.size(); k++) if (iv.get(k)[0] < iv.get(k - 1)[1] - 1e-6) { noOverlap = false; break; }
        }
        check("V7  tidak ada task tumpang tindih di satu VM", noOverlap, null);

        // V9: penempatan VM -> Host sesuai slide 13
        boolean placeOk = MappedVmAllocationPolicy.PLACEMENT.size() == SimConfig.NUM_VM;
        for (int v = 0; v < SimConfig.NUM_VM && placeOk; v++) {
            Integer h = MappedVmAllocationPolicy.PLACEMENT.get(v);
            if (h == null || h != SimConfig.vmHostIndex(v) + 1) placeOk = false;
        }
        check("V9  VM 0-2 -> Host1, VM 3-5 -> Host2, ... VM 12-14 -> Host5", placeOk,
              "15 VM terpasang = " + MappedVmAllocationPolicy.PLACEMENT.size());
    }
}
