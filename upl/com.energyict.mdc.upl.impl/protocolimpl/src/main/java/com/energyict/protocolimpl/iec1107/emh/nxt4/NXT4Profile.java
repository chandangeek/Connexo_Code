package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NXT4Profile extends VDEWProfile {

    private static final int OPERATION_LOG_PROFILE_ID = 98;
    private static final int EVENT_LOG_PROFILE_ID = 210;

    private static final int DEVICE_CLOCK_STATUS = 0x8000;
    private static final int DEVICE_CLOCK_SET_AFTER = 0x0020;
    private static final int READ_MODE = 6;

    private final NXT4 protocol;

    /**
     * Creates a new instance of LZQJProfile
     */
    public NXT4Profile(NXT4 protocol,
                       AbstractVDEWRegistry abstractVDEWRegistry) {
        super(protocol, protocol, abstractVDEWRegistry, false);
        this.protocol = protocol;
        this.setReadMode(READ_MODE);
    }

    /**
     * Fetch the {@link ProfileData} from a certain date in the past to a certain date. Include the events if necessary.
     *
     * @param fromReading   - the date in the past
     * @param toReading     - the end date to read
     * @param includeEvents - boolean to indicate whether the events should be read
     * @return a filled up ProfileData object
     * @throws IOException when something goes wrong during fetching
     */
    public ProfileData getProfileData(Date fromReading, Date toReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        fromCalendar.setTime(fromReading);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
        toCalendar.setTime(toReading);

		ProfileData profileData = doGetProfileData(fromCalendar, toCalendar, 1);
        disconnectAndReconnect();

        if (includeEvents) {
            List<MeterEvent> meterEvents = readMeterEvents(fromCalendar, toCalendar);
            profileData.getMeterEvents().addAll(meterEvents);
        }

        profileData.sort();
        profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);
        return profileData;
    }

    private List<MeterEvent> readMeterEvents(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        events.addAll(doGetLogBook(OPERATION_LOG_PROFILE_ID, fromCalendar, toCalendar));
        disconnectAndReconnect();
        if (getProtocol().getProperties().readUserLogBook()) {
            events.addAll(doGetLogBook(EVENT_LOG_PROFILE_ID, fromCalendar, toCalendar));
            disconnectAndReconnect();
        }
        return events;
    }

    /**
     * After readout of data using the R6 command, the connection to the device should be re-established. <br/>
     * This should be done by doing a disconnect, followed by a reconnect.
     * @throws IOException
     */
    private void disconnectAndReconnect() throws IOException {
        if (getProtocol().getProperties().reconnectAfterR6Read()) {
            boolean oldDataReadoutMode = getProtocol().getProperties().isDataReadout();
            // Disable datareadout, in order to prevent a 2th readout of the data dump during reconnect
            getProtocol().getProperties().setDataReadout(false);
            if (getProtocol().getFlagIEC1107Connection().getHhuSignOn() != null) {
                getProtocol().getFlagIEC1107Connection().getHhuSignOn().enableDataReadout(false);
            }
            getProtocol().setReconnect(true); // To prevent the 'Extended logging' is printed a 2th time

            // Do the disconnect and reconnect
            getProtocol().disconnect();
            getProtocol().connect();

            // Restore datareadout properties
            getProtocol().getProperties().setDataReadout(oldDataReadoutMode);
            if (getProtocol().getFlagIEC1107Connection().getHhuSignOn() != null) {
                getProtocol().getFlagIEC1107Connection().getHhuSignOn().enableDataReadout(oldDataReadoutMode);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected byte parseIntervalStatus(byte[] ba, int startIdx) throws IOException {
        return (byte) Integer.parseInt(parseFindString(ba, startIdx), 16);
    }

    /**
     * Parse the string from a given byteArray with a given offset
     *
     * @param data    - the byteArray to fetch out the string
     * @param iOffset - the offset to start from
     * @return a String representing a value between the brackets
     */
    private String parseFindString(byte[] data, int iOffset) {
        int start = 0, stop = 0;
        if (iOffset >= data.length) {
            return null;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i + iOffset] == '(') {
                start = i;
            }
            if ((data[i + iOffset] == ')') || (data[i + iOffset] == '*')) {
                stop = i;
                break;
            }
        }
        byte[] strparse = new byte[stop - start - 1];
        for (int i = 0; i < (stop - start - 1); i++) {
            strparse[i] = data[i + start + 1 + iOffset];
        }
        return new String(strparse);
    }

    @Override
    protected List buildMeterEvents(byte[] responseData) throws IOException {
        String response = new String(responseData);
        if (response.contains("P." + OPERATION_LOG_PROFILE_ID)) {
            return buildMeterEvents(OPERATION_LOG_PROFILE_ID, responseData);
        } else if (response.contains("P." + EVENT_LOG_PROFILE_ID)) {
            return buildMeterEvents(EVENT_LOG_PROFILE_ID, responseData);
        } else {
            throw new IOException("Failed to parse event profile data");
        }
    }

    private List<MeterEvent> buildMeterEvents(int logBookProfileId, byte[] responseData) throws IOException {
        List meterEvents = new ArrayList();
        int t;
        Calendar calendar;
        DataParser dp = new DataParser(getProtocolLink().getTimeZone());

        try {
            VDEWTimeStamp vts = new VDEWTimeStamp(getProtocolLink().getTimeZone());
            int i = 0;
            while (true) {
                if (responseData[i] == 'P') {
                    i += 4; // skip P.98
                    i = gotoNextOpenBracket(responseData, i);

                    // No entries in logbook
                    if (dp.parseBetweenBrackets(responseData, i).compareTo("ERROR") == 0) {
                        return meterEvents;
                    }

                    // P.98 (ZSTs13)(Status)()(nrofentries)(KZ1)()..(KZz)()(Element1)..(Elementz)
                    //         0        1    2      3(eg 2)  4   5    6   7    8           9
                    vts.parse(dp.parseBetweenBrackets(responseData, i, 0));
                    calendar = (Calendar) vts.getCalendar().clone();

                    long status = Long.parseLong(dp.parseBetweenBrackets(responseData, i, 1), 16);
                    if (logBookProfileId == OPERATION_LOG_PROFILE_ID) {
                        for (t = 0; t < 16; t++) {
                            String msg = null;
                            long logBit = (status & (long) (0x0001 << t));
                            if (logBit != 0) {
                                if ((logBit == DEVICE_CLOCK_STATUS) || (logBit == DEVICE_CLOCK_SET_AFTER) || (logBit == SEASONAL_SWITCHOVER)) {
                                    String timePart = dp.parseBetweenBrackets(responseData, i, 8);
                                    String datePart = dp.parseBetweenBrackets(responseData, i, 9);
                                    vts.parse(datePart, timePart);
                                    msg = vts.getCalendar().getTime().toString();
                                } else if ((logBit == FATAL_DEVICE_ERROR)) {
                                    msg = dp.parseBetweenBrackets(responseData, i, 8);
                                }
                                MeterEvent event = getOperationLogEvent(new Date(calendar.getTime().getTime()), (int) logBit, msg);
                                if (event != null) {
                                    meterEvents.add(event);
                                }
                            }
                        }
                    } else {
                        meterEvents.add(getManipulationsLogEvent(new Date(calendar.getTime().getTime()), (int) status));
                    }

                    i = gotoNextCR(responseData, i + 1);
                } else if ((responseData[i] == '\r') || (responseData[i] == '\n')) {
                    i += 1; // skip
                } else {
                    i = gotoNextCR(responseData, i + 1);
                }

                if (i >= responseData.length) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new IOException("buildOperationLogMeterEvents> " + e.getMessage());
        }

        return meterEvents;
    }

    private MeterEvent getOperationLogEvent(Date date, int eventCode, String msg) {
        switch(eventCode) {
            case DEVICE_CLOCK_STATUS: return null; // DO not log this as event
            case CLEAR_LOADPROFILE: return new MeterEvent(date,MeterEvent.CLEAR_DATA, eventCode,"Erase load profile");
            case CLEAR_LOGBOOK: return new MeterEvent(date,MeterEvent.CLEAR_DATA, eventCode,"Erase logbook");
            case VARIABLE_SET: return new MeterEvent(date,MeterEvent.CONFIGURATIONCHANGE, eventCode,"Variable set");
            case POWER_FAILURE: return new MeterEvent(date,MeterEvent.POWERDOWN, eventCode, "Voltage failure");
            case POWER_RECOVERY: return new MeterEvent(date,MeterEvent.POWERUP, eventCode, "Voltage returned");
            case DEVICE_CLOCK_SET_AFTER: return new MeterEvent(date,MeterEvent.SETCLOCK_AFTER, eventCode,"Device clock has been set, "+msg);
            case DEVICE_RESET: return new MeterEvent(date,MeterEvent.CLEAR_DATA, eventCode, "Reset was carried out");
            case SEASONAL_SWITCHOVER: return new MeterEvent(date,MeterEvent.OTHER, eventCode,"Season change has taken place, "+msg);
            case DISTURBED_MEASURE: return new MeterEvent(date,MeterEvent.OTHER, eventCode, "Measured value interfered");
            case RUNNING_RESERVE_EXHAUSTED: return new MeterEvent(date,MeterEvent.OTHER, eventCode, "Running reserve of the meter clock is exhausted");
            case FATAL_DEVICE_ERROR: return new MeterEvent(date,MeterEvent.FATAL_ERROR, eventCode,"There is a critical meter error, error code: "+msg);
            default: return new MeterEvent(date,MeterEvent.OTHER, eventCode);
        }
    }

    private MeterEvent getManipulationsLogEvent(Date date, int eventCode) {
        switch (eventCode) {
            case 0x2000:
                return new MeterEvent(date,MeterEvent.CLEAR_DATA, eventCode,"Erase logbook");
            case 0x26A3:
            case 0x262C:
                return new MeterEvent(date, MeterEvent.TERMINAL_OPENED, eventCode, "Terminal cover opened");
            case 0x36A3:
            case 0x362C:
                return new MeterEvent(date, MeterEvent.TERMINAL_COVER_CLOSED, eventCode, "Terminal cover closed");
            case 0x26A2:
            case 0x262B:
                return new MeterEvent(date, MeterEvent.TAMPER, eventCode, "Housing cover opened");
            case 0x36A2:
            case 0x362B:
                return new MeterEvent(date, MeterEvent.OTHER, "Housing cover closed");
            case 0x26A4:
            case 0x262D:
                return new MeterEvent(date, MeterEvent.STRONG_DC_FIELD_DETECTED, "Magnetic field detected");
            case 0x36A4:
            case 0x362D:
                return new MeterEvent(date, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "Magnetic field eliminated");
            default:
                return new MeterEvent(date, MeterEvent.OTHER, eventCode);
        }
    }

    public NXT4 getProtocol() {
        return protocol;
    }
}