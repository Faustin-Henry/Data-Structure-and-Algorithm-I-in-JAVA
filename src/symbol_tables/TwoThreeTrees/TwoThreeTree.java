package symbol_tables.TwoThreeTrees;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * TwoThreeTree — a <em>balanced</em> search tree built from 2-nodes and 3-nodes.
 *
 * <ul>
 *   <li>A <b>2-node</b> holds 1 key and 2 children (like a BST node).</li>
 *   <li>A <b>3-node</b> holds 2 keys (k1 &lt; k2) and 3 children: everything in the
 *       left subtree &lt; k1, the middle between k1 and k2, the right &gt; k2.</li>
 *   <li><b>Perfect balance:</b> every null link is exactly the same distance from the
 *       root, so search/insert always cost O(log N).</li>
 * </ul>
 *
 * <p><b>Insertion</b> (the beautiful part): search for the key as usual. If it is missing,
 * insert it into the leaf where the search ended:</p>
 * <ul>
 *   <li>leaf was a 2-node → it becomes a 3-node. Done — the tree didn't grow.</li>
 *   <li>leaf was a 3-node → it temporarily holds 3 keys; <em>split</em> it into two
 *       2-nodes and pass the middle key up to the parent. If the parent was a 3-node,
 *       it splits too — splits cascade upward, and if the root splits the tree grows
 *       one level taller (still perfectly balanced).</li>
 * </ul>
 *
 * <p>This file implements insertion and search. Deletion in a 2-3 tree is fiddly; the
 * textbook replaces 2-3 trees with <em>left-leaning red-black trees</em>, which encode the
 * exact same structure as a BST with colored links — see RedBlackBST for the full
 * insert <b>and</b> delete.</p>
 */
public class TwoThreeTree<Key extends Comparable<Key>, Value> {

    private Node root;
    private int size;

    private class Node {
        int n;                                   // number of keys: 1 or 2 (3 during a split)
        @SuppressWarnings("unchecked")
        Key[] keys = (Key[]) new Comparable[3];
        @SuppressWarnings("unchecked")
        Value[] vals = (Value[]) new Object[3];
        // Node captures the outer class's type parameters, so `new Node[4]` is
        // rejected as generic array creation — build a real Node[] reflectively
        // (the array never escapes this class, so this stays type-safe).
        @SuppressWarnings("unchecked")
        Node[] children = (Node[]) java.lang.reflect.Array.newInstance(Node.class, 4);
        // children[0..n]; unused slots are null
        boolean isLeaf() { return children[0] == null; }
    }

    /** Carries a split upward: middle key plus the two new nodes it sits between. */
    private class Split {
        Key key; Value val; Node left, right;
        Split(Key key, Value val, Node left, Node right) {
            this.key = key; this.val = val; this.left = left; this.right = right;
        }
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    /** Perfect balance => every root-to-leaf path has the same length. */
    public int height() {
        int h = -1;
        for (Node x = root; x != null; x = x.children[0]) h++;
        return h;
    }

    // ------------------------------------------------------------------
    // Search — like a BST, but a 3-node has two keys to compare against
    // ------------------------------------------------------------------
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("get() with null key");
        Node x = root;
        while (x != null) {
            int i = 0;
            while (i < x.n && key.compareTo(x.keys[i]) > 0) i++;
            if (i < x.n && key.compareTo(x.keys[i]) == 0) return x.vals[i]; // hit
            x = x.children[i];                  // descend into the correct interval
        }
        return null;                            // miss
    }

    public boolean contains(Key key) { return get(key) != null; }

