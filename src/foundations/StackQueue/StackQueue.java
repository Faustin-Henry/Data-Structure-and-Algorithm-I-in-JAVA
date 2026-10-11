package foundations.StackQueue;

/**
 * StackQueue — the two classic "bag of tricks" ADTs, side by side.
 *
 * <p><b>Stack (LIFO)</b> implemented with a <em>resizing array</em>. Amortized cost is
 * constant per operation: we double the array when it fills and halve it when it is
 * one-quarter full (halving at 1/4 rather than 1/2 avoids "thrashing" — repeatedly
 * resizing on push/pop around the boundary).</p>
 *
 * <p><b>Queue (FIFO)</b> implemented with a <em>singly linked list</em> keeping both a
 * {@code first} (dequeue end) and a {@code last} (enqueue end) reference, so both ends
 * are O(1). Each node holds one item and one link.</p>
 *
 * <p>Both implementations are generic, iterable, and reject {@code null} items (a null
 * would collide with the "empty" signal from {@code pop}/{@code dequeue}).</p>
 */
public class StackQueue {

    /** Resizing-array stack. push/pop/peek/isEmpty/size — all O(1) amortized. */
    public static class Stack<E> implements Iterable<E> {
        private E[] a;          // holds the items; a[0..n-1]
        private int n;          // number of items

        @SuppressWarnings("unchecked")
        public Stack() { a = (E[]) new Object[2]; }

        public boolean isEmpty() { return n == 0; }
        public int size()        { return n; }

        private void resize(int capacity) {
            @SuppressWarnings("unchecked")
            E[] copy = (E[]) new Object[capacity];
            for (int i = 0; i < n; i++) copy[i] = a[i];
            a = copy;
        }

        /** Push: double the array when it is full. */
        public void push(E item) {
            if (item == null) throw new IllegalArgumentException("null item");
            if (n == a.length) resize(2 * a.length);
            a[n++] = item;
        }

        /** Pop: halve the array when it drops to one-quarter full. */
        public E pop() {
            if (isEmpty()) throw new java.util.NoSuchElementException("stack underflow");
            E item = a[--n];
            a[n] = null;                                   // avoid loitering (memory leak)
            if (n > 0 && n == a.length / 4) resize(a.length / 2);
            return item;
        }

        public E peek() {
            if (isEmpty()) throw new java.util.NoSuchElementException("stack underflow");
            return a[n - 1];
        }

        @Override
        public java.util.Iterator<E> iterator() {
            return new java.util.Iterator<E>() {
                private int i = n;
                @Override public boolean hasNext() { return i > 0; }
                @Override public E next() {
                    if (!hasNext()) throw new java.util.NoSuchElementException();
                    return a[--i];
                }
            };
        }
    }

    /** Linked-list queue. enqueue/dequeue/peek — all O(1). */
    public static class Queue<E> implements Iterable<E> {
        private static class Node<E> {
            E item;
            Node<E> next;
        }
        private Node<E> first;  // dequeue from here (oldest)
        private Node<E> last;   // enqueue here (newest)
        private int n;

        public boolean isEmpty() { return first == null; }
        public int size()        { return n; }

        /** Enqueue: link the new node after the current last. */
        public void enqueue(E item) {
            if (item == null) throw new IllegalArgumentException("null item");
            Node<E> oldLast = last;
            last = new Node<>();
            last.item = item;
            last.next = null;
            if (isEmpty()) first = last;
            else           oldLast.next = last;
            n++;
        }

        /** Dequeue: advance first to the next node. */
        public E dequeue() {
            if (isEmpty()) throw new java.util.NoSuchElementException("queue underflow");
            E item = first.item;
            first = first.next;
            if (isEmpty()) last = null;        // keep the empty invariant first==last==null
            n--;
            return item;
        }

        public E peek() {
            if (isEmpty()) throw new java.util.NoSuchElementException("queue underflow");
            return first.item;
        }

        @Override
        public java.util.Iterator<E> iterator() {
            return new java.util.Iterator<E>() {
                private Node<E> current = first;
                @Override public boolean hasNext() { return current != null; }
                @Override public E next() {
                    if (!hasNext()) throw new java.util.NoSuchElementException();
                    E item = current.item;
                    current = current.next;
                    return item;
                }
            };
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Stack (LIFO) — resizing array ===");
        Stack<String> stack = new Stack<>();
        String[] words = { "to", "be", "or", "not", "to", "-", "be" };
        System.out.print("pushing:  ");
        for (String w : words) {
            if (w.equals("-")) System.out.print("(" + stack.pop() + " popped) ");
            else { stack.push(w); System.out.print(w + " "); }
        }
        System.out.println("\n(" + stack.size() + " left on stack)");
        System.out.print("popping the rest in LIFO order: ");
        while (!stack.isEmpty()) System.out.print(stack.pop() + " ");
        System.out.println("\npeek would now fail on an empty stack — underflow is an exception, not null.");

        System.out.println("\n=== Queue (FIFO) — linked list ===");
        Queue<Integer> queue = new Queue<>();
        System.out.print("enqueue 1..5, dequeue 2:  ");
        for (int i = 1; i <= 5; i++) queue.enqueue(i);
        System.out.print(queue.dequeue() + " " + queue.dequeue() + " | remaining: ");
        for (int x : queue) System.out.print(x + " ");
        System.out.println("\npeek() sees " + queue.peek() + " (the front) without removing it.");

        System.out.println("\nTakeaway: a stack reverses order (LIFO) — think undo,");
        System.out.println("parsing, DFS. A queue preserves order (FIFO) — think");
        System.out.println("print jobs, breadth-first search, task scheduling.");
    }
}
