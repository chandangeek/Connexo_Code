package com.elster.jupiter.demo.impl.factories;

public abstract class NamedFactory<T, S> implements Factory<S> {
    private final Class<T> clazz;

    private String name;

    public NamedFactory(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T withName(String name){
        this.name = name;
        return clazz.cast(this);
    }

    protected String getName() {
        return this.name;
    }

    protected Class<T> getGeneratorClass() {
        return clazz;
    }
}
