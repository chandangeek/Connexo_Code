package com.elster.jupiter.events;

public interface LocalEventProperty {

    Object getValue();

    <T> T getValue(Class<T> clazz);

}
