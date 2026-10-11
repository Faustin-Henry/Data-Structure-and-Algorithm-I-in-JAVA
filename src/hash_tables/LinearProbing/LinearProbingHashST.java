package hash_tables.LinearProbing;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * LinearProbingHashST — a hash table with <em>open addressing</em>: everything lives in
 * one flat array, no linked lists.
 *
 * <p><b>How it works:</b> {@code hash(key)} gives a starting index i. To insert, walk
 * forward (wrapping around) until an empty slot or the key itself: empty → store there,
 * key found → update. To search, walk forward until the key or an empty slot
 * (empty slot = the key was never inserted). All operations touch one contiguous
 * cluster of the array.</p>
 *
 * <ul>
 *   <li><b>Resizing:</b> double M when N reaches M/2 (load factor 1/2) and halve it when
 *       N drops to M/8. Keeping the table at most half full bounds the expected
 *       cluster length — average O(1), great cache behavior.</li>
 *   <li><b>Deletion is the subtle part:</b> you can't just null the slot, because later
 *       searches probe <em>through</em> it. So after clearing slot i, re-insert every
 *       key in the following cluster (they may have probed past i). Deleting this way
 *       costs O(cluster length) but keeps searches correct.</li>
 * </ul>
 *
 * <p>(The draft README mentioned "Robin Hood hashing" — this file implements the classic
 * textbook linear probing instead; Robin Hood is a fancier variant that bounds the
 * worst-case probe length.)</p>
 */
public class LinearProbingHashST<Key, Value> {
    private static final int INIT_CAPACITY = 4;

    private int n;            // number of key-value pairs
    private int m;            // size of the hash table (always a power of two here)
    private Key[] keys;
    private Value[] vals;

    @SuppressWarnings("unchecked")
    public LinearProbingHashST() { this(INIT_CAPACITY); }

    @SuppressWarnings("unchecked")
    public LinearProbingHashST(int capacity) {
        m = capacity;
        keys = (Key[])   new Object[m];
        vals = (Value[]) new Object[m];
    }

    public int size()        { return n; }
    public boolean isEmpty() { return n == 0; }

    private int hash(Key key) { return (key.hashCode() & 0x7fffffff) % m; }

    /** Load factor — we resize to keep this ≤ 1/2. */
    public double loadFactor() { return (double) n / m; }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("get() with null key");
        // Probe forward from hash(key) until the key or an empty slot.
        for (int i = hash(key); keys[i] != null; i = (i + 1) % m)
            if (key.equals(keys[i])) return vals[i];
        return null;                                  // hit an empty slot: miss
    }

    public boolean contains(Key key) { return get(key) != null; }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("put() with null key");
        if (val == null) { delete(key); return; }
        if (n >= m / 2) resize(2 * m);                // table half full: double
        int i;
        for (i = hash(key); keys[i] != null; i = (i + 1) % m) {
            if (key.equals(keys[i])) { vals[i] = val; return; }  // update
        }
        keys[i] = key;                                // empty slot: insert
        vals[i] = val;
        n++;
    }

    /**
     * Delete key, then <b>rehash the following cluster</b>: every key after the hole
     * might have probed past it, so remove and re-put each one to restore the
     * "no holes inside a cluster" invariant that get() relies on.
     */
    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("delete() with null key");
        if (!contains(key)) return;
        int i = hash(key);
        while (!key.equals(keys[i])) i = (i + 1) % m; // find the key's slot
        keys[i] = null;                               // clear it...
        vals[i] = null;
        i = (i + 1) % m;
        while (keys[i] != null) {                     // ...then rehash the cluster
            Key   keyToRehash = keys[i];
            Value valToRehash = vals[i];
            keys[i] = null;
            vals[i] = null;
            n--;                                      // put() will increment again
            put(keyToRehash, valToRehash);
            i = (i + 1) % m;
        }
        n--;
        if (n > 0 && n <= m / 8) resize(m / 2);        // table mostly empty: shrink
    }

    /** Rebuild the table at a new capacity, re-inserting every pair. */
    private void resize(int capacity) {
        LinearProbingHashST<Key, Value> temp = new LinearProbingHashST<>(capacity);
        for (int i = 0; i < m; i++)
            if (keys[i] != null) temp.put(keys[i], vals[i]);
        keys = temp.keys;
        vals = temp.vals;
        m = temp.m;
    }

    public Iterable<Key> keys() {
        Queue<Key> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++)
            if (keys[i] != null) q.add(keys[i]);
        return q;
    }

    public static void main(String[] args) {
        System.out.println("=== Linear-probing hash table ===\n");

        LinearProbingHashST<String, Integer> st = new LinearProbingHashST<>();
        String text = "linear probing stores everything in one flat array no chains";
        for (String w : text.split(" "))
            st.put(w, st.contains(w) ? st.get(w) + 1 : 1);

        System.out.println("size = " + st.size()
            + ", load factor = " + String.format("%.2f", st.loadFactor()) + " (kept <= 0.50)");
        System.out.println("get(\"probing\") = " + st.get("probing"));
        System.out.println("get(\"chains\")  = " + st.get("chains"));

        System.out.println("\n--- delete(\"probing\") exercises the cluster-rehash ---");
        st.delete("probing");
        System.out.println("  get(\"probing\") = " + st.get("probing") + "  (gone)");
        System.out.println("  get(\"array\")   = " + st.get("array")
            + "  (neighbors of the deleted slot still found — rehash worked)");
        System.out.println("  size = " + st.size());

        System.out.println("\n--- force growth: insert 50 numbered keys ---");
        for (int i = 0; i < 50; i++) st.put("k" + i, i);
        System.out.println("  size = " + st.size()
            + ", load factor = " + String.format("%.2f", st.loadFactor()));
        boolean allFound = true;
        for (int i = 0; i < 50; i++) allFound &= st.get("k" + i) == i;
        System.out.println("  all 50 retrievable after resizes? " + allFound);

        System.out.println("\nTakeaway: one array, probe forward on collision, never let it");
        System.out.println("get more than half full. Deletion must rehash the cluster —");
        System.out.println("the one subtlety that separates this from separate chaining.");
    }
}
