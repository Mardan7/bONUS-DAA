// Each node stores one key and references to its children.
class Node {
    int value;
    Node left;
    Node right;

    Node(int value) {
        this.value = value;
    }
}

public class BST {
    private Node root;

    public void insert(int value) {
        if (root == null) {
            root = new Node(value);
            return;
        }

        Node current = root;
        while (true) {
            if (value == current.value) {
                return; // Do not store duplicate keys.
            } else if (value < current.value) {
                if (current.left == null) {
                    current.left = new Node(value);
                    return;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node(value);
                    return;
                }
                current = current.right;
            }
        }
    }

    public boolean search(int value) {
        Node current = root;
        while (current != null) {
            if (value == current.value) {
                return true;
            } else if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return false;
    }

    public void delete(int value) {
        Node parent = null;
        Node current = root;

        // Find the node and remember its parent.
        while (current != null && current.value != value) {
            parent = current;
            if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        if (current == null) {
            return; // Deleting a missing key changes nothing.
        }

        if (current.left != null && current.right != null) {
            // Replace the key with the smallest key in the right subtree.
            Node successorParent = current;
            Node successor = current.right;
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }
            current.value = successor.value;
            // The successor has at most one child, so remove it below.
            parent = successorParent;
            current = successor;
        }

        // For a leaf, child is null. Otherwise it is the only child.
        Node child = current.left;
        if (child == null) {
            child = current.right;
        }
        if (parent == null) {
            root = child;
        } else if (parent.left == current) {
            parent.left = child;
        } else {
            parent.right = child;
        }
    }
}
