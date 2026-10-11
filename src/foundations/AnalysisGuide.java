package foundations;

import java.util.Arrays;
import java.util.Random;

/**
 * AnalysisGuide — a Big-O cheat sheet plus a tiny doubling-ratio experiment.
 *
 * <p>Big-O notation describes how an algorithm's running time (or memory use) grows as the
 * input size N grows, ignoring constant factors and lower-order terms. What matters is the
 * <em>shape</em> of the growth curve:</p>
 *
 * <ul>
 *   <li>O(1)        — constant: array access, stack push/pop, hash table get.</li>
 *   <li>O(log N)    — logarithmic: binary search, insert into a balanced BST.</li>
 *   <li>O(N)        — linear: scan every element once (find max, linear search).</li>
 *   <li>O(N log N)  — linearithmic: mergesort, heapsort, quicksort (typical case).</li>
 *   <li>O(N^2)      — quadratic: insertion sort, double loop over all pairs.</li>
 *   <li>O(N^3)      — cubic: triple loop (some matrix routines, naive Floyd-Warshall).</li>
 *   <li>O(2^N)      — exponential: brute-force over every subset.</li>
 * </ul>
 *
 * <p>The doubling hypothesis: if T(N) is the time for input size N, then running the same
 * program on size 2N and taking the ratio T(2N)/T(N) reveals the growth rate —
 * a ratio of ~2 means linear, ~4 means quadratic, and ~2+ε means N log N. The {@code main}
 * demo below measures this live: a naive O(N^2) pair-counting loop versus
 * {@link Arrays#sort(Object[])} at O(N log N).</p>
 *
 * @see <a href="https://algs4.cs.princeton.edu/14analysis/">Algorithms, 4th ed. §1.4</a>
 */
public class AnalysisGuide {

    /** Growth-rate rows shown in the cheat-sheet table. */
    private static final String[][] TABLE = {
        { "O(1)",      "constant",     "array access, stack push/pop, hash get" },
        { "O(log N)",  "logarithmic",  "binary search, balanced-BST insert" },
        { "O(N)",      "linear",       "linear scan, find max" },
        { "O(N log N)","linearithmic", "mergesort, heapsort, typical quicksort" },
        { "O(N^2)",    "quadratic",    "insertion/selection sort, all-pairs loops" },
        { "O(N^3)",    "cubic",        "triple-nested loops, naive matrix multiply" },
        { "O(2^N)",    "exponential",  "try every subset (travelling salesman brute force)" },
    };

    /** Naive O(N^2): count how many array entries are >= a threshold using a double loop. */
    private static long quadraticWork(int[] a) {
        long pairs = 0;
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a.length; j++)
                if (a[i] <= a[j]) pairs++;   // ~N^2 comparisons, no early exit
        return pairs;
    }

    /** O(N log N): sort a copy with the built-in dual-pivot quicksort (int overload). */
    private static void nlognWork(int[] a) {
        Arrays.sort(a);                       // Timsort/Dual-pivot: Theta(N log N) on average
    }

    /**
     * Times one doubling step, returning milliseconds.
     * Takes the minimum of several trials — min filters out GC pauses and
     * scheduling noise, so the ratio reflects the algorithm, not the machine.
     */
    private static double timeQuadratic(int n) {
        int[] a = new int[n];
        Arrays.fill(a, 1);                    // constant work per inner iteration
        double best = Double.POSITIVE_INFINITY;
        for (int trial = 0; trial < 5; trial++) {
            long start = System.nanoTime();
            quadraticWork(a);
            best = Math.min(best, (System.nanoTime() - start) / 1_000_000.0);
        }
        return best;
    }

    private static double timeNlogN(int n, Random rnd) {
        double best = Double.POSITIVE_INFINITY;
        for (int trial = 0; trial < 5; trial++) {
            int[] a = rnd.ints(n, 0, 1_000_000).toArray();
            long start = System.nanoTime();
            nlognWork(a);
            best = Math.min(best, (System.nanoTime() - start) / 1_000_000.0);
        }
        return best;
    }

    public static void main(String[] args) {
        System.out.println("=== Big-O cheat sheet ===\n");
        System.out.printf("%-10s %-14s %s%n", "Growth", "Name", "Typical example");
        System.out.println("-------------------------------------------------------------");
        for (String[] row : TABLE)
            System.out.printf("%-10s %-14s %s%n", row[0], row[1], row[2]);

        System.out.println("\n=== Doubling-ratio experiment ===");
        System.out.println("For each size N we also time size 2N. Expected ratios:");
        System.out.println("  ~4.0  -> O(N^2)   (doubling N quadruples the work)");
        System.out.println("  ~2.1  -> O(N log N) (doubling N roughly doubles the work)\n");

        // Warm up the JIT so the timings measure the algorithm, not compilation.
        for (int i = 0; i < 3; i++) { timeQuadratic(500); }
        Random rnd = new Random(42);
        for (int i = 0; i < 3; i++) { timeNlogN(50_000, rnd); }

        System.out.println("--- O(N^2): naive double loop ---");
        System.out.printf("%-10s %-12s %-10s%n", "N", "time (ms)", "ratio");
        double prev = timeQuadratic(1000);
        System.out.printf("%-10d %-12.1f %-10s%n", 1000, prev, "-");
        for (int n = 2000; n <= 32000; n *= 2) {
            double t = timeQuadratic(n);
            System.out.printf("%-10d %-12.1f %-10.2f%n", n, t, t / prev);
            prev = t;
        }

        System.out.println("\n--- O(N log N): Arrays.sort ---");
        System.out.printf("%-10s %-12s %-10s%n", "N", "time (ms)", "ratio");
        prev = timeNlogN(100_000, rnd);
        System.out.printf("%-10d %-12.1f %-10s%n", 100_000, prev, "-");
        for (int n = 200_000; n <= 1_600_000; n *= 2) {
            double t = timeNlogN(n, rnd);
            System.out.printf("%-10d %-12.1f %-10.2f%n", n, t, t / prev);
            prev = t;
        }

        System.out.println("\nTakeaway: watch the ratios settle toward ~4.0 and ~2.0 —");
        System.out.println("that is the doubling hypothesis identifying the growth rate.");
    }
}
