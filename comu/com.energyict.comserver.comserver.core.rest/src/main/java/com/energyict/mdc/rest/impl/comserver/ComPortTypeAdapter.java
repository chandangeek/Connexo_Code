package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class ComPortTypeAdapter extends MapBasedXmlAdapter<ComPortType> {

    public ComPortTypeAdapter() {
        register("", null);
        register("SERVLET", ComPortType.SERVLET);
        register("SERIAL", ComPortType.SERIAL);
        register("TCP", ComPortType.TCP);
        register("UDP", ComPortType.UDP);
    }
}
