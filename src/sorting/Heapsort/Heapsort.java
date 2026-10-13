package sorting.Heapsort;

import java.util.Arrays;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Heapsort — optimal <em>guaranteed</em> comparison sort: O(N log N) worst case, in place.
 *
 * <p>Two phases, both built on the {@code sink} operation ("the item sinks down the heap
 * until the heap order is restored"):</p>
 *
 * <ol>
 *   <li><b>Heap construction:</b> arrange the array into a max-heap, working backwards from
 *       the last parent (index N/2) down to the root. Sinking each node once costs O(N)
 *       total — cheaper than inserting N items one by one.</li>
 *   <li><b>Sortdown:</b> repeatedly take the maximum (the root), swap it into the last
 *       unsorted position, shrink the heap by one, and sink the new root. Each sink is
 *       O(log N), done N times: O(N log N).</li>
 * </ol>
 *
 * <p>The code uses the textbook's 1-based heap indexing inside a 0-based Java array via the
 * {@code less}/{@code exch} helpers (element i of the heap lives at {@code pq[i-1]}).
 * In-place, but not stable.</p>
 */
public class Heapsort {

    private Heapsort() { }   // utility class — no instances

    public static void sort(Comparable[] pq) {
        int n = pq.length;
        // Phase 1: heap construction — sink every parent, bottom-up.
        for (int k = n / 2; k >= 1; k--)
            sink(pq, k, n);
        // Phase 2: sortdown — move max to the end, re-heapify the rest.
        int k = n;
        while (k > 1) {
            exch(pq, 1, k--);                  // swap root (max) with last heap slot
            sink(pq, 1, k);                    // restore heap order in the smaller heap
        }
    }

    /**
     * Sink pq[k] down a max-heap of size n (1-based indexing): while k has a child,
     * swap it with the LARGER child if that child is bigger — this restores
     * "parent >= children" everywhere below k. O(log N).
     */
    private static void sink(Comparable[] pq, int k, int n) {
        while (2 * k <= n) {
            int j = 2 * k;                     // left child
            if (j < n && less(pq, j, j + 1)) j++;   // j = index of the larger child
            if (!less(pq, k, j)) break;        // heap order restored — stop
            exch(pq, k, j);
            k = j;
        }
    }

    // 1-based helpers: heap position i is stored at pq[i-1].
    private static boolean less(Comparable[] pq, int i, int j) {
        return pq[i - 1].compareTo(pq[j - 1]) < 0;
    }

    private static void exch(Object[] pq, int i, int j) {
        Object swap = pq[i - 1];
        pq[i - 1] = pq[j - 1];
        pq[j - 1] = swap;
    }


    public static void main(String[] args) {
        System.out.println("=== Heapsort ===\n");

        Scanner in = new Scanner(System.in);
        Integer[] data = null;

        while (data == null) {
            System.out.print("Enter numbers to sort (separated by spaces or commas),\n"
                    + "or press Enter to use a sample list: ");
            String line = in.nextLine().trim();

            if (line.isEmpty()) {
                data = new Integer[] { 38, 27, 43, 3, 9, 82, 10, 21, 55, 14 };
                System.out.println("Using sample data.");
                break;
            }

            String[] parts = line.split("[,\\s]+");
            Integer[] parsed = new Integer[parts.length];
            try {
                for (int i = 0; i < parts.length; i++) {
                    parsed[i] = Integer.parseInt(parts[i]);
                }
                data = parsed;
            } catch (NumberFormatException e) {
                System.out.println("Please enter whole numbers only. Try again.\n");
            }
        }

        System.out.println("\nbefore: " + Arrays.toString(data));
        long t0 = System.nanoTime();
        sort(data);
        long t1 = System.nanoTime();
        System.out.println("after:  " + Arrays.toString(data));
        System.out.printf("sorted %d items in %.3f ms%n", data.length, (t1 - t0) / 1e6);

        System.out.println("\n--- the heap-construction idea, step by step ---");
        Integer[] small = { 4, 1, 3, 2, 16, 9, 10, 14, 8, 7 };
        System.out.println("raw array:      " + Arrays.toString(small));
        // Reproduce phase 1 only, to show the heap that sortdown starts from:
        int n = small.length;
        for (int k = n / 2; k >= 1; k--) sink(small, k, n);
        System.out.println("after heapify:  " + Arrays.toString(small));
        System.out.println("(a max-heap: every parent >= its children; max = " + small[0] + " at the root)");
        sort(small);
        System.out.println("after sortdown: " + Arrays.toString(small));

        System.out.println("\nTakeaway: heapsort = heapify (O(N)) + N x delMax (O(N log N)).");
        System.out.println("The only sort that is simultaneously in-place AND worst-case N log N —");
        System.out.println("but it is not stable and has poor cache behavior, so quicksort");
        System.out.println("usually wins in practice.");

        in.close();
    }
}
