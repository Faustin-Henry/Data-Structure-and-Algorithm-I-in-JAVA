package foundations.UnionFind;

/**
 * UnionFind — dynamic connectivity via <b>weighted quick-union with path compression</b>.
 *
 * <p>We maintain a forest of trees: {@code parent[i]} is i's parent, and each root names one
 * connected component. Two ideas make the operations nearly constant time:</p>
 *
 * <ul>
 *   <li><b>Weighting</b> — when uniting two trees, always attach the <em>smaller</em> tree
 *       under the larger one's root. No tree ever grows taller than log N.</li>
 *   <li><b>Path compression</b> — every {@code find} re-links each visited node directly
 *       to the root, flattening the tree for future queries.</li>
 * </ul>
 *
 * <p>Amortized cost per operation is effectively O(1) — more precisely O(α(N)), where α is
 * the inverse Ackermann function (≤ 5 for any N you will ever see).</p>
 */
public class UnionFind {
    private final int[] parent;   // parent[i] = parent of i
    private final int[] size;     // size[i] = # of sites in tree rooted at i
    private int count;            // number of components

    public UnionFind(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        parent = new int[n];
        size   = new int[n];
        count  = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    public int count() { return count; }

    /**
     * Find the root of p's tree, compressing the path on the way up:
     * every node visited gets re-linked directly to the root it discovered.
     */
    public int find(int p) {
        validate(p);
        int root = p;
        while (root != parent[root]) root = parent[root];   // climb to the root
        while (p != root) {                                // second pass: flatten
            int next = parent[p];
            parent[p] = root;
            p = next;
        }
        return root;
    }

    /** Weighted union: link the smaller tree's root under the larger tree's root. */
    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if (rootP == rootQ) return;                        // already connected
        if (size[rootP] < size[rootQ]) {                  // attach small under large
            parent[rootP] = rootQ;
            size[rootQ] += size[rootP];
        } else {
            parent[rootQ] = rootP;
            size[rootP] += size[rootQ];
        }
        count--;
    }

    public boolean connected(int p, int q) { return find(p) == find(q); }

    private void validate(int p) {
        if (p < 0 || p >= parent.length)
            throw new IllegalArgumentException("index " + p + " out of bounds");
    }

    public static void main(String[] args) {
        // The classic example: 10 sites, a sequence of union operations.
        UnionFind uf = new UnionFind(10);
        int[][] pairs = { {4,3},{3,8},{6,5},{9,4},{2,1},{8,9},{5,0},{7,2},{6,1},{1,0},{6,7} };

        System.out.println("=== Union-Find: connecting 10 sites ===");
        System.out.println("start: " + uf.count() + " components\n");
        for (int[] pair : pairs) {
            int p = pair[0], q = pair[1];
            if (uf.connected(p, q)) {
                System.out.printf("union(%d, %d) -> already connected, skip%n", p, q);
            } else {
                uf.union(p, q);
                System.out.printf("union(%d, %d) -> merged; %d components left%n", p, q, uf.count());
            }
        }

        System.out.println("\nFinal check:");
        System.out.println("connected(0, 7)? " + uf.connected(0, 7) + "  (merged into one big component)");
        System.out.println("connected(0, 9)? " + uf.connected(0, 9) + "  (9 stayed in its own group)");
        System.out.println("components left: " + uf.count());
        System.out.println("\nTakeaway: union-find answers \"are p and q connected?\" in");
        System.out.println("essentially constant time — the backbone of Kruskal's MST");
        System.out.println("and percolation simulations.");
    }
}
