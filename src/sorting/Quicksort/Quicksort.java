package sorting.Quicksort;

import java.util.Arrays;
import java.util.Random;

/**
 * Quicksort — the fastest general-purpose sort in practice. O(N log N) on average,
 * O(N^2) worst case (avoided by shuffling first), in place.
 *
 * <p><b>How it works:</b> shuffle the array, then <em>partition</em> around a pivot value v:
 * rearrange so that everything left of position j is ≤ v and everything right of j is ≥ v.
 * Then sort the two sides recursively. The partition scan uses two pointers, i from the left
 * and j from the right, that stop on items that are "on the wrong side" and exchange them.</p>
 *
 * <p><b>3-way quicksort</b> (Dijkstra's Dutch-national-flag idea) partitions into three parts:
 * less than v, equal to v, greater than v. If the input has many duplicate keys it skips all
 * the equal items at once — on inputs with few distinct keys it runs in near-linear time.</p>
 */
public class Quicksort {

    private Quicksort() { }   // utility class — no instances

    // ------------------------------------------------------------------
    // Standard quicksort (with shuffle)
    // ------------------------------------------------------------------
    public static void sort(Comparable[] a) {
        shuffle(a);                            // random shuffle defeats the worst case
        sort(a, 0, a.length - 1);
    }

    private static void sort(Comparable[] a, int lo, int hi) {
        if (hi <= lo) return;
        int j = partition(a, lo, hi);          // pivot now sits at a[j], in its final spot
        sort(a, lo, j - 1);                    // sort left part
        sort(a, j + 1, hi);                    // sort right part
    }

    /**
     * Partition a[lo..hi] around a[lo]. Returns j such that:
     * a[lo..j-1] <= a[j] <= a[j+1..hi].
     */
    private static int partition(Comparable[] a, int lo, int hi) {
        int i = lo, j = hi + 1;
        Comparable v = a[lo];                  // the pivot value
        while (true) {
            // scan right: stop at an item >= v (something that belongs on the right)
            while (less(a[++i], v)) {
                if (i == hi) break;
            }
            // scan left: stop at an item <= v (something that belongs on the left)
            while (less(v, a[--j])) {
                if (j == lo) break;
            }
            if (i >= j) break;                 // pointers crossed: partitioning done
            exch(a, i, j);                     // swap the two out-of-place items
        }
        exch(a, lo, j);                        // put the pivot into its final place
        return j;
    }

    // ------------------------------------------------------------------
    // 3-way quicksort — fast when keys repeat
    // ------------------------------------------------------------------
    public static void sort3way(Comparable[] a) {
        shuffle(a);
        sort3way(a, 0, a.length - 1);
    }

    private static void sort3way(Comparable[] a, int lo, int hi) {
        if (hi <= lo) return;
        int lt = lo, gt = hi;                  // a[lo..lt-1] < v < a[gt+1..hi]
        Comparable v = a[lo];
        int i = lo + 1;
        while (i <= gt) {
            int cmp = a[i].compareTo(v);
            if      (cmp < 0) exch(a, lt++, i++);  // into the "less" region
            else if (cmp > 0) exch(a, i, gt--);    // into the "greater" region
            else              i++;                // equal: just skip it
        }
        // Now a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi]; no need to sort the equal middle.
        sort3way(a, lo, lt - 1);
        sort3way(a, gt + 1, hi);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private static boolean less(Comparable v, Comparable w) {
        return v.compareTo(w) < 0;
    }

    private static void exch(Object[] a, int i, int j) {
        Object swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    /** Fisher–Yates shuffle: guarantees every permutation is equally likely. */
    private static void shuffle(Object[] a) {
        Random rnd = new Random(42);           // fixed seed -> demo is reproducible
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            exch(a, i, j);
        }
    }

    // ------------------------------------------------------------------
    // Demo
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Quicksort ===\n");

        Integer[] data = { 38, 27, 43, 3, 9, 82, 10, 21, 55, 14 };
        System.out.println("before: " + Arrays.toString(data));
        Integer[] one = data.clone();
        sort(one);
        System.out.println("standard quicksort: " + Arrays.toString(one));

        System.out.println("\n--- where 3-way shines: lots of duplicates ---");
        Integer[] dupes = { 5, 1, 5, 3, 5, 2, 5, 4, 5, 1, 5, 3, 5, 2, 5 };
        System.out.println("before: " + Arrays.toString(dupes));
        Integer[] three = dupes.clone();
        sort3way(three);
        System.out.println("3-way quicksort:  " + Arrays.toString(three));
        System.out.println("(standard quicksort would waste work re-partitioning all those 5s)");

        System.out.println("\n--- words, too ---");
        String[] words = { "quicksort", "is", "fast", "and", "in", "place", "fast" };
        System.out.println("before: " + Arrays.toString(words));
        sort3way(words);
        System.out.println("after:  " + Arrays.toString(words));

        System.out.println("\nTakeaway: quicksort = partition + recurse. Shuffle first to");
        System.out.println("kill the worst case; reach for 3-way when keys repeat.");
        System.out.println("In-place like heapsort, faster in practice than mergesort, but");
        System.out.println("not stable and its worst case is quadratic (shuffling makes it");
        System.out.println("astronomically unlikely).");
    }
}
