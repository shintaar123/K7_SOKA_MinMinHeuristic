package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Membaca dataset GoCJ: satu angka MI per baris. Mengambil N baris pertama. */
public final class GoCJLoader {
    private GoCJLoader() {}

    public static double[] loadAll(String path, double scale) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new IOException("Dataset tidak ditemukan: " + f.getAbsolutePath());
        }
        List<Double> vals = new ArrayList<Double>();
        BufferedReader br = new BufferedReader(new FileReader(f));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;
                line = line.split("[\\s,;]+")[0];
                double v = Double.parseDouble(line);
                if (v <= 0) throw new IOException("Panjang task harus > 0, ditemukan: " + v);
                vals.add(v * scale);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Ada baris bukan angka di " + path + ": " + e.getMessage());
        } finally {
            br.close();
        }
        if (vals.isEmpty()) {
            throw new IOException("Dataset kosong: " + path);
        }
        double[] out = new double[vals.size()];
        for (int i = 0; i < vals.size(); i++) out[i] = vals.get(i);
        return out;
    }

    public static double[] load(String path, int n, double scale) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new IOException("Dataset tidak ditemukan: " + f.getAbsolutePath()
                + "\n -> taruh file GoCJ di folder data/ (atau jalankan tools/make_dummy_gocj.py untuk data uji).");
        }
        List<Double> vals = new ArrayList<Double>();
        BufferedReader br = new BufferedReader(new FileReader(f));
        try {
            String line;
            while ((line = br.readLine()) != null && vals.size() < n) {
                line = line.replace("\uFEFF", "").trim();      // buang BOM & spasi
                if (line.isEmpty()) continue;
                line = line.split("[\\s,;]+")[0];                // ambil kolom pertama saja
                double v = Double.parseDouble(line);
                if (v <= 0) throw new IOException("Panjang task harus > 0, ditemukan: " + v);
                vals.add(v * scale);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Ada baris bukan angka di " + path + ": " + e.getMessage());
        } finally {
            br.close();
        }
        if (vals.size() < n) {
            throw new IOException("Dataset hanya punya " + vals.size() + " baris, butuh " + n);
        }
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = vals.get(i);
        return out;
    }

    public static String stats(double[] a) {
        double min = Double.MAX_VALUE, max = 0, sum = 0;
        for (double v : a) { min = Math.min(min, v); max = Math.max(max, v); sum += v; }
        return String.format("n=%d  min=%.0f  max=%.0f  rata-rata=%.1f  total=%.0f MI", a.length, min, max, sum / a.length, sum);
    }
}
