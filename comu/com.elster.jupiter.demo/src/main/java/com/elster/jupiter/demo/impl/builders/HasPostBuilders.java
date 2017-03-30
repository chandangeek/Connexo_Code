/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import java.util.function.Consumer;

/**
 * {@link Builder}s providing some post processing on/with the built object
 * @param <T> Type (Class) of object that will be created
 * @param <B> Builder class
 *
 * Date: 2/10/2015
 * Time: 10:08
 */
public interface HasPostBuilders<T, B> extends Builder<T> {

    /**
     * Once the T object was 'build' we do some post processing on it.
     * @param postBuilder doing the post processing
     * @return it self allowing method chaining
     */
     B withPostBuilder(Consumer<T> postBuilder);
}
