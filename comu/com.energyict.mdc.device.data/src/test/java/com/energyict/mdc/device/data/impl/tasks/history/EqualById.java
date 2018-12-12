/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.util.HasId;

import java.util.Objects;

public class EqualById<T extends HasId> {

    private final T decorated;
    private final Class<T> clazz;

    private EqualById(T decorated, Class<T> clazz) {
        this.decorated = decorated;
        this.clazz = clazz;
    }

    public static <S extends HasId> EqualById<S> byId(S object, Class<S> clazz) {
        return new EqualById<>(object, clazz);
    }

    public static <S extends HasId> EqualById<S> byId(S object) {
        return new EqualById<>(object, (Class<S>) object.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EqualById)) {
            return false;
        }

        EqualById<?> equalById = (EqualById<?>) o;

        return decorated.getId() == ((EqualById<?>) o).decorated.getId() && clazz.equals(((EqualById<?>) o).clazz);

    }

    @Override
    public int hashCode() {
        return Objects.hash(decorated, clazz);
    }
}
