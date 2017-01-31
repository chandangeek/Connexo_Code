/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging.tariff;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.attributeobjects.DayProfileActions;
import com.energyict.dlms.cosem.attributeobjects.DayProfiles;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.dlms.cosem.attributeobjects.WeekProfiles;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.Change;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.Contract;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.Day;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.Season;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.SpecialDays;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.Week;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PrimeActivityCalendarController {

    private static final ObisCode ACTIVITY_CALENDAR_OBISCODE = ObisCode.fromString("0.0.13.0.0.255");
    private static final ObisCode SPECIAL_DAYS_TABLE_OBISCODE = ObisCode.fromString("0.0.11.0.0.255");
    private static final String UTF8 = "UTF-8";

    private CosemObjectFactory cosemObjectFactory;
    private TimeZone timeZone;
    private ObisCode activityCalendarObisCode;
    private ObisCode specialDaysCalendarObisCode;

    /**
     * The {@link com.energyict.dlms.axrdencoding.Array} containing the {@link com.energyict.dlms.cosem.attributeobjects.SeasonProfiles}
     */
    private Array seasonArray;
    /**
     * The {@link com.energyict.dlms.axrdencoding.Array} containing the {@link com.energyict.dlms.cosem.attributeobjects.WeekProfiles}
     */
    private Array weekArray;
    /**
     * The {@link com.energyict.dlms.axrdencoding.Array} containing the {@link com.energyict.dlms.cosem.attributeobjects.DayProfiles}
     */
    private Array dayArray;
    /**
     * The {@link com.energyict.dlms.axrdencoding.Array} containing the {@link com.energyict.dlms.cosem.SpecialDaysTable}
     */
    private Array specialDayArray;
    /**
     * The time when to active the passive Calendar
     */
    private OctetString activatePassiveCalendarTime;

    /**
     * The name of the passive Calendar
     */
    private OctetString passiveCalendarName;

    public PrimeActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone) {
        this(cosemObjectFactory, timeZone, ACTIVITY_CALENDAR_OBISCODE, SPECIAL_DAYS_TABLE_OBISCODE);
    }

    public PrimeActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, ObisCode activityCalendarObisCode, ObisCode specialDaysCalendarObisCode) {
        this.activityCalendarObisCode = activityCalendarObisCode;
        this.specialDaysCalendarObisCode = specialDaysCalendarObisCode;
        this.cosemObjectFactory = cosemObjectFactory;
        this.timeZone = timeZone;
        this.seasonArray = new Array();
        this.weekArray = new Array();
        this.dayArray = new Array();
        this.specialDayArray = new Array();
        this.activatePassiveCalendarTime = OctetString.fromString("");
        this.passiveCalendarName = OctetString.fromString("");
    }

    /**
     * Getter for the {@link #seasonArray}
     *
     * @return the current {@link #seasonArray}
     */
    protected Array getSeasonArray() {
        return seasonArray;
    }

    /**
     * Getter for the {@link #weekArray}
     *
     * @return the current {@link #weekArray}
     */
    protected Array getWeekArray() {
        return weekArray;
    }

    /**
     * Getter for the {@link #dayArray}
     *
     * @return the current {@link #dayArray}
     */
    protected Array getDayArray() {
        return dayArray;
    }

    protected Array getSpecialDayArray() {
        return specialDayArray;
    }

    /**
     * Parse the given content to a proper ActivityCalendar and SpecialDay table related objects.
     */
    public void parseContent(Contract contractDefinitions) throws IOException {
        int contractNumber = contractDefinitions.getC();
        activityCalendarObisCode = ProtocolTools.setObisCodeField(ACTIVITY_CALENDAR_OBISCODE, 4, (byte) contractNumber);            //Set number to E-field
        specialDaysCalendarObisCode = ProtocolTools.setObisCodeField(SPECIAL_DAYS_TABLE_OBISCODE, 4, (byte) (contractNumber + 3));

        final String calendarName = contractDefinitions.getCalendarName();
        final String paddedCalendarName = ProtocolTools.addPaddingAndClip(calendarName, '0', 12, true);
        passiveCalendarName = getOctetStringFromHexString(paddedCalendarName);

        activatePassiveCalendarTime = getOctetStringFromTimeStamp(contractDefinitions.getActDate());
        createSeasonProfiles(contractDefinitions.getSeason());
        createDayProfiles(contractDefinitions.getDay());
        createWeekProfiles(contractDefinitions.getWeek());
        createSpecialDays(contractDefinitions.getSpecialDays());

    }

    private OctetString getOctetStringFromTimeStamp(String dateString) throws IOException {
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(timeZone);
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            throw new IOException("Error parsing activation date + '" + dateString + "', expected format: 'yyyyMMddHHmmss'");
        }
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(date);
        OctetString result = new OctetString(new AXDRDateTime(cal).getBEREncodedByteArray(), 0);
        result.getOctetStr()[9] = (byte) 0x80;    //Set timezone to unspecified
        result.getOctetStr()[10] = (byte) 0x00;
        if (timeZone.inDaylightTime(date)) {
            result.getOctetStr()[11] = (byte) 0x80;    //Set DST flag
        } else {
            result.getOctetStr()[11] = (byte) 0x00;    //No DST flag
        }
        return result;
    }

    private void createSpecialDays(List<SpecialDays> specialDays) throws IOException {
        specialDayArray = new Array(specialDays.size());
        int index = 0;
        for (SpecialDays specialDay : specialDays) {
            Structure specialDayStructure = new Structure();
            specialDayStructure.addDataType(new Unsigned16(index));
            OctetString dateTime = getOctetStringFromTimeStamp(specialDay.getDT());
            if ("Y".equalsIgnoreCase(specialDay.getDTCard())) {
                dateTime.getOctetStr()[0] = (byte) 0xFF; //Ignore year
                dateTime.getOctetStr()[1] = (byte) 0xFF;
            }

            byte[] date = ProtocolTools.getSubArray(dateTime.getOctetStr(), 0, 5);  //Only use the date part
            specialDayStructure.addDataType(OctetString.fromByteArray(date));

            specialDayStructure.addDataType(new Unsigned8(specialDay.getDayID()));
            specialDayArray.setDataType(index, specialDayStructure);
            index++;
        }
    }

    protected OctetString getActivatePassiveCalendarTime(Long activationDate) throws IOException {
        return new OctetString(convertUnixToGMTDateTime(activationDate).getBEREncodedByteArray(), 0);
    }

    /**
     * Write a given name to the Calendar
     */
    public void writeCalendarName() throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeCalendarNamePassive(this.passiveCalendarName);
    }

    /**
     * Write the complete ActivityCalendar to the device
     */
    public void writeAndActivateCalendar() throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeSeasonProfilePassive(getSeasonArray());
        ac.writeWeekProfileTablePassive(getWeekArray());
        ac.writeDayProfileTablePassive(getDayArray());

        if ("01".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())) {
            ac.activateNow();
        } else if (!"0".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())) {
            ac.writeActivatePassiveCalendarTime(this.activatePassiveCalendarTime);
        }
    }

    /**
     * Write the SpecialDays table to the device
     */
    public void writeSpecialDaysTable() throws IOException {
        getSpecialDayTable().writeSpecialDays(getSpecialDayArray());
    }

    /**
     * Write a time from which the new ActivityCalendar should be active
     *
     * @param activationDate the given time
     */
    public void writeCalendarActivationTime(Calendar activationDate) throws IOException {
        if (activationDate == null) {
            getActivityCalendar().activateNow();
        } else {
            getActivityCalendar().writeActivatePassiveCalendarTime(getActivatePassiveCalendarTime(activationDate.getTimeInMillis()));
        }
    }

    /**
     * Get the name of the current <u>Active</u> Calendar
     *
     * @return the name of the current <u>Active</u> Calendar
     */
    public String getCalendarName() throws IOException {
        return getActivityCalendar().readCalendarNameActive().stringValue();
    }

    /**
     * Getter for the LOCAL {@link com.energyict.dlms.cosem.ActivityCalendar}
     *
     * @return the current local {@link com.energyict.dlms.cosem.ActivityCalendar}
     */
    protected ActivityCalendar getActivityCalendar() throws IOException {
        return cosemObjectFactory.getActivityCalendar(activityCalendarObisCode);
    }


    protected SpecialDaysTable getSpecialDayTable() throws IOException {
        return cosemObjectFactory.getSpecialDaysTable(specialDaysCalendarObisCode);
    }

    /**
     * Create the Season{@link com.energyict.dlms.axrdencoding.Array}. The season Array to write to the meter should contain :<br>
     * <code>
     * seasonProfile::=structure{<br>
     * - season_profilename :   OctetString<br>
     * - season_start      :   OctetString<br>
     * - week_name         :   OctetString<br>
     * }
     * </code>
     *
     * @throws java.io.IOException
     */
    private void createSeasonProfiles(List<Season> seasons) throws IOException {
        for (Season season : seasons) {
            SeasonProfiles sp = new SeasonProfiles();

            sp.setSeasonProfileName(getOctetStringFromHexString(season.getName()));     //octetstring, length 1
            sp.setSeasonStart(getOctetStringFromHexString(season.getStart()));          //octetstring, length 12
            sp.setWeekName(getOctetStringFromHexString(season.getWeek()));              //octetstring, length 1
            seasonArray.addDataType(sp);
        }
    }

    private OctetString getOctetStringFromHexString(String hex) {
        byte[] bytes = ProtocolTools.getBytesFromHexString(hex, "");
        return OctetString.fromByteArray(bytes);
    }

    /**
     * Create the Week{@link com.energyict.dlms.axrdencoding.Array}. The week Array to write to the meter should contain :<br>
     * <code>
     * week_profiles::=structure {
     * - week_profile_name  :   OctetString<br>
     * - monday             :   day_id (int)<br>
     * - tuseday            :   day_id (int)<br>
     * - wednesday          :   day_id (int)<br>
     * - thursday           :   day_id (int)<br>
     * - friday             :   day_id (int)<br>
     * - saturday           :   day_id (int)<br>
     * - sunday             :   day_id (int)<br>
     * }
     * </code>
     */
    private void createWeekProfiles(List<Week> weeks) {

        for (Week week : weeks) {
            WeekProfiles wp = new WeekProfiles();
            wp.setWeekProfileName(getOctetStringFromHexString(week.getName()));
            wp.setMonday(getUnsigned8FromOctetString(week.getWeek(), 0));
            wp.setTuesday(getUnsigned8FromOctetString(week.getWeek(), 1));
            wp.setWednesday(getUnsigned8FromOctetString(week.getWeek(), 2));
            wp.setThursday(getUnsigned8FromOctetString(week.getWeek(), 3));
            wp.setFriday(getUnsigned8FromOctetString(week.getWeek(), 4));
            wp.setSaturday(getUnsigned8FromOctetString(week.getWeek(), 5));
            wp.setSunday(getUnsigned8FromOctetString(week.getWeek(), 6));
            weekArray.addDataType(wp);
        }
    }

    /**
     * Fetch a day value from a week string, e.g. fetch '01' from '01010101010101'
     */
    private Unsigned8 getUnsigned8FromOctetString(String week, int offset) {
        String substring;
        try {
            int position = offset * 2;
            substring = week.substring(position, position + 2);
        } catch (IndexOutOfBoundsException e) {
            substring = "00";
        }
        return new Unsigned8(Integer.valueOf(substring));
    }

    /**
     * Create the Day{@link com.energyict.dlms.axrdencoding.Array}. The day Array to write to the meter should contain :<br>
     * <code>
     * <p/>
     * day_profile::=structure{
     * - day_id         : unsigned (int)
     * - day_schedule   : array of day_profile_actions
     * }
     * <p/>
     * day_profile_actions::=structure{
     * - start_time             : OctetString
     * - script_logical_name    : OctetString
     * - script_selector        : long_unsigned
     * }
     * </code>
     *
     * @throws java.io.IOException
     */
    private void createDayProfiles(List<Day> days) throws IOException {
        for (Day day : days) {

            DayProfiles dp = new DayProfiles();
            dp.setDayId(new Unsigned8(day.getId()));  //Up to 24 possible

            Array dpsArray = new Array();    //Contains up to 6 entries

            for (Change change : day.getChange()) {
                DayProfileActions dpa = new DayProfileActions();
                dpa.setStartTime(getOctetStringFromHexString(change.getHour()));
                dpa.setScriptSelector(new Unsigned16(change.getTariffRate()));
                dpa.setScriptLogicalName(OctetString.fromObisCode("0.0.10.0.100.255"));  //Fixed, not used
                dpsArray.addDataType(dpa);
            }

            dp.setDayProfileActions(dpsArray);

            //add day definition
            dayArray.addDataType(dp);
        }
    }

    /**
     * Convert a given epoch timestamp in MILLISECONDS to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time - the time in milliSeconds sins 1th jan 1970 00:00:00 GMT
     * @return the AXDRDateTime of the given time
     */
    public AXDRDateTime convertUnixToGMTDateTime(Long time) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(time);
        return new AXDRDateTime(cal);
    }
}