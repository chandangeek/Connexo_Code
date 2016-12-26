package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocol.exceptions.CodingException;
import test.com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

import java.util.Date;

public class DateOfLastDailyIndexStorage extends AbstractParameter {

    DateOfLastDailyIndexStorage(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.DateOfLastDailyIndexStorage;
    }

    @Override
    protected void parse(byte[] data) {
        date = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() {
        throw CodingException.unsupportedMethod(getClass(), "prepare");
    }
}