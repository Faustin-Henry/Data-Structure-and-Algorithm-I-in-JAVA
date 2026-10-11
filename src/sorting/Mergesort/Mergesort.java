package sorting.Mergesort;

import java.util.Arrays;

/**
 * Mergesort — the divide-and-conquer workhorse. Guaranteed O(N log N) time, O(N) extra space.
 *
 * <p><b>Top-down:</b> split the array in half, sort each half recursively, then merge the two
 * sorted halves back together in linear time. <b>Bottom-up:</b> skip recursion — merge
 * runs of size 1, then 2, then 4, ... straight through the array.</p>
 *
 * <p>The merge is <em>stable</em> (equal keys keep their original relative order) because we
 * take from the left half on ties. Both versions run in Θ(N log N) comparisons.</p>
 */
public class Mergesort {

    private Mergesort() { }   // utility class — no instances

    // ------------------------------------------------------------------
    // Top-down mergesort
    // ------------------------------------------------------------------
    public static void sort(Comparable[] a) {
        Comparable[] aux = new Comparable[a.length];
        sort(a, aux, 0, a.length - 1);
    }

    private static void sort(Comparable[] a, Comparable[] aux, int lo, int hi) {
        if (hi <= lo) return;                       // one element: already sorted
        int mid = lo + (hi - lo) / 2;               // (avoids integer overflow)
        sort(a, aux, lo, mid);                      // sort left half
        sort(a, aux, mid + 1, hi);                  // sort right half
        // Small optimization from the book: skip the merge when already ordered.
        if (a[mid].compareTo(a[mid + 1]) <= 0) return;
        merge(a, aux, lo, mid, hi);                 // merge the two sorted halves
    }

    // ------------------------------------------------------------------
    // Bottom-up mergesort
    // ------------------------------------------------------------------
    public static void sortBU(Comparable[] a) {
        int n = a.length;
        Comparable[] aux = new Comparable[n];
        for (int size = 1; size < n; size *= 2) {   // run size: 1, 2, 4, 8, ...
            for (int lo = 0; lo < n - size; lo += 2 * size) {
                int mid = lo + size - 1;
                int hi  = Math.min(lo + 2 * size - 1, n - 1);
                merge(a, aux, lo, mid, hi);        // merge a[lo..mid] with a[mid+1..hi]
            }
        }
    }

    /**
     * Merge a[lo..mid] with a[mid+1..hi], assuming each half is sorted.
     * Copies to aux[], then writes back taking the smaller front element —
     * the left half wins ties, which is what makes mergesort stable.
     */
    private static void merge(Comparable[] a, Comparable[] aux, int lo, int mid, int hi) {
        for (int k = lo; k <= hi; k++) aux[k] = a[k];   // copy to aux[]
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if      (i > mid)              a[k] = aux[j++];   // left half exhausted
            else if (j > hi)               a[k] = aux[i++];   // right half exhausted
            else if (aux[j].compareTo(aux[i]) < 0) a[k] = aux[j++]; // right smaller
            else                           a[k] = aux[i++];   // left smaller OR TIE (stable)
        }
    }

    // ------------------------------------------------------------------
    // Demo
    // ------------------------------------------------------------------
    private static void print(String label, Comparable[] a) {
        System.out.println(label + Arrays.toString(a));
    }

    public static void main(String[] args) {
        System.out.println("=== Mergesort: top-down vs bottom-up ===\n");

        Integer[] data = { 38, 27, 43, 3, 9, 82, 10, 21, 55, 14 };
        print("before:          ", data);

        Integer[] topDown = data.clone();
        long t0 = System.nanoTime();
        sort(topDown);                                   // top-down
        long t1 = System.nanoTime();
        print("top-down after:  ", topDown);
        System.out.printf("top-down took %.3f ms%n", (t1 - t0) / 1e6);

        Integer[] bottomUp = data.clone();
        t0 = System.nanoTime();
        sortBU(bottomUp);                                // bottom-up
        t1 = System.nanoTime();
        print("bottom-up after: ", bottomUp);
        System.out.printf("bottom-up took %.3f ms%n", (t1 - t0) / 1e6);

        System.out.println("\n--- stability check (equal keys keep original order) ---");
        String[] words = { "pear", "apple", "fig", "apple", "date" };
        System.out.println("before: " + Arrays.toString(words));
        // decorate each word with its original index, sort by word only
        Word[] decorated = new Word[words.length];
        for (int i = 0; i < words.length; i++) decorated[i] = new Word(words[i], i);
        sort(decorated);
        System.out.print("after:  [");
        for (int i = 0; i < decorated.length; i++)
            System.out.print(decorated[i] + (i + 1 < decorated.length ? ", " : ""));
        System.out.println("]");
        System.out.println("(the two \"apple\" entries kept their original order: apple#1 before apple#3)");

        System.out.println("\nTakeaway: mergesort always costs N log N, is stable, but needs");
        System.out.println("an auxiliary array — quicksort is usually faster in practice,");
        System.out.println("heapsort matches the guarantee without extra space.");
    }

    /** Helper: compares by word only, remembers original index to show stability. */
    private static class Word implements Comparable<Word> {
        final String word;
        final int index;
        Word(String word, int index) { this.word = word; this.index = index; }
        @Override public int compareTo(Word o) { return word.compareTo(o.word); }
        @Override public String toString() { return word + "#" + index; }
    }
}
