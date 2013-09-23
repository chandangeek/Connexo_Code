package com.elster.jupiter.util;

import java.math.BigDecimal;

/**
 * Part of fluent API, see Checks.
 */
public class BigDecimalChecker extends ObjectChecker<BigDecimal> {

    public BigDecimalChecker(BigDecimal bigDecimal) {
        super(bigDecimal);
    }

    public boolean equalToIgnoringScale(BigDecimal other) {
        if (toCheck == null) {
            return other == null;
        }
        return other != null && toCheck.compareTo(other) == 0;
    }
}
