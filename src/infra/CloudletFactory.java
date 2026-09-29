package infra;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

/**
 * Membuat 300 cloudlet. id cloudlet = indeks task. List DIISI menurut urutan hasil Min-Min (order),
 * karena broker mengirim cloudlet sesuai urutan list dan antrean tiap VM mengikuti urutan itu.
 */
public final class CloudletFactory {
    private CloudletFactory() {}

    public static List<Cloudlet> create(int brokerId, double[] lengthMI, int[] order) {
        List<Cloudlet> list = new ArrayList<Cloudlet>();
        UtilizationModel full = new UtilizationModelFull();
        for (int k = 0; k < order.length; k++) {
            int id = order[k];
            Cloudlet c = new Cloudlet(id, (long) Math.round(lengthMI[id]), 1, 300, 300, full, full, full);
            c.setUserId(brokerId);
            list.add(c);
        }
        return list;
    }
}
