package symbol_tables.RedBlackTrees;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * RedBlackBST — a <b>left-leaning red-black BST</b> that encodes a 2-3 tree as a BST.
 *
 * <p>The trick: a 3-node of a 2-3 tree is represented as <em>two BST nodes joined by a
 * red link</em> (leaning left). Red links "glue" 3-nodes together; black links connect
 * separate 2-3 nodes. Three invariants keep the encoding valid:</p>
 * <ul>
 *   <li>red links lean left (a right-leaning red link is rotated away),</li>
 *   <li>no node has two red links to children (that would be a 4-node),</li>
 *   <li>the tree has <b>perfect black balance</b>: every root-to-null path has the same
 *       number of black links (the 2-3 tree's perfect balance in disguise).</li>
 * </ul>
 *
 * <p><b>Insert</b> is ordinary BST insertion with a new node colored red, followed by
 * {@code balance}: rotate left-leaning reds, rotate away right reds, and flip colors to
 * split 4-nodes. <b>Delete</b> is the delicate part — Sedgewick's approach:</p>
 * <ul>
 *   <li>To delete the minimum: push a red link down the left spine with
 *       {@code moveRedLeft} (borrowing from the sibling so the current node is never a
 *       2-node), delete the min leaf, then rebalance on the way up.</li>
 *   <li>General delete: rotate the tree so the search goes left when possible
 *       ({@code moveRedRight} pushes red rightward), and when the key is found in an
 *       internal node, swap in its successor (min of right subtree) and delete that.</li>
 * </ul>
 *
 * <p>Guaranteed O(log N) for put/get/delete; height is at most ~2 log N. The {@code check()}
 * method in the demo verifies all three invariants after random operations.</p>
 */
public class RedBlackBST<Key extends Comparable<Key>, Value> {

    private static final boolean RED   = true;
    private static final boolean BLACK = false;

    private Node root;

    private class Node {
        Key key;
        Value val;
        Node left, right;
        boolean color;      // color of the link from parent to this node
        int size;           // subtree node count
        Node(Key key, Value val, boolean color, int size) {
            this.key = key; this.val = val; this.color = color; this.size = size;
        }
    }

    private boolean isRed(Node x) {
        if (x == null) return false;         // null links are black
        return x.color == RED;
    }

    private int size(Node x) { return x == null ? 0 : x.size; }
    public int size()        { return size(root); }
    public boolean isEmpty() { return root == null; }

    // ------------------------------------------------------------------
    // The three primitive transformations (see §3.4 diagrams)
    // ------------------------------------------------------------------

    /**
     * Rotate left: fixes a right-leaning red link. h's right child x (red) becomes
     * the subtree root; h becomes x's red left child. Preserves symmetric order
     * and subtree sizes; color of h moves up to x.
     */
    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        x.size = h.size;
        h.size = 1 + size(h.left) + size(h.right);
        return x;
    }

    /**
     * Rotate right: fixes two red links in a row leaning left. Mirror of rotateLeft.
     */
    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        x.size = h.size;
        h.size = 1 + size(h.left) + size(h.right);
        return x;
    }

    /**
     * Flip colors: splits a 4-node. h's two red child links become black and h's
     * own link becomes red — the 4-node's middle key is "passed up" to the parent.
     */
    private void flipColors(Node h) {
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    /** Restore LLRB invariants after an insertion or deletion in h's subtree. */
    private Node balance(Node h) {
        if (isRed(h.right) && !isRed(h.left))      h = rotateLeft(h);
        if (isRed(h.left)  && isRed(h.left.left))  h = rotateRight(h);
        if (isRed(h.left)  && isRed(h.right))       flipColors(h);
        h.size = 1 + size(h.left) + size(h.right);
        return h;
    }

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("get() with null key");
        Node x = root;
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if      (cmp < 0) x = x.left;
            else if (cmp > 0) x = x.right;
            else              return x.val;
        }
        return null;
    }

    public boolean contains(Key key) { return get(key) != null; }

    // ------------------------------------------------------------------
    // Insert
    // ------------------------------------------------------------------
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("put() with null key");
        if (val == null) { delete(key); return; }
        root = put(root, key, val);
        root.color = BLACK;                       // the root's link is always black
    }

    private Node put(Node h, Key key, Value val) {
        if (h == null) return new Node(key, val, RED, 1);   // new nodes start red
        int cmp = key.compareTo(h.key);
        if      (cmp < 0) h.left  = put(h.left, key, val);
        else if (cmp > 0) h.right = put(h.right, key, val);
        else              h.val   = val;
        return balance(h);                        // fix any right-leaning/red-red links
    }

    // ------------------------------------------------------------------
    // Delete — the moveRedLeft / moveRedRight machinery
    // ------------------------------------------------------------------
    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("deleteMin() on empty tree");
        // If both children are black, paint the root red so moveRedLeft's
        // precondition (h is red or a child is red) holds at the top.
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;
        root = deleteMin(root);
        if (!isEmpty()) root.color = BLACK;
    }

    private Node deleteMin(Node h) {
        if (h.left == null) return null;          // h is the minimum: remove it
        // Ensure h.left is not a 2-node (both children black => push red left).
        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);
        h.left = deleteMin(h.left);
        return balance(h);
    }

    public void deleteMax() {
        if (isEmpty()) throw new NoSuchElementException("deleteMax() on empty tree");
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;
        root = deleteMax(root);
        if (!isEmpty()) root.color = BLACK;
    }

    private Node deleteMax(Node h) {
        if (isRed(h.left))                        // lean the red link right first
            h = rotateRight(h);
        if (h.right == null) return null;         // h is the maximum: remove it
        if (!isRed(h.right) && !isRed(h.right.left))
            h = moveRedRight(h);
        h.right = deleteMax(h.right);
        return balance(h);
    }

    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("delete() with null key");
        if (!contains(key)) return;
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;
        root = delete(root, key);
        if (!isEmpty()) root.color = BLACK;
    }

    private Node delete(Node h, Key key) {
        if (key.compareTo(h.key) < 0) {
            // Key is in the left subtree: make sure we don't descend into a 2-node.
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);
            h.left = delete(h.left, key);
        } else {
            if (isRed(h.left))                    // keep red links leaning left
                h = rotateRight(h);
            // Found it and it's a leaf/min-style case: remove directly.
            if (key.compareTo(h.key) == 0 && h.right == null)
                return null;
            // Ensure the right child is not a 2-node before descending right.
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (key.compareTo(h.key) == 0) {
                // Two-child internal node: swap in the successor (min of right),
                // then delete that successor from the right subtree.
                Node x = min(h.right);
                h.key = x.key;
                h.val = x.val;
                h.right = deleteMin(h.right);
            } else {
                h.right = delete(h.right, key);
            }
        }
        return balance(h);
    }

    /**
     * moveRedLeft: h is red and both children are black (a 3-node's worth of red
     * is missing on the left). Borrow from the right sibling: flip colors, and if
     * the right sibling's left child is red, rotate to keep the lean-left shape.
     * Result: h.left (or one of its children) is red — safe to descend left.
     */
    private Node moveRedLeft(Node h) {
        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    /**
     * moveRedRight: mirror image — borrows from the left sibling so that h.right
     * (or one of its children) is red before descending right.
     */
    private Node moveRedRight(Node h) {
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }

    // ------------------------------------------------------------------
    // Ordered operations
    // ------------------------------------------------------------------
    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("min() on empty tree");
        return min(root).key;
    }

    private Node min(Node x) {
        if (x.left == null) return x;
        return min(x.left);
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("max() on empty tree");
        return max(root).key;
    }

    private Node max(Node x) {
        if (x.right == null) return x;                    // rightmost = maximum
        return max(x.right);
    }

    public Iterable<Key> keys() {
        Queue<Key> q = new ArrayDeque<>();
        inorder(root, q);
        return q;
    }

    private void inorder(Node x, Queue<Key> q) {
        if (x == null) return;
        inorder(x.left, q);
        q.add(x.key);
        inorder(x.right, q);
    }

    public int height() { return height(root); }

    private int height(Node x) {
        if (x == null) return -1;
        return 1 + Math.max(height(x.left), height(x.right));
    }

    // ------------------------------------------------------------------
    // Integrity checks (used by the demo)
    // ------------------------------------------------------------------
    /** True if the tree satisfies BST order, the 2-3 encoding, and black balance. */
    public boolean check() { return isBST() && is23() && isBalanced(); }

    private boolean isBST() { return isBST(root, null, null); }

    private boolean isBST(Node x, Key lo, Key hi) {
        if (x == null) return true;
        if (lo != null && x.key.compareTo(lo) <= 0) return false;
        if (hi != null && x.key.compareTo(hi) >= 0) return false;
        return isBST(x.left, lo, x.key) && isBST(x.right, x.key, hi);
    }

    // No right-leaning red links, no two reds in a row.
    private boolean is23() { return is23(root); }

    private boolean is23(Node x) {
        if (x == null) return true;
        if (isRed(x.right)) return false;
        if (x != root && isRed(x) && isRed(x.left)) return false;
        return is23(x.left) && is23(x.right);
    }

    // Every root-to-null path has the same number of black links.
    private boolean isBalanced() {
        int black = 0;
        for (Node x = root; x != null; x = x.left)
            if (!isRed(x)) black++;
        return isBalanced(root, black);
    }

    private boolean isBalanced(Node x, int black) {
        if (x == null) return black == 0;
        if (!isRed(x)) black--;
        return isBalanced(x.left, black) && isBalanced(x.right, black);
    }

    // ------------------------------------------------------------------
    // Demo
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Left-leaning Red-Black BST ===\n");

        RedBlackBST<String, Integer> st = new RedBlackBST<>();
        String[] keys = { "S", "E", "A", "R", "C", "H", "X", "M", "P", "L" };
        for (int i = 0; i < keys.length; i++) st.put(keys[i], i);

        System.out.print("inorder keys: ");
        for (String k : st.keys()) System.out.print(k + " ");
        System.out.println("\nsize = " + st.size() + ", height = " + st.height()
            + "  (a plain BST on sorted input would have height 9)");
        System.out.println("invariants hold after inserts? " + st.check());

        System.out.println("\n--- deletion with fixups ---");
        for (String victim : new String[] { "E", "S", "A" }) {
            st.delete(victim);
            System.out.print("delete(" + victim + "): keys = ");
            for (String k : st.keys()) System.out.print(k + " ");
            System.out.println("| size=" + st.size() + ", height=" + st.height()
                + ", invariants ok? " + st.check());
        }

        System.out.println("\n--- stress test: 1000 random inserts and deletes ---");
        RedBlackBST<Integer, Integer> big = new RedBlackBST<>();
        java.util.Random rnd = new java.util.Random(7);
        for (int i = 0; i < 1000; i++) big.put(rnd.nextInt(2000), i);
        for (int i = 0; i < 500; i++)  big.delete(rnd.nextInt(2000));
        System.out.println("size = " + big.size() + ", height = " + big.height()
            + "  (log2(1000) ~= 10; LLRB guarantees height <= ~2 log N)");
        System.out.println("invariants hold after stress? " + big.check());

        System.out.println("\nTakeaway: red links encode 3-nodes; rotations and color flips");
        System.out.println("keep the tree balanced through inserts AND deletes. This is the");
        System.out.println("ordered symbol table behind Java's TreeMap — guaranteed log N,");
        System.out.println("always.");
    }
}
