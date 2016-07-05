/*
 * Data.java
 *
 * Created on 30 augustus 2004, 13:52
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.ActivityCalendarAttributes;
import com.energyict.dlms.cosem.methods.ActivityCalendarMethods;

import java.io.IOException;

/**
 * Straightforward implementation of the ActivityCalendar object according to the BlueBook
 * @author Koen
 *         Changes:
 *         GNA|02022009| Added the activatePassiveCalendar method
 *
 * @since protocols-8.9.4 we added support for ShortNaming and LogicalNaming by using the {@link com.energyict.dlms.cosem.attributes.ActivityCalendarAttributes}
 * and {@link com.energyict.dlms.cosem.methods.ActivityCalendarMethods} objects
 */
public class ActivityCalendar extends AbstractCosemObject {

    private OctetString calendarNameActive = null;
    private Array seasonProfileActive = null;
    private Array weekProfileTableActive = null;
    private Array dayProfileTableActive = null;

    private OctetString calendarNamePassive = null;
    private Array seasonProfilePassive = null;
    private Array weekProfileTablePassive = null;
    private Array dayProfileTablePassive = null;

    private OctetString activatePassiveCalendarTime = null;

    /**
     * Creates a new instance of Data
     */
    public ActivityCalendar(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * {@inheritDoc}
     */
    protected int getClassId() {
        return DLMSClassId.ACTIVITY_CALENDAR.getClassId();
    }

    /**
     * Write the <b>Active</b> calendarName to the device<br/>
     * (Mostly this is not enabled by the meter)
     *
     * @param calendarNameActive the new Active calendarName to set
     * @throws java.io.IOException if the writing failed
     */
    public void writeCalendarNameActive(OctetString calendarNameActive) throws IOException {
        write(ActivityCalendarAttributes.CALENDAR_NAME_ACTIVE, calendarNameActive.getBEREncodedByteArray());
        this.calendarNameActive = calendarNameActive;
    }

    /**
     * Read the <b>Active</b> calendarName from the device
     *
     * @return the Active calendarName
     * @throws IOException if the reading failed
     */
    public OctetString readCalendarNameActive() throws IOException {
        if (calendarNameActive == null) {
            calendarNameActive = (OctetString) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.CALENDAR_NAME_ACTIVE));
        }
        return calendarNameActive;
    }

    /**
     * Write the <b>Active</b> SeasonProfile to the device<br/>
     * (Mostly this is not enabled by the meter)
     *
     * @param seasonProfileActive the new Active seasonProfile
     * @throws java.io.IOException if the write failed
     */
    public void writeSeasonProfileActive(Array seasonProfileActive) throws IOException {
        write(ActivityCalendarAttributes.SEASON_PROFILE_ACTIVE, seasonProfileActive.getBEREncodedByteArray());
        this.seasonProfileActive = seasonProfileActive;
    }

    /**
     * Read the <b>Active</b> seasonProfile from the device
     *
     * @return the Active seasonProfile
     * @throws java.io.IOException if the reading failed
     */
    public Array readSeasonProfileActive() throws IOException {
        if (seasonProfileActive == null) {
            seasonProfileActive = (Array) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.SEASON_PROFILE_ACTIVE));
        }
        return seasonProfileActive;
    }

    /**
     * Write the <b>Active</b> weekProfileTable to the device<br/>
     * (Mostly this is not enabled by the meter)
     *
     * @param weekProfileTableActive the new Active weekProfileTable
     * @throws java.io.IOException if the write failed
     */
    public void writeWeekProfileTableActive(Array weekProfileTableActive) throws IOException {
        write(ActivityCalendarAttributes.WEEK_PROFILE_TABLE_ACTIVE, weekProfileTableActive.getBEREncodedByteArray());
        this.weekProfileTableActive = weekProfileTableActive;
    }

    /**
     * Read the <b>Active</b> weekProfileTable from the device
     *
     * @return the Active weekProfileTable
     * @throws java.io.IOException if the reading failed
     */
    public Array readWeekProfileTableActive() throws IOException {
        if (weekProfileTableActive == null) {
            weekProfileTableActive = (Array) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.WEEK_PROFILE_TABLE_ACTIVE));
        }
        return weekProfileTableActive;
    }

    /**
     * Write the <b>Active</b> dayProfileTable to the device<br/>
     * (Mostly this is not enabled by the meter)
     *
     * @param dayProfileTableActive the new Active dayProfileTable
     * @throws java.io.IOException if the write failed
     */
    public void writeDayProfileTableActive(Array dayProfileTableActive) throws IOException {
        write(ActivityCalendarAttributes.DAY_PROFILE_TABLE_ACTIVE, dayProfileTableActive.getBEREncodedByteArray());
        this.dayProfileTableActive = dayProfileTableActive;
    }

    /**
     * Read the <b>Active</b> dayProfileTable from the device
     *
     * @return the Active dayProfileTable
     * @throws java.io.IOException if the reading failed
     */
    public Array readDayProfileTableActive() throws IOException {
        if (dayProfileTableActive == null) {
            dayProfileTableActive = (Array) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.DAY_PROFILE_TABLE_ACTIVE));
        }
        return dayProfileTableActive;
    }

    /**
     * Write the <b>Passive</b> calendarName to the device
     *
     * @param calendarNamePassive the new Passive calendarName to write
     * @throws java.io.IOException if the write failed
     */
    public void writeCalendarNamePassive(OctetString calendarNamePassive) throws IOException {
        write(ActivityCalendarAttributes.CALENDAR_NAME_PASSIVE, calendarNamePassive.getBEREncodedByteArray());
        this.calendarNamePassive = calendarNamePassive;
    }

    /**
     * Read the <b>Passive</b> calendarName from the device
     *
     * @return the Passive calendarName
     * @throws java.io.IOException if the reading failed
     */
    public OctetString readCalendarNamePassive() throws IOException {
        if (calendarNamePassive == null) {
            calendarNamePassive = (OctetString) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.CALENDAR_NAME_PASSIVE));
        }
        return calendarNamePassive;
    }

    /**
     * Write the <b>Passive</b> seasonProfile to the device
     *
     * @param seasonProfilePassive the new Passive seasonProfile
     * @throws java.io.IOException if the write failed
     */
    public void writeSeasonProfilePassive(Array seasonProfilePassive) throws IOException {
        write(ActivityCalendarAttributes.SEASON_PROFILE_PASSIVE, seasonProfilePassive.getBEREncodedByteArray());
        this.seasonProfilePassive = seasonProfilePassive;
    }

    /**
     * Read the <b>Passive</b> seasonProfile from the device
     *
     * @return the Passive seasonProfile
     * @throws java.io.IOException if the reading failed
     */
    public Array readSeasonProfilePassive() throws IOException {
        if (seasonProfilePassive == null) {
            seasonProfilePassive = (Array) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.SEASON_PROFILE_PASSIVE));
        }
        return seasonProfilePassive;
    }

    /**
     * Write the <b>Passive</b> weekProfileTable Array to the Device
     *
     * @param weekProfileTablePassive the Passive weekProfileTable to write
     * @throws java.io.IOException if an error occurred during the write
     */
    public void writeWeekProfileTablePassive(Array weekProfileTablePassive) throws IOException {
        write(ActivityCalendarAttributes.WEEK_PROFILE_TABLE_PASSIVE, weekProfileTablePassive.getBEREncodedByteArray());
        this.weekProfileTablePassive = weekProfileTablePassive;
    }

    /**
     * Read the <b>Passive</b> weekProfileTable from the device
     *
     * @return the Passive weekProfileTable
     * @throws java.io.IOException if the reading failed
     */
    public Array readWeekProfileTablePassive() throws IOException {
        if (weekProfileTablePassive == null) {
            weekProfileTablePassive = (Array) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.WEEK_PROFILE_TABLE_PASSIVE));
        }
        return weekProfileTablePassive;
    }

    /**
     * Write the <b>Passive</b> dayProfileTable to the device
     *
     * @param dayProfileTablePassive the new Passive dayProfileTable
     * @throws java.io.IOException if the write failed
     */
    public void writeDayProfileTablePassive(Array dayProfileTablePassive) throws IOException {
        write(ActivityCalendarAttributes.DAY_PROFILE_TABLE_PASSIVE, dayProfileTablePassive.getBEREncodedByteArray());
        this.dayProfileTablePassive = dayProfileTablePassive;
    }

    /**
     * Read the <b>Passive</b> dayProfileTable from the device
     *
     * @return the Passive dayProfileTable
     * @throws java.io.IOException if the reading failed
     */
    public Array readDayProfileTablePassive() throws IOException {
        if (dayProfileTablePassive == null) {
            dayProfileTablePassive = (Array) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.DAY_PROFILE_TABLE_PASSIVE));
        }
        return dayProfileTablePassive;
    }

    /**
     * Write the activationTime to activate the <b>PASSIVE</b> calendar
     *
     * @param activatePassiveCalendarTime the activationTime to write
     * @throws java.io.IOException if the write failed
     */
    public void writeActivatePassiveCalendarTime(OctetString activatePassiveCalendarTime) throws IOException {
        write(ActivityCalendarAttributes.ACTIVATE_PASSIVE_CALENDAR_TIME, activatePassiveCalendarTime.getBEREncodedByteArray());
        this.activatePassiveCalendarTime = activatePassiveCalendarTime;
    }

    /**
     * Read the activationTime to activate the <b>PASSIVE</b> calendar
     *
     * @return the activationTime of the Passive calendar
     * @throws java.io.IOException if the reading failed
     */
    public OctetString readActivatePassiveCalendarTime() throws IOException {
        if (activatePassiveCalendarTime == null) {
            activatePassiveCalendarTime = (OctetString) AXDRDecoder.decode(getResponseData(ActivityCalendarAttributes.ACTIVATE_PASSIVE_CALENDAR_TIME));
        }
        return activatePassiveCalendarTime;
    }

    /**
     * Trigger the 'activatePassiveCalendar' method
     *
     * @throws java.io.IOException if the invocation failed
     */
    public void activateNow() throws IOException {
        methodInvoke(ActivityCalendarMethods.ACTIVATE_PASSIVE_CALENDAR, new Integer8(0).getBEREncodedByteArray());
    }
}
