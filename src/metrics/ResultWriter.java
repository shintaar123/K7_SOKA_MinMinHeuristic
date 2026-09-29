package metrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import config.SimConfig;
import infra.MappedVmAllocationPolicy;
import scheduler.MinMinScheduler;

/** Menulis ringkasan terminal + 3 file CSV + console_output.txt ke folder results/. */
public final class ResultWriter {
    private ResultWriter() {}

    public static String buildSummary(String datasetInfo, MetricsCalculator m, MinMinScheduler.Result r, double[] lengthMI, Validator v) {
        double total = 0; for (double x : lengthMI) total += x;
        double lb = total / 30000.0;
        StringBuilder s = new StringBuilder();
        String L = "%s%n";
        s.append("================= HASIL SIMULASI MIN-MIN =================\n");
        s.append("Dataset        : ").append(datasetInfo).append("\n");
        s.append(String.format(Locale.US, "Datacenter     : 1 datacenter, %d host, %d VM (Small/Medium/Large x%d)%n", SimConfig.NUM_HOST, SimConfig.NUM_VM, SimConfig.NUM_HOST));
        s.append("Algoritma      : Min-Min (batch, independent, non-preemptive)\n\n");

        s.append("--- Penempatan VM ---\n");
        for (int i = 0; i < SimConfig.NUM_VM; i++) {
            s.append(String.format(Locale.US, "VM %2d (%-6s %4.0f MIPS) -> Host %s%n", i, SimConfig.vmTypeName(i) + ",", SimConfig.vmMipsOf(i),
                     MappedVmAllocationPolicy.PLACEMENT.get(i)));
        }
        s.append("\n--- Metrik ---\n");
        s.append(String.format(Locale.US, "Makespan (simulasi)       : %.2f detik%n", m.makespan));
        s.append(String.format(Locale.US, "Makespan (prediksi MinMin): %.2f detik%n", r.makespan()));
        s.append(String.format(Locale.US, "Batas bawah teoritis      : %.2f detik%n", lb));
        s.append(String.format(Locale.US, "Resource Utilization      : %.2f %%%n", m.utilization));
        s.append(String.format(Locale.US, "Average Waiting Time      : %.2f detik%n", m.avgWaiting));
        s.append(String.format(Locale.US, "Throughput                : %.4f task/detik%n", m.throughput));
        s.append(String.format(Locale.US, "Task sukses               : %d / %d%n%n", m.success, m.n));

        s.append("--- Beban per VM ---\n");
        s.append("VM | tipe   | MIPS | jumlah task | busy time (s) | utilisasi VM (%)\n");
        for (int i = 0; i < SimConfig.NUM_VM; i++) {
            s.append(String.format(Locale.US, "%2d | %-6s | %4.0f | %11d | %13.2f | %8.2f%n", i, SimConfig.vmTypeName(i), SimConfig.vmMipsOf(i),
                     m.vmCount[i], m.vmBusy[i], m.vmUtilization(i)));
        }
        s.append("\n--- Validasi otomatis ---\n").append(v.report);
        s.append(v.failed == 0 ? "SEMUA VALIDASI LULUS\n" : "ADA VALIDASI GAGAL: " + v.failed + "\n");
        s.append("==========================================================\n");
        return s.toString();
    }

    public static void write(String dir, String summaryText, MetricsCalculator m, MinMinScheduler.Result r, double[] lengthMI) throws IOException {
        new File(dir).mkdirs();
        PrintWriter w;

        w = new PrintWriter(new FileWriter(new File(dir, "console_output.txt")));
        w.print(summaryText); w.close();

        w = new PrintWriter(new FileWriter(new File(dir, "summary.csv")));
        w.println("metrik,nilai");
        w.println(String.format(Locale.US, "makespan_simulasi_detik,%.4f", m.makespan));
        w.println(String.format(Locale.US, "makespan_prediksi_minmin_detik,%.4f", r.makespan()));
        w.println(String.format(Locale.US, "resource_utilization_persen,%.4f", m.utilization));
        w.println(String.format(Locale.US, "average_waiting_time_detik,%.4f", m.avgWaiting));
        w.println(String.format(Locale.US, "throughput_task_per_detik,%.6f", m.throughput));
        w.println("task_sukses," + m.success);
        w.close();

        // urutan_dijadwalkan: posisi task di r.order (1 = dijadwalkan pertama)
        int[] rank = new int[m.n];
        for (int k = 0; k < r.order.length; k++) rank[r.order[k]] = k + 1;
        w = new PrintWriter(new FileWriter(new File(dir, "cloudlet_result.csv")));
        w.println("cloudlet_id,panjang_MI,urutan_dijadwalkan,vm_id,host_id,waktu_mulai,waktu_selesai,waiting_time,status");
        for (int i = 0; i < m.n; i++) {
            w.println(String.format(Locale.US, "%d,%.0f,%d,%d,%d,%.4f,%.4f,%.4f,%s", i, lengthMI[i], rank[i], m.vmOf[i], m.hostOf[i],
                      m.start[i], m.finish[i], m.wait[i], m.status[i] == org.cloudbus.cloudsim.Cloudlet.SUCCESS ? "SUCCESS" : "GAGAL"));
        }
        w.close();

        w = new PrintWriter(new FileWriter(new File(dir, "vm_load.csv")));
        w.println("vm_id,tipe,MIPS,host_id,jumlah_task,busy_time,utilisasi_vm");
        for (int i = 0; i < SimConfig.NUM_VM; i++) {
            w.println(String.format(Locale.US, "%d,%s,%.0f,%d,%d,%.4f,%.4f", i, SimConfig.vmTypeName(i), SimConfig.vmMipsOf(i),
                      SimConfig.vmHostIndex(i) + 1, m.vmCount[i], m.vmBusy[i], m.vmUtilization(i)));
        }
        w.close();
    }
}
