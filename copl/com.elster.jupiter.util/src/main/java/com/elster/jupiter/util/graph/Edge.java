/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public final class Edge<T> {

    private final Set<T> nodes;
    private final long weight;

    private Edge(T from, T to) {
        this.nodes = ImmutableSet.of(from, to);
        this.weight = 1L;
    }

    private Edge(Edge<T> source, long weight) {
        this.nodes = source.nodes;
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(nodes(), edge.nodes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes());
    }

    public Set<T> nodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "nodes=" + nodes +
                '}';
    }

    public long weight() {
        return weight;
    }

    public static <S> Edge<S> between(S a, S b) {
        return new Edge<>(a, b);
    }

    public Edge<T> withWeight(long weight) {
        return new Edge<>(this, weight);
    }

}