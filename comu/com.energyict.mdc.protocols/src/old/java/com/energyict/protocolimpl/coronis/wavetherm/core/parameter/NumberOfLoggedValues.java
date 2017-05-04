/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class NumberOfLoggedValues extends AbstractParameter {

    NumberOfLoggedValues(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private int number;

    public int getNumber() {
        return number;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfLoggedValues;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        byte[] bytes = ProtocolTools.getSubArray(data, 0, 2);
        number = ProtocolTools.getUnsignedIntFromBytes(ProtocolTools.reverseByteArray(bytes));      //It is sent LSB first!
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}