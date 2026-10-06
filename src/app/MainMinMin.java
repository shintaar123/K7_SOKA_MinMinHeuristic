package app;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import config.SimConfig;
import data.GoCJLoader;
import infra.CloudletFactory;
import infra.DatacenterFactory;
import infra.VmFactory;
import metrics.MetricsCalculator;
import metrics.ResultWriter;
import metrics.Validator;
import scheduler.MinMinScheduler;

/** Program utama: baca dataset -> bangun datacenter -> Min-Min -> simulasi CloudSim -> metrik + CSV. */
public class MainMinMin {

    public static void main(String[] args) throws Exception {
        String datasetPath = args.length > 0 ? args[0] : SimConfig.DATASET_PATH;

        // 1) baca dataset (bisa dinamis dari file atau jumlah SimConfig jika default)
        double[] lengthMI = GoCJLoader.loadAll(datasetPath, SimConfig.MI_SCALE);
        int numTasks = lengthMI.length;

        // ---- (V10) cetak parameter di awal run, cocokkan dengan PPT ----
        System.out.println("=== PARAMETER RUN ===");
        System.out.println("Dataset     : " + datasetPath);
        System.out.println("Jumlah task : " + numTasks + " (skala MI = " + SimConfig.MI_SCALE + ")");
        System.out.println("Host        : " + SimConfig.NUM_HOST + "  PE=" + Arrays.toString(SimConfig.HOST_PES)
                + "  MIPS/PE=" + Arrays.toString(SimConfig.HOST_PE_MIPS) + "  RAM(MB)=" + Arrays.toString(SimConfig.HOST_RAM_MB));
        System.out.println("VM          : " + SimConfig.NUM_VM + "  MIPS=" + Arrays.toString(SimConfig.VM_TYPE_MIPS)
                + " (Small/Medium/Large)  RAM(MB)=" + Arrays.toString(SimConfig.VM_TYPE_RAM));
        System.out.println();

        String datasetInfo = (datasetPath.contains("Synthetic") ? "Synthetic | " : "GoCJ | ") + GoCJLoader.stats(lengthMI);
        System.out.println("Dataset OK  : " + datasetInfo);

        // 2) hitung penjadwalan Min-Min (kode murni, di luar CloudSim)
        double[] mips = SimConfig.vmMips();
        MinMinScheduler.Result r = MinMinScheduler.schedule(lengthMI, mips);
        System.out.printf("Min-Min OK  : prediksi makespan = %.2f detik%n", r.makespan());

        // 3) bangun simulasi CloudSim
        Log.setDisabled(!SimConfig.VERBOSE_CLOUDSIM);
        CloudSim.init(1, Calendar.getInstance(), false);            // 1 user (broker), tanpa trace event
        Datacenter dc = DatacenterFactory.create("Datacenter_0");
        DatacenterBroker broker = new DatacenterBroker("Broker_0");

        List<Vm> vms = VmFactory.create(broker.getId());            // id 0..14
        List<Cloudlet> cloudlets = CloudletFactory.create(broker.getId(), lengthMI, r.order); // urut sesuai order Min-Min

        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);

        // 4) ikat tiap cloudlet ke VM hasil Min-Min (WAJIB sebelum startSimulation)
        for (Cloudlet c : cloudlets) {
            broker.bindCloudletToVm(c.getCloudletId(), r.taskToVm[c.getCloudletId()]);
        }

        CloudSim.startSimulation();
        List<Cloudlet> finished = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();
        Log.setDisabled(false);

        // 5) metrik + validasi + tulis file
        MetricsCalculator m = new MetricsCalculator(finished, numTasks);
        Validator v = new Validator(m, r, lengthMI);
        // V8 (deterministik): jalankan penjadwal sekali lagi, hasil harus identik
        MinMinScheduler.Result r2 = MinMinScheduler.schedule(lengthMI, mips);
        boolean det = Arrays.equals(r.taskToVm, r2.taskToVm) && Arrays.equals(r.order, r2.order);
        v.report.append(det ? "[PASS] " : "[FAIL] ").append("V8  penjadwal deterministik (dua kali jalan identik)\n");
        if (!det) v.failed++;

        String summary = ResultWriter.buildSummary(datasetInfo, m, r, lengthMI, v);
        System.out.println();
        System.out.print(summary);
        ResultWriter.write(SimConfig.RESULT_DIR, summary, m, r, lengthMI);
        System.out.println("File tersimpan di folder: " + SimConfig.RESULT_DIR + "/");
        System.exit(v.failed == 0 ? 0 : 2);
    }
}
