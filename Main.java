import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

public class Main {
    private static final int WARMUP_REPETITIONS = 10;
    private static final int REPETITIONS = 30;

    public static void main(String[] args) throws IOException {
        runCorrectnessChecks();
        System.out.println("All correctness checks passed.");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Times are average milliseconds for a whole batch of n operations.");
        System.out.println("Warm-up trials per size: " + WARMUP_REPETITIONS
                + "; measured trials per size: " + REPETITIONS);

        int[] sizes = {100, 1000, 10000};
        String[] operations = {"Insertion", "Searching", "Deletion"};
        Random random = new Random(42); // Repeatable input, but timings will vary.

        try (PrintWriter csv = new PrintWriter("results.csv", "UTF-8")) {
            csv.println("Elements,Operation,BST_Time_ms,HashTable_Time_ms");
            System.out.printf("%10s %-12s %15s %20s%n",
                    "Elements", "Operation", "BST (ms)", "Hash Table (ms)");

            for (int n : sizes) {
                long[] bstTotals = new long[3];
                long[] hashTotals = new long[3];
                int bucketCount = n / 2;

                for (int trial = 0; trial < WARMUP_REPETITIONS + REPETITIONS; trial++) {
                    // Generate and shuffle all inputs before starting any timer.
                    int[] insertionKeys = makeShuffledKeys(n, random);
                    int[] searchKeys = makeShuffledKeys(n, random);
                    int[] deletionKeys = makeShuffledKeys(n, random);
                    long[] bstTimes;
                    long[] hashTimes;
                    if (trial % 2 == 0) {
                        bstTimes = measureBST(insertionKeys, searchKeys, deletionKeys);
                        hashTimes = measureHashTable(insertionKeys, searchKeys,
                                deletionKeys, bucketCount);
                    } else {
                        hashTimes = measureHashTable(insertionKeys, searchKeys,
                                deletionKeys, bucketCount);
                        bstTimes = measureBST(insertionKeys, searchKeys, deletionKeys);
                    }
                    if (trial >= WARMUP_REPETITIONS) {
                        for (int operation = 0; operation < 3; operation++) {
                            bstTotals[operation] += bstTimes[operation];
                            hashTotals[operation] += hashTimes[operation];
                        }
                    }
                }

                for (int operation = 0; operation < 3; operation++) {
                    double bstMs = bstTotals[operation] / (REPETITIONS * 1_000_000.0);
                    double hashMs = hashTotals[operation] / (REPETITIONS * 1_000_000.0);
                    System.out.printf(Locale.US, "%10d %-12s %15.6f %20.6f%n",
                            n, operations[operation], bstMs, hashMs);
                    csv.printf(Locale.US, "%d,%s,%.6f,%.6f%n",
                            n, operations[operation], bstMs, hashMs);
                }
            }
            if (csv.checkError()) {
                throw new IOException("Could not write results.csv.");
            }
        }
        System.out.println("Measured results saved to results.csv.");
    }

