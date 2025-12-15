import java.util.*;
import java.lang.Math.*;

public class McMetro {
    protected Track[] tracks;
    protected HashMap<BuildingID, Building> buildingTable = new HashMap<>();

    private TrieNode passengerTrie;

    McMetro(Track[] tracks, Building[] buildings) {
        this.tracks = tracks;

        // Populate buildings table
        for (Building building : buildings) {
            buildingTable.putIfAbsent(building.id(), building);
        }

        passengerTrie = new TrieNode();
    }

    // Maximum number of passengers that can be transported from start to end
    int maxPassengers(BuildingID start, BuildingID end) {
        if (!buildingTable.containsKey(start) || !buildingTable.containsKey(end)) {
            return 0;
        }

        if (start.equals(end)) {
            return buildingTable.get(start).occupants();
        }

        // directed adjacency list, capacity = track.capacity
        HashMap<BuildingID, ArrayList<Edge>> graph = new HashMap<>();
        for (BuildingID id : buildingTable.keySet()) {
            graph.put(id, new ArrayList<>());
        }

        for (Track t : tracks) {
            BuildingID a = t.startBuildingId();
            BuildingID b = t.endBuildingId();
            if (!graph.containsKey(a) || !buildingTable.containsKey(b)) continue;

            int cap = t.capacity();
            if (cap < 0) cap = 0;

            graph.get(a).add(new Edge(b, cap));
        }

        // widest path: maximize the minimum edge capacity along the path
        HashMap<BuildingID, Integer> best = new HashMap<>();
        for (BuildingID id : buildingTable.keySet()) {
            best.put(id, 0);
        }
        best.put(start, Integer.MAX_VALUE);

        PriorityQueue<State> pq = new PriorityQueue<>((x, y) -> Integer.compare(y.bottleneck, x.bottleneck));
        pq.add(new State(start, Integer.MAX_VALUE));

        while (!pq.isEmpty()) {
            State cur = pq.poll();

            // stale entry
            if (cur.bottleneck != best.get(cur.id)) continue;

            if (cur.id.equals(end)) break;

            for (Edge e : graph.get(cur.id)) {
                int nb = Math.min(cur.bottleneck, e.cap);
                if (nb > best.get(e.to)) {
                    best.put(e.to, nb);
                    pq.add(new State(e.to, nb));
                }
            }
        }

        int widestCap = best.get(end);
        if (widestCap == 0) return 0;

        int occStart = buildingTable.get(start).occupants();
        int occEnd = buildingTable.get(end).occupants();

        return Math.min(widestCap, Math.min(occStart, occEnd));
    }

    // Returns a list of trackIDs that connect to every building maximizing total network capacity taking cost into account
    TrackID[] bestMetroSystem() {
        if (buildingTable.size() <= 1) {
            return new TrackID[0];
        }

        ArrayList<Track> list = new ArrayList<>();
        Collections.addAll(list, tracks);

        // sort descending by goodness, tie break by id
        list.sort((a, b) -> {
            int ga = trackGoodness(a);
            int gb = trackGoodness(b);
            if (ga != gb) return Integer.compare(gb, ga);
            return a.id().compareTo(b.id());
        });

        NaiveDisjointSet<BuildingID> ds = new NaiveDisjointSet<>();
        for (BuildingID id : buildingTable.keySet()) {
            ds.add(id);
        }

        ArrayList<TrackID> res = new ArrayList<>();

        // treat tracks as undirected here
        for (Track t : list) {
            BuildingID u = t.startBuildingId();
            BuildingID v = t.endBuildingId();

            if (!buildingTable.containsKey(u) || !buildingTable.containsKey(v)) continue;

            if (!ds.find(u).equals(ds.find(v))) {
                ds.union(u, v);
                res.add(t.id());
            }

            if (res.size() == buildingTable.size() - 1) break;
        }

        TrackID[] out = new TrackID[res.size()];
        for (int i = 0; i < res.size(); i++) {
            out[i] = res.get(i);
        }
        return out;
    }

    // Adds a passenger to the system
    void addPassenger(String name) {
        if (name == null) return;
        String trimmed = name.trim();
        if (trimmed.length() == 0) return;

        String normalized = normalizeName(trimmed);
        String lower = normalized.toLowerCase();

        TrieNode node = passengerTrie;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }

        node.isEnd = true;
        node.names.add(normalized);
    }

    // Do not change this
    void addPassengers(String[] names) {
        for (String s : names) {
            addPassenger(s);
        }
    }

    // Returns all passengers in the system whose names start with firstLetters
    ArrayList<String> searchForPassengers(String firstLetters) {
        ArrayList<String> out = new ArrayList<>();
        if (firstLetters == null) return out;

        String pref = firstLetters.toLowerCase();
        TrieNode node = passengerTrie;

        for (int i = 0; i < pref.length(); i++) {
            char c = pref.charAt(i);
            if (!node.children.containsKey(c)) {
                return out;
            }
            node = node.children.get(c);
        }

        HashSet<String> set = new HashSet<>();
        collectNames(node, set);

        out.addAll(set);
        Collections.sort(out);
        return out;
    }

    // Return how many ticket checkers will be hired
    static int hireTicketCheckers(int[][] schedule) {
        if (schedule == null || schedule.length == 0) return 0;

        int n = schedule.length;
        int[][] arr = new int[n][2];
        for (int i = 0; i < n; i++) {
            arr[i][0] = schedule[i][0];
            arr[i][1] = schedule[i][1];
        }

        Arrays.sort(arr, (a, b) -> {
            if (a[1] != b[1]) return Integer.compare(a[1], b[1]);
            return Integer.compare(a[0], b[0]);
        });

        int count = 0;
        int lastEnd = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            if (arr[i][0] >= lastEnd) {
                count++;
                lastEnd = arr[i][1];
            }
        }

        return count;
    }

    // helpers

    private int trackGoodness(Track t) {
        BuildingID u = t.startBuildingId();
        BuildingID v = t.endBuildingId();

        if (!buildingTable.containsKey(u) || !buildingTable.containsKey(v)) {
            return Integer.MIN_VALUE;
        }

        int occU = buildingTable.get(u).occupants();
        int occV = buildingTable.get(v).occupants();
        int cap = t.capacity();
        if (cap < 0) cap = 0;

        int cost = t.cost();
        if (cost <= 0) return Integer.MAX_VALUE;

        int m = Math.min(Math.min(occU, occV), cap);
        return m / cost;
    }

    private void collectNames(TrieNode node, HashSet<String> out) {
        if (node.isEnd) {
            out.addAll(node.names);
        }
        for (TrieNode child : node.children.values()) {
            collectNames(child, out);
        }
    }

    private String normalizeName(String s) {
        String lower = s.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static class Edge {
        BuildingID to;
        int cap;

        Edge(BuildingID to, int cap) {
            this.to = to;
            this.cap = cap;
        }
    }

    private static class State {
        BuildingID id;
        int bottleneck;

        State(BuildingID id, int bottleneck) {
            this.id = id;
            this.bottleneck = bottleneck;
        }
    }

    private static class TrieNode {
        HashMap<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
        HashSet<String> names = new HashSet<>();
    }
}
