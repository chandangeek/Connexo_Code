package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BackflowDetectionFlags extends AbstractParameter {

    public static final String A = "A";
    public static final String B = "B";

    public BackflowDetectionFlags(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public BackflowDetectionFlags(PropertySpecService propertySpecService, RTM rtm, int input, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.input = input;
    }

    private int input = 1; //1 = A, 2 = B
    private int flags;

    /**
     * These bytes indicate the months containing backflow. LSB = most recent month.
     */
    public int getFlags() {
        return flags;
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (int i = 0; i <= 12; i++) {
            if (flagIsSet(i)) {
                Date eventDate = getEventDate(i);
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, A.equals(getInputChannelName()) ? EventStatusAndDescription.EVENTCODE_BACKFLOW_OCCURRENCE_IN_MONTH_A : EventStatusAndDescription.EVENTCODE_BACKFLOW_OCCURRENCE_IN_MONTH_B, "Backflow occurrence detected in month, on input " + getInputChannelName()));
            }
        }
        return meterEvents;
    }


    @Override
    ParameterId getParameterId() throws WaveFlowException {
        switch (input) {
            case 1:
                return ParameterId.BackflowDetectionFlagsA;
            case 2:
                return ParameterId.BackflowDetectionFlagsB;
        }
        throw new WaveFlowException("Module doesn't support back flow detection.");
    }

    @Override
    public void parse(byte[] data) throws IOException {
        flags = ProtocolTools.getUnsignedIntFromBytes(data, 0, 2);
    }

    protected byte[] prepare() throws IOException {
        throw new WaveFlowException("Not allowed to write this flag.");
    }

    /**
     * Checks if the flag is set for a specific month. e.g. current month: i = 0, flag is the LSB of the flags int.
     *
     * @param i: the month
     * @return boolean if flag of that month is set
     */
    public boolean flagIsSet(int i) {
        return (1 == ((flags >> i) & 0x01));
    }

    /**
     * Creates an event date for the backflow detection.
     * Theres only one flag per month.
     *
     * @param i = the requested month. i = 0 means the current month, i = 1 means the previous month, etc.
     * @return even time stamp
     */
    public Date getEventDate(int i) {
        Calendar cal = Calendar.getInstance(getRTM().getTimeZone());
        cal.setLenient(true);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        cal.add(Calendar.MONTH, -1 * i);
        return cal.getTime();
    }

    public String getInputChannelName() {
        switch (input) {
            case 0:
                return A;
            case 1:
                return B;
            default:
                return "";
        }
    }

}