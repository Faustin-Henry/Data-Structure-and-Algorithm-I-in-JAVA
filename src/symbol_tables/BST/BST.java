package symbol_tables.BST;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * BST — an ordered symbol table implemented as a binary search tree.
 *
 * <p>The <em>symmetric order</em> invariant: every node's key is larger than all keys in its
 * left subtree and smaller than all keys in its right subtree. That single invariant gives
 * us the whole ordered API:</p>
 *
 * <ul>
 *   <li>{@code put}/{@code get} — recursive descent, O(height). Random keys → ~1.39 log N.</li>
 *   <li>{@code min}/{@code max} — walk left / right to the end.</li>
 *   <li>{@code floor} — largest key ≤ the query: go left when the key is smaller, but
 *       remember the current node as a candidate when going right.</li>
 *   <li>{@code rank} — number of keys &lt; the query, computed from subtree sizes.</li>
 *   <li>{@code select} — key of rank k: use subtree sizes to decide which side holds it.</li>
 *   <li>{@code delete} — <b>Hibbard deletion</b>: no children → remove; one child → replace
 *       with it; two children → replace with the <em>successor</em> (min of right subtree),
 *       then delete that successor from the right subtree.</li>
 *   <li>{@code keys()} — inorder traversal yields keys in sorted order.</li>
 * </ul>
 *
 * <p>Each node stores its subtree size, which makes {@code rank}/{@code select}/{@code size}
 * O(height) instead of O(N). Caveat: with sorted input the tree degenerates to a linked
 * list (height N) — the fix is a balanced tree (see RedBlackBST).</p>
 */
public class BST<Key extends Comparable<Key>, Value> {

    private Node root;

    private class Node {
        Key key;
        Value val;
        Node left, right;
        int size;                       // # of nodes in the subtree rooted here
        Node(Key key, Value val, int size) { this.key = key; this.val = val; this.size = size; }
    }

