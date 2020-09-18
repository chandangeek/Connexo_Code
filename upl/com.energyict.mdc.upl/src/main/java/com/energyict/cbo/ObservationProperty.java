package com.energyict.cbo;

public class ObservationProperty<T> {

    private final String name;
    private final T value;

    public ObservationProperty(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }
}
