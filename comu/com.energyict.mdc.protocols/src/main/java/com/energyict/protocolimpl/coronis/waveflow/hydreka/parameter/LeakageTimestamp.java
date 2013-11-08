package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.ParameterType;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 18/12/12
 * Time: 11:17
 * Author: khe
 */
public class LeakageTimestamp extends AbstractParameter {

    private Date timestamp;

    public LeakageTimestamp(WaveFlow waveFlow) {
        super(waveFlow);
        parameterType = ParameterType.Hydreka;
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() throws WaveFlowException {
        return AbstractParameter.ParameterId.LeakageTimestamp;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        timestamp = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];   //Cannot set this parameter
    }

    public Date getTimeStamp() {
        return timestamp;
    }
}
