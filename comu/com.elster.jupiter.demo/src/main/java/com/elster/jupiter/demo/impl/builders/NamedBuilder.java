package com.elster.jupiter.demo.impl.builders;

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
