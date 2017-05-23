/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstimationErrorException extends RuntimeException {

    private List<Range<Instant>> ranges = new ArrayList<>();

    public EstimationErrorException(List<Range<Instant>> ranges) {
        super();
        this.ranges = ranges;
    }

    public List<Range<Instant>> getRanges() {
        return Collections.unmodifiableList(this.ranges);
    }
}
