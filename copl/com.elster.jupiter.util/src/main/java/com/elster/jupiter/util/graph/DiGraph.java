package com.elster.jupiter.util.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DiGraph<T> {

    private final Set<Node<T>> vertices = new HashSet<>();
    private final Set<DiEdge<T>> edges = new HashSet<>();
    private final Map<Node<T>, List<Node<T>>> adjacency = new HashMap<>();

    public void addVertex(Node<T> vertex) {
        if (vertices.add(vertex)) {
            adjacency.put(vertex, new ArrayList<>());
        }
    }

    public void addEdge(DiEdge<T> edge) {
        addVertex(edge.from());
        addVertex(edge.to());
        if (edges.add(edge)) {
            adjacency.computeIfAbsent(edge.from(), vertex -> new ArrayList<>())
                    .add(edge.to());
        }
    }

    private boolean isCyclic(Node<T> vertex, Set<Node<T>> visited, Set<Node<T>> recursionStack) {
        if (visited.add(vertex)) {
            recursionStack.add(vertex);

            for (Node<T> i : adjacency.get(vertex)) {
                if (!visited.contains(i) && isCyclic(i, visited, recursionStack)) {
                    return true;
                } else if (recursionStack.contains(i)) {
                    return true;
                }
            }
        }
        recursionStack.remove(vertex);
        return false;
    }

    public boolean isCyclic() {
        Set<Node<T>> visited = new HashSet<>();
        Set<Node<T>> recursionStack = new HashSet<>();

        return vertices.stream()
                .anyMatch(v -> isCyclic(v, visited, recursionStack));
    }

    public Graph<T> undirected() {
        Graph<T> undirected = new Graph<>();
        edges.stream()
                .map(DiEdge::undirected)
                .forEach(undirected::addEdge);
        vertices.forEach(undirected::addVertex);
        return undirected;
    }

    public boolean isTree() {
        Graph<T> undirected = undirected();
        return undirected.isConnected() && !undirected.isCyclic();
    }

    public boolean isForrest() {
       Graph<T> undirected = undirected();
       return !undirected.isCyclic();
    }

    public boolean isConnected() {
        return undirected().isConnected();
    }

    public Optional<List<Node<T>>> shortestPath(Node<T> start, Node<T> end) {
        if (!vertices.contains(start) || !vertices.contains(end)) {
            return Optional.empty();
        }
        return null; // TODO
    }
}


