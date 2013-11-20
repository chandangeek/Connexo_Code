package com.elster.jupiter.util;

import com.google.common.base.Function;

public class Upcast<T extends S, S> implements Function<T, S> {

    @Override
    public S apply(T input) {
        return input;
    }
}
