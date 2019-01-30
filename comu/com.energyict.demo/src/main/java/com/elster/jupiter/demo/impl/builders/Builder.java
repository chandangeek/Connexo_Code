/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import java.util.Optional;

/**
 *
 * @param <T> Type (Class) of object that will be created
 */
public interface Builder<T> {

    /**
     * Search for a T in the system.
     * If it is not found it creates a new T object with the builders attributes.
     * @return the object of type T
     */
    default T get(){
        return find().orElseGet(this::create);
    }

    /**
     * Search for a T in the system
     * @return the <code>Optional</code> T
     */
    Optional<T> find();

    /**
     * Create a new T with the builders attributes.
     * @return the new T
     */
    T create();

}