    public boolean isEmpty() { return size() == 0; }
    public int size()        { return size(root); }
    private int size(Node x) { return x == null ? 0 : x.size; }

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("get() with null key");
        return get(root, key);
    }

    private Value get(Node x, Key key) {
        if (x == null) return null;                          // miss
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return get(x.left, key);            // search left
        else if (cmp > 0) return get(x.right, key);           // search right
        else              return x.val;                      // hit
    }

    public boolean contains(Key key) { return get(key) != null; }

    // ------------------------------------------------------------------
    // Insert / update
    // ------------------------------------------------------------------
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("put() with null key");
        if (val == null) { delete(key); return; }            // Sedgewick convention
        root = put(root, key, val);
    }

    private Node put(Node x, Key key, Value val) {
        if (x == null) return new Node(key, val, 1);          // new leaf
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) x.left  = put(x.left, key, val);
        else if (cmp > 0) x.right = put(x.right, key, val);
        else              x.val   = val;                      // key exists: update
        x.size = 1 + size(x.left) + size(x.right);            // fix subtree count
        return x;
    }

    // ------------------------------------------------------------------
    // Ordered operations
    // ------------------------------------------------------------------
    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("min() on empty table");
        return min(root).key;
    }

    private Node min(Node x) {
        if (x.left == null) return x;                         // leftmost = minimum
        return min(x.left);
    }

    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("max() on empty table");
        return max(root).key;
    }

    private Node max(Node x) {
        if (x.right == null) return x;
        return max(x.right);
    }

    /**
     * Floor: largest key ≤ the query. If key < x.key, the floor must be in the
     * left subtree; if key == x.key, x is the floor; otherwise the floor is in
     * the right subtree — unless the right subtree has none smaller-or-equal,
     * in which case x itself is the answer.
     */
    public Key floor(Key key) {
        if (key == null) throw new IllegalArgumentException("floor() with null key");
        if (isEmpty()) throw new NoSuchElementException("floor() on empty table");
        Node x = floor(root, key);
        if (x == null) throw new NoSuchElementException("no floor for " + key);
        return x.key;
    }

    private Node floor(Node x, Key key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp < 0)  return floor(x.left, key);
        Node t = floor(x.right, key);                        // candidate in right subtree
        if (t != null) return t;
        return x;                                            // x is the floor
    }

    /** Rank: how many keys are strictly less than {@code key}. */
    public int rank(Key key) {
        if (key == null) throw new IllegalArgumentException("rank() with null key");
        return rank(root, key);
    }

    private int rank(Node x, Key key) {
        if (x == null) return 0;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return rank(x.left, key);
        else if (cmp > 0) return 1 + size(x.left) + rank(x.right, key);
        else              return size(x.left);
    }

    /** Select: the key of rank k (0-based) — the (k+1)-th smallest key. */
    public Key select(int k) {
        if (k < 0 || k >= size()) throw new IllegalArgumentException("rank out of bounds: " + k);
        return select(root, k).key;
    }

    private Node select(Node x, int k) {
        int leftSize = size(x.left);
        if      (leftSize > k) return select(x.left, k);            // target is left
        else if (leftSize < k) return select(x.right, k - leftSize - 1); // target is right
        else                   return x;                           // x is the one
    }

    // ------------------------------------------------------------------
    // Deletion (Hibbard)
    // ------------------------------------------------------------------
    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("delete() with null key");
        root = delete(root, key);
    }

    private Node delete(Node x, Key key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) x.left  = delete(x.left, key);
        else if (cmp > 0) x.right = delete(x.right, key);
        else {
            // Found it. Cases: 0 children -> null; 1 child -> that child;
            // 2 children -> replace with successor (min of right subtree).
            if (x.right == null) return x.left;
            if (x.left  == null) return x.right;
            Node t = x;
            x = min(t.right);                          // successor takes x's place
            x.right = deleteMin(t.right);              // delete successor from right subtree
            x.left = t.left;
        }
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    public void deleteMin() {
        if (isEmpty()) throw new NoSuchElementException("deleteMin() on empty table");
        root = deleteMin(root);
    }

    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;             // x is the min: splice it out
        x.left = deleteMin(x.left);
        x.size = 1 + size(x.left) + size(x.right);
        return x;
    }

    // ------------------------------------------------------------------
    // Iteration: inorder traversal = keys in sorted order
    // ------------------------------------------------------------------
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
        if (x == null) return -1;                       // empty tree has height -1
        return 1 + Math.max(height(x.left), height(x.right));
    }

    // ------------------------------------------------------------------
    // Demo: word-frequency counter, the canonical symbol-table client
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== BST: word-frequency counter ===\n");

        BST<String, Integer> st = new BST<>();
        String text = "it was the best of times it was the worst of times";
        int words = 0;
        for (String word : text.split(" ")) {
            words++;
            st.put(word, st.contains(word) ? st.get(word) + 1 : 1);   // frequency count
        }
        System.out.println("distinct words: " + st.size() + "  (of " + words + " total)");
        System.out.println("tree height: " + st.height() + " (random-ish input stays shallow)");

        System.out.println("\nkeys() walks inorder — always sorted:");
        for (String w : st.keys()) System.out.println("  " + w + " -> " + st.get(w));

        System.out.println("\nOrdered queries:");
        System.out.println("  min()        = " + st.min());
        System.out.println("  max()        = " + st.max());
        System.out.println("  floor(\"was\") = " + st.floor("was") + "  (largest key <= \"was\")");
        System.out.println("  rank(\"was\")  = " + st.rank("was") + "  (keys smaller than \"was\")");
        System.out.println("  select(2)    = " + st.select(2) + "  (3rd smallest key)");

        System.out.println("\nHibbard delete(\"was\") — the word with count 2:");
        st.delete("was");
        System.out.println("  size now " + st.size() + "; get(\"was\") = " + st.get("was"));
        System.out.print("  keys now: ");
        for (String w : st.keys()) System.out.print(w + " ");
        System.out.println();

        System.out.println("\nTakeaway: one invariant (symmetric order) + subtree sizes");
        System.out.println("gives a full ordered map. Fast when the tree stays balanced —");
        System.out.println("insert keys in sorted order and it degenerates to a linked");
        System.out.println("list, which is exactly why balanced trees (LLRB) exist.");
    }
}
