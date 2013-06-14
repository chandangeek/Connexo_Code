package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 9:08:01
 */
public class WriteDataFeature extends AbstractRadioCommand {

    public WriteDataFeature(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int featureData = 0;

    public void setFeatureData(int featureData) {
        this.featureData = featureData;
    }

    @Override
    protected void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error writing the data feature, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() {
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