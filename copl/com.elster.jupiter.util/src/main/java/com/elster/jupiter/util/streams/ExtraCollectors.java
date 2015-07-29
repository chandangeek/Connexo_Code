package com.elster.jupiter.util.streams;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public enum ExtraCollectors {
    ;

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return new Collector<T, ImmutableList.Builder<T>, ImmutableList<T>>() {
            @Override
            public Supplier<ImmutableList.Builder<T>> supplier() {
                return ImmutableList::builder;
            }

            @Override
            public BiConsumer<ImmutableList.Builder<T>, T> accumulator() {
                return ImmutableList.Builder::add;
            }

            @Override
            public BinaryOperator<ImmutableList.Builder<T>> combiner() {
                return (b1, b2) -> b1.addAll(b2.build());
            }

            @Override
            public Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher() {
                return ImmutableList.Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    public static <T extends Comparable<? super T>> Collector<Range<T>, ?, RangeSet<T>> toImmutableRangeSet() {
        return new Collector<Range<T>, ImmutableRangeSet.Builder<T>, RangeSet<T>>() {
            @Override
            public Supplier<ImmutableRangeSet.Builder<T>> supplier() {
                return ImmutableRangeSet::builder;
            }

            @Override
            public BiConsumer<ImmutableRangeSet.Builder<T>, Range<T>> accumulator() {
                return ImmutableRangeSet.Builder::add;
            }

            @Override
            public BinaryOperator<ImmutableRangeSet.Builder<T>> combiner() {
                return (b1, b2) -> b1.addAll(b2.build());
            }

            @Override
            public Function<ImmutableRangeSet.Builder<T>, RangeSet<T>> finisher() {
                return ImmutableRangeSet.Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return ImmutableSet.of(Characteristics.UNORDERED);
            }
        };
    }
}
