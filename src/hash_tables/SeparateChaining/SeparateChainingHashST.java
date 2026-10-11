package hash_tables.SeparateChaining;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * SeparateChainingHashST — a hash table where each bucket holds a linked list of entries.
 *
 * <p><b>How it works:</b> {@code hash(key)} picks one of M buckets; the key lives in that
 * bucket's chain. Collisions just make chains longer — no probing, no clustering.</p>
 *
 * <ul>
 *   <li>put/get/delete walk one chain: O(length of chain).</li>
 *   <li><b>Resizing:</b> we double M when the average chain length (N/M) reaches 10 and
 *       halve it when it drops to 2. That keeps chains short, so operations are
 *       O(1) on average. Rehashing costs O(N) but happens rarely (amortized).</li>
 *   <li>Worst case is still O(N) if every key hashes to one bucket — a good
 *       {@code hashCode()} (and the {@code & 0x7fffffff} mask that drops the sign bit)
 *       makes this vanishingly unlikely.</li>
 * </ul>
 */
public class SeparateChainingHashST<Key, Value> {
    private static final int INIT_CAPACITY = 4;

    private int n;                                   // number of key-value pairs
    private int m;                                   // number of chains (buckets)
    private Node[] st;                               // array of linked-list heads

    private static class Node {
        Object key;
        Object val;
        Node next;
        Node(Object key, Object val, Node next) { this.key = key; this.val = val; this.next = next; }
    }

    public SeparateChainingHashST() { this(INIT_CAPACITY); }

    @SuppressWarnings("unchecked")
    public SeparateChainingHashST(int m) {
        this.m = m;
        st = new Node[m];                            // raw Node[] — keys checked with equals()
    }

    public int size()        { return n; }
    public boolean isEmpty() { return n == 0; }

    /** Hash to a bucket index in [0, m): mask off the sign bit, then mod m. */
    private int hash(Key key) {
        return (key.hashCode() & 0x7fffffff) % m;
    }

    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("get() with null key");
        for (Node x = st[hash(key)]; x != null; x = x.next)
            if (key.equals(x.key)) return (Value) x.val;
        return null;
    }

    public boolean contains(Key key) { return get(key) != null; }

    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("put() with null key");
        if (val == null) { delete(key); return; }
        if (n >= 10 * m) resize(2 * m);              // average chain hit 10: double
        int i = hash(key);
        for (Node x = st[i]; x != null; x = x.next)
            if (key.equals(x.key)) { x.val = val; return; }  // update existing
        st[i] = new Node(key, val, st[i]);           // insert at head of chain
        n++;
    }

    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("delete() with null key");
        int i = hash(key);
        Node prev = null;
        for (Node x = st[i]; x != null; prev = x, x = x.next) {
            if (key.equals(x.key)) {
                if (prev == null) st[i] = x.next;    // unlink the head
                else              prev.next = x.next;
                n--;
                break;
            }
        }
        if (m > INIT_CAPACITY && n <= 2 * m) resize(m / 2);   // chains short: shrink
    }

    /** Rebuild the table with a new bucket count, rehashing every key. O(N). */
    private void resize(int chains) {
        SeparateChainingHashST<Key, Value> temp = new SeparateChainingHashST<>(chains);
        for (Key key : keys()) temp.put(key, get(key));
        this.st = temp.st;
        this.m = temp.m;
        this.n = temp.n;
    }

    public Iterable<Key> keys() {
        Queue<Key> q = new ArrayDeque<>();
        for (int i = 0; i < m; i++)
            for (Node x = st[i]; x != null; x = x.next)
                q.add((Key) x.key);
        return q;
    }

    /** Average chain length — a live health metric for the table. */
    public double avgChainLength() { return (double) n / m; }

    public static void main(String[] args) {
        System.out.println("=== Separate-chaining hash table ===\n");

        SeparateChainingHashST<String, Integer> st = new SeparateChainingHashST<>();
        String text = "search example hash table with separate chaining for collisions";
        for (String w : text.split(" "))
            st.put(w, st.contains(w) ? st.get(w) + 1 : 1);

        System.out.println("stored " + st.size() + " distinct words; "
            + "avg chain length = " + String.format("%.2f", st.avgChainLength()));
        System.out.println("get(\"hash\")      = " + st.get("hash"));
        System.out.println("get(\"collisions\") = " + st.get("collisions"));
        System.out.println("get(\"missing\")    = " + st.get("missing") + "  (miss -> null)");

        System.out.println("\ndelete(\"hash\"), then re-check:");
        st.delete("hash");
        System.out.println("  get(\"hash\") = " + st.get("hash") + ", size = " + st.size());

        System.out.println("\nupdate: put(\"table\", 99)");
        st.put("table", 99);
        System.out.println("  get(\"table\") = " + st.get("table") + "  (same key, new value, size unchanged: "
            + st.size() + ")");

        System.out.println("\nTakeaway: hashing turns the key into an array index, chains");
        System.out.println("absorb collisions, and resizing keeps chains short — average");
        System.out.println("O(1) get/put/delete. This is the idea behind Java's HashMap");
        System.out.println("(which uses trees instead of lists for very long chains).");
    }
}
