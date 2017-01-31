/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Arrays;

/**
 * Part of fluent API, see Checks.
 */
public class ObjectChecker<T> {

    private final T toCheck;

    public ObjectChecker(T toCheck) {
        this.toCheck = toCheck;
    }

    public boolean equalTo(Object other) {
        return getToCheck() == other || (getToCheck() != null && getToCheck().equals(other));
    }

    protected T getToCheck() {
        return toCheck;
    }

    /**
     * @deprecated to avoid confusion with equalTo()
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public boolean in(Object... objects) {
        return Arrays.stream(objects)
                .anyMatch(this::equalTo);
    }
}
