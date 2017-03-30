/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public final class DiEdge<T> {

    private final T from;
    private final T to;
    private final long weight;

    private DiEdge(T from, T to, long weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public T from() {
        return from;
    }

    public T to() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiEdge<?> edge = (DiEdge<?>) o;
        return Objects.equals(from, edge.from) &&
                Objects.equals(to, edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    public List<T> nodes() {
        return ImmutableList.of(from(), to());
    }

    public boolean touches(T data) {
        return from.equals(data) || to.equals(data);
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

    public static <S> DiEdge<S> between(S a, S b) {
        return new DiEdge<>(a, b, 1L);
    }

    public DiEdge<T> withWeight(long weight) {
        return new DiEdge<>(from, to, weight);
    }

    public long weight() {
        return weight;
    }

}