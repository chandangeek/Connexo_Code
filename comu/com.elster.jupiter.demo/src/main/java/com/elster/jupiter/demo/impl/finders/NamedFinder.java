package com.elster.jupiter.demo.impl.finders;

import com.elster.jupiter.demo.impl.UnableToCreate;

import java.util.function.Supplier;

public abstract class NamedFinder<T, S> implements Finder<S> {
    private final Class<T> clazz;

    private String name;

    public NamedFinder(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T withName(String name){
        this.name = name;
        return clazz.cast(this);
    }

    protected String getName() {
        return this.name;
    }

    protected Supplier<? extends RuntimeException> getFindException(){
        final String name = getName();
        return () -> new UnableToCreate("Unable to find object from " + getGeneratorClass().getSimpleName() + " with name: " + name);
    }

    protected Class<T> getGeneratorClass() {
        return clazz;
    }
}
