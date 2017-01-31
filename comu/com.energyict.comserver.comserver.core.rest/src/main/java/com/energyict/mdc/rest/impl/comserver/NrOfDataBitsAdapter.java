/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.io.NrOfDataBits;

public class NrOfDataBitsAdapter extends MapBasedXmlAdapter<NrOfDataBits> {
    public NrOfDataBitsAdapter() {
        register("", null);
        register("5", NrOfDataBits.FIVE);
        register("6", NrOfDataBits.SIX);
        register("7", NrOfDataBits.SEVEN);
        register("8", NrOfDataBits.EIGHT);
    }
}
