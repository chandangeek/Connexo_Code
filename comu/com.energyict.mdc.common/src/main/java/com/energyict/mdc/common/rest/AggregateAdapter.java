/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.Aggregate;

public class AggregateAdapter extends MapBasedXmlAdapter<Aggregate> {

    public AggregateAdapter() {
        register("", Aggregate.NOTAPPLICABLE);
        register("Not applicable", Aggregate.NOTAPPLICABLE);
        register("Average", Aggregate.AVERAGE);
        register("Excess", Aggregate.EXCESS);
        register("High", Aggregate.HIGH);
        register("Low", Aggregate.LOW);
        register("High threshold", Aggregate.HIGHTHRESHOLD);
        register("Low threshold", Aggregate.LOWTHRESHOLD);
        register("Maximum", Aggregate.MAXIMUM);
        register("Minimum", Aggregate.MINIMUM);
        register("Normal", Aggregate.NORMAL);
        register("Nominal", Aggregate.NOMINAL);
        register("2nd maximum", Aggregate.SECONDMAXIMUM);
        register("3th maximum", Aggregate.THIRDMAXIMUM);
        register("4th maximum", Aggregate.FOURTHMAXIMUM);
        register("5th maximum", Aggregate.FIFTHMAXIMIMUM);
        register("Sum", Aggregate.SUM);
        register("2nd minimum", Aggregate.SECONDMINIMUM);
    }
}
