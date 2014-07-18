package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class ParitiesAdapter extends MapBasedXmlAdapter<Parities> {

    public ParitiesAdapter() {
        register("", null);
        register("No parity", Parities.NONE);
        register("Even parity", Parities.EVEN);
        register("Odd parity", Parities.ODD);
        register("Mark parity", Parities.MARK);
        register("Space parity", Parities.SPACE);
    }
}
