# Credit Approval Classification with Tribuo

A Java-based academic project exploring credit approval classification using decision tree algorithms. The project includes a CART implementation using Tribuo, an ID3 implementation, and a Java Swing application.

## Project Overview

This project demonstrates how machine learning can be applied to a categorical credit approval dataset. It explores decision tree construction, dataset preprocessing, target classification, and information-based decision making.

### Implementations

- **CART with Tribuo:** Uses the Tribuo machine learning library to build a classification model for the `Approved` target.
- **ID3:** Includes a separate decision tree implementation for educational analysis.
- **Java Swing GUI:** Includes application classes for a graphical user interface.

## Technologies

- Java
- Apache Maven
- Tribuo 4.3.2
- Eclipse IDE
- CSV datasets
- PlantUML

The Maven configuration currently specifies Java release 25. Use a compatible JDK to build the project.

## Project Structure

```text
CreditApprovalTribuo/
├── src/
│   ├── CARTCreditApproval.java
│   ├── CreditApprovalApp.java
│   ├── CreditApprovalGUI.java
│   ├── ID3.java
│   ├── Node.java
│   ├── NodeType.java
│   ├── Tree.java
│   └── TribuoTest.java
├── data/
│   ├── credit_approval_id3_dataset_v1.csv
│   └── credit_approval_numeric.csv
├── resources/
├── CreditApprovalClass.puml
├── CreditApprovalSequence.puml
├── pom.xml
└── README.md
```

## Dataset

The project contains CSV datasets under `data/`.

- `credit_approval_id3_dataset_v1.csv`: Dataset used for credit approval decision tree experimentation.
- `credit_approval_numeric.csv`: Numeric-formatted dataset generated for the Tribuo workflow.

The CART implementation uses `Approved` as the target label. Check the input path and expected column names in `CARTCreditApproval.java` before running the program.

## Getting Started

### Prerequisites

- A compatible JDK (Java release 25, as configured in `pom.xml`)
- Apache Maven
- Eclipse IDE or another Java IDE

### Open in Eclipse

1. Open Eclipse.
2. Import the project using **File → Import → Existing Maven Projects**.
3. Select the `CreditApprovalTribuo` project directory.
4. Allow Maven to resolve the dependencies.
5. Verify that the configured JDK is compatible with the Maven compiler release.

### Run the CART implementation

1. Open `src/CARTCreditApproval.java`.
2. Verify that the configured input dataset exists.
3. Run `CARTCreditApproval` as a Java Application.

### Run the main application

Open `src/CreditApprovalApp.java` and run it as a Java Application.

### Build with Maven

From the project root, run:

```bash
mvn clean compile
```

If Maven is not installed or is not available in the terminal, configure Maven or use Eclipse's Maven integration.

## Decision Tree Concepts

### CART

CART stands for Classification and Regression Trees. For classification, it recursively partitions data into groups based on split criteria and produces a tree used to predict a target class.

### ID3

ID3 constructs a decision tree by selecting attributes using Information Gain, which measures the reduction in entropy after a split.

The project provides an opportunity to compare decision tree approaches and their implementation workflows.

## UML Documentation

- `CreditApprovalClass.puml`: Class diagram.
- `CreditApprovalSequence.puml`: Sequence diagram.

These files document aspects of the application's structure and interaction flow.

## Limitations

This is an academic and experimental project. Results depend on the dataset, preprocessing, feature definitions, and model configuration. The included data should not be assumed to represent a real financial institution's credit policy.

The implementation has not necessarily been validated for real-world credit decisions. Practical deployment would require additional evaluation, fairness analysis, security controls, and regulatory review.

## Author

Yoga Arfiansyah Firdaus

Master of Information Systems  
Telkom University

---

For educational purposes only.
