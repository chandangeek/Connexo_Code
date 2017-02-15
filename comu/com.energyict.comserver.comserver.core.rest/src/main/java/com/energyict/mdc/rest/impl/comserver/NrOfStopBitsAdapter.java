package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class NrOfStopBitsAdapter extends MapBasedXmlAdapter<NrOfStopBits> {
    public NrOfStopBitsAdapter() {
        register("1", NrOfStopBits.ONE);
        register("2", NrOfStopBits.TWO);
        register("1.5", NrOfStopBits.ONE_AND_HALF);
        register("", null);
    }
}
