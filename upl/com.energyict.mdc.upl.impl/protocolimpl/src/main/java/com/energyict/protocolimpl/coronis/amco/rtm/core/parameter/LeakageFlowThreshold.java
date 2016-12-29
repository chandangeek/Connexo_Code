package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class LeakageFlowThreshold extends AbstractParameter {

    public LeakageFlowThreshold(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    public LeakageFlowThreshold(PropertySpecService propertySpecService, RTM rtm, int thresholdType, int input) {
        super(propertySpecService, rtm);
        this.thresholdType = thresholdType;
        this.input = input;
    }

    private int thresholdType = 0; //0 = low, 1 = high
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
    ParameterId getParameterId() {
        switch (thresholdType) {
            case 0:
                switch (input) {
                    case 1:
                        return ParameterId.ResidualLeakageThresholdA;
                    case 2:
                        return ParameterId.ResidualLeakageThresholdB;
                    case 3:
                        return ParameterId.ResidualLeakageThresholdC;
                    case 4:
                        return ParameterId.ResidualLeakageThresholdD;
                }
            case 1:
                switch (input) {
                    case 1:
                        return ParameterId.ExtremeLeakageThresholdA;
                    case 2:
                        return ParameterId.ExtremeLeakageThresholdB;
                    case 3:
                        return ParameterId.ExtremeLeakageThresholdC;
                    case 4:
                        return ParameterId.ExtremeLeakageThresholdD;
                }
            default:
                return ParameterId.ResidualLeakageThresholdA;
        }
    }

    @Override
    public void parse(byte[] data) throws IOException {
        thresholdValue = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromInt(getThresholdValue(), 2);
    }
}