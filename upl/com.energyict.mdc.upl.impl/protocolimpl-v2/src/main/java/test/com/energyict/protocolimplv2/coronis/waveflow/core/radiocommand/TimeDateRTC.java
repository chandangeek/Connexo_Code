package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocol.exception.CommunicationException;
import test.com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

import java.io.IOException;
import java.util.Calendar;

public class TimeDateRTC extends AbstractRadioCommand {

    private Calendar calendar = null;

    static final int SET_OK = 0x00;
    static final int SET_ERROR = 0xFF;

    public final Calendar getCalendar() {
        return calendar;
    }


    public final void setCalendar(final Calendar calendar) {
        this.calendar = calendar;
    }

    public TimeDateRTC(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected void parse(byte[] data) {
        if (getRadioCommandId() == RadioCommandId.ReadCurrentRTC || getRadioCommandId() == RadioCommandId.ReadCurrentRTCLegacy) {
            calendar = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone());      //is already V1 compatible, it checks the length of the received data.
        } else {
            // check if write clock succeeded
            if (WaveflowProtocolUtils.toInt(data[0]) == SET_ERROR) {
                IOException e = new IOException("Error setting the RTC in the waveflow device, returned [" + WaveflowProtocolUtils.toInt(data[0]) + "]");
                throw CommunicationException.unexpectedResponse(e);
            }
        }
    }

    @Override
    protected byte[] prepare() {
        if (getRadioCommandId() == RadioCommandId.ReadCurrentRTC || getRadioCommandId() == RadioCommandId.ReadCurrentRTCLegacy) {
            return new byte[0];
        } else {
            if (getWaveFlow().isV1()) {
                return TimeDateRTCParser.prepare6(calendar);     //TODO test
            }
            return TimeDateRTCParser.prepare(calendar);
        }
    }


    @Override
    protected RadioCommandId getRadioCommandId() {
        if (calendar == null) {
            if (getWaveFlow().isV1()) {
                return RadioCommandId.ReadCurrentRTCLegacy;
            }
            return RadioCommandId.ReadCurrentRTC;
        } else {
            if (getWaveFlow().isV1()) {
                return RadioCommandId.WriteCurrentRTCLegacy;
            }
            return RadioCommandId.WriteCurrentRTC;
        }
    }
}
