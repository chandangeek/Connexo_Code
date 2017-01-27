package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class BackflowDetectionFlags extends AbstractParameter {

    int portId;

    /**
     * Detection flags for the backflow...
     * Back flow detection flags : this word contains 12 relevant bits that express back flow detection in the month
     * bit0: current month
     * bit1..12: month -1..-12
     */
    int flags;

    final int getFlags() {
        return flags;
    }

    public BackflowDetectionFlags(WaveFlow waveFlow, int portId) {
        super(waveFlow);
        this.portId = portId;
    }

    @Override
    protected ParameterId getParameterId() {
        return portId == 0 ? ParameterId.SimpleBackflowDetectionFlagsPortA : ParameterId.SimpleBackflowDetectionFlagsPortB;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        data = ProtocolTools.reverseByteArray(data);       //Module sends the LSB first..
        flags = ProtocolUtils.getInt(data, 0, 2);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) flags};
    }

    /**
     * Checks if the flag is set for a specific month. e.g. current month: i = 0, flag is the LSB of the flags int.
     * @param i: the month
     * @return boolean if flag of that month is set
     */
    public boolean flagIsSet(int i) {
        return (1 == ((flags >> i) & 0x01));
    }

    /**
     * Creates an event date for the backflow detection.
     * Theres only one flag per month.
     * @param i = the requested month. i = 0 means the current month, i = 1 means the previous month, etc.
     * @return even time stamp
     */
    public Date getEventDate(int i) {
        Calendar cal = Calendar.getInstance(getWaveFlow().getTimeZone());
        cal.setLenient(true);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        cal.add(Calendar.MONTH, -1 * i);
        return cal.getTime();
    }

    public String getInputChannelName() {
        switch (portId) {
            case 0: return "A";
            case 1: return "B";
            default: return "";
        }
    }
}