package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class LeakageFlowThreshold extends AbstractParameter {

    public LeakageFlowThreshold(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public LeakageFlowThreshold(WaveFlow waveFlow, int thresholdType, int input) {
        super(waveFlow);
        this.thresholdType = thresholdType;
        this.input = input;
    }

    private int thresholdType = 0; //0 = low (residual), 1 = high (extreme)
    private int input = 1; //1 = A, 2 = B,...
    private int thresholdValue;

    /**
     * This is the threshold, in number of pulses per measurement step (parameter 0xC4)
     */
    public int getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(int thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    @Override
    protected ParameterId getParameterId() {
        switch (thresholdType) {
            case 0:
                switch (input) {
                    case 1:
                        return ParameterId.ResidualLeakageFlowA;
                    case 2:
                        return ParameterId.ResidualLeakageFlowB;
                    case 3:
                        return ParameterId.ResidualLeakageFlowC;
                    case 4:
                        return ParameterId.ResidualLeakageFlowD;
                }
            case 1:
                switch (input) {
                    case 1:
                        return ParameterId.ExtremeLeakageFlowA;
                    case 2:
                        return ParameterId.ExtremeLeakageFlowB;
                    case 3:
                        return ParameterId.ExtremeLeakageFlowC;
                    case 4:
                        return ParameterId.ExtremeLeakageFlowD;
                }
            default:
                return ParameterId.ResidualLeakageFlowA;
        }
    }

    @Override
    public void parse(byte[] data) throws IOException {
        if (isHighThreshold()) {
            thresholdValue = ProtocolTools.getUnsignedIntFromBytes(ProtocolTools.reverseByteArray(data)); //LSB comes first, 2 bytes long
        } else {
            thresholdValue = WaveflowProtocolUtils.toInt(data[0]); //1 byte long.
        }
    }

    private boolean isHighThreshold() {
        return (getParameterId() == ParameterId.ExtremeLeakageFlowA
                || getParameterId() == ParameterId.ExtremeLeakageFlowB
                || getParameterId() == ParameterId.ExtremeLeakageFlowC
                || getParameterId() == ParameterId.ExtremeLeakageFlowD);
    }

    @Override
    protected byte[] prepare() throws IOException {
        switch (thresholdType) {
            case 0:
                return new byte[]{(byte) getThresholdValue()};
            case 1:
                return ProtocolTools.reverseByteArray(ProtocolTools.getBytesFromInt(getThresholdValue(), 2));
            default:
                return new byte[0];
        }
    }
}