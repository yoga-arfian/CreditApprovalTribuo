
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class CreditApprovalGUI extends JFrame {

    private static final Color NAVY = new Color(35, 75, 115);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BACKGROUND = new Color(245, 247, 250);

    private final ID3 model;
    private final String csvPath;

    private final Map<String, JComboBox<String>> inputFields =
            new LinkedHashMap<>();

    private JLabel predictionLabel;
    private JLabel statusLabel;
    private JTextArea pathArea;
    private JTree decisionTree;
    private JTextArea analysisArea;
    private DefaultMutableTreeNode visualRoot;

    private final DecimalFormat decimalFormat =
            new DecimalFormat("0.0000");

    public CreditApprovalGUI(ID3 model, String csvPath) {
        this.model = model;
        this.csvPath = csvPath;

        setTitle("Credit Approval Prediction - ID3");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 750));
        setSize(1450, 900);
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(BACKGROUND);
        main.setBorder(new EmptyBorder(16, 16, 10, 16));

        main.add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createInputPanel(),
                createResultPanel()
        );

        split.setResizeWeight(0.47);
        split.setDividerLocation(0.47);
        split.setBorder(null);

        main.add(split, BorderLayout.CENTER);

        statusLabel = new JLabel("Dataset siap digunakan.");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(new EmptyBorder(4, 2, 0, 2));
        main.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Logo Telkom University
        JLabel logoLabel = new JLabel();
        logoLabel.setPreferredSize(new Dimension(110, 90));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        java.net.URL logoUrl = getClass().getResource(
            "/telkom_university.png"
        );

        if (logoUrl != null) {
            ImageIcon original = new ImageIcon(logoUrl);
            Image scaled = original.getImage().getScaledInstance(
                100, 80, Image.SCALE_SMOOTH
            );
            logoLabel.setIcon(new ImageIcon(scaled));
        } else {
            logoLabel.setText("TEL-U");
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        }

        // Judul aplikasi
        JLabel title = new JLabel("Credit Approval Prediction");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 29));

        JLabel subtitle = new JLabel(
            "Decision Support System | ID3 Decision Tree Algorithm"
        );
        subtitle.setForeground(new Color(220, 232, 245));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
            new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(subtitle);

        header.add(logoLabel, BorderLayout.WEST);
        header.add(textPanel, BorderLayout.CENTER);

        return header;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 226, 235)),
                new EmptyBorder(20, 20, 16, 20)
        ));

        JLabel heading = new JLabel("Applicant Information");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel description = new JLabel(
                "Pilih nilai untuk setiap atribut calon peminjam."
        );
        description.setForeground(Color.GRAY);
        description.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(7));
        headingPanel.add(description);

        panel.add(headingPanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        List<String> attributes = getPredictorAttributes();
        int row = 0;

        for (String attribute : attributes) {
            List<String> values = getAttributeValues(attribute);

            if (values.isEmpty()) {
                values = defaultValues(attribute);
            }

            JLabel label = new JLabel(formatAttributeName(attribute));
            label.setFont(new Font("SansSerif", Font.BOLD, 13));

            JComboBox<String> combo =
                    new JComboBox<>(values.toArray(new String[0]));
            combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
            combo.setPreferredSize(new Dimension(250, 34));
            combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            inputFields.put(attribute, combo);

            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = row++;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(7, 2, 3, 2);
            form.add(label, gc);

            gc.gridy = row++;
            gc.insets = new Insets(0, 2, 8, 2);
            form.add(combo, gc);
        }

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(Color.WHITE);
        panel.add(formScroll, BorderLayout.CENTER);

        JButton predictButton = new JButton("Predict Approval");
        predictButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        predictButton.setForeground(Color.WHITE);
        predictButton.setBackground(BLUE);
        predictButton.setOpaque(true);
        predictButton.setBorderPainted(false);
        predictButton.setFocusPainted(false);
        predictButton.setPreferredSize(new Dimension(180, 42));
        predictButton.addActionListener(e -> predict());

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        resetButton.setPreferredSize(new Dimension(100, 42));
        resetButton.addActionListener(e -> resetForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(predictButton);
        buttons.add(resetButton);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 226, 235)),
                new EmptyBorder(20, 20, 16, 20)
        ));

        JLabel heading = new JLabel("Prediction Result");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 12));
        center.setBackground(Color.WHITE);

        JPanel resultCard = new JPanel();
        resultCard.setLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));
        resultCard.setBackground(new Color(243, 246, 250));
        resultCard.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel modelLabel = new JLabel("MODEL PREDICTION");
        modelLabel.setForeground(Color.GRAY);
        modelLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        modelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        predictionLabel = new JLabel("AWAITING INPUT");
        predictionLabel.setFont(new Font("SansSerif", Font.BOLD, 29));
        predictionLabel.setForeground(new Color(90, 100, 115));
        predictionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        predictionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel resultDescription = new JLabel(
                "Hasil berdasarkan pohon keputusan ID3"
        );
        resultDescription.setForeground(Color.GRAY);
        resultDescription.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultDescription.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultCard.add(modelLabel);
        resultCard.add(Box.createVerticalStrut(14));
        resultCard.add(predictionLabel);
        resultCard.add(Box.createVerticalStrut(14));
        resultCard.add(resultDescription);

        JTabbedPane tabs = new JTabbedPane();

        pathArea = new JTextArea();
        pathArea.setEditable(false);
        pathArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        pathArea.setMargin(new Insets(10, 10, 10, 10));
        pathArea.setText(
                "Applicant prediction\n" +
                "====================\n\n" +
                "Pilih atribut calon peminjam, lalu tekan Predict Approval."
        );

        tabs.addTab("Decision Path", new JScrollPane(pathArea));

        visualRoot = new DefaultMutableTreeNode("Decision Tree");
        decisionTree = new JTree(visualRoot);
        decisionTree.setFont(new Font("SansSerif", Font.PLAIN, 13));
        decisionTree.setRootVisible(true);
        decisionTree.setShowsRootHandles(true);
        tabs.addTab("Decision Tree", new JScrollPane(decisionTree));

        analysisArea = new JTextArea();
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        analysisArea.setMargin(new Insets(12, 12, 12, 12));
        analysisArea.setBackground(new Color(248, 250, 252));
        tabs.addTab("Dataset & ID3 Analysis",
                new JScrollPane(analysisArea));

        refreshDecisionTree(null);
        refreshAnalysis();

        center.add(resultCard, BorderLayout.NORTH);
        center.add(tabs, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void predict() {
        try {
            Map<String, String> input = new LinkedHashMap<>();

            for (Map.Entry<String, JComboBox<String>> entry
                    : inputFields.entrySet()) {
                input.put(entry.getKey(),
                        String.valueOf(entry.getValue().getSelectedItem()));
            }

            String prediction = predictWithModel(input);

            if (prediction == null || prediction.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Model tidak menghasilkan prediksi."
                );
            }

            prediction = normalizePrediction(prediction);

            boolean approved = prediction.equalsIgnoreCase("Yes")
                    || prediction.equalsIgnoreCase("Approved");

            predictionLabel.setText(approved
                    ? "YES — APPROVED"
                    : "NO — REJECTED");

            predictionLabel.setForeground(approved
                    ? new Color(22, 101, 52)
                    : new Color(153, 27, 27));

            pathArea.setText(buildPredictionPath(input, prediction));
            pathArea.setCaretPosition(0);

            refreshDecisionTree(input);

            statusLabel.setText("Prediksi berhasil dihitung.");
            statusLabel.setForeground(new Color(22, 101, 52));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Prediksi gagal:\n" + ex.getMessage(),
                    "Prediction Error",
                    JOptionPane.ERROR_MESSAGE
            );
            statusLabel.setText("Prediksi gagal.");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void resetForm() {
        for (JComboBox<String> combo : inputFields.values()) {
            if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }
        }

        predictionLabel.setText("AWAITING INPUT");
        predictionLabel.setForeground(new Color(90, 100, 115));

        pathArea.setText(
                "Applicant prediction\n" +
                "====================\n\n" +
                "Pilih atribut calon peminjam, lalu tekan Predict Approval."
        );

        refreshDecisionTree(null);
        statusLabel.setText("Form berhasil di-reset.");
        statusLabel.setForeground(Color.GRAY);
    }

    /*
     * Mengambil atribut dari data training model.
     * ApplicantID dan Approved tidak digunakan sebagai input.
     */
    private List<String> getPredictorAttributes() {
        List<String> attributes = new ArrayList<>();

        try {
            Object trainingData = invokeNoArg(model, "getTrainingData");

            if (trainingData instanceof List<?>) {
                List<?> rows = (List<?>) trainingData;

                if (!rows.isEmpty() && rows.get(0) instanceof Map<?, ?>) {
                    for (Object key :
                            ((Map<?, ?>) rows.get(0)).keySet()) {
                        String name = String.valueOf(key);
                        if (!name.equalsIgnoreCase("Approved")
                                && !name.equalsIgnoreCase("ApplicantID")) {
                            attributes.add(name);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Fallback untuk versi ID3 yang tidak mengekspos training data.
        }

        if (attributes.isEmpty()) {
            attributes.addAll(Arrays.asList(
                    "AgeGroup", "Income", "Employment", "Debt",
                    "CreditHistory", "Collateral", "DownPayment",
                    "LoanPurpose", "ResidenceStatus", "EmploymentTenure"
            ));
        }

        return attributes;
    }

    private List<String> getAttributeValues(String attribute) {
        Set<String> values = new LinkedHashSet<>();

        try {
            Object trainingData = invokeNoArg(model, "getTrainingData");

            if (trainingData instanceof List<?>) {
                for (Object row : (List<?>) trainingData) {
                    if (row instanceof Map<?, ?>) {
                        Object value = ((Map<?, ?>) row).get(attribute);
                        if (value != null) {
							values.add(String.valueOf(value));
						}
                    }
                }
            }
        } catch (Exception ignored) {
            // Gunakan fallback di bawah.
        }

        return new ArrayList<>(values);
    }

    private List<String> defaultValues(String attribute) {
        Map<String, List<String>> defaults = new HashMap<>();

        defaults.put("AgeGroup",
                Arrays.asList("Young", "Adult", "Senior"));
        defaults.put("Income",
                Arrays.asList("Low", "Medium", "High"));
        defaults.put("Employment",
                Arrays.asList("Contract", "Permanent", "Self-employed"));
        defaults.put("Debt",
                Arrays.asList("Low", "Medium", "High"));
        defaults.put("CreditHistory",
                Arrays.asList("Poor", "Fair", "Good"));
        defaults.put("Collateral",
                Arrays.asList("None", "Car", "Motorcycle"));
        defaults.put("DownPayment",
                Arrays.asList("Low", "Medium", "High"));
        defaults.put("LoanPurpose",
                Arrays.asList("Personal", "Business", "Education"));
        defaults.put("ResidenceStatus",
                Arrays.asList("Rent", "Family", "Own"));
        defaults.put("EmploymentTenure",
                Arrays.asList("Short", "Medium", "Long"));

        return defaults.getOrDefault(attribute,
                Arrays.asList("Low", "Medium", "High"));
    }

    private String predictWithModel(Map<String, String> input)
            throws Exception {

        /*
         * Sesuaikan otomatis dengan beberapa kemungkinan nama method
         * yang umum digunakan pada implementasi ID3.
         */
        String[] candidates = {
                "predict", "classify", "prediction"
        };

        for (String methodName : candidates) {
            for (Method method : model.getClass().getMethods()) {
                if (!method.getName().equals(methodName)
                        || method.getParameterCount() != 1) {
                    continue;
                }

                Class<?> parameterType = method.getParameterTypes()[0];

                if (Map.class.isAssignableFrom(parameterType)) {
                    Object result = method.invoke(model, input);
                    if (result != null) {
						return String.valueOf(result);
					}
                }
            }
        }

        throw new NoSuchMethodException(
                "Method prediksi ID3 tidak ditemukan. " +
                "Periksa method predict() di ID3.java."
        );
    }

    private String normalizePrediction(String prediction) {
        String value = prediction.trim();

        if (value.equalsIgnoreCase("Yes")
                || value.equalsIgnoreCase("Approved")) {
            return "Yes";
        }

        if (value.equalsIgnoreCase("No")
                || value.equalsIgnoreCase("Rejected")
                || value.equalsIgnoreCase("Not Approved")) {
            return "No";
        }

        return value;
    }

    private String buildPredictionPath(
            Map<String, String> input,
            String prediction) {

        StringBuilder sb = new StringBuilder();

        sb.append("APPLICANT PREDICTION\n");
        sb.append("====================\n\n");

        sb.append("Prediction: ").append(prediction).append("\n\n");

        sb.append("INPUT ATTRIBUTES\n");
        sb.append("--------------------\n");

        for (Map.Entry<String, String> entry : input.entrySet()) {
            sb.append("- ")
              .append(formatAttributeName(entry.getKey()))
              .append(": ")
              .append(entry.getValue())
              .append("\n");
        }

        sb.append("\nDECISION PATH\n");
        sb.append("--------------------\n");

        List<String> path = model.getDecisionPath(input);

        for (int i = 0; i < path.size(); i++) {
            sb.append(i + 1)
              .append(". ")
              .append(path.get(i))
              .append("\n");
        }

        return sb.toString();
    }


    private void refreshDecisionTree(Map<String, String> input) {
        DefaultMutableTreeNode root = extractVisualTree();

        if (root == null) {
            root = new DefaultMutableTreeNode(
                    "Decision Tree (root tidak tersedia)"
            );
        }

        visualRoot = root;
        decisionTree.setModel(new DefaultTreeModel(visualRoot));

        for (int i = 0; i < decisionTree.getRowCount(); i++) {
            decisionTree.expandRow(i);
        }
    }

    private DefaultMutableTreeNode extractVisualTree() {
        try {
            Object tree = invokeNoArg(model, "getTree");
            if (tree == null) {
                tree = invokeNoArg(model, "getDecisionTree");
            }

            Object root = null;

            if (tree != null) {
                root = invokeNoArg(tree, "getRoot");
            }

            if (root == null) {
                root = invokeNoArg(model, "getRoot");
            }

            if (root == null) {
				return null;
			}

            return convertNode(root, new HashSet<>());
        } catch (Exception ex) {
            return null;
        }
    }

    private DefaultMutableTreeNode convertNode(
            Object node, Set<Object> visited) throws Exception {

        if (node == null || visited.contains(node)) {
            return new DefaultMutableTreeNode("...");
        }

        visited.add(node);

        Object data = invokeNoArg(node, "getData");
        String label = data == null ? "Node" : String.valueOf(data);

        DefaultMutableTreeNode visual =
                new DefaultMutableTreeNode(label);

        Object childrenObject = invokeNoArg(node, "getChildren");

        if (childrenObject instanceof Iterable<?>) {
            for (Object child : (Iterable<?>) childrenObject) {
                visual.add(convertNode(child, visited));
            }
        }

        return visual;
    }

    private void refreshAnalysis() {
        try {
            List<Map<String, String>> rows = loadCsvRows(csvPath);

            if (rows.isEmpty()) {
                analysisArea.setText("Dataset tidak ditemukan atau kosong.");
                return;
            }

            List<String> headers = new ArrayList<>(rows.get(0).keySet());
            List<String> predictors = new ArrayList<>(headers);
            predictors.remove("Approved");
            predictors.remove("ApplicantID");

            Map<String, Integer> distribution = new LinkedHashMap<>();

            for (Map<String, String> row : rows) {
                String target = row.get("Approved");
                distribution.put(target,
                        distribution.getOrDefault(target, 0) + 1);
            }

            double entropy = entropy(rows, "Approved");

            Map<String, Double> gains = new LinkedHashMap<>();
            for (String predictor : predictors) {
                gains.put(predictor,
                        informationGain(rows, predictor, "Approved"));
            }

            List<Map.Entry<String, Double>> sorted =
                    new ArrayList<>(gains.entrySet());

            sorted.sort((a, b) ->
                    Double.compare(b.getValue(), a.getValue()));

            StringBuilder sb = new StringBuilder();

            sb.append("DATASET SUMMARY\n");
            sb.append("========================================\n");
            sb.append("File: ").append(new File(csvPath).getName()).append("\n");
            sb.append("Records: ").append(rows.size()).append("\n");
            sb.append("Predictor attributes: ").append(predictors.size()).append("\n");
            sb.append("Target: Approved\n\n");

            sb.append("TARGET DISTRIBUTION\n");
            sb.append("========================================\n");

            for (Map.Entry<String, Integer> e : distribution.entrySet()) {
                double percent = 100.0 * e.getValue() / rows.size();

                sb.append(String.format(
                        Locale.US, "%-12s %4d (%5.1f%%)%n",
                        e.getKey(), e.getValue(), percent));
            }

            sb.append(String.format(
                    Locale.US, "%nTarget entropy: %.4f%n%n", entropy));

            sb.append("INFORMATION GAIN (ROOT LEVEL)\n");
            sb.append("========================================\n");

            for (Map.Entry<String, Double> e : sorted) {
                sb.append(String.format(
                        Locale.US, "%-20s %.4f%n",
                        e.getKey(), e.getValue()));
            }

            if (!sorted.isEmpty()) {
                sb.append("\nHighest gain: ")
                  .append(sorted.get(0).getKey())
                  .append(String.format(
                          Locale.US, " (%.4f)", sorted.get(0).getValue()))
                  .append("\n");
            }

            sb.append("\nNOTE\n");
            sb.append("========================================\n");
            sb.append("This is a synthetic academic dataset,\n");
            sb.append("not a real credit policy.\n");

            analysisArea.setText(sb.toString());
            analysisArea.setCaretPosition(0);

        } catch (Exception ex) {
            analysisArea.setText(
                    "Gagal membuat analisis dataset.\n\n" +
                    ex.getMessage()
            );
        }
    }

    private List<Map<String, String>> loadCsvRows(String filePath)
            throws IOException {

        List<Map<String, String>> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new FileReader(filePath))) {

            String line = br.readLine();
            if (line == null) {
				return rows;
			}

            List<String> headers = parseCsvLine(line);

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
					continue;
				}

                List<String> values = parseCsvLine(line);
                if (values.size() != headers.size()) {
					continue;
				}

                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), values.get(i).trim());
                }

                rows.add(row);
            }
        }

        return rows;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString().trim());
        return values;
    }

    private double entropy(List<Map<String, String>> rows, String target) {
        if (rows.isEmpty()) {
			return 0.0;
		}

        Map<String, Integer> counts = new HashMap<>();

        for (Map<String, String> row : rows) {
            String value = row.get(target);
            counts.put(value, counts.getOrDefault(value, 0) + 1);
        }

        double result = 0.0;

        for (int count : counts.values()) {
            double p = (double) count / rows.size();
            if (p > 0) {
				result -= p * (Math.log(p) / Math.log(2));
			}
        }

        return result;
    }

    private double informationGain(
            List<Map<String, String>> rows,
            String attribute,
            String target) {

        Map<String, List<Map<String, String>>> groups = new HashMap<>();

        for (Map<String, String> row : rows) {
            String value = row.get(attribute);
            groups.computeIfAbsent(value, k -> new ArrayList<>()).add(row);
        }

        double remainder = 0.0;

        for (List<Map<String, String>> group : groups.values()) {
            remainder += ((double) group.size() / rows.size())
                    * entropy(group, target);
        }

        return entropy(rows, target) - remainder;
    }

    private Object invokeNoArg(Object target, String methodName)
            throws Exception {

        if (target == null) {
			return null;
		}

        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private String formatAttributeName(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
			return attribute;
		}

        if ("DownPayment".equals(attribute)) {
			return "Down Payment";
		}
        if ("CreditHistory".equals(attribute)) {
			return "Credit History";
		}
        if ("LoanPurpose".equals(attribute)) {
			return "Loan Purpose";
		}
        if ("ResidenceStatus".equals(attribute)) {
			return "Residence Status";
		}
        if ("EmploymentTenure".equals(attribute)) {
			return "Employment Tenure";
		}
        if ("AgeGroup".equals(attribute)) {
			return "Age Group";
		}

        return attribute;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String csvPath = resolveCsvPath();

                ID3 model = new ID3();
                model.loadCsv(csvPath);
                model.train();

                CreditApprovalGUI gui =
                        new CreditApprovalGUI(model, csvPath);
                gui.setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Aplikasi gagal dijalankan:\n" + ex.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
    }

    private static String resolveCsvPath() {
        String[] candidates = {
                "credit_approval_id3_dataset_v1.csv",
                "CreditApprovalID3/credit_approval_id3_dataset_v1.csv"
        };

        for (String candidate : candidates) {
            if (Files.exists(Paths.get(candidate))) {
                return Paths.get(candidate).toAbsolutePath().toString();
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih Dataset Credit Approval CSV");

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }

        throw new IllegalStateException("Dataset CSV belum dipilih.");
    }
}
