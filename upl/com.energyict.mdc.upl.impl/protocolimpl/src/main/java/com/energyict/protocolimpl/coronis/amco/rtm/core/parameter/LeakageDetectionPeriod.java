package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class LeakageDetectionPeriod extends AbstractParameter {

    public LeakageDetectionPeriod(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    public LeakageDetectionPeriod(PropertySpecService propertySpecService, RTM rtm, int thresholdType, int input) {
        super(propertySpecService, rtm);
        this.thresholdType = thresholdType;
        this.input = input;
    }

    private int thresholdType = 0; //0 = low, 1 = high
    private int input = 1; //1 = A, 2 = B,...
    private int detectionPeriod;

    /**
     * Expressed in multiple of profile interval
     * Time necessary to cause a leakage event.
     * @return
     */
    public int getDetectionPeriod() {
        return detectionPeriod;
    }

    public void setDetectionPeriod(int detectionPeriod) {
        this.detectionPeriod = detectionPeriod;
    }

    @Override
    ParameterId getParameterId() {
        switch (thresholdType) {
            case 0:
                switch (input) {
                    case 1:
                        return ParameterId.ResidualLeakageDetectionPeriodA;
                    case 2:
                        return ParameterId.ResidualLeakageDetectionPeriodB;
                    case 3:
                        return ParameterId.ResidualLeakageDetectionPeriodC;
                    case 4:
                        return ParameterId.ResidualLeakageDetectionPeriodD;
                }
            case 1:
                switch (input) {
                    case 1:
                        return ParameterId.ExtremeLeakageDetectionPeriodA;
                    case 2:
                        return ParameterId.ExtremeLeakageDetectionPeriodB;
                    case 3:
                        return ParameterId.ExtremeLeakageDetectionPeriodC;
                    case 4:
                        return ParameterId.ExtremeLeakageDetectionPeriodD;
                }
            default:
                return ParameterId.ResidualLeakageDetectionPeriodA;
        }
    }

    @Override
    public void parse(byte[] data) throws IOException {
        detectionPeriod = data[0] & 0xFF;
    }


    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getDetectionPeriod()};
    }
}