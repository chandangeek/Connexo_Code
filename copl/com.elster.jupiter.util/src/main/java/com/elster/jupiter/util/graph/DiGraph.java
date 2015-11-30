package com.elster.jupiter.util.graph;

import com.elster.jupiter.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DiGraph<T> {

    private final Set<Node<T>> vertices = new HashSet<>();
    private final Set<DiEdge<T>> edges = new HashSet<>();
    private final Map<Node<T>, List<DiEdge<T>>> adjacency = new HashMap<>();

    public void addVertex(T data) {
        Node<T> vertex = Node.of(data);
        doAddVertex(vertex);
    }

    private void doAddVertex(Node<T> vertex) {
        if (vertices.add(vertex)) {
            adjacency.put(vertex, new ArrayList<>());
        }
    }

    private void addEdge(DiEdge<T> edge) {
        doAddVertex(edge.from());
        doAddVertex(edge.to());
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

    private boolean isCyclic(Node<T> vertex, Set<Node<T>> visited, Set<Node<T>> recursionStack) {
        if (visited.add(vertex)) {
            recursionStack.add(vertex);

            for (DiEdge<T> diEdge : adjacency.get(vertex)) {
                Node<T> otherVertex = diEdge.to();
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

    public boolean isForest() {
       Graph<T> undirected = undirected();
       return !undirected.isCyclic();
    }

    public boolean isConnected() {
        return undirected().isConnected();
    }

    public Optional<List<T>> shortestPath(T start, T end) {
        return shortestPath(Node.of(start), Node.of(end));
    }

    private Optional<List<T>> shortestPath(Node<T> start, Node<T> end) {
        if (!vertices.contains(start) || !vertices.contains(end)) {
            return Optional.empty();
        }
        Set<Node<T>> remainingVertices = new HashSet<>(vertices);

        Map<Node<T>, Distance> distance = new HashMap<>();
        Map<Node<T>, Node<T>> previous = new HashMap<>();

        vertices.forEach(vertex -> {
                    distance.put(vertex, Infinity.INFINITY);
                });
        distance.put(start, LongDistance.ZERO);

        while (!remainingVertices.isEmpty()) {
            Node<T> nextClosestVertex = remainingVertices.stream()
                    .min(Comparator.comparing(distance::get))
                    .get();

            if (end.equals(nextClosestVertex)) {
                ArrayList<T> result = new ArrayList<>();
                result.add(end.getData());
                while (previous.get(nextClosestVertex) != null) {
                    Node<T> prev = previous.get(nextClosestVertex);
                    result.add(prev.getData());
                    nextClosestVertex = prev;
                }
                Collections.reverse(result);
                return Optional.of(result);
            }

            remainingVertices.remove(nextClosestVertex);

            Node<T> u = nextClosestVertex;
            adjacency.get(u)
                    .stream()
                    .forEach(edge -> {
                        Distance alt = distance.get(u).plus(edge.weight());
                        Node<T> vertex = edge.to();
                        if (alt.isSmallerThan(distance.get(vertex))) {
                            distance.put(vertex, alt);
                            previous.put(vertex, u);
                        }
                    });
        }

        return Optional.empty();
    }

    private Pair<Map<Node<T>, Distance>, Map<Node<T>, Node<T>>> minimalSpanningTree(Node<T> start) {
        Set<Node<T>> remainingVertices = new HashSet<>(vertices);

        Map<Node<T>, Distance> distance = new HashMap<>();
        Map<Node<T>, Node<T>> previous = new HashMap<>();

        vertices.forEach(vertex -> {
            distance.put(vertex, Infinity.INFINITY);
        });
        distance.put(start, LongDistance.ZERO);

        while (!remainingVertices.isEmpty()) {
            Node<T> nextClosestVertex = remainingVertices.stream()
                    .min(Comparator.comparing(distance::get))
                    .get();

            remainingVertices.remove(nextClosestVertex);

            adjacency.get(nextClosestVertex)
                    .stream()
                    .forEach(edge -> {
                        Distance alt = distance.get(nextClosestVertex).plus(edge.weight());
                        Node<T> vertex = edge.to();
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

    private Distance eccentricity(Node<T> vertex) {
        return minimalSpanningTree(vertex).getFirst().values().stream().max(Comparator.naturalOrder()).orElse(Infinity.INFINITY);
    }

    private Distance distance(Node<T> one, Node<T> other) {
        return minimalSpanningTree(one).getFirst().get(other);
    }

    public interface Distance extends Comparable<Distance> {
        Distance plus(long distance);

        default boolean isSmallerThan(Distance other) {
            return this.compareTo(other) < 0;
        }

        default boolean isInfinite() {
            return Infinity.INFINITY.equals(this);
        }
    }

    private static final class Infinity implements Distance {

        private static final Infinity INFINITY = new Infinity();

        private Infinity() {
        }

        @Override
        public int compareTo(Distance other) {
            if (this.equals(other)) {
                return 0;
            }
            return 1;
        }

        @Override
        public Distance plus(long distance) {
            return this;
        }

        @Override
        public String toString() {
            return "Infinity";
        }
    }

    private static class LongDistance implements Distance {
        private final long distance;

        private static final LongDistance ZERO = new LongDistance(0L);

        private LongDistance(long distance) {
            this.distance = distance;
        }

        public static Distance of(long distance) {
            return new LongDistance(distance);
        }

        @Override
        public int compareTo(Distance o) {
            if (o instanceof Infinity) {
                return -1;
            }
            LongDistance other = (LongDistance) o;
            return Long.compare(distance, other.distance);
        }

        @Override
        public Distance plus(long distance) {
            return new LongDistance(this.distance + distance);
        }

        @Override
        public String toString() {
            return "LongDistance{" +
                    "distance=" + distance +
                    '}';
        }
    }

    Set<Node<T>> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    Set<DiEdge<T>> edges() {
        return Collections.unmodifiableSet(edges);
    }

    private List<Tree<T>> getTrees() {
        return Collections.emptyList();
    }
 }


