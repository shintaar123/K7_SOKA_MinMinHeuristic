package config;

/**
 * SATU-SATUNYA tempat semua parameter simulasi (sumber kebenaran tunggal).
 * Nilai mengikuti PPT slide 12-14 Kelompok 7. Yang bertanda [USULAN] belum ada di PPT
 * -> samakan dulu dengan tugas minggu sebelumnya, lalu ubah di sini SAJA.
 */
public final class SimConfig {
    private SimConfig() {}

    // ---------- Workload (slide 12) ----------
    public static final String DATASET_PATH = "data/GoCJ_Dataset_300.txt";
    public static final int    NUM_TASK     = 300;
    public static final double MI_SCALE     = 1.0;   // jangan diubah tanpa dicatat di laporan

    // ---------- Datacenter (slide 13) ----------
    public static final int NUM_HOST     = 5;
    public static final int VMS_PER_HOST = 3;                       // Small, Medium, Large
    public static final int NUM_VM       = NUM_HOST * VMS_PER_HOST; // 15

    // Host 1..5 (indeks 0..4)
    public static final int[]  HOST_PES     = {4, 4, 6, 6, 8};
    public static final int[]  HOST_PE_MIPS = {3000, 3000, 3500, 3500, 4000};
    public static final int[]  HOST_RAM_MB  = {16384, 16384, 24576, 24576, 32768}; // [USULAN pemetaan] 16/24/32 GB
    public static final long   HOST_BW      = 10000;      // [USULAN]
    public static final long   HOST_STORAGE = 1000000;    // MB [USULAN]

    // ---------- VM (slide 13): 0=Small, 1=Medium, 2=Large ----------
    public static final String[] VM_TYPE_NAME = {"Small", "Medium", "Large"};
    public static final int[]    VM_TYPE_MIPS = {1000, 2000, 3000};
    public static final int[]    VM_TYPE_RAM  = {1024, 2048, 4096};  // MB
    public static final int      VM_PES       = 1;
    public static final long     VM_IMAGE_MB  = 10000;   // [USULAN]
    public static final long     VM_BW        = 1000;    // [USULAN]

    // ---------- Output ----------
    public static final String RESULT_DIR = "results";
    /** true = tampilkan log internal CloudSim (sangat panjang untuk 300 task). */
    public static final boolean VERBOSE_CLOUDSIM = false;

    // ---------- Helper (id VM 0..14: Host1 = VM0-2, Host2 = VM3-5, dst.) ----------
    public static int vmHostIndex(int vmId) { return vmId / VMS_PER_HOST; }
    public static int vmTypeIndex(int vmId) { return vmId % VMS_PER_HOST; }
    public static String vmTypeName(int vmId) { return VM_TYPE_NAME[vmTypeIndex(vmId)]; }
    public static double vmMipsOf(int vmId) { return VM_TYPE_MIPS[vmTypeIndex(vmId)]; }

    /** MIPS ke-15 VM, urut sesuai id VM. */
    public static double[] vmMips() {
        double[] m = new double[NUM_VM];
        for (int i = 0; i < NUM_VM; i++) m[i] = vmMipsOf(i);
        return m;
    }
}
