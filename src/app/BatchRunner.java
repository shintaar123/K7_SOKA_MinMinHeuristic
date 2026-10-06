package app;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
import scheduler.MinMinScheduler;

/**
 * Menjalankan rangkaian simulasi Min-Min secara batch (banyak file dataset)
 * dan mencatat hasilnya ke file CSV.
 *
 * Contoh penggunaan:
 *   java -cp "out:lib/cloudsim-3.0.3.jar" app.BatchRunner gocj results/batch_gocj.csv data/GoCJ_Dataset_100.txt data/GoCJ_Dataset_200.txt ...
 *   java -cp "out:lib/cloudsim-3.0.3.jar" app.BatchRunner synthetic results/batch_synthetic.csv data/Synthetic_1000.txt ...
 */
public class BatchRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java app.BatchRunner <type_label> <output_csv> <dataset1> [dataset2 ...]");
            System.exit(1);
        }

        String typeLabel = args[0];
        String outputCsv = args[1];
        List<String> datasetFiles = new ArrayList<String>();
        for (int i = 2; i < args.length; i++) {
            datasetFiles.add(args[i]);
        }

        File outFile = new File(outputCsv);
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }

        System.out.println("==================================================");
        System.out.printf("Memulai Batch Run [%s]: %d dataset%n", typeLabel, datasetFiles.size());
        System.out.printf("Output CSV: %s%n", outputCsv);
        System.out.println("==================================================");

        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        pw.println("dataset_type,dataset_file,jumlah_task,makespan_simulasi_detik,makespan_prediksi_detik,avg_waiting_time_detik,resource_utilization_persen,throughput_task_per_detik");

        // Supaya log CloudSim tidak mengotori output batch
        Log.setDisabled(true);

        for (String datasetPath : datasetFiles) {
            File f = new File(datasetPath);
            if (!f.exists()) {
                System.err.println("[SKIP] File tidak ditemukan: " + datasetPath);
                continue;
            }

            double[] lengthMI = GoCJLoader.loadAll(datasetPath, SimConfig.MI_SCALE);
            int numTasks = lengthMI.length;
            double[] mips = SimConfig.vmMips();

            // 1. Min-Min Scheduler
            MinMinScheduler.Result r = MinMinScheduler.schedule(lengthMI, mips);

            // 2. CloudSim init & simulation
            CloudSim.init(1, Calendar.getInstance(), false);
            Datacenter dc = DatacenterFactory.create("Datacenter_" + numTasks);
            DatacenterBroker broker = new DatacenterBroker("Broker_" + numTasks);

            List<Vm> vms = VmFactory.create(broker.getId());
            List<Cloudlet> cloudlets = CloudletFactory.create(broker.getId(), lengthMI, r.order);

            broker.submitVmList(vms);
            broker.submitCloudletList(cloudlets);

            for (Cloudlet c : cloudlets) {
                broker.bindCloudletToVm(c.getCloudletId(), r.taskToVm[c.getCloudletId()]);
            }

            CloudSim.startSimulation();
            List<Cloudlet> finished = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            MetricsCalculator m = new MetricsCalculator(finished, numTasks);

            System.out.printf(Locale.US, "[DONE] %-25s | Task: %5d | Makespan: %10.2f s | Util: %6.2f %% | Wait: %8.2f s%n",
                    f.getName(), numTasks, m.makespan, m.utilization, m.avgWaiting);

            pw.println(String.format(Locale.US, "%s,%s,%d,%.4f,%.4f,%.4f,%.4f,%.6f",
                    typeLabel, f.getName(), numTasks, m.makespan, r.makespan(), m.avgWaiting, m.utilization, m.throughput));
            pw.flush();
        }

        pw.close();
        System.out.println("==================================================");
        System.out.println("Batch selesai! Data tersimpan di: " + outputCsv);
        System.out.println("==================================================");
    }
}
