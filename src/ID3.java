
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ID3 {

    private static final String TARGET = "Approved";
    private static final String POSITIVE = "Yes";
    private static final String NEGATIVE = "No";

    private final List<String> attributes = new ArrayList<>();
    private final List<Map<String, String>> trainingData = new ArrayList<>();
    private final Map<Node, String> fallbackClasses = new HashMap<>();

    private Tree tree;
    private String majorityClass;

    // Membaca dataset CSV.
    public void loadCsv(String filePath) throws IOException {
        attributes.clear();
        trainingData.clear();

        Path path = Paths.get(filePath);

        try (BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {

            String headerLine = reader.readLine();

            if (headerLine == null) {
                throw new IOException("Dataset kosong.");
            }

            List<String> headers = parseCsvLine(headerLine);

            // Menghapus BOM jika CSV berasal dari aplikasi tertentu.
            if (!headers.isEmpty()) {
                headers.set(0, headers.get(0).replace("\uFEFF", ""));
            }

            if (!headers.contains(TARGET)) {
                throw new IOException(
                        "Kolom target '" + TARGET + "' tidak ditemukan.");
            }

            // ApplicantID sengaja dikecualikan.
            for (String header : headers) {
                if (!header.equals("ApplicantID")
                        && !header.equals(TARGET)) {
                    attributes.add(header);
                }
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> values = parseCsvLine(line);

                if (values.size() != headers.size()) {
                    throw new IOException(
                            "Jumlah kolom tidak sesuai pada baris "
                            + lineNumber);
                }

                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), values.get(i).trim());
                }

                String label = row.get(TARGET);

                if (!POSITIVE.equals(label) && !NEGATIVE.equals(label)) {
                    throw new IOException(
                            "Label Approved harus Yes atau No pada baris "
                            + lineNumber);
                }

                for (String attribute : attributes) {
                    String value = row.get(attribute);

                    if (value == null || value.isEmpty()) {
                        throw new IOException(
                                "Nilai atribut kosong pada baris "
                                + lineNumber + ", atribut " + attribute);
                    }
                }

                trainingData.add(row);
            }
        }

        if (trainingData.isEmpty()) {
            throw new IOException("Tidak ada data training.");
        }
    }

    // Parser CSV sederhana yang mendukung field di dalam tanda kutip.
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        result.add(field.toString());
        return result;
    }

    // Memulai training ID3.
    public void train() {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException(
                    "Load dataset terlebih dahulu.");
        }

        fallbackClasses.clear();
        tree = new Tree();
        majorityClass = majorityLabel(trainingData);

        Node root = buildTree(trainingData,
                new ArrayList<>(attributes), true);

        tree.setRoot(root);
    }

    // Membentuk decision tree secara rekursif.
    private Node buildTree(
            List<Map<String, String>> rows,
            List<String> remainingAttributes,
            boolean isRoot) {

        String firstLabel = rows.get(0).get(TARGET);
        boolean allSame = true;

        for (Map<String, String> row : rows) {
            if (!firstLabel.equals(row.get(TARGET))) {
                allSame = false;
                break;
            }
        }

        // Basis rekursi 1: seluruh label sama.
        if (allSame) {
            return new Node(firstLabel, NodeType.LEAFNODE);
        }

        String currentMajority = majorityLabel(rows);

        // Basis rekursi 2: atribut sudah habis.
        if (remainingAttributes.isEmpty()) {
            return new Node(currentMajority, NodeType.LEAFNODE);
        }

        String bestAttribute = chooseBestAttribute(
                rows, remainingAttributes);

        // Jika tidak ada information gain positif, gunakan mayoritas.
        if (bestAttribute == null) {
            return new Node(currentMajority, NodeType.LEAFNODE);
        }

        Node node = new Node(
                bestAttribute,
                isRoot ? NodeType.ROOTNODE : NodeType.BRANCH);

        // Fallback untuk nilai atribut yang tidak ditemukan saat training.
        fallbackClasses.put(node, currentMajority);

        Set<String> values = new LinkedHashSet<>();

        for (Map<String, String> row : rows) {
            values.add(row.get(bestAttribute));
        }

        List<String> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);

        List<String> nextAttributes =
                new ArrayList<>(remainingAttributes);
        nextAttributes.remove(bestAttribute);

        for (String value : sortedValues) {
            List<Map<String, String>> subset =
                    filterByAttribute(rows, bestAttribute, value);

            Node branch = new Node(
                    bestAttribute + " = " + value,
                    NodeType.BRANCH);

            node.addChild(branch);

            Node child = buildTree(
                    subset, nextAttributes, false);

            branch.addChild(child);
        }

        return node;
    }

    // Memilih atribut dengan information gain terbesar.
    private String chooseBestAttribute(
            List<Map<String, String>> rows,
            List<String> candidateAttributes) {

        double baseEntropy = entropy(rows);
        double bestGain = 0.0;
        String bestAttribute = null;

        for (String attribute : candidateAttributes) {
            double gain = informationGain(rows, attribute, baseEntropy);

            if (gain > bestGain + 1e-12) {
                bestGain = gain;
                bestAttribute = attribute;
            }
        }

        return bestAttribute;
    }

    // Information Gain = entropy awal - weighted entropy setelah split.
    public double informationGain(
            List<Map<String, String>> rows,
            String attribute) {

        return informationGain(rows, attribute, entropy(rows));
    }

    private double informationGain(
            List<Map<String, String>> rows,
            String attribute,
            double baseEntropy) {

        Map<String, List<Map<String, String>>> subsets =
                new LinkedHashMap<>();

        for (Map<String, String> row : rows) {
            String value = row.get(attribute);

            subsets.computeIfAbsent(
                    value, k -> new ArrayList<>()).add(row);
        }

        double weightedEntropy = 0.0;

        for (List<Map<String, String>> subset : subsets.values()) {
            double weight = (double) subset.size() / rows.size();

            weightedEntropy += weight * entropy(subset);
        }

        return baseEntropy - weightedEntropy;
    }

    // Menghitung entropy berdasarkan distribusi Yes dan No.
    public double entropy(List<Map<String, String>> rows) {
        if (rows.isEmpty()) {
            return 0.0;
        }

        int yes = 0;
        int no = 0;

        for (Map<String, String> row : rows) {
            if (POSITIVE.equals(row.get(TARGET))) {
                yes++;
            } else if (NEGATIVE.equals(row.get(TARGET))) {
                no++;
            }
        }

        double pYes = (double) yes / rows.size();
        double pNo = (double) no / rows.size();

        return entropyTerm(pYes) + entropyTerm(pNo);
    }

    private double entropyTerm(double probability) {
        if (probability <= 0.0) {
            return 0.0;
        }

        return -probability * (Math.log(probability) / Math.log(2));
    }

    private List<Map<String, String>> filterByAttribute(
            List<Map<String, String>> rows,
            String attribute,
            String value) {

        List<Map<String, String>> subset = new ArrayList<>();

        for (Map<String, String> row : rows) {
            if (value.equals(row.get(attribute))) {
                subset.add(row);
            }
        }

        return subset;
    }

    private String majorityLabel(List<Map<String, String>> rows) {
        int yes = 0;
        int no = 0;

        for (Map<String, String> row : rows) {
            if (POSITIVE.equals(row.get(TARGET))) {
                yes++;
            } else if (NEGATIVE.equals(row.get(TARGET))) {
                no++;
            }
        }

        // Jika seri, pilih No secara deterministik.
        return yes > no ? POSITIVE : NEGATIVE;
    }

    // Menghasilkan prediksi dari input berbentuk Map atribut -> nilai.
    public String predict(Map<String, String> input) {
        if (tree == null || tree.isEmpty()) {
            throw new IllegalStateException(
                    "Model belum dilatih. Panggil train() dahulu.");
        }

        Node current = tree.getRoot();

        while (!current.isLeaf()) {
            String attribute = current.getData();
            String value = input.get(attribute);

            if (value == null) {
                return fallbackClasses.getOrDefault(
                        current, majorityClass);
            }

            Node matchedBranch = null;

            for (Node branch : current.getChildren()) {
                String prefix = attribute + " = ";

                if (branch.getData().startsWith(prefix)
                        && branch.getData().substring(prefix.length())
                        .equals(value)) {
                    matchedBranch = branch;
                    break;
                }
            }

            // Nilai yang tidak pernah ditemukan saat training.
            if (matchedBranch == null
                    || matchedBranch.getChildren().isEmpty()) {
                return fallbackClasses.getOrDefault(
                        current, majorityClass);
            }

            current = matchedBranch.getChildren().get(0);
        }

        return current.getData();
    }

    // Mengembalikan jalur keputusan untuk ditampilkan di GUI.
    public List<String> getDecisionPath(Map<String, String> input) {
        List<String> path = new ArrayList<>();

        if (tree == null || tree.isEmpty()) {
            path.add("Model belum dilatih.");
            return path;
        }

        Node current = tree.getRoot();

        while (!current.isLeaf()) {
            String attribute = current.getData();
            String value = input.get(attribute);

            if (value == null) {
                path.add("Atribut " + attribute + " tidak diberikan.");
                path.add("Fallback: " + fallbackClasses.getOrDefault(
                        current, majorityClass));
                return path;
            }

            String branchLabel = attribute + " = " + value;
            Node matchedBranch = null;

            for (Node branch : current.getChildren()) {
                if (branch.getData().equals(branchLabel)) {
                    matchedBranch = branch;
                    break;
                }
            }

            if (matchedBranch == null
                    || matchedBranch.getChildren().isEmpty()) {
                path.add(branchLabel + " (tidak ditemukan di training)");
                path.add("Fallback: " + fallbackClasses.getOrDefault(
                        current, majorityClass));
                return path;
            }

            path.add(branchLabel);
            current = matchedBranch.getChildren().get(0);
        }

        path.add("Prediksi akhir: " + current.getData());
        return path;
    }

    public Tree getTree() {
        return tree;
    }

    public List<String> getAttributes() {
        return new ArrayList<>(attributes);
    }

    public List<Map<String, String>> getTrainingData() {
        return new ArrayList<>(trainingData);
    }

    public String getMajorityClass() {
        return majorityClass;
    }

    public int getTrainingSize() {
        return trainingData.size();
    }
}
