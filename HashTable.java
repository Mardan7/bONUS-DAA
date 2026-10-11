// A linked chain holds keys that map to the same bucket.
class HashNode {
    int value;
    HashNode next;

    HashNode(int value, HashNode next) {
        this.value = value;
        this.next = next;
    }
}

public class HashTable {
    private HashNode[] buckets;

    public HashTable(int bucketCount) {
        if (bucketCount <= 0) {
            throw new IllegalArgumentException("Bucket count must be positive.");
        }
        buckets = new HashNode[bucketCount];
    }

    private int hash(int value) {
        int index = value % buckets.length;
        if (index < 0) {
            index += buckets.length;
        }
        return index; // Also works for Integer.MIN_VALUE.
    }

    public void insert(int value) {
        int index = hash(value);
        HashNode current = buckets[index];
        while (current != null) {
            if (current.value == value) {
                return;
            }
            current = current.next;
        }
        buckets[index] = new HashNode(value, buckets[index]);
    }

    public boolean search(int value) {
        HashNode current = buckets[hash(value)];
        while (current != null) {
            if (current.value == value) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public void delete(int value) {
        int index = hash(value);
        HashNode previous = null;
        HashNode current = buckets[index];
        while (current != null) {
            if (current.value == value) {
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                return;
            }
            previous = current;
            current = current.next;
        }
    }
}
