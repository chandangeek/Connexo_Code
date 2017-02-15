package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class NrOfDataBitsAdapter extends MapBasedXmlAdapter<NrOfDataBits> {
    public NrOfDataBitsAdapter() {
        register("", null);
        register("5", NrOfDataBits.FIVE);
        register("6", NrOfDataBits.SIX);
        register("7", NrOfDataBits.SEVEN);
        register("8", NrOfDataBits.EIGHT);
    }
}
