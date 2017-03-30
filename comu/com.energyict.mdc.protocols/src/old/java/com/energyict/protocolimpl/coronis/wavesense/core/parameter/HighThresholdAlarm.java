/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;


import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class HighThresholdAlarm extends AbstractParameter {

    public HighThresholdAlarm(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.HighThreshold;
    }

    private int threshold;

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        threshold = ProtocolTools.getUnsignedIntFromBytes(data, 0, 2);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromInt(threshold, 2);
    }
}