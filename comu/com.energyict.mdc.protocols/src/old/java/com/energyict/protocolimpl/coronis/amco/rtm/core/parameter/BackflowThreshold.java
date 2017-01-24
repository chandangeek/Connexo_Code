package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class BackflowThreshold extends AbstractParameter {

    public BackflowThreshold(RTM rtm) {
        super(rtm);
    }

    public BackflowThreshold(RTM rtm, int input) {
        super(rtm);
        this.input = input;
    }

    private int input = 1; //1 = A, 2 = B,...
    private int threshold;

    /**
     * This is the threshold, it has the same unit as the encoder
     */
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }


    @Override
    ParameterId getParameterId() throws WaveFlowException {
        switch (input) {
            case 1:
                return ParameterId.BackflowDetectionThresholdA;
            case 2:
                return ParameterId.BackflowDetectionThresholdB;
        }
        throw new WaveFlowException("Module doesn't support back flow detection.");
    }

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        threshold = WaveflowProtocolUtils.toInt(data[0]); //1 byte long.
    }

    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getThreshold()};
    }
}