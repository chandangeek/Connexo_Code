package com.elster.jupiter.demo.impl.generators;

public class NamedGenerator<T> {

    private String name;
    private Class<T> clazz;

    public NamedGenerator(Class<T> clazz) {
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
