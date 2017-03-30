/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class WriteDataFeature extends AbstractRadioCommand {

    public WriteDataFeature(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int featureData = 0;

    public void setFeatureData(int featureData) {
        this.featureData = featureData;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the data feature, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromInt(featureData, 4);
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WriteDataFeature;
    }

    public void resetAlarmDisplay() {
        featureData = featureData & 0xFFFFFF00;     //Reset LSB.
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}