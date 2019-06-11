/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests;

import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

/**
 * Provides useful extensions for Mockito's {@link Matchers}, e.g. additional implementations of {@link ArgumentMatcher}.
 */
public class MatchersExtension {

    private MatchersExtension() {
        // not intended as instantiable class
    }

    public static Number anyNumberWithValue(Number value) {
        return Matchers.argThat(new IsAnyNumberWithValue(value));
    }

    private static class IsAnyNumberWithValue extends ArgumentMatcher<Number> {
        private Number expected;

        private IsAnyNumberWithValue(Number expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object o) {
            return ((Number) o).doubleValue() == expected.doubleValue();
        }
    }
}
