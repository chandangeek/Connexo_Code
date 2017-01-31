/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.io.NrOfStopBits;

public class NrOfStopBitsAdapter extends MapBasedXmlAdapter<NrOfStopBits> {
    public NrOfStopBitsAdapter() {
        register("1", NrOfStopBits.ONE);
        register("2", NrOfStopBits.TWO);
        register("1.5", NrOfStopBits.ONE_AND_HALF);
        register("", null);
    }
}
