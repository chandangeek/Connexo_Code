package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class ParitiesAdapter extends MapBasedXmlAdapter<Parities> {

    public ParitiesAdapter() {
        register("", null);
        register("None", Parities.NONE);
        register("Even", Parities.EVEN);
        register("Odd", Parities.ODD);
        register("Mark", Parities.MARK);
        register("Space", Parities.SPACE);
    }
}
