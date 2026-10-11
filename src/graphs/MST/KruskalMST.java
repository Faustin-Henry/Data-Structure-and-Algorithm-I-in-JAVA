package graphs.MST;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * KruskalMST — minimum spanning tree via Kruskal's algorithm (greedy + union-find).
 *
 * <p>A <b>spanning tree</b> connects all V vertices with V-1 edges; the <b>minimum</b>
 * spanning tree minimizes the total edge weight. Kruskal's idea, in one sentence:</p>
 *
 * <p><em>Consider the edges in increasing weight order; add an edge unless it would
 * create a cycle.</em></p>
 *
 * <p>The "would it create a cycle?" test is exactly what union-find answers: an edge
 * v–w creates a cycle iff v and w are already connected in the growing forest. The
 * cut property guarantees the greedy choice is optimal. Cost: O(E log E) to sort the
 * edges, plus near-constant union-find operations.</p>
 *
 * <p>This file is self-contained: it includes minimal {@code Edge},
 * {@code EdgeWeightedGraph}, and {@code UnionFind} implementations so the MST topic
 * can be studied on its own.</p>
 */
public class KruskalMST {

    /** A weighted undirected edge v-w. Compares by weight (for sorting). */
    public static class Edge implements Comparable<Edge> {
        private final int v, w;
        private final double weight;

        public Edge(int v, int w, double weight) {
            if (v < 0 || w < 0) throw new IllegalArgumentException("vertex index must be >= 0");
            if (Double.isNaN(weight)) throw new IllegalArgumentException("weight is NaN");
            this.v = v; this.w = w; this.weight = weight;
        }

        public double weight()          { return weight; }
        public int either()             { return v; }
        public int other(int vertex) {
            if      (vertex == v) return w;
            else if (vertex == w) return v;
            else throw new IllegalArgumentException("not an endpoint");
        }

        @Override public int compareTo(Edge that) { return Double.compare(weight, that.weight); }

        @Override public String toString() { return v + "-" + w + " " + String.format("%.2f", weight); }
    }

    /** An edge-weighted undirected graph (adjacency lists of Edges). */
    public static class EdgeWeightedGraph {
        private final int V;
        private int E;
        private final List<List<Edge>> adj;

        public EdgeWeightedGraph(int V) {
            this.V = V;
            adj = new ArrayList<>(V);
            for (int v = 0; v < V; v++) adj.add(new ArrayList<>());
        }

        public int V() { return V; }
        public int E() { return E; }

        public void addEdge(Edge e) {
            int v = e.either(), w = e.other(v);
            adj.get(v).add(e);
            adj.get(w).add(e);
            E++;
        }

        public Iterable<Edge> adj(int v) { return adj.get(v); }

        /** All edges, each exactly once (only from the lower endpoint's list). */
        public Iterable<Edge> edges() {
            List<Edge> list = new ArrayList<>();
            for (int v = 0; v < V; v++) {
                int selfLoops = 0;
                for (Edge e : adj(v)) {
                    if (e.other(v) > v) list.add(e);
                    else if (e.other(v) == v) {          // self-loop: add once
                        if (selfLoops % 2 == 0) list.add(e);
                        selfLoops++;
                    }
                }
            }
            return list;
        }
    }

    /** Minimal weighted quick-union with path compression (see foundations.UnionFind). */
    public static class UnionFind {
        private final int[] parent, size;
        private int count;

        public UnionFind(int n) {
            parent = new int[n]; size = new int[n]; count = n;
            for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        }

        public int find(int p) {
            while (p != parent[p]) { parent[p] = parent[parent[p]]; p = parent[p]; } // halving
            return p;
        }

        public boolean connected(int p, int q) { return find(p) == find(q); }

        public void union(int p, int q) {
            int i = find(p), j = find(q);
            if (i == j) return;
            if (size[i] < size[j]) { parent[i] = j; size[j] += size[i]; }
            else                   { parent[j] = i; size[i] += size[j]; }
            count--;
        }
    }

    // ------------------------------------------------------------------
    // Kruskal's algorithm
    // ------------------------------------------------------------------
    private final Queue<Edge> mst = new ArrayDeque<>();
    private double weight;

    public KruskalMST(EdgeWeightedGraph G) {
        // Sort all edges by weight — the greedy order.
        List<Edge> edges = new ArrayList<>();
        for (Edge e : G.edges()) edges.add(e);
        Collections.sort(edges);

        UnionFind uf = new UnionFind(G.V());
        for (Edge e : edges) {
            int v = e.either(), w = e.other(v);
            if (!uf.connected(v, w)) {      // no cycle: take the edge (cut property)
                uf.union(v, w);
                mst.add(e);
                weight += e.weight();
            }                               // else: skip — it would close a cycle
            if (mst.size() == G.V() - 1) break;   // a tree on V vertices has V-1 edges
        }
    }

    public Iterable<Edge> edges() { return mst; }
    public double weight()        { return weight; }

    // ------------------------------------------------------------------
    // Demo — the textbook's tinyEWG (8 vertices)
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Kruskal's MST ===\n");

        EdgeWeightedGraph G = new EdgeWeightedGraph(8);
        double[][] data = {
            {4,5,.35},{4,7,.37},{5,7,.28},{0,7,.16},{1,5,.32},{0,4,.38},
            {2,3,.17},{1,7,.19},{0,2,.26},{1,2,.36},{1,3,.29},{2,7,.34},
            {6,2,.40},{3,6,.52},{6,0,.58},{6,4,.93}
        };
        for (double[] d : data) G.addEdge(new Edge((int) d[0], (int) d[1], d[2]));

        KruskalMST mst = new KruskalMST(G);
        System.out.println("edges of the MST (taken in greedy order):");
        for (Edge e : mst.edges()) System.out.println("  " + e);
        System.out.printf("%ntotal weight = %.2f (the minimum possible)%n", mst.weight());

        System.out.println("\nNotice the greedy pattern: the cheapest edge that doesn't");
        System.out.println("close a cycle is always safe to take (the cut property).");
        System.out.println("Union-find is the cycle detector — an edge v-w is skipped");
        System.out.println("exactly when v and w are already connected.");
        System.out.println("\nTakeaway: sort edges, union-find to skip cycles. O(E log E),");
        System.out.println("optimal by the cut property. Used for network design,");
        System.out.println("clustering, and anywhere 'cheapest way to connect everything'.");
    }
}
