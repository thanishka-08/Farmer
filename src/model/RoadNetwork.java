package model;

import java.util.*;

/**
 * Graph representation of the agricultural road network.
 * Implements Dijkstra's Shortest Path Algorithm using PriorityQueue.
 * Reused and extended from the console Agri_70 implementation.
 */
public class RoadNetwork {
    private HashMap<String, HashMap<String, Double>> graph = new HashMap<>();

    public RoadNetwork() {
        initDefaultNetwork();
    }

    public void addNode(String node) {
        if (node != null && !node.trim().isEmpty()) {
            graph.putIfAbsent(node.trim().toUpperCase(), new HashMap<>());
        }
    }

    public void addRoad(String from, String to, double time) {
        if (from == null || to == null) return;
        from = from.trim().toUpperCase();
        to = to.trim().toUpperCase();
        if (time <= 0) {
            throw new IllegalArgumentException("Road travel time must be positive.");
        }

        addNode(from);
        addNode(to);

        graph.get(from).put(to, time);
        graph.get(to).put(from, time);
    }

    public boolean hasNode(String node) {
        if (node == null) return false;
        return graph.containsKey(node.trim().toUpperCase());
    }

    public Set<String> getAllNodes() {
        return new TreeSet<>(graph.keySet());
    }

    public Map<String, HashMap<String, Double>> getGraph() {
        return graph;
    }

    /**
     * If a user types a new location not in the graph, auto-connect it to
     * a central junction (J1 or J2) so the route calculation succeeds.
     */
    public void ensureNodeConnected(String node) {
        if (node == null || node.trim().isEmpty()) return;
        String upper = node.trim().toUpperCase();
        if (!graph.containsKey(upper) || graph.get(upper).isEmpty()) {
            addNode(upper);
            // Default connect to J1 and J2 with realistic travel times
            addRoad(upper, "J1", 3.0);
            addRoad(upper, "J2", 3.5);
        }
    }

    /**
     * Calculates the shortest travel time between start and end using Dijkstra's algorithm.
     */
    public double shortestTime(String start, String end) {
        if (start == null || end == null) return Double.MAX_VALUE;
        start = start.trim().toUpperCase();
        end = end.trim().toUpperCase();

        ensureNodeConnected(start);
        ensureNodeConnected(end);

        if (start.equals(end)) return 0.0;
        if (!graph.containsKey(start) || !graph.containsKey(end)) return Double.MAX_VALUE;

        HashMap<String, Double> distance = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(
            Comparator.comparingDouble(node -> distance.getOrDefault(node, Double.MAX_VALUE))
        );

        distance.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentDistance = distance.get(current);

            if (current.equals(end)) {
                return currentDistance;
            }

            for (Map.Entry<String, Double> road : graph.getOrDefault(current, new HashMap<>()).entrySet()) {
                String next = road.getKey();
                double newDistance = currentDistance + road.getValue();

                if (newDistance < distance.getOrDefault(next, Double.MAX_VALUE)) {
                    distance.put(next, newDistance);
                    queue.add(next);
                }
            }
        }

        return Double.MAX_VALUE;
    }

    /**
     * Calculates the shortest route path list using Dijkstra's algorithm.
     */
    public List<String> shortestRoute(String start, String end) {
        if (start == null || end == null) return new ArrayList<>();
        start = start.trim().toUpperCase();
        end = end.trim().toUpperCase();

        ensureNodeConnected(start);
        ensureNodeConnected(end);

        if (start.equals(end)) {
            List<String> single = new ArrayList<>();
            single.add(start);
            return single;
        }

        HashMap<String, Double> distance = new HashMap<>();
        HashMap<String, String> previous = new HashMap<>();

        PriorityQueue<String> queue = new PriorityQueue<>(
            Comparator.comparingDouble(node -> distance.getOrDefault(node, Double.MAX_VALUE))
        );

        distance.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentDistance = distance.get(current);

            for (Map.Entry<String, Double> road : graph.getOrDefault(current, new HashMap<>()).entrySet()) {
                String next = road.getKey();
                double newDistance = currentDistance + road.getValue();

                if (newDistance < distance.getOrDefault(next, Double.MAX_VALUE)) {
                    distance.put(next, newDistance);
                    previous.put(next, current);
                    queue.add(next);
                }
            }
        }

        if (!distance.containsKey(end)) {
            return new ArrayList<>();
        }

        LinkedList<String> route = new LinkedList<>();
        String current = end;

        while (current != null) {
            route.addFirst(current);
            current = previous.get(current);
        }

        return route;
    }

    public void initDefaultNetwork() {
        graph.clear();

        // 12 Nodes from the console implementation
        String[] nodes = {
            "F01", "F02", "F03", "F04", "F05",
            "J1", "J2", "MARKET1", "MARKET2", "MARKET3",
            "STORE1", "STORE2"
        };

        for (String node : nodes) {
            addNode(node);
        }

        // Road connections with travel times (in hours)
        addRoad("F01", "J1", 2.0);
        addRoad("F02", "J1", 3.0);
        addRoad("F03", "J1", 2.0);
        addRoad("F04", "J2", 3.0);
        addRoad("F05", "J2", 2.0);

        addRoad("J1", "J2", 2.0);
        addRoad("J1", "MARKET1", 2.0);
        addRoad("J1", "MARKET2", 4.0);
        addRoad("J2", "MARKET2", 2.0);
        addRoad("J2", "MARKET3", 3.0);
        addRoad("J1", "STORE1", 3.0);
        addRoad("J2", "STORE2", 2.0);
    }
}
