/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.math.BigDecimal;

public enum ValueCorrection {

    MULTIPLY("MULTIPLY") {
        @Override
        public BigDecimal correctValue(BigDecimal value, BigDecimal amount) {
            return value.multiply(amount);
        }
    },
    ADD("ADD") {
        @Override
        public BigDecimal correctValue(BigDecimal value, BigDecimal amount) {
            return value.add(amount);
        }
    },
    SUBTRACT("SUBTRACT") {
        @Override
        public BigDecimal correctValue(BigDecimal value, BigDecimal amount) {
            return value.subtract(amount);
        }
    };

    private final String type;

    ValueCorrection(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public abstract BigDecimal correctValue(BigDecimal value, BigDecimal amount);
}
