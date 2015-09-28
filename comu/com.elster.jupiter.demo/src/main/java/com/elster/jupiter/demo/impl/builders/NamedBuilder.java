package com.elster.jupiter.demo.impl.builders;

/**
 * Extension of {@link Builder} having an attribute 'name'
 * @param <T> Type of object
 * @param <B> Builder class
 */
public abstract class NamedBuilder<T, B> implements Builder<T> {
    private final Class<B> clazz;

    private String name;

    public NamedBuilder(Class<B> clazz) {
        this.clazz = clazz;
    }

    public B withName(String name){
        this.name = name;
        return clazz.cast(this);
    }

    protected String getName() {
        return this.name;
    }

    protected Class<B> getGeneratorClass() {
        return clazz;
    }
}
