/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Extension of {@link Builder} having an attribute 'name'
 * Providing a way for post processing the built T  by implementing the HasPostBuilders interface
 * @param <T> Type of object
 * @param <B> Builder class
 */
public abstract class NamedBuilder<T, B> implements HasPostBuilders<T, B> {
    private final Class<B> clazz;

    private String name;
    private List<Consumer<T>> postBuilders = new ArrayList<>();

    public NamedBuilder(Class<B> clazz) {
        this.clazz = clazz;
    }

    public B withName(String name){
        this.name = name;
        return clazz.cast(this);
    }

    /**
     * Attach some 'PostBuilder' to this builder
     * @param postBuilder doing the post processing
     * @return this allowing method chaining
     */
    public B withPostBuilder(Consumer<T> postBuilder){
        this.postBuilders.add(postBuilder);
        return clazz.cast(this);
    }

    protected String getName() {
        return this.name;
    }

    protected Class<B> getGeneratorClass() {
        return clazz;
    }

    /**
     * Call applyPostbuilders(T) once the T created and/or found.
     * @param justBuilt item to 'post process'
     * @return the object T
     */
    protected T applyPostBuilders(T justBuilt){
        postBuilders.forEach(pb -> pb.accept(justBuilt));
        return justBuilt;
    }
}
