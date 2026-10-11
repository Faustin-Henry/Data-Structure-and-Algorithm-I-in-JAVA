package graphs.UndirectedGraphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * UndirectedGraph — graphs, depth-first search, breadth-first search, and connected
 * components, all in one file.
 *
 * <p>A graph is a set of vertices (0..V-1) plus edges. We store <em>adjacency lists</em>:
 * {@code adj[v]} lists every vertex adjacent to v. Space is O(V + E), and iterating over
 * v's neighbors costs O(degree(v)).</p>
 *
 * <ul>
 *   <li><b>DepthFirstSearch</b> — maze-style: go as deep as possible, backtrack when stuck.
 *       Marks every vertex reachable from the source. O(V + E).</li>
 *   <li><b>BreadthFirstSearch</b> — wavefront: visit vertices in order of distance from the
 *       source using a queue. Records {@code edgeTo} (the tree of shortest paths) and
 *       {@code distTo} (fewest edges from source). O(V + E).</li>
 *   <li><b>ConnectedComponents</b> — run DFS from every unmarked vertex; each run labels
 *       one component. Answers "are v and w connected?" in O(1) after O(V + E) setup.</li>
 * </ul>
 */
public class UndirectedGraph {

    /** An undirected graph using adjacency lists. */
    public static class Graph {
        private final int V;
        private int E;
        private final List<List<Integer>> adj;

        public Graph(int V) {
            if (V < 0) throw new IllegalArgumentException("vertex count must be >= 0");
            this.V = V;
            this.E = 0;
            adj = new ArrayList<>(V);
            for (int v = 0; v < V; v++) adj.add(new ArrayList<>());
        }

        public int V() { return V; }
        public int E() { return E; }

        private void validate(int v) {
            if (v < 0 || v >= V) throw new IllegalArgumentException("vertex " + v + " out of bounds");
        }

        /** Add an undirected edge v-w (appears in both adjacency lists). */
        public void addEdge(int v, int w) {
            validate(v); validate(w);
            adj.get(v).add(w);
            adj.get(w).add(v);
            E++;
        }

        public Iterable<Integer> adj(int v) { validate(v); return adj.get(v); }

