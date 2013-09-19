package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class BackflowDetectionPeriod extends AbstractParameter {

    public BackflowDetectionPeriod(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public BackflowDetectionPeriod(WaveFlow waveFlow, int input, boolean simple, boolean advanced) {
        super(waveFlow);
        this.input = input;
        this.simple = simple;
        this.advanced = advanced;
    }

    private int input = 1; //1 = A, 2 = B
    private int detectionPeriod;
    private boolean simple;
    private boolean advanced;

    /**
     * Expressed in hours
     * Time necessary to cause a back flow event.
     *
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
        switch (input) {
            case 1:
                if (simple) {
                    return ParameterId.SimpleBackflowDetectionPeriodA;
                }
                if (advanced) {
                    return ParameterId.AdvancedBackflowDetectionPeriodA;
                }
            case 2:
                if (simple) {
                    return ParameterId.SimpleBackflowDetectionPeriodB;
                }
                if (advanced) {
                    return ParameterId.AdvancedBackflowDetectionPeriodB;
                }
        }
        throw createWaveFlowException("Module doesn't support back flow detection.");
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