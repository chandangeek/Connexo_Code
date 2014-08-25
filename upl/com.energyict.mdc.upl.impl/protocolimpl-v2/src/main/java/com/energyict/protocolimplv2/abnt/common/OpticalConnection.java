package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.protocol.ComChannel;

/**
* @author sva
* @since 21/08/2014 - 16:24
*/
public class OpticalConnection extends SerialConnection {

    public OpticalConnection(ComChannel comChannel, AbntProperties properties) {
        super(comChannel, properties);
    }
}