/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class NumberOfRepeaters extends AbstractParameter {

    int number;

    NumberOfRepeaters(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfRepeaters;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) number};
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
