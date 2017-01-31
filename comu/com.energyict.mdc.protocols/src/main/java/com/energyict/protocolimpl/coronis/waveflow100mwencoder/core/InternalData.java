/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.List;

abstract public class InternalData {

    /**
     * 0 = port A, 1 = port B
     */
    protected int portId;

    /**
     * Returns a list of meterevents specific for the connected meters
     */
    public abstract List<MeterEvent> getMeterEvents();

    /**
     * Extra port info for the event description
     */
    protected String getPortInfo() {
        return " on port " + (portId == 0 ? "A" : "B");
    }

    public abstract String getSerialNumber();

}
