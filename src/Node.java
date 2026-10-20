
import java.util.ArrayList;
import java.util.List;

public class Node {
    private String data;
    private NodeType type;
    private Node parent;
    private final List<Node> children = new ArrayList<>();

    public Node(String data, NodeType type) {
        this.data = data;
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        if (child == null) {
            throw new IllegalArgumentException("Child tidak boleh null.");
        }
        child.parent = this;
        children.add(child);
    }

    public void removeChildren() {
        for (Node child : children) {
            child.parent = null;
        }
        children.clear();
    }

    public boolean isLeaf() {
        return type == NodeType.LEAFNODE;
    }
}
