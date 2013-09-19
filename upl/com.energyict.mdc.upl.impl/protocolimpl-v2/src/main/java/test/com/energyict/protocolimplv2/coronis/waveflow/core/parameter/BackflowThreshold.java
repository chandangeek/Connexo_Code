package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class BackflowThreshold extends AbstractParameter {

    public BackflowThreshold(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public BackflowThreshold(WaveFlow waveFlow, int input, boolean simple, boolean advanced) {
        super(waveFlow);
        this.input = input;
        this.simple = simple;
        this.advanced = advanced;
    }

    private int input = 1; //1 = A, 2 = B,...
    private int threshold;
    private boolean simple;
    private boolean advanced;

    /**
     * This is the threshold, in number of pulses per detection period
     */
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }


    @Override
    protected ParameterId getParameterId() {
        switch (input) {
            case 1:
                if (simple) {
                    return ParameterId.SimpleBackflowThresholdA;
                }
                if (advanced) {
                    return ParameterId.AdvancedBackflowThresholdA;
                }
            case 2:
                if (simple) {
                    return ParameterId.SimpleBackflowThresholdB;
                }
                if (advanced) {
                    return ParameterId.AdvancedBackflowThresholdB;
                }
        }
        throw createWaveFlowException("Module doesn't support back flow detection.");
    }

    @Override
    public void parse(byte[] data) {
        threshold = WaveflowProtocolUtils.toInt(data[0]); //1 byte long.
    }

    protected byte[] prepare() {
        return new byte[]{(byte) getThreshold()};
    }
}