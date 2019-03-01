/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.Builder;

/**
 * A <code>Template</code> is used as a way to initialize builder with (default) values.
 * On its turn a builder creates the objects
 *
 * @param <T> Type (Class) of object for which the template is used for
 * @param <B> Builder user to create objects of type T
 */
public interface Template<T, B extends Builder<T>> {
    Class<B> getBuilderClass();
    B get(B builder);
}
