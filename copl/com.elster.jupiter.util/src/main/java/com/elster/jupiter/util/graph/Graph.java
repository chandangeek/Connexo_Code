package com.elster.jupiter.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

public class Graph<T> {

    private final Set<T> vertices = new HashSet<>();
    private final Set<Edge<T>> edges = new HashSet<>();
    private final Map<T, List<Edge<T>>> adjacency = new HashMap<>();

    public void addVertex(T vertex) {
        if (vertices.add(vertex)) {
            adjacency.put(vertex, new ArrayList<>());
        }
    }

    public void addEdge(Edge<T> edge) {
        vertices.addAll(edge.nodes());
        if (edges.add(edge)) {
            edge.nodes()
                    .forEach(node -> {
                        adjacency.computeIfAbsent(node, vertex -> new ArrayList<>())
                                .add(edge);
                    });
        }
    }

    public boolean isCyclic() {
        Set<T> visited = new HashSet<>();
        Set<Edge<T>> edgesLeft = new HashSet<>(edges);

        return vertices.stream().anyMatch(vertex -> {
            if (!visited.contains(vertex)) {
                Set<T> marked = new HashSet<>();
                visited.add(vertex);
                Deque<T> toExpand = new ArrayDeque<>();
                toExpand.add(vertex);

                for (T next = toExpand.poll(); next != null; next = toExpand.poll()) {
                    T current = next;
                    if (!marked.add(current)) {
                        return true;
                    }
                    visited.add(current);
                    adjacency.get(current)
                            .stream()
                            .filter(edgesLeft::contains)
                            .collect(Collectors.toList())
                            .stream()
                            .peek(edgesLeft::remove)
                            .map(edge -> edge.nodes().stream().filter(not(current::equals)).findAny().orElse(current))
                            .forEach(toExpand::add);
                }

            }
            return false;
        });

    }

    public boolean isConnected() {
        Set<Edge<T>> edgesLeft = new HashSet<>(edges);

        if (vertices.isEmpty()) {
            return true;
        }

        T vertex = vertices.iterator().next();

        Deque<T> toExpand = new ArrayDeque<T>();
        toExpand.add(vertex);

        for (T next = toExpand.poll(); next != null; next = toExpand.poll()) {
            T current = next;
            adjacency.get(current)
                    .stream()
                    .filter(edgesLeft::contains)
                    .collect(Collectors.toList())
                    .stream()
                    .peek(edgesLeft::remove)
                    .map(edge -> edge.nodes().stream().filter(not(current::equals)).findAny().get())
                    .forEach(toExpand::add);
        }

        return edgesLeft.isEmpty();
    }
}


