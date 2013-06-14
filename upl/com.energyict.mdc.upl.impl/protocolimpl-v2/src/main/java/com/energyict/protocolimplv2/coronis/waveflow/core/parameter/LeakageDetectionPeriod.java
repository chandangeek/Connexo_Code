package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class LeakageDetectionPeriod extends AbstractParameter {

    public LeakageDetectionPeriod(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public LeakageDetectionPeriod(WaveFlow waveFlow, int thresholdType, int input) {
        super(waveFlow);
        this.thresholdType = thresholdType;
        this.input = input;
    }

    private int thresholdType = 0; //0 = low, 1 = high
    private int input = 1; //1 = A, 2 = B,...
    private int detectionPeriod;

    /**
     * Expressed in multiple of measurement steps.
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
    protected ParameterId getParameterId() {
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
                return ParameterId.ResidualLeakageFlowA;
        }
    }

    @Override
    public void parse(byte[] data) {
        detectionPeriod = WaveflowProtocolUtils.toInt(data[0]);
    }


    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) getDetectionPeriod()};
    }
}