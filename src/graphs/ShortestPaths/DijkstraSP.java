package graphs.ShortestPaths;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * DijkstraSP — single-source shortest paths in a directed graph with non-negative
 * edge weights, via Dijkstra's algorithm.
 *
 * <p><b>The idea:</b> keep for each vertex v the best known distance {@code distTo[v]}
 * from the source. Repeatedly <em>relax</em> the closest unsettled vertex — take the
 * unsettled vertex with the smallest distTo, and for each outgoing edge v→w check
 * whether going through v improves distTo[w]:</p>
 *
 * <pre>
 *   relax(v -> w):  if distTo[w] > distTo[v] + weight   // a shorter path via v?
 *                       distTo[w] = distTo[v] + weight   // ...then update it
 *                       edgeTo[w] = v -> w               // and remember how we got there
 * </pre>
 *
 * <p>Because weights are non-negative, the first time a vertex leaves the priority queue
 * its distance is final — no later path can beat it. Each edge is relaxed once per
 * endpoint visit: O(E log V) with a binary-heap priority queue.</p>
 *
 * <p>This file is self-contained: it includes minimal {@code DirectedEdge},
 * {@code EdgeWeightedDigraph}, and a binary-heap {@code IndexMinPQ} (an indexed priority
 * queue keyed by vertex, supporting decrease-key — the operation Dijkstra needs).</p>
 */
public class DijkstraSP {

    /** A weighted directed edge v -> w. */
    public static class DirectedEdge {
        private final int v, w;
        private final double weight;

        public DirectedEdge(int v, int w, double weight) {
            if (v < 0 || w < 0) throw new IllegalArgumentException("vertex index must be >= 0");
            if (Double.isNaN(weight)) throw new IllegalArgumentException("weight is NaN");
            if (weight < 0) throw new IllegalArgumentException("Dijkstra needs non-negative weights");
            this.v = v; this.w = w; this.weight = weight;
        }

        public int from()         { return v; }
        public int to()           { return w; }
        public double weight()    { return weight; }

        @Override public String toString() { return v + "->" + w + " " + String.format("%.2f", weight); }
    }

    /** An edge-weighted digraph (adjacency lists of DirectedEdges). */
    public static class EdgeWeightedDigraph {
        private final int V;
        private int E;
        private final List<List<DirectedEdge>> adj;

        public EdgeWeightedDigraph(int V) {
            this.V = V;
            adj = new ArrayList<>(V);
            for (int v = 0; v < V; v++) adj.add(new ArrayList<>());
        }

        public int V() { return V; }
        public int E() { return E; }

        public void addEdge(DirectedEdge e) { adj.get(e.from()).add(e); E++; }
        public Iterable<DirectedEdge> adj(int v) { return adj.get(v); }

        public Iterable<DirectedEdge> edges() {
            List<DirectedEdge> list = new ArrayList<>();
            for (int v = 0; v < V; v++) list.addAll(adj.get(v));
            return list;
        }
    }

    /**
     * IndexMinPQ — a binary heap keyed by vertex index, with decrease-key.
     * Three parallel arrays keep everything in sync:
     *   pq[i]   = vertex at heap position i        (the heap)
     *   qp[v]   = heap position of vertex v        (the inverse index; -1 = absent)
     *   keys[v] = priority of vertex v             (here: distTo[v])
     * changeKey lowers a vertex's key and swims it up; delMin removes the top.
     */
    public static class IndexMinPQ {
        private final int maxN;
        private int n;
        private final int[] pq;        // binary heap, 1-based: pq[1..n]
        private final int[] qp;        // inverse: qp[vertex] = heap position
        private final double[] keys;   // keys[vertex] = priority

        public IndexMinPQ(int maxN) {
            this.maxN = maxN;
            n = 0;
            pq = new int[maxN + 1];
            qp = new int[maxN + 1];
            keys = new double[maxN + 1];
            for (int i = 0; i <= maxN; i++) qp[i] = -1;
        }

        public boolean isEmpty()      { return n == 0; }
        public boolean contains(int i){ validate(i); return qp[i] != -1; }

        public void insert(int i, double key) {
            validate(i);
            if (contains(i)) throw new IllegalArgumentException("index already in pq");
            n++;
            qp[i] = n;
            pq[n] = i;
            keys[i] = key;
            swim(n);
        }

        public int delMin() {
            if (isEmpty()) throw new NoSuchElementException("pq underflow");
            int min = pq[1];
            exch(1, n--);
            sink(1);
            qp[min] = -1;              // mark absent
            pq[n + 1] = -1;
            return min;
        }

        /** Decrease the key of vertex i (Dijkstra only ever lowers distances). */
        public void decreaseKey(int i, double key) {
            validate(i);
            if (!contains(i)) throw new NoSuchElementException("index not in pq");
            if (keys[i] <= key) throw new IllegalArgumentException("key did not decrease");
            keys[i] = key;
            swim(qp[i]);               // a smaller key can only move up
        }

