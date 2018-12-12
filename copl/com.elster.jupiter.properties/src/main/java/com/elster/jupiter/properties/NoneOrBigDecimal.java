/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class NoneOrBigDecimal {

    private static final NoneOrBigDecimal NONE = new NoneOrBigDecimal(true, null);

    private boolean isNone;
    private BigDecimal value;

    private NoneOrBigDecimal(boolean isNone, BigDecimal value) {
        this.isNone = isNone;
        this.value = value;
    }

    public static NoneOrBigDecimal none() {
        return NONE;
    }

    public static NoneOrBigDecimal of(BigDecimal value) {
        return new NoneOrBigDecimal(false, value);
    }

    @JsonProperty("isNone")
    public boolean isNone() {
        return isNone;
    }

    @JsonProperty
    public BigDecimal getValue() {
        return value;
    }
}
