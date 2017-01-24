package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.ParameterType;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class TimeDateRTC extends AbstractParameter {

    private Date time;

    public TimeDateRTC(WaveFlow waveFlow) {
        super(waveFlow);
        parameterType = ParameterType.Hydreka;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() throws WaveFlowException {
        return AbstractParameter.ParameterId.TimeDateRTC;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        time = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        Calendar cal = Calendar.getInstance(getWaveFlow().getTimeZone());
        cal.setTime(time);
        return TimeDateRTCParser.prepare(cal);     //7 bytes long
    }
}