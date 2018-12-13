/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

import com.elster.jupiter.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    void addEdge(Edge<T> edge) {
        vertices.addAll(edge.nodes());
        if (edges.add(edge)) {
            edge.nodes()
                    .forEach(node -> {
                        adjacency.computeIfAbsent(node, vertex -> new ArrayList<>())
                                .add(edge);
                    });
        }
    }

    public void addEdge(T one, T another) {
        addEdge(Edge.between(one, another));
    }

    public void addEdge(T one, T another, long weight) {
        addEdge(Edge.between(one, another).withWeight(weight));
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
        Set<T> verticesLeft = new HashSet<>(vertices);

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
                    .peek(edge -> verticesLeft.removeAll(edge.nodes()))
                    .map(edge -> edge.nodes().stream().filter(not(current::equals)).findAny().get())
                    .forEach(toExpand::add);
        }

        return verticesLeft.isEmpty();
    }

    public Optional<List<T>> shortestPath(T start, T end) {
        if (!vertices.contains(start) || !vertices.contains(end)) {
            return Optional.empty();
        }
        Set<T> remainingVertices = new HashSet<>(vertices);

        Map<T, Distance> distance = new HashMap<>();
        Map<T, T> previous = new HashMap<>();

        vertices.forEach(vertex -> {
            distance.put(vertex, Infinity.INFINITY);
        });
        distance.put(start, LongDistance.ZERO);

        while (!remainingVertices.isEmpty()) {
            T nextClosestVertex = remainingVertices.stream()
                    .min(Comparator.comparing(distance::get))
                    .get();

            if (end.equals(nextClosestVertex) && !distance.get(nextClosestVertex).isInfinite()) {
                ArrayList<T> result = new ArrayList<>();
                result.add(end);
                while (previous.get(nextClosestVertex) != null) {
                    T prev = previous.get(nextClosestVertex);
                    result.add(prev);
                    nextClosestVertex = prev;
                }
                Collections.reverse(result);
                return Optional.of(result);
            }

            remainingVertices.remove(nextClosestVertex);

            T u = nextClosestVertex;
            adjacency.get(u)
                    .stream()
                    .forEach(edge -> {
                        Distance alt = distance.get(u).plus(edge.weight());
                        T vertex = edge.nodes().stream().filter(not(u::equals)).findAny().get();
                        if (alt.isSmallerThan(distance.get(vertex))) {
                            distance.put(vertex, alt);
                            previous.put(vertex, u);
                        }
                    });
        }

        return Optional.empty();
    }

    private Pair<Map<T, Distance>, Map<T, T>> minimalSpanningTree(T start) {
        Set<T> remainingVertices = new HashSet<>(vertices);

        Map<T, Distance> distance = new HashMap<>();
        Map<T, T> previous = new HashMap<>();

        vertices.forEach(vertex -> {
            distance.put(vertex, Infinity.INFINITY);
        });
        distance.put(start, LongDistance.ZERO);

        while (!remainingVertices.isEmpty()) {
            T nextClosestVertex = remainingVertices.stream()
                    .min(Comparator.comparing(distance::get))
                    .get();

            remainingVertices.remove(nextClosestVertex);

            adjacency.get(nextClosestVertex)
                    .stream()
                    .forEach(edge -> {
                        Distance alt = distance.get(nextClosestVertex).plus(edge.weight());
                        T vertex = edge.nodes().stream().filter(not(nextClosestVertex::equals)).findAny().get();
                        if (alt.isSmallerThan(distance.get(vertex))) {
                            distance.put(vertex, alt);
                            previous.put(vertex, nextClosestVertex);
                        }
                    });
        }

        return Pair.of(distance, previous);
    }



}


