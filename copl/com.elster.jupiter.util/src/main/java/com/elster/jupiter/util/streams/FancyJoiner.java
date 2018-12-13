/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class FancyJoiner implements Collector<Object, ArrayList<String>, String> {
    private final String separator;
    private final String lastSeparator;
    private final String prefix;
    private final String postfix;

    private FancyJoiner(String separator, String lastSeparator, String prefix, String postfix) {
        this.separator = separator;
        this.lastSeparator = lastSeparator;
        this.prefix = prefix;
        this.postfix = postfix;
    }

    public static FancyJoiner joining(String separator, String lastSeparator, String prefix, String postfix) {
        return new FancyJoiner(separator, lastSeparator, prefix, postfix);
    }

    public static FancyJoiner joining(String separator, String lastSeparator) {
        return new FancyJoiner(separator, lastSeparator, "", "");
    }

    @Override
    public Supplier<ArrayList<String>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<ArrayList<String>, Object> accumulator() {
        return (list, obj) -> list.add(obj.toString());
    }

    @Override
    public BinaryOperator<ArrayList<String>> combiner() {
        return (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        };
    }

    @Override
    public Function<ArrayList<String>, String> finisher() {
        return list -> {
            StringJoiner joiner = new StringJoiner(lastSeparator, prefix, postfix);
            int lengthMinusOne = Math.max(list.size() - 1, 0);
            if (lengthMinusOne != 0) {
                joiner.add(list.stream()
                        .limit(lengthMinusOne)
                        .collect(java.util.stream.Collectors.joining(separator)));
            }
            list.stream().skip(lengthMinusOne).findFirst().ifPresent(joiner::add);
            return joiner.toString();
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
