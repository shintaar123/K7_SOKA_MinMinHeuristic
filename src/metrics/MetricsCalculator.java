package metrics;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

import config.SimConfig;
import infra.MappedVmAllocationPolicy;

/** Menghitung 4 metrik (Makespan, Resource Utilization, Avg Waiting Time, Throughput) + beban per VM. */
public class MetricsCalculator {

    public final int n;                   // jumlah task yang dikirim
    public int success;                   // jumlah cloudlet berstatus SUCCESS
    public double t0;                     // waktu submit awal di CloudSim (offset internal, biasanya 0.1 dtk)
    public double makespanRaw;            // max finish time apa adanya dari CloudSim
    public double makespan;               // makespanRaw - t0  (pembanding langsung dengan prediksi Min-Min)
    public double avgWaiting;
    public double throughput;
    public double utilization;            // persen

    // per cloudlet (indeks = cloudlet id = indeks task); NaN / -1 kalau cloudlet tidak selesai
    public final double[] start, finish, wait;
    public final int[] vmOf, hostOf, status;
    // per VM
    public final double[] vmBusy;
    public final int[] vmCount;

    public MetricsCalculator(List<Cloudlet> finished, int nTask) {
        this.n = nTask;
        start = new double[n]; finish = new double[n]; wait = new double[n];
        vmOf = new int[n]; hostOf = new int[n]; status = new int[n];
        vmBusy = new double[SimConfig.NUM_VM];
        vmCount = new int[SimConfig.NUM_VM];
        for (int i = 0; i < n; i++) { start[i] = Double.NaN; finish[i] = Double.NaN; wait[i] = Double.NaN; vmOf[i] = -1; hostOf[i] = -1; status[i] = -1; }

        t0 = Double.MAX_VALUE;
        for (Cloudlet c : finished) t0 = Math.min(t0, c.getSubmissionTime());
        if (finished.isEmpty()) t0 = 0;

        double sumWait = 0;
        makespanRaw = 0;
        for (Cloudlet c : finished) {
            int id = c.getCloudletId();
            status[id] = c.getCloudletStatus();
            vmOf[id]   = c.getVmId();
            Integer h  = MappedVmAllocationPolicy.PLACEMENT.get(c.getVmId());
            hostOf[id] = (h == null) ? -1 : h;
            if (c.getCloudletStatus() != Cloudlet.SUCCESS) continue;

            success++;
            start[id]  = c.getExecStartTime() - t0;
            finish[id] = c.getFinishTime() - t0;
            wait[id]   = c.getExecStartTime() - c.getSubmissionTime();
            makespanRaw = Math.max(makespanRaw, c.getFinishTime());
            sumWait += wait[id];
            vmBusy[c.getVmId()] += finish[id] - start[id];
            vmCount[c.getVmId()]++;
        }
        makespan   = makespanRaw - t0;
        avgWaiting = success == 0 ? 0 : sumWait / success;
        throughput = makespan <= 0 ? 0 : success / makespan;
        double busy = 0;
        for (double b : vmBusy) busy += b;
        utilization = makespan <= 0 ? 0 : busy / (makespan * SimConfig.NUM_VM) * 100.0;
    }

    public double vmUtilization(int vm) {
        return makespan <= 0 ? 0 : vmBusy[vm] / makespan * 100.0;
    }
}
