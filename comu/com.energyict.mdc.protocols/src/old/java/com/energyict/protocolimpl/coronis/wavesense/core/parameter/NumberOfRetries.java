/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;


import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class NumberOfRetries extends AbstractParameter {

    public NumberOfRetries(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfRetriesForAlarmTransmission;
    }

    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) number};
    }
}