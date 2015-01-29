package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.Builder;

public interface Template<T, B extends Builder<T>> {
    Class<B> getBuilderClass();
    B get(B builder);
}
