/*
 * Data.java
 *
 * Created on 30 augustus 2004, 13:52
 */

package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
/**
 *
 * @author  Koen
 * Changes:
 * GNA|02022009| Added the activatePassiveCalendar method
 */
public class ActivityCalendar extends AbstractCosemObject {

    private OctetString calendarNameActive=null;
    private Array seasonProfileActive=null;
    private Array weekProfileTableActive=null;
    private Array dayProfileTableActive=null;

    private OctetString calendarNamePassive=null;
    private Array seasonProfilePassive=null;
    private Array weekProfileTablePassive=null;
    private Array dayProfileTablePassive=null;

    private OctetString activatePassiveCalendarTime=null;

    /* LongName Attribute numbers */
    private static final int CALENDAR_NAME_ACTIVE = 2;
    private static final int SEASON_PROFILE_ACTIVE = 3;
    private static final int WEEK_PROFILE_ACTIVE = 4;
    private static final int DAY_PROFILE_ACTIVE = 5;
    private static final int CALENDAR_NAME_PASSIVE = 6;
    private static final int SEASON_PROFILE_PASSIVE = 7;
    private static final int WEEK_PROFILE_PASSIVE = 8;
    private static final int DAY_PROFILE_PASSIVE = 9;
    private static final int ACTIVATE_PASSIVE_CALENDAR_TIME = 10;

    /* ShortName Attribute values */
    private static final int CALENDAR_NAME_ACTIVE_SN = 0x08;
    private static final int SEASON_PROFILE_ACTIVE_SN = 0x10;
    private static final int WEEK_PROFILE_ACTIVE_SN = 0x18;
    private static final int DAY_PROFILE_ACTIVE_SN = 0x20;
    private static final int CALENDAR_NAME_PASSIVE_SN = 0x28;
    private static final int SEASON_PROFILE_PASSIVE_SN = 0x30;
    private static final int WEEK_PROFILE_PASSIVE_SN = 0x38;
    private static final int DAY_PROFILE_PASSIVE_SN = 0x40;
    private static final int ACTIVATE_PASSIVE_CALENDAR_TIME_SN = 0x48;

    /* Method numbers */
    private static int ACTIVATE_PASSIVE_CALENDAR = 1;

    /* ShortName Method numbers */
    private static int ACTIVATE_PASSIVE_CALENDAR_SN = 0x50;

    /** Creates a new instance of Data */
    public ActivityCalendar(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    protected int getClassId() {
        return DLMSClassId.ACTIVITY_CALENDAR.getClassId();
    }

    public void writeCalendarNameActive(OctetString calendarNameActive) throws IOException {
        write(2, calendarNameActive.getBEREncodedByteArray());
        this.calendarNameActive=calendarNameActive;
    }
    public OctetString readCalendarNameActive() throws IOException {
        if (calendarNameActive == null) {
            calendarNameActive = (OctetString)AXDRDecoder.decode(getLNResponseData(2));
        }
        return calendarNameActive;
    }

    public void writeSeasonProfileActive(Array seasonProfileActive) throws IOException {
        write(3, seasonProfileActive.getBEREncodedByteArray());
        this.seasonProfileActive=seasonProfileActive;
    }
    public Array readSeasonProfileActive() throws IOException {
        if (seasonProfileActive == null) {
            seasonProfileActive = (Array)AXDRDecoder.decode(getLNResponseData(3));
        }
        return seasonProfileActive;
    }

    public void writeWeekProfileTableActive(Array weekProfileTableActive) throws IOException {
        write(4, weekProfileTableActive.getBEREncodedByteArray());
        this.weekProfileTableActive=weekProfileTableActive;
    }
    public Array readWeekProfileTableActive() throws IOException {
        if (weekProfileTableActive == null) {
            weekProfileTableActive = (Array)AXDRDecoder.decode(getLNResponseData(4));
        }
        return weekProfileTableActive;
    }

    public void writeDayProfileTableActive(Array dayProfileTableActive) throws IOException {
        write(5, dayProfileTableActive.getBEREncodedByteArray());
        this.dayProfileTableActive=dayProfileTableActive;
    }
    public Array readDayProfileTableActive() throws IOException {
        if (dayProfileTableActive == null) {
            dayProfileTableActive = (Array)AXDRDecoder.decode(getLNResponseData(5));
        }
        return dayProfileTableActive;
    }

    public void writeCalendarNamePassive(OctetString calendarNamePassive) throws IOException {
        write(6, calendarNamePassive.getBEREncodedByteArray());
        this.calendarNamePassive=calendarNamePassive;
    }
    public OctetString readCalendarNamePassive() throws IOException {
        if (calendarNamePassive == null) {
            calendarNamePassive = (OctetString)AXDRDecoder.decode(getLNResponseData(6));
        }
        return calendarNamePassive;
    }

    public void writeSeasonProfilePassive(Array seasonProfilePassive) throws IOException {
        write(7, seasonProfilePassive.getBEREncodedByteArray());
        this.seasonProfilePassive=seasonProfilePassive;
    }
    public Array readSeasonProfilePassive() throws IOException {
        if (seasonProfilePassive == null) {
            seasonProfilePassive = (Array)AXDRDecoder.decode(getLNResponseData(7));
        }
        return seasonProfilePassive;
    }

    /**
     * Write the given WeekProfileTablePassive Array to the Device
     *
     * @param weekProfileTablePassive the weekProfile to write
     * @throws IOException if an error occurred during the write
     */
    public void writeWeekProfileTablePassive(Array weekProfileTablePassive) throws IOException {
        if(getObjectReference().isLNReference()){
            write(WEEK_PROFILE_PASSIVE, weekProfileTablePassive.getBEREncodedByteArray());
        } else {
            write(WEEK_PROFILE_PASSIVE_SN, weekProfileTablePassive.getBEREncodedByteArray());
        }
        this.weekProfileTablePassive=weekProfileTablePassive;
    }
    public Array readWeekProfileTablePassive() throws IOException {
        if (weekProfileTablePassive == null) {
            weekProfileTablePassive = (Array)AXDRDecoder.decode(getLNResponseData(8));
        }
        return weekProfileTablePassive;
    }

    public void writeDayProfileTablePassive(Array dayProfileTablePassive) throws IOException {
        write(9, dayProfileTablePassive.getBEREncodedByteArray());
        this.dayProfileTablePassive=dayProfileTablePassive;
    }
    public Array readDayProfileTablePassive() throws IOException {
        if (dayProfileTablePassive == null) {
            dayProfileTablePassive = (Array)AXDRDecoder.decode(getLNResponseData(9));
        }
        return dayProfileTablePassive;
    }

    public void writeActivatePassiveCalendarTime(OctetString activatePassiveCalendarTime) throws IOException {
        write(10, activatePassiveCalendarTime.getBEREncodedByteArray());
        this.activatePassiveCalendarTime=activatePassiveCalendarTime;
    }
    public OctetString readActivatePassiveCalendarTime() throws IOException {
        if (activatePassiveCalendarTime == null) {
            activatePassiveCalendarTime = (OctetString)AXDRDecoder.decode(getLNResponseData(10));
        }
        return activatePassiveCalendarTime;
    }

    /**
     * Trigger the 'activatePassiveCalendar' method
     * @throws IOException
     */
	public void activateNow() throws IOException {
        if(getObjectReference().isLNReference()){
            invoke(ACTIVATE_PASSIVE_CALENDAR, new Integer8(0).getBEREncodedByteArray());
        } else {
            write(ACTIVATE_PASSIVE_CALENDAR_SN, new Integer8(0).getBEREncodedByteArray());
        }
	}
}
