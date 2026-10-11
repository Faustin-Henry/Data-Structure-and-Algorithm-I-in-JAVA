package graphs.DirectedGraphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * DirectedGraph — digraphs: directed DFS/BFS, cycle detection, topological order,
 * and Kosaraju's strongly-connected-components algorithm.
 *
 * <p>A digraph's edges have direction: {@code addEdge(v, w)} means v→w only, so w's
 * adjacency list does <em>not</em> gain v. That one-sidedness makes reachability
 * asymmetric — and much more interesting.</p>
 *
 * <ul>
 *   <li><b>DirectedDFS</b> — vertices reachable from one source (or a set of sources).</li>
 *   <li><b>DirectedCycle</b> — DFS with an {@code onStack} array: a back edge to a vertex
 *       currently on the recursion stack proves a directed cycle. O(V + E).</li>
 *   <li><b>Topological</b> — if the digraph is a DAG, ordering vertices by
 *       <em>reverse postorder</em> of DFS (the order vertices finish) yields a topological
 *       order: every edge v→w has v before w. O(V + E). Classic use: task scheduling.</li>
 *   <li><b>KosarajuSCC</b> — strongly connected components in O(V + E):
 *       <ol>
 *         <li>compute reverse postorder of the <em>reverse</em> graph,</li>
 *         <li>run DFS on the original graph visiting vertices in that order — each DFS
 *             tree is exactly one SCC.</li>
 *       </ol>
 *       Why it works: processing in reverse-postorder of G<sup>R</sup> guarantees we start
 *       each DFS inside a "sink" SCC of the component DAG, so the search can't leak out.</li>
 * </ul>
 */
public class DirectedGraph {

    /** A directed graph using adjacency lists. */
    public static class Digraph {
        private final int V;
        private int E;
        private final List<List<Integer>> adj;

        public Digraph(int V) {
            if (V < 0) throw new IllegalArgumentException("vertex count must be >= 0");
            this.V = V;
            adj = new ArrayList<>(V);
            for (int v = 0; v < V; v++) adj.add(new ArrayList<>());
        }

        public int V() { return V; }
        public int E() { return E; }

        private void validate(int v) {
            if (v < 0 || v >= V) throw new IllegalArgumentException("vertex " + v + " out of bounds");
        }

        /** Add the directed edge v -> w (only v's list changes). */
        public void addEdge(int v, int w) {
            validate(v); validate(w);
            adj.get(v).add(w);
            E++;
        }

        public Iterable<Integer> adj(int v) { validate(v); return adj.get(v); }