        public int degree(int v) { validate(v); return adj.get(v).size(); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(V + " vertices, " + E + " edges\n");
            for (int v = 0; v < V; v++) {
                sb.append(v).append(": ");
                for (int w : adj(v)) sb.append(w).append(" ");
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /** Depth-first search from a source: which vertices are reachable? */
    public static class DepthFirstSearch {
        private final boolean[] marked;
        private int count;

        public DepthFirstSearch(Graph G, int s) {
            marked = new boolean[G.V()];
            validate(G, s);
            dfs(G, s);
        }

        private void dfs(Graph G, int v) {
            marked[v] = true;
            count++;
            for (int w : G.adj(v))
                if (!marked[w]) dfs(G, w);            // go deep before going wide
        }

        public boolean marked(int v) { return marked[v]; }
        public int count()            { return count; }

        private static void validate(Graph G, int s) {
            if (s < 0 || s >= G.V()) throw new IllegalArgumentException("source out of bounds");
        }
    }

    /**
     * Breadth-first search from a source: shortest paths (fewest edges) to every
     * reachable vertex. The queue processes vertices in distance order, so the first
     * time we reach w we have found a shortest path; {@code edgeTo[w]} remembers
     * the previous vertex on that path.
     */
    public static class BreadthFirstSearch {
        private final boolean[] marked;
        private final int[] edgeTo;     // edgeTo[w] = previous vertex on shortest s->w path
        private final int[] distTo;     // distTo[w] = # edges on shortest s->w path
        private final int s;

        public BreadthFirstSearch(Graph G, int s) {
            this.s = s;
            marked = new boolean[G.V()];
            edgeTo = new int[G.V()];
            distTo = new int[G.V()];
            for (int v = 0; v < G.V(); v++) distTo[v] = Integer.MAX_VALUE;
            validate(G, s);
            bfs(G, s);
        }

        private void bfs(Graph G, int s) {
            Queue<Integer> q = new ArrayDeque<>();
            marked[s] = true;
            distTo[s] = 0;
            q.add(s);
            while (!q.isEmpty()) {
                int v = q.remove();
                for (int w : G.adj(v)) {
                    if (!marked[w]) {                 // first visit = shortest path found
                        marked[w] = true;
                        edgeTo[w] = v;
                        distTo[w] = distTo[v] + 1;
                        q.add(w);
                    }
                }
            }
        }

        public boolean hasPathTo(int v) { return marked[v]; }
        public int distTo(int v)        { return distTo[v]; }

        /** Reconstruct the shortest path by walking edgeTo[] backwards from v to s. */
        public Iterable<Integer> pathTo(int v) {
            if (!hasPathTo(v)) return null;
            Deque<Integer> path = new ArrayDeque<>();
            for (int x = v; x != s; x = edgeTo[x]) path.push(x);
            path.push(s);
            return path;
        }

        private static void validate(Graph G, int s) {
            if (s < 0 || s >= G.V()) throw new IllegalArgumentException("source out of bounds");
        }
    }

    /** Labels every vertex with its component id via repeated DFS. */
    public static class ConnectedComponents {
        private final boolean[] marked;
        private final int[] id;         // id[v] = component number of v
        private int count;

        public ConnectedComponents(Graph G) {
            marked = new boolean[G.V()];
            id = new int[G.V()];
            for (int v = 0; v < G.V(); v++) {
                if (!marked[v]) {
                    dfs(G, v);                        // one DFS per component
                    count++;
                }
            }
        }

        private void dfs(Graph G, int v) {
            marked[v] = true;
            id[v] = count;
            for (int w : G.adj(v))
                if (!marked[w]) dfs(G, w);
        }

        public int count()              { return count; }
        public int id(int v)            { return id[v]; }
        public boolean connected(int v, int w) { return id[v] == id[w]; }
    }

    // ------------------------------------------------------------------
    // Demo — the textbook's 13-vertex example graph
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Undirected graphs: DFS, BFS, components ===\n");

        Graph G = new Graph(13);
        int[][] edges = { {0,5},{4,3},{0,1},{9,12},{6,4},{5,4},{0,2},{11,12},
                          {9,10},{0,6},{7,8},{9,11},{5,3} };
        for (int[] e : edges) G.addEdge(e[0], e[1]);
        System.out.println(G);

        System.out.println("--- DFS from 0: what is reachable? ---");
        DepthFirstSearch dfs = new DepthFirstSearch(G, 0);
        System.out.print("reachable: ");
        for (int v = 0; v < G.V(); v++) if (dfs.marked(v)) System.out.print(v + " ");
        System.out.println("\ncount = " + dfs.count());

        System.out.println("\n--- BFS from 0: shortest paths ---");
        BreadthFirstSearch bfs = new BreadthFirstSearch(G, 0);
        for (int v = 0; v < G.V(); v++) {
            if (bfs.hasPathTo(v)) {
                System.out.print("0 to " + v + " (dist " + bfs.distTo(v) + "): ");
                for (int x : bfs.pathTo(v)) System.out.print(x + (x == v ? "" : "-"));
                System.out.println();
            } else {
                System.out.println("0 to " + v + ": not connected");
            }
        }

        System.out.println("\n--- Connected components ---");
        ConnectedComponents cc = new ConnectedComponents(G);
        System.out.println(cc.count() + " components");
        for (int v = 0; v < G.V(); v++)
            System.out.println("  vertex " + v + " -> component " + cc.id(v));
        System.out.println("connected(0, 9)? " + cc.connected(0, 9));
        System.out.println("connected(9, 12)? " + cc.connected(9, 12));

        System.out.println("\nTakeaway: DFS answers reachability, BFS gives shortest");
        System.out.println("(fewest-edge) paths, and repeated DFS partitions the graph");
        System.out.println("into components — all in linear O(V + E) time.");
    }
}
