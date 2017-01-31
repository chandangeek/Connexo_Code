/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.protocol.api.ComPortType;

public class ComPortTypeAdapter extends MapBasedXmlAdapter<ComPortType> {

    public ComPortTypeAdapter() {
        register("SERVLET", ComPortType.SERVLET);
        register("SERIAL", ComPortType.SERIAL);
        register("TCP", ComPortType.TCP);
        register("UDP", ComPortType.UDP);
    }
}