        /** The reverse graph: every edge v->w becomes w->v. O(V + E). */
        public Digraph reverse() {
            Digraph R = new Digraph(V);
            for (int v = 0; v < V; v++)
                for (int w : adj(v)) R.addEdge(w, v);
            return R;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(V + " vertices, " + E + " edges\n");
            for (int v = 0; v < V; v++) {
                sb.append(v).append(" -> ");
                for (int w : adj(v)) sb.append(w).append(" ");
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /** Depth-first search on a digraph from a single source or a set of sources. */
    public static class DirectedDFS {
        private final boolean[] marked;
        private int count;

        public DirectedDFS(Digraph G, int s) {
            marked = new boolean[G.V()];
            validate(G, s);
            dfs(G, s);
        }

        public DirectedDFS(Digraph G, Iterable<Integer> sources) {
            marked = new boolean[G.V()];
            for (int s : sources) { validate(G, s); if (!marked[s]) dfs(G, s); }
        }

        private void dfs(Digraph G, int v) {
            marked[v] = true;
            count++;
            for (int w : G.adj(v))
                if (!marked[w]) dfs(G, w);
        }

        public boolean marked(int v) { return marked[v]; }
        public int count()            { return count; }

        private static void validate(Digraph G, int s) {
            if (s < 0 || s >= G.V()) throw new IllegalArgumentException("source out of bounds");
        }
    }

    /** Breadth-first directed paths: shortest directed paths from a source. */
    public static class BreadthFirstDirectedPaths {
        private final boolean[] marked;
        private final int[] edgeTo;
        private final int s;

        public BreadthFirstDirectedPaths(Digraph G, int s) {
            this.s = s;
            marked = new boolean[G.V()];
            edgeTo = new int[G.V()];
            Queue<Integer> q = new ArrayDeque<>();
            marked[s] = true;
            q.add(s);
            while (!q.isEmpty()) {
                int v = q.remove();
                for (int w : G.adj(v))
                    if (!marked[w]) {
                        marked[w] = true;
                        edgeTo[w] = v;
                        q.add(w);
                    }
            }
        }

        public boolean hasPathTo(int v) { return marked[v]; }

        public Iterable<Integer> pathTo(int v) {
            if (!hasPathTo(v)) return null;
            Deque<Integer> path = new ArrayDeque<>();
            for (int x = v; x != s; x = edgeTo[x]) path.push(x);
            path.push(s);
            return path;
        }
    }

    /** Detects a directed cycle using DFS + "on the recursion stack" tracking. */
    public static class DirectedCycle {
        private final boolean[] marked;
        private final boolean[] onStack;   // vertices on the current DFS path
        private final int[] edgeTo;
        private Deque<Integer> cycle;      // the cycle, if found

        public DirectedCycle(Digraph G) {
            marked = new boolean[G.V()];
            onStack = new boolean[G.V()];
            edgeTo = new int[G.V()];
            for (int v = 0; v < G.V(); v++)
                if (!marked[v]) dfs(G, v);
        }

        private void dfs(Digraph G, int v) {
            onStack[v] = true;
            marked[v] = true;
            for (int w : G.adj(v)) {
                if (cycle != null) return;                    // stop once found
                else if (!marked[w]) { edgeTo[w] = v; dfs(G, w); }
                else if (onStack[w]) {                       // back edge v->w: cycle!
                    cycle = new ArrayDeque<>();
                    for (int x = v; x != w; x = edgeTo[x]) cycle.push(x);
                    cycle.push(w);
                    cycle.push(v);
                }
            }
            onStack[v] = false;                              // leaving v's subtree
        }

        public boolean hasCycle() { return cycle != null; }
        public Iterable<Integer> cycle() { return cycle; }
    }

    /**
     * Depth-first vertex ordering: preorder, postorder, and reverse postorder.
     * Reverse postorder of a DAG is a topological order.
     */
    public static class DepthFirstOrder {
        private final boolean[] marked;
        private final Queue<Integer> pre = new ArrayDeque<>();
        private final Queue<Integer> post = new ArrayDeque<>();
        private final Deque<Integer> reversePost = new ArrayDeque<>();

        public DepthFirstOrder(Digraph G) {
            marked = new boolean[G.V()];
            for (int v = 0; v < G.V(); v++)
                if (!marked[v]) dfs(G, v);
        }

        private void dfs(Digraph G, int v) {
            marked[v] = true;
            pre.add(v);
            for (int w : G.adj(v))
                if (!marked[w]) dfs(G, w);
            post.add(v);
            reversePost.push(v);          // push when finished -> reverse of finish order
        }

        public Iterable<Integer> pre()         { return pre; }
        public Iterable<Integer> post()        { return post; }
        public Iterable<Integer> reversePost() { return reversePost; }
    }

    /** Topological order of a DAG (null if the digraph has a cycle). */
    public static class Topological {
        private final Iterable<Integer> order;

        public Topological(Digraph G) {
            DirectedCycle cycle = new DirectedCycle(G);
            order = cycle.hasCycle() ? null : new DepthFirstOrder(G).reversePost();
        }

        public boolean isDAG()              { return order != null; }
        public Iterable<Integer> order()    { return order; }
    }

    /** Kosaraju's algorithm: strongly connected components in O(V + E). */
    public static class KosarajuSCC {
        private final boolean[] marked;
        private final int[] id;
        private int count;

        public KosarajuSCC(Digraph G) {
            marked = new boolean[G.V()];
            id = new int[G.V()];
            // Pass 1: reverse postorder of the REVERSE graph.
            DepthFirstOrder order = new DepthFirstOrder(G.reverse());
            // Pass 2: DFS on G in that order — each tree is one SCC.
            for (int v : order.reversePost()) {
                if (!marked[v]) { dfs(G, v); count++; }
            }
        }

        private void dfs(Digraph G, int v) {
            marked[v] = true;
            id[v] = count;
            for (int w : G.adj(v))
                if (!marked[w]) dfs(G, w);
        }

        public int count()                   { return count; }
        public int id(int v)                 { return id[v]; }
        public boolean stronglyConnected(int v, int w) { return id[v] == id[w]; }
    }

    // ------------------------------------------------------------------
    // Demo
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Directed graphs ===\n");

        // A small DAG: course prerequisites (edge v->w means "take v before w").
        Digraph dag = new Digraph(7);
        int[][] prereqs = { {0,1},{0,5},{1,2},{2,3},{3,4},{5,2},{5,6} };
        for (int[] e : prereqs) dag.addEdge(e[0], e[1]);
        System.out.println("prerequisite DAG:\n" + dag);

        Topological topo = new Topological(dag);
        System.out.println("is a DAG? " + topo.isDAG());
        System.out.print("topological order (a valid course sequence): ");
        for (int v : topo.order()) System.out.print(v + " ");
        System.out.println("\n(every prerequisite appears before the course that needs it)");

        System.out.println("\n--- shortest directed path 0 -> 4 ---");
        BreadthFirstDirectedPaths bfp = new BreadthFirstDirectedPaths(dag, 0);
        System.out.print("path: ");
        for (int x : bfp.pathTo(4)) System.out.print(x + " ");
        System.out.println();

        System.out.println("\n--- cycle detection ---");
        Digraph cyclic = new Digraph(4);
        cyclic.addEdge(0, 1); cyclic.addEdge(1, 2); cyclic.addEdge(2, 0); cyclic.addEdge(2, 3);
        DirectedCycle dc = new DirectedCycle(cyclic);
        System.out.println("cyclic graph has a cycle? " + dc.hasCycle());
        if (dc.hasCycle()) {
            System.out.print("one cycle: ");
            for (int x : dc.cycle()) System.out.print(x + " ");
            System.out.println();
        }
        System.out.println("DAG has a cycle? " + new DirectedCycle(dag).hasCycle());

        System.out.println("\n--- Kosaraju SCC on the cyclic graph ---");
        KosarajuSCC scc = new KosarajuSCC(cyclic);
        System.out.println(scc.count() + " strongly connected components:");
        for (int v = 0; v < cyclic.V(); v++)
            System.out.println("  vertex " + v + " -> component " + scc.id(v));
        System.out.println("stronglyConnected(0, 2)? " + scc.stronglyConnected(0, 2)
            + "   stronglyConnected(0, 3)? " + scc.stronglyConnected(0, 3));

        System.out.println("\nTakeaway: direction changes everything. DFS on a digraph");
        System.out.println("finds reachability; reverse postorder sorts a DAG;");
        System.out.println("Kosaraju finds mutually-reachable groups — all linear time.");
    }
}
