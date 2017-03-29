/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * Created by dantonov on 28.03.2017.
 */

public class TwoValuesAbsoluteDifference extends TwoValuesDifference {

    public TwoValuesAbsoluteDifference() {
        super(Type.absolute);
    }

    public BigDecimal value;
}
