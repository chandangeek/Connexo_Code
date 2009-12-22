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
    private static int ACTIVATE_PASSIVE_CALENDAR = 1;

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

    public void writeWeekProfileTablePassive(Array weekProfileTablePassive) throws IOException {
        write(8, weekProfileTablePassive.getBEREncodedByteArray());
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
		invoke(ACTIVATE_PASSIVE_CALENDAR, new Integer8(0).getBEREncodedByteArray());
	}
}
