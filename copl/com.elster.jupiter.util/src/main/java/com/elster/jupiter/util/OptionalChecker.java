/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Optional;

/**
 * Part of fluent API, see Checks.
 */
public class OptionalChecker<T> {

    private final Optional<T> toCheck;

    OptionalChecker(Optional<T> toCheck) {
        this.toCheck = toCheck;
    }

    public boolean equalTo(Optional<T> other) {
        if (this.toCheck.isPresent()) {
            return other.isPresent() && Checks.is(this.toCheck.get()).equalTo(other.get());
        } else {
            return !other.isPresent();
        }
    }

    public boolean presentAndEqualTo(Optional<T> other) {
        return this.toCheck.isPresent() && other.isPresent() && Checks.is(this.toCheck.get()).equalTo(other.get());
    }

    /**
     * @deprecated to avoid confusion with equalTo()
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}