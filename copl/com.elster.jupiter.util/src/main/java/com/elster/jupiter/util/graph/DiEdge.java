package com.elster.jupiter.util.graph;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public final class DiEdge<T> {

    private final Node<T> from;
    private final Node<T> to;

    private DiEdge(Node<T> from, Node<T> to) {
        this.from = from;
        this.to = to;
    }

    public Node<T> from() {
        return from;
    }

    public Node<T> to() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiEdge<?> edge = (DiEdge<?>) o;
        return Objects.equals(from, edge.from) &&
                Objects.equals(to, edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    public List<Node<T>> nodes() {
        return ImmutableList.of(from, to);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }

    public Edge<T> undirected() {
        return Edge.between(from, to);
    }

    public static <S> DiEdge<S> between(Node<S> a, Node<S> b) {
        return new DiEdge<>(a, b);
    }
}
