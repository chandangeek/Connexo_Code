package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolcommon.exceptions.CodingException;
import test.com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

import java.util.Calendar;

public class BatteryLifeDateEnd extends AbstractParameter {

    private Calendar calendar;


    final Calendar getCalendar() {
        return calendar;
    }

    BatteryLifeDateEnd(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.BatteryLifeDateEnd;
    }

    @Override
    protected void parse(byte[] data) {
        try {
            calendar = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone());
        } catch (Exception e) {
            calendar = Calendar.getInstance();
        }
    }

    @Override
    protected byte[] prepare() {
        throw CodingException.unsupportedMethod(getClass(), "prepare");
    }
}