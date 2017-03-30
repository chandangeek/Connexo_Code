/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.util.function.BiFunction;

public class Accumulator<T, R> {
    
    private final BiFunction<T, R, T> accumulation;
    private T accumulated = null;

    public Accumulator(BiFunction<T, R, T> accumulation) {
        this.accumulation = accumulation;
    }
    
    public Accumulator(T initialValue, BiFunction<T, R, T> accumulation) {
    	this.accumulated = initialValue;
    	this.accumulation = accumulation;
    }

    public void accept(R element) {
        accumulated = accumulation.apply(accumulated, element);
    }

    public T getAccumulated() {
        return accumulated;
    }
}
