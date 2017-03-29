/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

/**
 * Created by dantonov on 28.03.2017.
 */
public class TwoValuesPercentDifference extends TwoValuesDifference {

    public TwoValuesPercentDifference() {
        super(Type.percent);
    }

    public Double percent;
}
