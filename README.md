# BST vs Hash Table Performance Analysis

## 1. Objective

This Java project compares a Binary Search Tree (BST) and a hash table. It
measures insertion, searching, and deletion with 100, 1,000, and 10,000
integer keys.

## 2. Implementation

`BST.java` stores smaller keys in the left subtree and larger keys in the
right subtree. It uses an ordinary BST that does not balance itself.
Deletion handles nodes with no children, one child, or two children. For two
children, it replaces the key with the smallest key in the right subtree.

`HashTable.java` uses an array of buckets. A remainder calculation chooses
the bucket for each key. Keys that share a bucket form a linked list. This
handles collisions and is called separate chaining. The table does not resize.

Both structures provide `insert`, `search`, and `delete`. They ignore
duplicates and safely handle missing keys. `Main.java` checks correctness,
runs the experiment, prints the results, and saves them to `results.csv`.
The project uses standard Java without external libraries.

## 3. Time Complexity

Here, `n` is the number of stored keys. The table describes one operation.

| Structure | Operation | Best | Average / Expected | Worst |
|---|---|---|---|---|
| BST | Insertion | O(1) | O(log n) | O(n) |
| BST | Searching | O(1) | O(log n) | O(n) |
| BST | Deletion | O(1) | O(log n) | O(n) |
| Hash Table | Insertion | O(1) | O(1) | O(n) |
| Hash Table | Searching | O(1) | O(1) | O(n) |
| Hash Table | Deletion | O(1) | O(1) | O(n) |

BST best cases include inserting into an empty tree, finding the root, or
deleting a root with at most one child. Average O(log n) assumes a reasonably
short tree, as expected with random insertion order. An ordinary BST can
become unbalanced, making operations O(n).

Hash table operations are expected O(1) when keys are spread well and there
are enough buckets to keep chains short. Best cases need only one bucket
and no long scan. If many keys collide, operations can take O(n).
These bounds describe growth in work, not exact execution times.

## 4. Results

Both structures receive the same distinct keys and shuffled orders. Each
operation uses a fresh structure. Searches and deletions use existing keys.
Data preparation and setup happen outside the timers. The hash table uses
`n / 2` buckets, giving two keys per bucket when full.

For each size, the program runs 10 warm-up trials and averages 30 measured
trials using `System.nanoTime()`. The values below come from `results.csv`.
They are milliseconds for a **whole batch of n operations**, not one operation.

| Elements | Operation | BST (ms) | Hash Table (ms) |
|---|---|---|---|
| 100 | Insertion | 0.008517 | 0.004503 |
| 100 | Searching | 0.009053 | 0.004170 |
| 100 | Deletion | 0.067593 | 0.004150 |
| 1,000 | Insertion | 0.103933 | 0.047193 |
| 1,000 | Searching | 0.094073 | 0.032137 |
| 1,000 | Deletion | 0.090950 | 0.023947 |
| 10,000 | Insertion | 1.443270 | 0.168520 |
| 10,000 | Searching | 1.462270 | 0.152060 |
| 10,000 | Deletion | 1.287590 | 0.115467 |

## 5. Conclusion

The hash table was faster in all nine comparisons in this experiment. Its
short bucket chains help explain this result: it checks only a few keys,
while the BST follows a path through the tree.

This does not mean the hash table always wins. Tree shape, collisions, memory
allocation, how Java runs the code, and measurement noise can change the
times. The results support the expected behavior for this input, but do not
prove the complexity bounds.

## 6. How to Run

Use a JDK version 8 or newer, with `javac` and `java` on PATH. Open a terminal
in the project folder and run:

```text
javac Main.java BST.java HashTable.java
java Main
```

Each run checks correctness and replaces `results.csv` with new measurements.
If you run it again, update this README's results and conclusion to match.
