package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;
import java.util.Date;

public class TamperDetectionDate extends AbstractParameter {

    public TamperDetectionDate(RTM rtm) {
        super(rtm);
    }

    public TamperDetectionDate(RTM rtm, int input) {
        super(rtm);
        this.input = input;
    }

    private int input = 1; //1 = A, 2 = B,...
    private Date date;

    /**
     * This is the threshold, it has the same unit as the encoder
     */
    public Date getDate() {
        return date;
    }

    public void setPort(int input) {
        this.input = input;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        switch (input) {
            case 1:
                return ParameterId.TamperDetectionDateA;
            case 2:
                return ParameterId.TamperDetectionDateB;
            case 3:
                return ParameterId.TamperDetectionDateC;
            case 4:
                return ParameterId.TamperDetectionDateD;
        }
        throw new WaveFlowException("Module doesn't support tamper detection.");
    }

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        date = TimeDateRTCParser.parse(data, 0, 7, getRTM().getTimeZone()).getTime();
    }

    protected byte[] prepare() throws IOException {
        throw new WaveFlowException("Not allowed to write this parameter.");
    }
}