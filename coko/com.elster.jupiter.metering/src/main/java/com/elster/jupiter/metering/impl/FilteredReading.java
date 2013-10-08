package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredReading extends FilteredBaseReading implements Reading {
	
    FilteredReading(Reading source, int... indices) {
        super(source,indices);
    }

}
