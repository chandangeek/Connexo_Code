/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class NumberOfStoredValues extends AbstractParameter {

    private int numberOfValues = 0;

    public int getNumberOfValues() {
        return numberOfValues;
    }

    NumberOfStoredValues(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfStoredValues;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        byte[] bigEndianData = ProtocolTools.reverseByteArray(data);
        numberOfValues = ProtocolTools.getUnsignedIntFromBytes(bigEndianData);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];         //No additional bytes required in the request.
    }
}