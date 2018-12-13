package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;
import java.util.Calendar;

/**
 * Class can read AND write the meter's clock!
 */
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

    public TimeDateRTC(WaveLog waveLog) {
        super(waveLog);
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if (getRadioCommandId() == RadioCommandId.ReadCurrentRTC) {
            calendar = TimeDateRTCParser.parse(data, getWaveLog().getTimeZone());
        } else {
            // check if write clock succeeded
            if (WaveflowProtocolUtils.toInt(data[0]) == SET_ERROR) {
                getWaveLog().getLogger().severe("Error setting the RTC in the wavesense device, returned [" + WaveflowProtocolUtils.toInt(data[0]) + "]");
            }
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        if (getRadioCommandId() == RadioCommandId.ReadCurrentRTC) {
            return new byte[0];
        } else {
            return TimeDateRTCParser.prepare6(calendar);
        }
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        if (calendar == null) {
            return RadioCommandId.ReadCurrentRTC;
        } else {
            return RadioCommandId.WriteCurrentRTC;
        }
    }
}