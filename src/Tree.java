
public class Tree {
    private Node root;

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void print() {
        if (isEmpty()) {
            System.out.println("Decision tree masih kosong.");
            return;
        }

        printNode(root, "");
    }

    private void printNode(Node node, String indent) {
        System.out.println(indent + node.getData()
                + " [" + node.getType() + "]");

        for (Node child : node.getChildren()) {
            printNode(child, indent + "    ");
        }
    }
}
