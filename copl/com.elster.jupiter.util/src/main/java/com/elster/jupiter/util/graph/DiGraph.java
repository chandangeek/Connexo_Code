/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

import com.elster.jupiter.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.elster.jupiter.util.streams.Currying.test;

public class DiGraph<T> {

    private final Set<T> vertices = new HashSet<>();
    private final Set<DiEdge<T>> edges = new HashSet<>();
    private final Map<T, List<DiEdge<T>>> adjacency = new HashMap<>();

    public void addVertex(T data) {
        if (vertices.add(data)) {
            adjacency.put(data, new ArrayList<>());
        }
    }

    private void addEdge(DiEdge<T> edge) {
        addVertex(edge.from());
        addVertex(edge.to());
        if (edges.add(edge)) {
            adjacency.computeIfAbsent(edge.from(), vertex -> new ArrayList<>())
                    .add(edge);
        }
    }

    public void addEdge(T from, T to) {
        addEdge(DiEdge.between(from, to));
    }

    public void addEdge(T from, T to, long weight) {
        addEdge(DiEdge.between(from, to).withWeight(weight));
    }

    private boolean isCyclic(T vertex, Set<T> visited, Set<T> recursionStack) {
        if (visited.add(vertex)) {
            recursionStack.add(vertex);

            for (DiEdge<T> diEdge : adjacency.get(vertex)) {
                T otherVertex = diEdge.to();
                if (!visited.contains(otherVertex) && isCyclic(otherVertex, visited, recursionStack)) {
                    return true;
                } else if (recursionStack.contains(otherVertex)) {
                    return true;
                }
            }
        }
        recursionStack.remove(vertex);
        return false;
    }

    public boolean isCyclic() {
        Set<T> visited = new HashSet<>();
        Set<T> recursionStack = new HashSet<>();

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

    public boolean isForest() {
       Graph<T> undirected = undirected();
       return !undirected.isCyclic();
    }

    public boolean isConnected() {
        return undirected().isConnected();
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
                        T vertex = edge.to();
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
                        T vertex = edge.to();
                        if (alt.isSmallerThan(distance.get(vertex))) {
                            distance.put(vertex, alt);
                            previous.put(vertex, nextClosestVertex);
                        }
                    });
        }

        return Pair.of(distance, previous);
    }


    public Distance radius() {
        return vertices.stream()
                .map(this::eccentricity)
                .min(Comparator.naturalOrder())
                .orElse(Infinity.INFINITY);
    }

    public Distance diameter() {
        return vertices.stream()
                .map(this::eccentricity)
                .max(Comparator.naturalOrder())
                .orElse(Infinity.INFINITY);
    }

    private Distance eccentricity(T vertex) {
        return minimalSpanningTree(vertex).getFirst().values().stream().max(Comparator.naturalOrder()).orElse(Infinity.INFINITY);
    }

    private Distance distance(T one, T other) {
        return minimalSpanningTree(one).getFirst().get(other);
    }

    public void remove(T vertex) {
        vertices.removeIf(node -> Objects.equals(node, vertex));
        Predicate<DiEdge<T>> touches = test(DiEdge<T>::touches).with(vertex);
        edges.removeIf(touches);
        adjacency.entrySet().removeIf(entry -> entry.getKey().equals(vertex));
        adjacency.values().forEach(list -> list.removeIf(touches));
    }

    public void removeEdge(T from, T to) {
        Predicate<DiEdge<T>> isEdge = edge -> edge.from().equals(from) && edge.to().equals(to);
        edges.removeIf(isEdge);
        adjacency.values().forEach(list -> list.removeIf(isEdge));
    }

    public Set<T> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    public Set<DiEdge<T>> edges() {
        return Collections.unmodifiableSet(edges);
    }

 }


