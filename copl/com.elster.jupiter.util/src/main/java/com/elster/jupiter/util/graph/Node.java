package com.elster.jupiter.util.graph;

import java.util.Objects;

public final class Node<T> {

    private final T data;

    private Node(T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(data, node.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                '}';
    }

    public static <S> Node<S> of(S s) {
        return new Node<>(s);
    }
}
