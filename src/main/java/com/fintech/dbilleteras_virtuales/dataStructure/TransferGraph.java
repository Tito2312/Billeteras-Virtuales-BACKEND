package com.fintech.dbilleteras_virtuales.dataStructure;

import java.util.*;

public class TransferGraph {

    private Map<String, GraphNode> nodes;
    private Map<String, List<GraphEdge>> adjacencyList;

    public TransferGraph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(String userId, String userName) {
        if (!nodes.containsKey(userId)) {
            nodes.put(userId, new GraphNode(userId, userName));
            adjacencyList.put(userId, new ArrayList<>());
        }
    }

    public void addEdge(String sourceUserId, String targetUserId, double amount, String transactionId) {
        if (!adjacencyList.containsKey(sourceUserId)) {
            adjacencyList.put(sourceUserId, new ArrayList<>());
        }
        adjacencyList.get(sourceUserId).add(new GraphEdge(sourceUserId, targetUserId, amount, transactionId));
    }

    public List<GraphEdge> getTransfersFrom(String userId) {
        return adjacencyList.getOrDefault(userId, new ArrayList<>());
    }

    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String userId : nodes.keySet()) {
            if (detectCycle(userId, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycle(String userId, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(userId)) return true;
        if (visited.contains(userId)) return false;

        visited.add(userId);
        recursionStack.add(userId);

        for (GraphEdge edge : adjacencyList.getOrDefault(userId, new ArrayList<>())) {
            if (detectCycle(edge.targetUserId, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(userId);
        return false;
    }

    public List<String> findPath(String sourceUserId, String targetUserId) {

        Map<String, List<String>> undirected = new HashMap<>();
        for (Map.Entry<String, List<GraphEdge>> entry : adjacencyList.entrySet()) {
            String from = entry.getKey();
            for (GraphEdge edge : entry.getValue()) {
                undirected.computeIfAbsent(from, k -> new ArrayList<>()).add(edge.targetUserId);
                undirected.computeIfAbsent(edge.targetUserId, k -> new ArrayList<>()).add(from);
            }
        }

        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new Queue<>();
        Set<String> visited = new HashSet<>();

        queue.enqueue(sourceUserId);
        visited.add(sourceUserId);

        while (!queue.isEmpty()) {
            String current = queue.dequeue();

            if (current.equals(targetUserId)) {
                return buildPath(parent, sourceUserId, targetUserId);
            }

            for (String neighbor : undirected.getOrDefault(current, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.enqueue(neighbor);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<String> buildPath(Map<String, String> parent, String source, String target) {
        List<String> path = new ArrayList<>();
        String current = target;
        while (!current.equals(source)) {
            path.add(0, current);
            current = parent.get(current);
        }
        path.add(0, source);
        return path;
    }

    public String getMostActiveUser() {
        return adjacencyList.entrySet().stream()
            .max(Comparator.comparingInt(e -> e.getValue().size()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public Map<String, GraphNode> getNodes() { return nodes; }
    public Map<String, List<GraphEdge>> getAdjacencyList() { return adjacencyList; }
}
