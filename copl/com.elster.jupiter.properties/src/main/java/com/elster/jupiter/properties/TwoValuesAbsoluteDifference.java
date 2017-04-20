/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

public class TwoValuesAbsoluteDifference extends TwoValuesDifference {

    public TwoValuesAbsoluteDifference() {
        super(Type.absolute);
    }

    public TwoValuesAbsoluteDifference(BigDecimal value) {
        this();
        this.value = value;
    }

    public BigDecimal value;
}
