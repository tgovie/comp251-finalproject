import java.util.*;

public class NaiveDisjointSet<T> {
    HashMap<T, T> parentMap = new HashMap<>();
    HashMap<T, Integer> rankMap = new HashMap<>();  // For union by rank

    void add(T element) {
        parentMap.put(element, element);
        rankMap.put(element, 0);  // Initialize rank to 0
    }

    // PATH COMPRESSION: Make all nodes on path point directly to root
    T find(T a) {
        T node = parentMap.get(a);
        if (node.equals(a)) {
            return node;  // Found the root
        } else {
            T root = find(node);           // Find root recursively
            parentMap.put(a, root);        // PATH COMPRESSION: point directly to root
            return root;
        }
    }

    // UNION BY RANK: Attach smaller tree under larger tree
    void union(T a, T b) {
        T rootA = find(a);  // Find root of a
        T rootB = find(b);  // Find root of b
        
        if (rootA.equals(rootB)) {
            return;  // Already in same set
        }
        
        int rankA = rankMap.get(rootA);
        int rankB = rankMap.get(rootB);
        
        // Attach smaller rank tree under larger rank tree
        if (rankA < rankB) {
            parentMap.put(rootA, rootB);  // Make rootB parent of rootA
        } else if (rankA > rankB) {
            parentMap.put(rootB, rootA);  // Make rootA parent of rootB
        } else {
            // Equal ranks: choose one as parent and increment its rank
            parentMap.put(rootB, rootA);
            rankMap.put(rootA, rankA + 1);
        }
    }
}
