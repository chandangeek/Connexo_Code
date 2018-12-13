/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Instant;

public class EstimationErrorException extends RuntimeException {

    private RangeSet<Instant> ranges = TreeRangeSet.create();

    public EstimationErrorException(RangeSet<Instant> ranges) {
        super();
        this.ranges = ranges;
    }

    public RangeSet<Instant> getRanges() {
        return this.ranges;
    }
}