    // ------------------------------------------------------------------
    // Insert — returns the (possibly new) node, or a Split to pass upward
    // ------------------------------------------------------------------
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("put() with null key");
        if (val == null) throw new IllegalArgumentException("put() with null value");
        boolean existed = contains(key);
        Object result = insert(root, key, val);
        if (result instanceof TwoThreeTree.Split) {
            // The root itself split: build a new root — the tree grows one level.
            Split s = (Split) result;
            Node newRoot = new Node();
            newRoot.keys[0] = s.key; newRoot.vals[0] = s.val; newRoot.n = 1;
            newRoot.children[0] = s.left; newRoot.children[1] = s.right;
            root = newRoot;
        } else {
            root = (Node) result;
        }
        if (!existed) size++;
    }

    private Object insert(Node h, Key key, Value val) {
        if (h == null) {                        // empty spot: create a 2-node leaf
            Node t = new Node();
            t.keys[0] = key; t.vals[0] = val; t.n = 1;
            return t;
        }
        // Find the child interval i for this key (also detects duplicates).
        int i = 0;
        while (i < h.n && key.compareTo(h.keys[i]) > 0) i++;
        if (i < h.n && key.compareTo(h.keys[i]) == 0) {
            h.vals[i] = val;                    // duplicate key: update the value
            return h;
        }
        if (h.isLeaf()) {
            insertIntoNode(h, i, key, val, null, null);   // leaf absorbs the key
            if (h.n == 3) return split(h);       // overfull leaf -> split upward
            return h;
        }
        Object result = insert(h.children[i], key, val);
        if (result instanceof TwoThreeTree.Split) {
            Split s = (Split) result;
            insertIntoNode(h, i, s.key, s.val, s.left, s.right); // parent absorbs middle key
            if (h.n == 3) return split(h);       // cascade: parent splits too
        }
        return h;
    }

    /**
     * Insert key/val at position i of node h (keys stay sorted), splicing in the
     * two children produced by a split (null when inserting into a leaf).
     */
    private void insertIntoNode(Node h, int i, Key key, Value val, Node left, Node right) {
        for (int j = h.n; j > i; j--) {         // shift keys/children right to make room
            h.keys[j] = h.keys[j - 1];
            h.vals[j] = h.vals[j - 1];
            h.children[j + 1] = h.children[j];
        }
        h.keys[i] = key; h.vals[i] = val;
        h.children[i] = left; h.children[i + 1] = right;
        h.n++;
    }

    /** Split a 3-key node into two 2-nodes; the middle key travels upward. */
    private Split split(Node h) {
        Node left = new Node(), right = new Node();
        left.keys[0] = h.keys[0];  left.vals[0] = h.vals[0];  left.n = 1;
        right.keys[0] = h.keys[2]; right.vals[0] = h.vals[2]; right.n = 1;
        left.children[0]  = h.children[0]; left.children[1]  = h.children[1];
        right.children[0] = h.children[2]; right.children[1] = h.children[3];
        return new Split(h.keys[1], h.vals[1], left, right);
    }

    // ------------------------------------------------------------------
    // Pretty-printing: level-order, one line per level
    // ------------------------------------------------------------------
    private String nodeToString(Node x) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < x.n; i++) sb.append(x.keys[i]).append(i + 1 < x.n ? "," : "");
        return sb.append("]").toString();
    }

    public void printTree() {
        if (root == null) { System.out.println("(empty)"); return; }
        Queue<Node> q = new ArrayDeque<>();
        q.add(root);
        int level = 0;
        while (!q.isEmpty()) {
            int count = q.size();
            System.out.print("level " + level++ + ": ");
            for (int k = 0; k < count; k++) {
                Node x = q.remove();
                System.out.print(nodeToString(x) + " ");
                for (int i = 0; i <= x.n; i++)
                    if (x.children[i] != null) q.add(x.children[i]);
            }
            System.out.println();
        }
    }

    // ------------------------------------------------------------------
    // Demo
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== 2-3 tree: insertion with splits ===\n");

        TwoThreeTree<String, Integer> tree = new TwoThreeTree<>();
        // Insert in an order that forces several splits (Sedgewick's running example).
        String[] keys = { "S", "E", "A", "R", "C", "H", "X", "M", "P", "L" };
        for (int i = 0; i < keys.length; i++) {
            tree.put(keys[i], i);
            System.out.println("after put(" + keys[i] + "): height=" + tree.height()
                + ", size=" + tree.size());
        }

        System.out.println("\nFinal tree (level order; [a,b] is a 3-node):");
        tree.printTree();

        System.out.println("\nSearches:");
        System.out.println("  get(\"H\") = " + tree.get("H"));
        System.out.println("  get(\"L\") = " + tree.get("L"));
        System.out.println("  get(\"Z\") = " + tree.get("Z") + "  (miss -> null)");

        System.out.println("\nDuplicate put(\"E\", 99) updates the value, adds no node:");
        tree.put("E", 99);
        System.out.println("  get(\"E\") = " + tree.get("E") + ", size still " + tree.size());

        System.out.println("\nTakeaway: splits cascade upward but the tree only grows at");
        System.out.println("the root, so every leaf stays at the same depth — guaranteed");
        System.out.println("log N. Deletion is the messy part, which is why the textbook");
        System.out.println("implements this idea as a red-black BST instead (next file).");
    }
}
