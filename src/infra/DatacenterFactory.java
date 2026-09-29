package infra;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import config.SimConfig;

/** Membangun 1 datacenter berisi 5 host heterogen (slide 13). Panggil SETELAH CloudSim.init(). */
public final class DatacenterFactory {
    private DatacenterFactory() {}

    public static Datacenter create(String name) throws Exception {
        List<Host> hostList = new ArrayList<Host>();

        for (int h = 0; h < SimConfig.NUM_HOST; h++) {
            List<Pe> peList = new ArrayList<Pe>();
            for (int p = 0; p < SimConfig.HOST_PES[h]; p++) {
                peList.add(new Pe(p, new PeProvisionerSimple(SimConfig.HOST_PE_MIPS[h])));
            }
            hostList.add(new Host(
                h,                                                        // id host 0..4 (Host 1..5)
                new RamProvisionerSimple(SimConfig.HOST_RAM_MB[h]),
                new BwProvisionerSimple(SimConfig.HOST_BW),
                SimConfig.HOST_STORAGE,
                peList,
                new VmSchedulerTimeShared(peList)));
        }

        MappedVmAllocationPolicy.PLACEMENT.clear();
        DatacenterCharacteristics ch = new DatacenterCharacteristics(
            "x86", "Linux", "Xen", hostList,
            7.0,      // time zone (bebas)
            3.0,      // cost per detik  (tidak dipakai di metrik)
            0.05,     // cost per memori
            0.001,    // cost per storage
            0.0);     // cost per bandwidth

        return new Datacenter(name, ch, new MappedVmAllocationPolicy(hostList), new LinkedList<Storage>(), 0);
    }
}
