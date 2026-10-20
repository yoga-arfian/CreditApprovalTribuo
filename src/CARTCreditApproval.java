import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.Prediction;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.dtree.CARTClassificationTrainer;
import org.tribuo.data.csv.CSVLoader;

public class CARTCreditApproval {

    private static final String INPUT_FILE =
            "data/credit_approval_id3_dataset_v1.csv";

    private static final String TEMP_FILE =
            "data/credit_approval_numeric.csv";

    private static final String TARGET = "Approved";

    public static void main(String[] args) {
        try {
            Path input = Paths.get(INPUT_FILE);
            Path output = Paths.get(TEMP_FILE);

            if (!Files.exists(input)) {
                throw new IOException(
                    "Dataset tidak ditemukan: " + input.toAbsolutePath()
                );
            }

            List<String> headers;
            List<String[]> rows = new ArrayList<>();

            try (BufferedReader reader = Files.newBufferedReader(
                    input, StandardCharsets.UTF_8)) {

                String headerLine = reader.readLine();

                if (headerLine == null) {
                    throw new IOException("Dataset kosong.");
                }

                headers = parseCsvLine(headerLine);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        rows.add(parseCsvLine(line).toArray(new String[0]));
                    }
                }
            }

            int targetIndex = headers.indexOf(TARGET);

            if (targetIndex < 0) {
                throw new IOException(
                    "Kolom target Approved tidak ditemukan."
                );
            }

            for (String[] row : rows) {
                if (row.length != headers.size()) {
                    throw new IOException(
                        "Jumlah kolom tidak sesuai pada baris data."
                    );
                }
            }

            // Membuat fitur one-hot dari seluruh nilai kategorikal.
            Map<String, Integer> featureIndex = new LinkedHashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                if (i == targetIndex ||
                    headers.get(i).equalsIgnoreCase("ApplicantID")) {
                    continue;
                }

                for (String[] row : rows) {
                    String value = row[i].trim();
                    String featureName = headers.get(i) + "=" + value;
                    featureIndex.putIfAbsent(
                        featureName, featureIndex.size()
                    );
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    output, StandardCharsets.UTF_8)) {

                writer.write(String.join(",", featureIndex.keySet()));
                writer.write("," + TARGET);
                writer.newLine();

                for (String[] row : rows) {
                    int[] values = new int[featureIndex.size()];

                    for (int i = 0; i < headers.size(); i++) {
                        if (i == targetIndex ||
                            headers.get(i).equalsIgnoreCase("ApplicantID")) {
                            continue;
                        }

                        String featureName =
                                headers.get(i) + "=" + row[i].trim();

                        Integer index = featureIndex.get(featureName);
                        values[index] = 1;
                    }

                    for (int i = 0; i < values.length; i++) {
                        if (i > 0) writer.write(",");
                        writer.write(Integer.toString(values[i]));
                    }

                    writer.write("," + row[targetIndex].trim());
                    writer.newLine();
                }
            }

            // Membaca dataset hasil transformasi menggunakan Tribuo.

            CSVLoader<Label> loader =
                    new CSVLoader<>(new LabelFactory());

            MutableDataset<Label> dataset =
                    new MutableDataset<>(
                        loader.loadDataSource(output, TARGET)
                    );

            System.out.println("=== DATASET CREDIT APPROVAL ===");
            System.out.println("Jumlah record  : " + dataset.size());
            System.out.println("Jumlah fitur   : "
                    + dataset.getFeatureMap().size());
            System.out.println("Target         : " + TARGET);

            CARTClassificationTrainer trainer =
                    new CARTClassificationTrainer();

            Model<Label> model = trainer.train(dataset);

            System.out.println("\n=== MODEL CART BERHASIL DILATIH ===");
            System.out.println(model.toString());

            Prediction<Label> prediction =
                    model.predict(dataset.getExample(0));

            System.out.println("\n=== CONTOH PREDIKSI ===");
            System.out.println("Hasil prediksi : "
                    + prediction.getOutput());

            System.out.println(
                "\nCatatan: contoh prediksi menggunakan record pertama "
                + "dari data pelatihan."
            );

        } catch (Exception e) {
            System.err.println("Proses gagal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                result.add(value.toString());
                value.setLength(0);
            } else {
                value.append(c);
            }
        }

        result.add(value.toString());
        return result;
    }
}