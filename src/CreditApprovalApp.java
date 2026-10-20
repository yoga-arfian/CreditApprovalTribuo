
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CreditApprovalApp {

    private static final String CSV_PATH =
            "credit_approval_id3_dataset_v1.csv";

    public static void main(String[] args) {

        ID3 model = new ID3();

        try {
            // 1. Memuat dataset dan melatih model
            model.loadCsv(CSV_PATH);
            model.train();

            List<Map<String, String>> data =
                    model.getTrainingData();

            List<String> attributes =
                    model.getAttributes();

            System.out.println(
                    "\n========================================");
            System.out.println("   ID3 CREDIT APPROVAL - CONSOLE");
            System.out.println(
                    "========================================");

            // 2. Ringkasan dataset
            System.out.println("\n[1] RINGKASAN DATASET");
            System.out.println("---------------------");
            System.out.println("Jumlah record : " + data.size());
            System.out.println("Jumlah atribut: " + attributes.size());
            System.out.println("Daftar atribut: " + attributes);

            // 3. Distribusi target
            int yes = 0;
            int no = 0;

            for (Map<String, String> row : data) {
                if ("Yes".equals(row.get("Approved"))) {
                    yes++;
                } else if ("No".equals(row.get("Approved"))) {
                    no++;
                }
            }

            System.out.println("\n[2] DISTRIBUSI TARGET");
            System.out.println("--------------------");
            System.out.printf("Approved Yes: %d (%.2f%%)%n",
                    yes, 100.0 * yes / data.size());
            System.out.printf("Approved No : %d (%.2f%%)%n",
                    no, 100.0 * no / data.size());

            // 4. Sampel sepuluh baris
            System.out.println("\n[3] SAMPEL DATA (10 RECORD PERTAMA)");
            System.out.println("----------------------------------");

            for (int i = 0; i < Math.min(10, data.size()); i++) {
                System.out.println(
                        (i + 1) + ". " + data.get(i));
            }

            // 5. Information Gain pada root
            System.out.println("\n[4] INFORMATION GAIN ROOT");
            System.out.println("-------------------------");

            String rootAttribute = null;
            double highestGain = Double.NEGATIVE_INFINITY;

            for (String attribute : attributes) {
                double gain =
                        model.informationGain(data, attribute);

                System.out.printf("%-20s : %.6f%n",
                        attribute, gain);

                if (gain > highestGain) {
                    highestGain = gain;
                    rootAttribute = attribute;
                }
            }

            System.out.println("Atribut dengan gain tertinggi: "
                    + rootAttribute);

            // 6. Evaluasi sederhana pada data training
            int correct = 0;
            int falseYes = 0;
            int falseNo = 0;

            for (Map<String, String> row : data) {
                Map<String, String> input =
                        new LinkedHashMap<>();

                for (String attribute : attributes) {
                    input.put(attribute, row.get(attribute));
                }

                String actual = row.get("Approved");
                String predicted = model.predict(input);

                if (actual.equals(predicted)) {
                    correct++;
                }

                if ("Yes".equals(actual)
                        && "No".equals(predicted)) {
                    falseNo++;
                }

                if ("No".equals(actual)
                        && "Yes".equals(predicted)) {
                    falseYes++;
                }
            }

            System.out.println("\n[5] EVALUASI DATA TRAINING");
            System.out.println("--------------------------");
            System.out.printf("Prediksi benar : %d%n", correct);
            System.out.printf("Prediksi salah : %d%n",
                    data.size() - correct);
            System.out.printf("Akurasi training: %.2f%%%n",
                    100.0 * correct / data.size());

            System.out.println("\nConfusion Matrix:");
            System.out.println("Actual Yes, Predicted No : "
                    + falseNo);
            System.out.println("Actual No, Predicted Yes : "
                    + falseYes);

            System.out.println(
                    "Catatan: ini evaluasi pada data training, "
                    + "bukan data uji independen.");

            // 7. Prediksi profil pemohon dari input console
            try (Scanner scanner = new Scanner(System.in)) {

                boolean running = true;

                while (running) {
                    System.out.println("\n[6] MENU");
                    System.out.println("1. Prediksi pemohon baru");
                    System.out.println("2. Tampilkan pohon keputusan");
                    System.out.println("3. Keluar");
                    System.out.print("Pilihan: ");

                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "1": {
                            Map<String, String> applicant =
                                    new LinkedHashMap<>();

                            System.out.println(
                                    "\nMasukkan profil pemohon:");

                            for (String attribute : attributes) {
                                System.out.print(
                                        attribute + ": ");

                                applicant.put(
                                        attribute,
                                        scanner.nextLine().trim());
                            }

                            System.out.println(
                                    "\n=== HASIL PREDIKSI ===");

                            String prediction =
                                    model.predict(applicant);

                            System.out.println(
                                    "Approved: " + prediction);

                            System.out.println(
                                    "\n=== DECISION PATH ===");

                            for (String step :
                                    model.getDecisionPath(applicant)) {
                                System.out.println(" - " + step);
                            }

                            break;
                        }

                        case "2":
                            System.out.println(
                                    "\n=== POHON KEPUTUSAN ===");
                            model.getTree().print();
                            break;

                        case "3":
                            running = false;
                            System.out.println(
                                    "Aplikasi selesai.");
                            break;

                        default:
                            System.out.println(
                                    "Pilihan tidak valid.");
                    }
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "Gagal membaca dataset: " + e.getMessage());
            System.err.println(
                    "Pastikan CSV tersedia di working directory "
                    + "Eclipse.");
        } catch (Exception e) {
            System.err.println(
                    "Terjadi error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
