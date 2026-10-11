# Data Structures & Algorithms in Java ☕

![Java](https://img.shields.io/badge/Java-11%2B-blue)
![Course](https://img.shields.io/badge/Course-CS2920%20%40%20UPEI-green)

A collection of Java implementations covering core data structures and algorithm concepts, built alongside coursework in **CS2920** at the University of Prince Edward Island.
The reference textbook is *Algorithms, 4th ed.* by Robert Sedgewick and Kevin Wayne.

## Who is this for?

This repository is for anyone learning data structures and algorithms, and it assumes only a basic familiarity with programming syntax and semantics. The code is written in Java, but the concepts and logic are what matter, so it can be easily rewritten in most other languages. Every file has an explanatory `main()` demo — program output walks you through the behavior of each structure and algorithm step by step, making it easier to follow and understand.

## Topics Covered

### Foundations
- **Algorithm Analysis** — Big-O notation, growth rates, and a live doubling-ratio timing experiment.
- **Stacks & Queues** — Resizing-array stack and linked-list queue.
- **Union-Find** — Weighted quick-union with path compression.

### Sorting Algorithms
- **Mergesort** — Top-down and bottom-up approaches (stable, guaranteed N log N).
- **Quicksort** — Partitioning, plus 3-way quicksort for inputs with duplicate keys.
- **Heapsort** — In-place sorting using a binary heap (worst-case N log N).

### Priority Queues
- **Binary Heap (MaxPQ)** — Insert, delMax, swim/sink on a heap-ordered array.

### Symbol Tables & Search Trees
- **Binary Search Trees (BST)** — Full ordered symbol table: put/get, floor, rank, select, Hibbard deletion.
- **2-3 Trees** — Balanced search tree with 2-nodes and 3-nodes (insertion and search; splits cascade upward).
- **Left-Leaning Red-Black Trees (LLRB)** — The 2-3 tree encoded as a BST, with full insertion *and* deletion (rotations, color flips, moveRedLeft/moveRedRight fixups).

### Hash Tables
- **Separate Chaining** — Linked-list buckets with resizing.
- **Linear Probing** — Open addressing with cluster rehashing on delete.

### Graph Algorithms
- **Undirected Graphs** — DFS, BFS (shortest paths), connected components.
- **Directed Graphs** — Directed DFS/BFS, cycle detection, topological sort, Kosaraju's strongly connected components.
- **Minimum Spanning Trees** — Kruskal's algorithm (greedy + union-find).
- **Shortest Paths** — Dijkstra's algorithm with an indexed binary-heap priority queue.

## 📁 Structure

```
src/
├── foundations/
│   ├── AnalysisGuide.java
│   ├── StackQueue/
│   │   └── StackQueue.java
│   └── UnionFind/
│       └── UnionFind.java
├── sorting/
│   ├── Mergesort/
│   │   └── Mergesort.java
│   ├── Quicksort/
│   │   └── Quicksort.java
│   └── Heapsort/
│       └── Heapsort.java
├── priority_queues/
│   └── BinaryHeap/
│       └── MaxPQ.java
├── symbol_tables/
│   ├── BST/
│   │   └── BST.java
│   ├── TwoThreeTrees/
│   │   └── TwoThreeTree.java
│   └── RedBlackTrees/
│       └── RedBlackBST.java
├── hash_tables/
│   ├── SeparateChaining/
│   │   └── SeparateChainingHashST.java
│   └── LinearProbing/
│       └── LinearProbingHashST.java
└── graphs/
    ├── UndirectedGraphs/
    │   └── UndirectedGraph.java
    ├── DirectedGraphs/
    │   └── DirectedGraph.java
    ├── MST/
    │   └── KruskalMST.java
    └── ShortestPaths/
        └── DijkstraSP.java
```

Each topic folder is self-contained: the package declaration matches the folder path, and graph topics that need supporting structures (union-find, indexed priority queue) include minimal copies so you can study each file on its own.

## 🚀 Getting Started

Requirements: **Java 11 or later** (no external dependencies).

```bash
# Clone the repository
git clone https://github.com/Faustin-Henry/Data-Structure-and-Algorithm-I-in-JAVA.git
cd Data-Structure-and-Algorithm-I-in-JAVA
```

Compile and run any single file — for example, mergesort:

```bash
# Compile (class files land next to the sources)
javac src/sorting/Mergesort/Mergesort.java

# Run (package name = folder path with dots)
java -cp src sorting.Mergesort.Mergesort
```

Or keep build output tidy with `-d`:

```bash
javac -d out src/sorting/Mergesort/Mergesort.java
java -cp out sorting.Mergesort.Mergesort
```

The same pattern works for every file, e.g.:

```bash
javac -d out src/graphs/ShortestPaths/DijkstraSP.java
java -cp out graphs.ShortestPaths.DijkstraSP
```

## 📖 Reference

- Sedgewick, R. & Wayne, K. — *Algorithms, 4th Edition* (Princeton University Press). Companion site: [algs4.cs.princeton.edu](https://algs4.cs.princeton.edu)
- Course: CS2920, University of Prince Edward Island
