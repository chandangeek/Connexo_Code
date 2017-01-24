package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 10:18:24
 */
public class CommunicationErrorDetectionDate extends AbstractParameter {

    private int port;
    private Date date;


    CommunicationErrorDetectionDate(RTM rtm, int port) {
        super(rtm);
        this.port = port;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        if (port == 1) {
            return ParameterId.CommunicationErrorDetectionDateA;
        } else {
            return ParameterId.CommunicationErrorDetectionDateB;
        }
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        date = TimeDateRTCParser.parse(data, getRTM().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new WaveFlowException("Not allowed to write this parameter");
    }

    public Date getDate() {
        return date;
    }
}