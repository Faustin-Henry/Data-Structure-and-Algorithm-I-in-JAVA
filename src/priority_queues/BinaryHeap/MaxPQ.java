package priority_queues.BinaryHeap;

/**
 * MaxPQ — a max-oriented priority queue backed by a binary heap in a resizing array.
 *
 * <p>A binary heap is a complete binary tree stored in an array with 1-based indexing:
 * the parent of node k is at k/2, its children at 2k and 2k+1. The <em>heap order</em>
 * invariant says every parent is ≥ its children, so the maximum item is always at
 * {@code pq[1]}.</p>
 *
 * <ul>
 *   <li><b>insert</b> — add at the end, then <em>swim</em> up while the parent is smaller. O(log N).</li>
 *   <li><b>delMax</b> — take the root, move the last item to the root, then <em>sink</em> it
 *       down while a child is larger. O(log N).</li>
 *   <li><b>max / peek</b> — read pq[1]. O(1).</li>
 * </ul>
 *
 * <p>(A MinPQ is the mirror image: flip the comparisons. See the swim/sink comments.)</p>
 */
public class MaxPQ<Key extends Comparable<Key>> implements Iterable<Key> {
    private Key[] pq;       // heap-ordered complete binary tree, pq[1..n]; pq[0] unused
    private int n;          // number of items

    @SuppressWarnings("unchecked")
    public MaxPQ() { this(2); }

    @SuppressWarnings("unchecked")
    public MaxPQ(int capacity) {
        pq = (Key[]) new Comparable[capacity + 1];
        n = 0;
    }

    public boolean isEmpty() { return n == 0; }
    public int size()        { return n; }

    /** Largest key. O(1). */
    public Key max() {
        if (isEmpty()) throw new java.util.NoSuchElementException("priority queue underflow");
        return pq[1];
    }

    private void resize(int capacity) {
        @SuppressWarnings("unchecked")
        Key[] temp = (Key[]) new Comparable[capacity];
        for (int i = 1; i <= n; i++) temp[i] = pq[i];
        pq = temp;
    }

    /** Insert: put the new key at the end, then swim it up into place. O(log N). */
    public void insert(Key x) {
        if (x == null) throw new IllegalArgumentException("null key");
        if (n == pq.length - 1) resize(2 * pq.length);
        pq[++n] = x;
        swim(n);
    }

    /** Remove and return the largest key. O(log N). */
    public Key delMax() {
        if (isEmpty()) throw new java.util.NoSuchElementException("priority queue underflow");
        Key max = pq[1];
        exch(1, n--);                 // swap max with the last item, shrink the heap
        sink(1);                     // restore heap order
        pq[n + 1] = null;            // avoid loitering
        if (n > 0 && n == (pq.length - 1) / 4) resize(pq.length / 2);
        return max;
    }

    /**
     * Swim: while k's parent is smaller, swap them. Restores heap order on the
     * path from k up to the root. O(log N).
     */
    private void swim(int k) {
        while (k > 1 && less(k / 2, k)) {
            exch(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * Sink: while k has a child, swap it with the LARGER child if that child is
     * bigger. Restores heap order in k's subtree. O(log N).
     */
    private void sink(int k) {
        while (2 * k <= n) {
            int j = 2 * k;                     // left child
            if (j < n && less(j, j + 1)) j++;   // j = larger child
            if (!less(k, j)) break;            // heap order holds — done
            exch(k, j);
            k = j;
        }
    }

    private boolean less(int i, int j) { return pq[i].compareTo(pq[j]) < 0; }

    private void exch(int i, int j) {
        Key swap = pq[i];
        pq[i] = pq[j];
        pq[j] = swap;
    }

    @Override
    public java.util.Iterator<Key> iterator() {
        // Iterate in descending order by draining a copy (does not disturb this queue).
        return new java.util.Iterator<Key>() {
            private final MaxPQ<Key> copy;
            { copy = new MaxPQ<>(size()); for (int i = 1; i <= n; i++) copy.insert(pq[i]); }
            @Override public boolean hasNext() { return !copy.isEmpty(); }
            @Override public Key next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                return copy.delMax();
            }
        };
    }

    /** A demo task with a name and a priority — compares by priority. */
    private static class Task implements Comparable<Task> {
        final String name;
        final int priority;
        Task(String name, int priority) { this.name = name; this.priority = priority; }
        @Override public int compareTo(Task o) { return Integer.compare(priority, o.priority); }
        @Override public String toString() { return name + " (priority " + priority + ")"; }
    }

    public static void main(String[] args) {
        System.out.println("=== MaxPQ: a to-do list that serves the most urgent task first ===\n");

        MaxPQ<Task> pq = new MaxPQ<>();
        Task[] tasks = {
            new Task("reply to landlord", 2),
            new Task("fix production bug", 9),
            new Task("study for CS2920", 7),
            new Task("water the plants", 1),
            new Task("call mom", 8),
        };
        for (Task t : tasks) {
            pq.insert(t);
            System.out.println("inserted: " + t + "   -> current max: " + pq.max());
        }

        System.out.println("\nProcessing in priority order (delMax " + pq.size() + " times):");
        while (!pq.isEmpty())
            System.out.println("  doing: " + pq.delMax());

        System.out.println("\nTakeaway: insert and delMax are both O(log N) because the tree");
        System.out.println("is always complete (height ~ log N) and swim/sink only walk");
        System.out.println("one root-to-leaf path. Flip the comparisons and you get a MinPQ.");
        System.out.println("This structure also powers heapsort and Dijkstra's algorithm.");
    }
}
