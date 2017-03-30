/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium150;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.modbus.enerdis.enerium200.Enerium200;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;

public class Enerium150 extends Enerium200 {

    @Override
    public String getProtocolDescription() {
        return "Enerdis Enerium 150 Modbus";
    }

    @Inject
    public Enerium150(PropertySpecService propertySpecService, Clock clock) {
        super(propertySpecService, clock);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "Enerium 150 " + getMeterInfo().getVersion();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-05-02 09:42:35 +0200 (do, 02 mei 2013) $";
    }

}