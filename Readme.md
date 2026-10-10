# Data Structures & Algorithms in Java ☕

A collection of Java implementations covering core data structures and algorithm concepts, built alongside coursework in CS2920 at the University of Prince Edward Island. 
The reference textbook is Algorithms, 4th ed. by Robert Sedgewick and Kevin Wayne.

Topics Covered

Foundations
    Algorithm Analysis — Big-O notation, time, and space complexity
    Stacks & Queues — Array and linked-list based implementations
    Union-Find — Quick Union, Weighted Union-Find with path compression

Sorting Algorithms
    Mergesort — Top-down and bottom-up approaches
    Quicksort — Partitioning, 3-way quicksort
    Heapsort — In-place sorting using a binary heap

Priority Queues
    Binary Heaps — Min-heap and max-heap implementations
    Priority Queue ADT — Insert, delMax, and swim/sink operations

Symbol Tables & Search Trees
    Binary Search Trees (BST) — Insertion, search, floor, rank, deletion (Hibbard)
    2-3 Trees — Balanced search tree with 2-nodes and 3-nodes
    Left-Leaning Red-Black Trees (LLRB) — Insertion and deletion with rotations
    Red-Black Tree Deletion — Full deletion including fixup operations

Hash Tables
    Separate Chaining — Linked-list buckets
    Linear Probing — Open addressing with Robin Hood hashing

Graph Algorithms
    Undirected Graphs — BFS, DFS, connected components
    Directed Graphs — Topological sort, Kosaraju's strongly connected components (SCC)
    Minimum Spanning Trees — Kruskal's algorithm
    Shortest Paths — Dijkstra's algorithm


📁 Structure
src/
├── foundations/
│   ├── StackQueue/
│   └── UnionFind/
├── sorting/
│   ├── Mergesort/
│   ├── Quicksort/
│   └── Heapsort/
├── priority_queues/
│   └── BinaryHeap/
├── symbol_tables/
│   ├── BST/
│   ├── TwoThreeTrees/
│   └── RedBlackTrees/
├── hash_tables/
│   ├── SeparateChaining/
│   └── LinearProbing/
└── graphs/
    ├── UndirectedGraphs/
    ├── DirectedGraphs/
    ├── MST/
    └── ShortestPaths/

🚀 Getting Started
Requirements: Java 11 or later
bash# Clone the repository
git clone https://github.com/Faustin-Henry/Data-Structure-and-Algorithm-I-in-JAVA.git
cd Data-Structure-and-Algorithm-I-in-JAVA

# Compile a specific file
javac src/sorting/Mergesort/Mergesort.java

# Run it
java -cp src sorting.Mergesort.Mergesort

## 📖 Reference

Sedgewick, R. & Wayne, K. — Algorithms, 4th Edition (Princeton University Press)
Course: CS2920, University of Prince Edward Island
