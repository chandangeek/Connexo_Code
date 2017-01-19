package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.ports.ComPortType;

public class ComPortTypeAdapter extends MapBasedXmlAdapter<ComPortType> {

    public ComPortTypeAdapter() {
        register("", null);
        register(ComServerFieldTranslationKeys.COM_PORT_TYPE_SERVLET.getKey(), ComPortType.SERVLET);
        register(ComServerFieldTranslationKeys.COM_PORT_TYPE_SERIAL.getKey(), ComPortType.SERIAL);
        register(ComServerFieldTranslationKeys.COM_PORT_TYPE_TCP.getKey(), ComPortType.TCP);
        register(ComServerFieldTranslationKeys.COM_PORT_TYPE_UDP.getKey(), ComPortType.UDP);
    }
}
