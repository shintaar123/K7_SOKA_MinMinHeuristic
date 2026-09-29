package infra;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

import config.SimConfig;

/**
 * Menempatkan VM sesuai slide 13: VM 0-2 -> Host 1, VM 3-5 -> Host 2, dst. (vmId / 3 -> indeks host).
 * VmAllocationPolicySimple bawaan memilih host "paling longgar", jadi urutannya tidak sesuai gambar.
 */
public class MappedVmAllocationPolicy extends VmAllocationPolicySimple {

    /** vmId -> nomor host (1-based, sesuai slide). Diisi saat VM berhasil dibuat. */
    public static final Map<Integer, Integer> PLACEMENT = new TreeMap<Integer, Integer>();

    public MappedVmAllocationPolicy(List<? extends Host> list) {
        super(list);
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        List<Host> hosts = getHostList();
        Host target = hosts.get(SimConfig.vmHostIndex(vm.getId()));
        boolean ok = allocateHostForVm(vm, target);
        if (ok) PLACEMENT.put(vm.getId(), target.getId() + 1);
        return ok;
    }
}
