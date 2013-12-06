package com.elster.jupiter.util;

import java.math.BigDecimal;

/**
 * Part of fluent API, see Checks.
 */
public class BigDecimalChecker extends ObjectChecker<BigDecimal> {

    public BigDecimalChecker(BigDecimal bigDecimal) {
        super(bigDecimal);
    }

    public boolean equalValue(BigDecimal other) {
        if (getToCheck() == null) {
            return other == null;
        }
        return other != null && getToCheck().compareTo(other) == 0;
    }
}
