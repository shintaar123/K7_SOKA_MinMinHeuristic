package scheduler;

/**
 * Algoritma Min-Min (batch, independent task, semua tiba di t=0). TANPA import CloudSim,
 * jadi bisa dites sendiri (SchedulerSelfTest) dan mudah dijelaskan saat demo.
 *
 * Aturan seri (tie-break): antar VM -> indeks VM terkecil; antar task -> indeks task terkecil.
 */
public final class MinMinScheduler {
    private MinMinScheduler() {}

    public static class Result {
        public int[]    taskToVm;     // taskToVm[i] = VM untuk task i
        public int[]    order;        // order[0] = task yang dijadwalkan pertama, dst.
        public double[] startTime;    // waktu mulai prediksi task i
        public double[] finishTime;   // completion time prediksi task i
        public double[] readyTime;    // ready time akhir tiap VM

        public double makespan() {
            double max = 0;
            for (double r : readyTime) max = Math.max(max, r);
            return max;
        }
    }

    public static Result schedule(double[] lengthMI, double[] mips) {
        int n = lengthMI.length, m = mips.length;
        Result res = new Result();
        res.taskToVm   = new int[n];
        res.order      = new int[n];
        res.startTime  = new double[n];
        res.finishTime = new double[n];
        res.readyTime  = new double[m];          // ready[j] = 0 untuk semua VM
        boolean[] done = new boolean[n];         // done[i]=true -> task i sudah keluar dari U

        for (int step = 0; step < n; step++) {
            int bestTask = -1, bestVm = -1;
            double bestCT = Double.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (done[i]) continue;

                // (1) untuk task i, cari VM dengan CT terkecil
                int vm = 0;
                double ct = res.readyTime[0] + lengthMI[i] / mips[0];
                for (int j = 1; j < m; j++) {
                    double c = res.readyTime[j] + lengthMI[i] / mips[j];
                    if (c < ct) { ct = c; vm = j; }        // "<" murni -> seri: VM indeks terkecil
                }
                // (2) di antara semua task, pilih yang CT minimumnya paling kecil
                if (ct < bestCT) { bestCT = ct; bestTask = i; bestVm = vm; } // seri: task indeks terkecil
            }

            // (3) jadwalkan, (4) update ready time VM, (5) keluarkan task dari U
            res.startTime[bestTask]  = res.readyTime[bestVm];
            res.finishTime[bestTask] = bestCT;
            res.readyTime[bestVm]    = bestCT;
            res.taskToVm[bestTask]   = bestVm;
            res.order[step]          = bestTask;
            done[bestTask]           = true;
        }
        return res;
    }
}