        private void swim(int k) {
            while (k > 1 && greater(k / 2, k)) { exch(k, k / 2); k = k / 2; }
        }

        private void sink(int k) {
            while (2 * k <= n) {
                int j = 2 * k;
                if (j < n && greater(j, j + 1)) j++;
                if (!greater(k, j)) break;
                exch(k, j);
                k = j;
            }
        }

        private boolean greater(int i, int j) { return keys[pq[i]] > keys[pq[j]]; }

        private void exch(int i, int j) {
            int swap = pq[i]; pq[i] = pq[j]; pq[j] = swap;
            qp[pq[i]] = i;  qp[pq[j]] = j;    // keep the inverse index in sync!
        }

        private void validate(int i) {
            if (i < 0 || i >= maxN) throw new IllegalArgumentException("index out of bounds");
        }
    }

    // ------------------------------------------------------------------
    // Dijkstra's algorithm
    // ------------------------------------------------------------------
    private final double[] distTo;          // distTo[v] = shortest known s->v distance
    private final DirectedEdge[] edgeTo;    // edgeTo[v] = last edge on shortest s->v path
    private final IndexMinPQ pq;

    public DijkstraSP(EdgeWeightedDigraph G, int s) {
        distTo = new double[G.V()];
        edgeTo = new DirectedEdge[G.V()];
        for (int v = 0; v < G.V(); v++) distTo[v] = Double.POSITIVE_INFINITY;
        distTo[s] = 0.0;

        pq = new IndexMinPQ(G.V());
        pq.insert(s, 0.0);
        while (!pq.isEmpty()) {
            int v = pq.delMin();            // closest unsettled vertex: its distance is final
            for (DirectedEdge e : G.adj(v)) relax(e);
        }
    }

    /**
     * Relax edge e = v->w: if the path s~>v->w beats the best known s~>w path,
     * update distTo[w] and remember e. The priority queue keeps w keyed by its
     * (possibly improved) distance — decrease-key, or insert on first sight.
     */
    private void relax(DirectedEdge e) {
        int v = e.from(), w = e.to();
        if (distTo[w] > distTo[v] + e.weight()) {
            distTo[w] = distTo[v] + e.weight();
            edgeTo[w] = e;
            if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
            else                pq.insert(w, distTo[w]);
        }
    }

    public double distTo(int v)       { return distTo[v]; }
    public boolean hasPathTo(int v)  { return distTo[v] < Double.POSITIVE_INFINITY; }

    /** Reconstruct the shortest path by following edgeTo[] back to the source. */
    public Iterable<DirectedEdge> pathTo(int v) {
        if (!hasPathTo(v)) return null;
        Deque<DirectedEdge> path = new ArrayDeque<>();
        for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from()]) path.push(e);
        return path;
    }

    // ------------------------------------------------------------------
    // Demo — the textbook's tinyEWD, shortest paths from vertex 0
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Dijkstra's shortest paths ===\n");

        EdgeWeightedDigraph G = new EdgeWeightedDigraph(8);
        double[][] data = {
            {4,5,.35},{5,4,.35},{4,7,.37},{5,7,.28},{7,5,.28},{5,1,.32},{0,4,.38},
            {0,2,.26},{7,3,.39},{1,3,.29},{2,7,.34},{6,2,.40},{3,6,.52},{6,0,.58},{6,4,.93}
        };
        for (double[] d : data) G.addEdge(new DirectedEdge((int) d[0], (int) d[1], d[2]));

        int s = 0;
        DijkstraSP sp = new DijkstraSP(G, s);
        System.out.println("shortest paths from " + s + ":");
        for (int v = 0; v < G.V(); v++) {
            if (sp.hasPathTo(v)) {
                System.out.printf("  to %d  dist = %.2f   path: ", v, sp.distTo(v));
                boolean first = true;
                for (DirectedEdge e : sp.pathTo(v)) {
                    System.out.print((first ? "" : " + ") + e);
                    first = false;
                }
                if (first) System.out.print("(source)");
                System.out.println();
            } else {
                System.out.println("  to " + v + ": unreachable");
            }
        }

        System.out.println("\nWatch the key step for vertex 1: the direct-looking route");
        System.out.println("isn't the cheapest. Dijkstra settled 0->4 (0.38), then 4->5");
        System.out.println("(0.35), then 5->1 (0.32) for a total of 1.05 — each vertex");
        System.out.println("leaves the priority queue in order of increasing distance,");
        System.out.println("and with non-negative weights the first distance assigned");
        System.out.println("to a dequeued vertex is final: no undiscovered path can");
        System.out.println("beat it, because every extension only adds weight.");

        System.out.println("\nTakeaway: relaxation + a min-PQ = O(E log V) shortest paths.");
        System.out.println("Negative weights break the 'final when dequeued' argument —");
        System.out.println("that's what Bellman-Ford is for. Non-negative weights?");
        System.out.println("Dijkstra is the answer (GPS routing, network latency, ...).");
    }
}
