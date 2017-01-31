/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.Accumulation;

public class AccumulationAdapter extends MapBasedXmlAdapter<Accumulation> {

    public AccumulationAdapter() {
        register("", Accumulation.NOTAPPLICABLE);
        register("Not applicable", Accumulation.NOTAPPLICABLE);
        register("Bounded quantity", Accumulation.BOUNDEDQUANTITY);
        register("Bulk quantity", Accumulation.BULKQUANTITY);
        register("Cumulative", Accumulation.CUMULATIVE);
        register("Continuous cumulative", Accumulation.CONTINUOUSCUMULATIVE);
        register("Delta data", Accumulation.DELTADELTA);
        register("Indicating", Accumulation.INDICATING);
        register("Instantaneous", Accumulation.INSTANTANEOUS);
        register("Summation", Accumulation.SUMMATION);
        register("Time delay", Accumulation.TIMEDELAY);
        register("Latching quantity", Accumulation.LATCHINGQUANTITY);
    }
}
