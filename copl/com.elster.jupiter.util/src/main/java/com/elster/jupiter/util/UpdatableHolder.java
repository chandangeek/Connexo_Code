/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public final class UpdatableHolder<R> implements Holder<R> {
    private R held;

    public UpdatableHolder(R initial) {
        held = initial;
    }

    public void update(R newValue) {
        held = newValue;
    }

    public <Q> void update(BiFunction<? super R, Q, R> biFunction, Q factor) {
        held = biFunction.apply(held, factor);
    }

    public void update(UnaryOperator<R> manipulation) {
        held = manipulation.apply(held);
    }

    public R get() {
        return held;
    }
}
