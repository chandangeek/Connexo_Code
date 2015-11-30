package com.elster.jupiter.util.graph;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

public final class Edge<T> {

    private final Set<Node<T>> nodes;
    private final long weight;

    private Edge(Node<T> from, Node<T> to) {
        this.nodes = ImmutableSet.of(from, to);
        this.weight = 1L;
    }

    private Edge(Edge<T> source, long weight) {
        this.nodes = source.nodes;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(nodes(), edge.nodes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes());
    }

    public Set<Node<T>> nodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "nodes=" + nodes +
                '}';
    }

    public static <S> Edge<S> between(Node<S> a, Node<S> b) {
        return new Edge<>(a, b);
    }

    public Edge<T> withWeight(long weight) {
        return new Edge<>(this, weight);
    }

}