    private static int[] makeShuffledKeys(int n, Random random) {
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = i;
        }
        // Fisher-Yates shuffle: swap each position with a random earlier position.
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temporary = keys[i];
            keys[i] = keys[j];
            keys[j] = temporary;
        }
        return keys;
    }

    private static BST buildBST(int[] keys) {
        BST tree = new BST();
        for (int key : keys) {
            tree.insert(key);
        }
        return tree;
    }

    private static HashTable buildHashTable(int[] keys, int bucketCount) {
        HashTable table = new HashTable(bucketCount);
        for (int key : keys) {
            table.insert(key);
        }
        return table;
    }

    private static long[] measureBST(int[] insertion, int[] searches, int[] deletions) {
        long[] times = new long[3];
        BST insertionTree = new BST();
        long start = System.nanoTime();
        for (int key : insertion) {
            insertionTree.insert(key);
        }
        times[0] = System.nanoTime() - start;
        for (int key : insertion) {
            check(insertionTree.search(key), "BST benchmark insertion");
        }

        BST searchTree = buildBST(insertion);
        int found = 0;
        start = System.nanoTime();
        for (int key : searches) {
            if (searchTree.search(key)) {
                found++;
            }
        }
        times[1] = System.nanoTime() - start;
        check(found == searches.length, "BST benchmark search");

        BST deletionTree = buildBST(insertion);
        start = System.nanoTime();
        for (int key : deletions) {
            deletionTree.delete(key);
        }
        times[2] = System.nanoTime() - start;
        for (int key : deletions) {
            check(!deletionTree.search(key), "BST benchmark deletion");
        }
        return times;
    }

    private static long[] measureHashTable(int[] insertion, int[] searches,
            int[] deletions, int bucketCount) {
        long[] times = new long[3];
        HashTable insertionTable = new HashTable(bucketCount);
        long start = System.nanoTime();
        for (int key : insertion) {
            insertionTable.insert(key);
        }
        times[0] = System.nanoTime() - start;
        for (int key : insertion) {
            check(insertionTable.search(key), "Hash Table benchmark insertion");
        }

        HashTable searchTable = buildHashTable(insertion, bucketCount);
        int found = 0;
        start = System.nanoTime();
        for (int key : searches) {
            if (searchTable.search(key)) {
                found++;
            }
        }
        times[1] = System.nanoTime() - start;
        check(found == searches.length, "Hash Table benchmark search");

        HashTable deletionTable = buildHashTable(insertion, bucketCount);
        start = System.nanoTime();
        for (int key : deletions) {
            deletionTable.delete(key);
        }
        times[2] = System.nanoTime() - start;
        for (int key : deletions) {
            check(!deletionTable.search(key), "Hash Table benchmark deletion");
        }
        return times;
    }

    private static void check(boolean condition, String message) {
        // Unlike Java's assert keyword, this check works without the -ea option.
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkContents(BST tree, HashTable table,
            int[] keys, boolean[] present) {
        for (int i = 0; i < keys.length; i++) {
            check(tree.search(keys[i]) == present[i], "BST contents: " + keys[i]);
            check(table.search(keys[i]) == present[i], "Hash Table contents: " + keys[i]);
        }
    }

    private static void runCorrectnessChecks() {
        // Empty structures and missing keys.
        BST tree = new BST();
        HashTable table = new HashTable(1); // Force every key into one chain.
        check(!tree.search(7) && !table.search(7), "Empty search");
        tree.delete(7);
        table.delete(7);

        int[] keys = {50, 30, 70, 20, 40, 60, 80, 65, -5,
                Integer.MIN_VALUE, Integer.MAX_VALUE, 0};
        boolean[] present = new boolean[keys.length];
        for (int i = 0; i < keys.length; i++) {
            tree.insert(keys[i]);
            table.insert(keys[i]);
            tree.insert(keys[i]);
            table.insert(keys[i]);
            present[i] = true;
            checkContents(tree, table, keys, present);
        }
        tree.delete(12345);
        table.delete(12345);
        check(!tree.search(12345) && !table.search(12345), "Missing key");
        checkContents(tree, table, keys, present);

        // Leaf (40), one child (60), two children/root (50), then all others.
        // In the one-bucket table this also removes middle, tail, and head nodes.
        int[] deletionOrder = {4, 5, 0, 11, 1, 2, 3, 6, 7, 8, 9, 10};
        for (int index : deletionOrder) {
            tree.delete(keys[index]);
            table.delete(keys[index]);
            present[index] = false;
            checkContents(tree, table, keys, present); // Detect stored duplicates.
            tree.delete(keys[index]);
            table.delete(keys[index]);
            checkContents(tree, table, keys, present);
        }

        // Root with no children and root with only a left or right child.
        tree.insert(10);
        tree.delete(10);
        check(!tree.search(10), "Delete only root");
        tree.insert(10);
        tree.insert(5);
        tree.delete(10);
        check(tree.search(5) && !tree.search(10), "Root with left child");
        tree.delete(5);
        tree.insert(10);
        tree.insert(15);
        tree.delete(10);
        check(tree.search(15) && !tree.search(10), "Root with right child");

        // A two-child root whose successor is its immediate right child.
        tree = buildBST(new int[] {10, 5, 15, 20});
        tree.delete(10);
        check(!tree.search(10) && tree.search(5) && tree.search(15)
                && tree.search(20), "Immediate successor with child");
        // A deeper successor that itself has a right child.
        tree = buildBST(new int[] {50, 30, 80, 60, 65});
        tree.delete(50);
        check(!tree.search(50) && tree.search(30) && tree.search(80)
                && tree.search(60) && tree.search(65), "Deep successor with child");

        // Negative-key hashing with several buckets, including the minimum int.
        table = new HashTable(7);
        for (int key : keys) {
            table.insert(key);
        }
        for (int key : keys) {
            check(table.search(key), "Negative-key hash");
            table.delete(key);
            check(!table.search(key), "Negative-key deletion");
        }
        boolean rejected = false;
        try {
            new HashTable(0);
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        check(rejected, "Reject zero buckets");
    }
}
