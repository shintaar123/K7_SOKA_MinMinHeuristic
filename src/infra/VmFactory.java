package infra;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Vm;

import config.SimConfig;

/** Membuat 15 VM: id 0..14, pola Small-Medium-Large diulang 5x (slide 13). */
public final class VmFactory {
    private VmFactory() {}

    public static List<Vm> create(int brokerId) {
        List<Vm> vms = new ArrayList<Vm>();
        for (int id = 0; id < SimConfig.NUM_VM; id++) {
            int type = SimConfig.vmTypeIndex(id);
            vms.add(new Vm(
                id, brokerId,
                SimConfig.VM_TYPE_MIPS[type],
                SimConfig.VM_PES,
                SimConfig.VM_TYPE_RAM[type],
                SimConfig.VM_BW,
                SimConfig.VM_IMAGE_MB,
                "Xen",
                new CloudletSchedulerSpaceShared()));   // non-preemptive: 1 task jalan sampai selesai
        }
        return vms;
    }
}
