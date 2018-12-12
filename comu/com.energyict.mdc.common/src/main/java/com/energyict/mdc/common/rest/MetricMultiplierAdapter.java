/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.MetricMultiplier;

public class MetricMultiplierAdapter extends MapBasedXmlAdapter<MetricMultiplier> {

    public MetricMultiplierAdapter() {
        register("",MetricMultiplier.ZERO);
        register("y",MetricMultiplier.YOCTO);
        register("z",MetricMultiplier.ZEPTO);
        register("a",MetricMultiplier.ATTO);
        register("f",MetricMultiplier.FEMTO);
        register("p",MetricMultiplier.PICO);
        register("n",MetricMultiplier.NANO);
        register("\u00b5",MetricMultiplier.MICRO);
        register("m",MetricMultiplier.MILLI );
        register("c",MetricMultiplier.CENTI );
        register("d",MetricMultiplier.DECI );
        register("Da",MetricMultiplier.DECA );
        register("h",MetricMultiplier.HECTO );
        register("k",MetricMultiplier.KILO );
        register("M",MetricMultiplier.MEGA );
        register("G",MetricMultiplier.GIGA );
        register("T",MetricMultiplier.TERA);
        register("P",MetricMultiplier.PETA);
        register("E",MetricMultiplier.EXA);
        register("Z",MetricMultiplier.ZETTA);
        register("Y",MetricMultiplier.YOTTA);
    }
}
