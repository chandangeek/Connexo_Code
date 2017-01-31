/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.attributeobjects.DayProfileActions;
import com.energyict.dlms.cosem.attributeobjects.DayProfiles;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.dlms.cosem.attributeobjects.WeekProfiles;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * ActivityCalendar implementation for the ZMD Smartmeter protocol
 */
public class ZMDActivityCalendarController implements ActivityCalendarController {

    public static final String RAW_CONTENT_TAG = "Activity_Calendar";
    public static final String xmlDocType = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String ACTIVATION_DATE = "ActivationDate";
    public static final String CALENDAR_NAME = "CalendarName";

    public static final String[] errors = {"ActivityCalendar did not contain a valid SEASONName.",
            "ActivityCalendar did not contain a valid WEEKPROFILEName."};

    // Indexes of the DateTime OctetString
    private static final int indexDtYearHigh = 0;
    private static final int indexDtYearLow = 1;
    private static final int indexDtMonth = 2;
    private static final int indexDtDayOfMonth = 3;
    private static final int indexDtDayOfWeek = 4;
    private static final int indexDtHour = 5;
    private static final int indexDtMinutes = 6;
    private static final int indexDtSeconds = 7;
    private static final int indexDtHundredsOfSeconds = 8;
    private static final int indexDtDeviationHigh = 9;
    private static final int indexDtDeviationLow = 10;
    private static final int indexDtClockStatus = 11;
    private static final byte[] initialDateTimeArray = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            0, 0, 0, 0, (byte) 0x80, 0, 0};

    // Indexes of the Time OctetString
    private static final int indexTHour = 0;
    private static final int indexTMinutes = 1;
    private static final int indexTSeconds = 2;
    private static final int indexTHundredsOfSeconds = 3;
    private static final byte[] initialDayTariffStartTimeArray = new byte[]{0x00, 0x00, 0x00, 0x00};

    // Indexes of the Date OctetString
    private static final int indexDYearHigh = 0;
    private static final int indexDYearLow = 1;
    private static final int indexDMonth = 2;
    private static final int indexDDayOfMonth = 3;
    private static final int indexDDayOfWeek = 4;
    private static final byte[] initialSpecialDayDateArray = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    private static final ObisCode activityCalendar = ObisCode.fromString("0.0.13.0.0.255");
    private static final ObisCode specialDayTable = ObisCode.fromString("0.0.11.0.0.255");

    private ZMD protocol;

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
     * A temporary Map to hold the specialDayNodes
     */
    private Map<Long, Node> tempSpecialDayMap = new HashMap<Long, Node>();
    /**
     * Sorted list containing the specialDayEntryDatesNode ...
     */
    private List<Node> sortedSpecialDayNodes = new ArrayList<Node>();

    /**
     * The time when to active the passive Calendar
     */
    private OctetString activatePassiveCalendarTime;

    /**
     * The name of the passive Calendar
     */
    private OctetString passiveCalendarName;

    /**
     * The current {@link org.apache.commons.logging.Log}
     */
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Contains a map of given DayProfile Ids and usable DayProfile Ids. The ApolloMeter does not allow a dayId starting from <b>0</b>
     */
    private Map<String, Integer> tempShiftedDayIdMap = new HashMap<String, Integer>();

    public ZMDActivityCalendarController(){
        this(null);
    }

    public ZMDActivityCalendarController(ZMD protocol) {
        this.protocol = protocol;
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
     * Some restrictions are added:
     * <ul>
     * <li> Only 1 season is supported because Andrea added some additional restrictions to the dayId's per season
     * <li> Only 4 dayTypes are supported, same reason as above
     * </ul>
     *
     * @param xmlContent the activityCalendar content
     * @throws java.io.IOException if a parsing exception occurred
     */
    public void parseContent(String xmlContent) throws IOException {

        final String openingTag = "<" + RAW_CONTENT_TAG + ">";
        final String closingTag = "</" + RAW_CONTENT_TAG + ">";
        String compressedBase64Content = getCompressedBase64Content(xmlContent, openingTag, closingTag);
        String content = openingTag + ProtocolTools.decompress(compressedBase64Content) + closingTag;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlDocType.concat(content))));

            NodeList actDateList = doc.getElementsByTagName(ACTIVATION_DATE);

            Long activationDate = Long.valueOf(actDateList.item(0).getTextContent());
            if (0 == activationDate) {
                activatePassiveCalendarTime = OctetString.fromString(Long.toString(activationDate));
            } else {
                activatePassiveCalendarTime = new OctetString(convertUnixToDeviceTimeZoneDateTime(activationDate).getBEREncodedByteArray(), 0);
            }

            NodeList passiveNameList = doc.getElementsByTagName(CALENDAR_NAME);
            passiveCalendarName = OctetString.fromByteArray(constructEightByteCalendarName(passiveNameList.item(0).getTextContent()));

            NodeList dayProfileList = doc.getElementsByTagName(CodeTableXml.dayProfile);
            createShiftedDayIdMap(dayProfileList);
            createDayProfiles(dayProfileList);

            NodeList seasonProfileList = doc.getElementsByTagName(CodeTableXml.seasonProfile);
            createSeasonProfiles(seasonProfileList);

            NodeList weekProfileList = doc.getElementsByTagName(CodeTableXml.weekProfile);
            createWeekProfiles(weekProfileList);

            NodeList specialDayEntryDate = doc.getElementsByTagName(CodeTableXml.specialDayEntryDate);
            createTempSortedDateList(specialDayEntryDate);
            sortSpecialDays();

            NodeList specialDayList = doc.getElementsByTagName(CodeTableXml.specialDays);
            createSpecialDays(specialDayList);

            logger.debug("SeasonArray : " + ParseUtils.decimalByteToString(seasonArray.getBEREncodedByteArray()));
            logger.debug("WeekArray : " + ParseUtils.decimalByteToString(weekArray.getBEREncodedByteArray()));
            logger.debug("DayArray : " + ParseUtils.decimalByteToString(dayArray.getBEREncodedByteArray()));
            logger.debug("SpecialDayArray : " + ParseUtils.decimalByteToString(specialDayArray.getBEREncodedByteArray()));

        } catch (ParserConfigurationException e) {
            logger.error("ActivityCalendar parser -> Could not create a DocumentBuilder.");
            throw new IOException("ActivityCalendar parser -> Could not create a DocumentBuilder. ParseConfigurationException message : " + e.getLocalizedMessage());
        } catch (SAXException e) {
            logger.error("ActivityCalendar parser -> A parse ERROR occurred.");
            throw new IOException("ActivityCalendar parser -> A parse ERROR occurred. SAXException message : " + e.getLocalizedMessage());
        }
    }

    private String getCompressedBase64Content(final String xmlContent, final String openingTag, final String closingTag) {
        return xmlContent.substring(xmlContent.indexOf(openingTag) + openingTag.length(), xmlContent.indexOf(closingTag));
    }

    /**
     * Construct a map of shifted dayProfile ID's. We are reusing the {@link com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml}, but this results
     * in dayIds with values of 0, which the device does not allow ...
     * This method will most likely result in a shift of all the dayProfileId's (all ID's +1)
     *
     * @param dayProfileList the given list of dayProfiles
     */
    private void createShiftedDayIdMap(final NodeList dayProfileList) {
        Node dayProfile;
        int dayIdCounter = 1;
        for (int i = 0; i < dayProfileList.getLength(); i++) {
            dayProfile = dayProfileList.item(i);
            for (int j = 0; j < dayProfile.getChildNodes().getLength(); j++) {
                if (dayProfile.getChildNodes().item(j).getNodeName().equalsIgnoreCase(CodeTableXml.dayId)) {
                    this.tempShiftedDayIdMap.put(dayProfile.getChildNodes().item(j).getTextContent(), dayIdCounter++);
                    logger.debug("DayId : " + dayProfile.getChildNodes().item(j).getTextContent() + " is changed to " + dayIdCounter);
                }
            }
        }
    }

    /**
     * Construct the calendarName, which should be 8 bytes
     *
     * @param name the name to convert to a Calendar.
     * @return a byte Array containing the CalendarName
     */
    protected byte[] constructEightByteCalendarName(String name) {
        byte[] calName = new byte[]{0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        System.arraycopy(name.getBytes(), 0, calName, 0, (name.getBytes().length > 8) ? 8 : name.getBytes().length);
        return calName;
    }

    /**
     * Write a given name to the Calendar
     *
     * @param name the name of the ActivityCalendar
     */
    public void writeCalendarName(String name) throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeCalendarNamePassive(this.passiveCalendarName);
    }

    /**
     * Write the complete ActivityCalendar to the device
     */
    public void writeCalendar() throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeSeasonProfilePassive(getSeasonArray());
        ac.writeWeekProfileTablePassive(getWeekArray());
        ac.writeDayProfileTablePassive(getDayArray());

        getSpecialDayTable().writeSpecialDays(getSpecialDayArray());

        // Always write activationDate - activateNow() function is not supported
        if (!"0".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())) {
            ac.writeActivatePassiveCalendarTime(this.activatePassiveCalendarTime);
        } else {
            logger.trace("No passiveCalendar activation date was given.");
        }
    }

    /**
     * Write the SpecialDays table to the device
     */
    public void writeSpecialDaysTable() {
        //NotSupported - SpecialDaysTable.writeSpecialDays() is uesd instead.
    }

    /**
     * Write a time from which the new ActivityCalendar should be active
     *
     * @param activationDate the given time
     */
    public void writeCalendarActivationTime(Calendar activationDate) throws IOException {
        if (activationDate == null) {
            logger.trace("No passiveCalendar activation date was given! No activation date will be set!");
        } else {
            getActivityCalendar().writeActivatePassiveCalendarTime(new OctetString(convertUnixToDeviceTimeZoneDateTime(activationDate.getTimeInMillis()).getBEREncodedByteArray(), 0));
        }
    }

    @Override
    public String getCalendarName() throws IOException {
        return getActivityCalendar().readCalendarNameActive().stringValue();
    }

    @Override
    public String getPassiveCalendarName() throws IOException {
        return getActivityCalendar().readCalendarNamePassive().stringValue();
    }

    /**
     * Getter for the LOCAL {@link com.energyict.dlms.cosem.ActivityCalendar}
     *
     * @return the current local {@link com.energyict.dlms.cosem.ActivityCalendar}
     */
    protected ActivityCalendar getActivityCalendar() throws IOException {
        return this.protocol.getCosemObjectFactory().getActivityCalendar(activityCalendar);
    }


    protected SpecialDaysTable getSpecialDayTable() throws IOException {
        return this.protocol.getCosemObjectFactory().getSpecialDaysTable(specialDayTable);
    }

    /**
     * Create the Season{@link com.energyict.dlms.axrdencoding.Array}. The season Array to write to the meter should contain :<br>
     * <code>
     * seasonProfile::=structure{<br>
     * - season_profilename :   OctetString<br>
     * - season_start      :   OctetString<br>
     * - week_name         :   OctetString<br>
     * <b>- index           :  long-unsigned</b><br>
     * }
     * </code>
     * The last <b>index</b> is added because the array could be larger then one block and the AM500 could not parse that correctly.
     * To prevent large blocks you can send them one by one ...
     *
     * @param seasonProfileList a list containing all the seasons
     * @throws java.io.IOException
     */
    private void createSeasonProfiles(NodeList seasonProfileList) throws IOException {
        Node seasonProfile;
        for (int i = 0; i < seasonProfileList.getLength(); i++) {
            seasonProfile = seasonProfileList.item(i);
            SeasonProfiles sp = new SeasonProfiles();
            for (int j = 0; j < seasonProfile.getChildNodes().getLength(); j++) {
                Node seasonNode = seasonProfile.getChildNodes().item(j);
                if (seasonNode.getNodeName().equalsIgnoreCase(CodeTableXml.seasonProfileName)) {
                    logger.debug("SeasonProfileName : " + seasonNode.getTextContent());
                    sp.setSeasonProfileName(createSeasonName(seasonNode.getTextContent()));
                } else if (seasonNode.getNodeName().equalsIgnoreCase(CodeTableXml.seasonStart)) {
                    byte[] startTime = initialDateTimeArray.clone();
                    //TODO it is possible that you have to give at least 1 valid value
                    logger.debug("Entering SeasonStart Element");
                    for (int k = 0; k < seasonNode.getChildNodes().getLength(); k++) {
                        Node startTimeElement = seasonNode.getChildNodes().item(k);
                        if (startTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dYear)) {
                            logger.debug("SeasonStartYear : " + startTimeElement.getTextContent());
                            startTime[indexDtYearLow] = (byte) (Integer.valueOf(startTimeElement.getTextContent()) & 0x00FF);
                            startTime[indexDtYearHigh] = (byte) ((Integer.valueOf(startTimeElement.getTextContent()) & 0xFF00) >> 8);
                        } else if (startTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dMonth)) {
                            logger.debug("SeasonStartMonth : " + startTimeElement.getTextContent());
                            startTime[indexDtMonth] = (byte) (Integer.valueOf(startTimeElement.getTextContent()) & 0xFF);
                        } else if (startTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dDay)) {
                            logger.debug("SeasonStartDay : " + startTimeElement.getTextContent());
                            startTime[indexDtDayOfMonth] = (byte) (Integer.valueOf(startTimeElement.getTextContent()) & 0xFF);
                        }
                    }
                    sp.setSeasonStart(OctetString.fromByteArray(startTime));
                } else if (seasonNode.getNodeName().equalsIgnoreCase(CodeTableXml.seasonWeekName)) {
                    logger.debug("SeasonWeekName : " + seasonNode.getTextContent());
                    sp.setWeekName(createWeekName(seasonNode.getTextContent()));
                }
            }
            seasonArray.addDataType(sp);
        }
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
     * The last <b>index</b> is added because the array could be larger then one block and the AM500 could not parse that correctly.
     * To prevent large blocks you can send them one by one ...
     *
     * @param weekProfileList a list containing all the weekProfiles
     * @throws java.io.IOException
     */
    private void createWeekProfiles(NodeList weekProfileList) throws IOException {
        Node weekProfile;
        for (int i = 0; i < weekProfileList.getLength(); i++) {
            weekProfile = weekProfileList.item(i);
            WeekProfiles wp = new WeekProfiles();
            for (int j = 0; j < weekProfile.getChildNodes().getLength(); j++) {
                Node weekNode = weekProfile.getChildNodes().item(j);
                if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.weekProfileName)) {
                    logger.debug("WeekProfileName : " + weekNode.getTextContent());
                    wp.setWeekProfileName(createWeekName(weekNode.getTextContent()));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkMonday)) {
                    logger.debug("Monday : " + weekNode.getTextContent());
                    wp.setMonday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkTuesday)) {
                    logger.debug("Tuesday : " + weekNode.getTextContent());
                    wp.setTuesday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkWednesDay)) {
                    logger.debug("WednesDay : " + weekNode.getTextContent());
                    wp.setWednesday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkThursday)) {
                    logger.debug("Thursday : " + weekNode.getTextContent());
                    wp.setThursday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkFriday)) {
                    logger.debug("Friday : " + weekNode.getTextContent());
                    wp.setFriday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkSaturday)) {
                    logger.debug("Saturday : " + weekNode.getTextContent());
                    wp.setSaturday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableXml.wkSunday)) {
                    logger.debug("Sunday : " + weekNode.getTextContent());
                    wp.setSunday(new Unsigned8(this.tempShiftedDayIdMap.get(weekNode.getTextContent())));
                }
            }
            weekArray.addDataType(wp);
        }
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
     * @param dayProfileList a list containing all the dayProfiles
     * @throws java.io.IOException
     */
    private void createDayProfiles(NodeList dayProfileList) throws IOException {
        Node dayProfile;
        for (int i = 0; i < dayProfileList.getLength(); i++) {
            dayProfile = dayProfileList.item(i);
//            Structure dp = new Structure();
            DayProfiles dp = new DayProfiles();
            for (int j = 0; j < dayProfile.getChildNodes().getLength(); j++) {
                if (dayProfile.getChildNodes().item(j).getNodeName().equalsIgnoreCase(CodeTableXml.dayId)) {
                    logger.debug("DayId : " + dayProfile.getChildNodes().item(j).getTextContent());
                    dp.setDayId(new Unsigned8(this.tempShiftedDayIdMap.get(dayProfile.getChildNodes().item(j).getTextContent())));

                } else if (dayProfile.getChildNodes().item(j).getNodeName().equalsIgnoreCase(CodeTableXml.dayTariffs)) {
                    Node dayProfileSchedules = dayProfile.getChildNodes().item(j);
                    logger.debug("NrOfDayProfileTariffs : " + dayProfileSchedules.getChildNodes().getLength());
                    Array dpsArray = new Array();
                    for (int k = 0; k < dayProfileSchedules.getChildNodes().getLength(); k++) {
                        Node dayProfileSchedule = dayProfileSchedules.getChildNodes().item(k);
                        logger.debug("NrOfElements : " + dayProfileSchedule.getChildNodes().getLength());
                        DayProfileActions dpa = new DayProfileActions();
                        for (int l = 0; l < dayProfileSchedule.getChildNodes().getLength(); l++) {
                            byte[] dayTariffStartTime = initialDayTariffStartTimeArray.clone();
                            Node schedule = dayProfileSchedule.getChildNodes().item(l);
                            if (schedule.getNodeName().equalsIgnoreCase(CodeTableXml.dayTariffStartTime)) {
                                logger.debug("DayScheduleStartTime : " + schedule.getTextContent());
                                for (int m = 0; m < schedule.getChildNodes().getLength(); m++) {
                                    Node timeElement = schedule.getChildNodes().item(m);
                                    if (timeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dHour)) {
                                        dayTariffStartTime[indexTHour] = (byte) (Integer.valueOf(timeElement.getTextContent()) & 0xFF);
                                    } else if (timeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dMinutes)) {
                                        dayTariffStartTime[indexTMinutes] = (byte) (Integer.valueOf(timeElement.getTextContent()) & 0xFF);
                                    } else if (timeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dSeconds)) {
                                        dayTariffStartTime[indexTSeconds] = (byte) (Integer.valueOf(timeElement.getTextContent()) & 0xFF);
                                    }
                                }
                                dpa.setStartTime(OctetString.fromByteArray(dayTariffStartTime));

                            } else if (schedule.getNodeName().equalsIgnoreCase(CodeTableXml.dayTariffId)) {
                                logger.debug("DayScheduleScriptSelector : " + schedule.getTextContent());
                                int value = Integer.parseInt(schedule.getTextContent());
                                if (value != 0) {
                                    dpa.setScriptSelector(new Unsigned16(value));
                                } else {
                                    throw new IOException("ActivityCalendar parser -> Invalid " + CodeTableXml.dayTariffId + " encountered. O is not allowed as " + CodeTableXml.dayTariffId + ". Please correct this first.");
                                }
                            }
                        }
                        dpa.setScriptLogicalName(OctetString.fromObisCode("0.0.10.0.100.255"));
                        dpsArray.addDataType(dpa);
                    }
                    dp.setDayProfileActions(dpsArray);
                }
            }
            dayArray.addDataType(dp);
        }

    }

    /**
     * Construct a temporary Map for the specialDayEntries;
     *
     * @param specialDayEntryDates
     */
    protected void createTempSortedDateList(NodeList specialDayEntryDates) {
        Calendar cal = Calendar.getInstance();

        Node entryDate;
        for (int i = 0; i < specialDayEntryDates.getLength(); i++) {
            entryDate = specialDayEntryDates.item(i);
            byte[] sdDate = initialSpecialDayDateArray.clone();

            cal.setTimeInMillis(0);
            for (int j = 0; j < entryDate.getChildNodes().getLength(); j++) {
                Node sdTimeElement = entryDate.getChildNodes().item(j);
                if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dYear)) {
                    sdDate[indexDYearLow] = (byte) (Integer.valueOf(sdTimeElement.getTextContent()) & 0x00FF);
                    sdDate[indexDYearHigh] = (byte) ((Integer.valueOf(sdTimeElement.getTextContent()) & 0xFF00) >> 8);
                    if (!sdTimeElement.getTextContent().equalsIgnoreCase("-1")) {
                        cal.set(Calendar.YEAR, Integer.valueOf(sdTimeElement.getTextContent()));
                    }
                } else if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dMonth)) {
                    sdDate[indexDMonth] = (byte) (Integer.valueOf(sdTimeElement.getTextContent()) & 0xFF);
                    cal.set(Calendar.MONTH, Integer.valueOf(sdTimeElement.getTextContent()) - 1);
                } else if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dDay)) {
                    sdDate[indexDDayOfMonth] = (byte) (Integer.valueOf(sdTimeElement.getTextContent()) & 0xFF);
                    cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(sdTimeElement.getTextContent()));
                }
            }
            tempSpecialDayMap.put(cal.getTimeInMillis(), entryDate);
        }
    }

    protected void sortSpecialDays() throws IOException {
        SortedSet<Long> sortedSet = new TreeSet<Long>(tempSpecialDayMap.keySet());
        Iterator<Long> it = sortedSet.iterator();
        while (it.hasNext()) {
            sortedSpecialDayNodes.add(tempSpecialDayMap.get(it.next()));
        }
    }

    /**
     * @param specialDayList
     * @throws java.io.IOException
     */
    private void createSpecialDays(NodeList specialDayList) throws IOException {
        this.specialDayArray = new Array(sortedSpecialDayNodes.size());
        int index = -1;
        Node specialDayProfile;
        for (int i = 0; i < specialDayList.getLength(); i++) {
            specialDayProfile = specialDayList.item(i);
            for (int j = 0; j < specialDayProfile.getChildNodes().getLength(); j++) {
                Structure sds = new Structure();
                Node specialDayNode = specialDayProfile.getChildNodes().item(j);
                for (int z = 0; z < specialDayNode.getChildNodes().getLength(); z++) {
                    Node specialDayEntry = specialDayNode.getChildNodes().item(z);

                    if (specialDayEntry.getNodeName().equalsIgnoreCase(CodeTableXml.specialDayEntryDate)) {
                        index = sortedSpecialDayNodes.indexOf(specialDayEntry);
                        logger.debug("SpecialDayEntryIndex : " + index);
                        sds.addDataType(new Unsigned16(index));
                        byte[] sdDate = initialSpecialDayDateArray.clone();
                        for (int k = 0; k < specialDayEntry.getChildNodes().getLength(); k++) {
                            Node sdTimeElement = specialDayEntry.getChildNodes().item(k);
                            if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dYear)) {
                                sdDate[indexDYearLow] = (byte) (Integer.valueOf(sdTimeElement.getTextContent()) & 0x00FF);
                                sdDate[indexDYearHigh] = (byte) ((Integer.valueOf(sdTimeElement.getTextContent()) & 0xFF00) >> 8);
                            } else if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dMonth)) {
                                sdDate[indexDMonth] = (byte) (Integer.valueOf(sdTimeElement.getTextContent()) & 0xFF);
                            } else if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dDay)) {
                                sdDate[indexDDayOfMonth] = (byte) (Integer.valueOf(sdTimeElement.getTextContent()) & 0xFF);
                            }
                        }
                        logger.debug("SpecialDayEntryDate : " + specialDayEntry.getTextContent());
                        sds.addDataType(OctetString.fromByteArray(sdDate));
                    } else if (specialDayEntry.getNodeName().equalsIgnoreCase(CodeTableXml.specialDayEntryDayId)) {
                        logger.debug("SpecialDayEntryDayId : " + specialDayEntry.getTextContent());
                        sds.addDataType(new Unsigned8(this.tempShiftedDayIdMap.get(specialDayEntry.getTextContent())));
                    }
                }
                specialDayArray.setDataType(index, sds);
            }
        }
    }

    /**
     * Construct the SeasonProfileName according to the additional requirements of Andrea.
     * The SeasonName should be an OctetString encoded as follow:
     * 0x09, 0x01, 0x0Z -> where Z = 0, 1, ..., 9
     *
     * @param seasonIndex the index of the season
     * @return the SeasonName.
     */
    protected OctetString createSeasonName(String seasonIndex) throws IOException {
        return createByteName(seasonIndex, 0);
    }

    /**
     * Construct the WeekProfileName according to the additional requirements of Andrea.
     * The WeekProfileName should be an OctetString encoded as follow:
     * 0x09, 0x01, 0x0Z -> where Z = 0, 1, ..., 3
     *
     * @param weekIndex the index of the weekProfile
     * @return the WeekName
     */
    private OctetString createWeekName(String weekIndex) throws IOException {
        return createByteName(weekIndex, 1);
    }

    /**
     * Create an OctetString from an index. The value of the OctetString should be the byteValue of the given String.
     *
     * @param index the Index to convert to OctetString
     * @param type  the type (0 for seasonName; 1 for weekProfileName)
     * @return the constructed OctetString
     * @throws java.io.IOException if the size of the index is not equal to 1
     */
    protected OctetString createByteName(String index, int type) throws IOException {
        byte[] content = new byte[index.length()];
        if (content.length == 1) {
            content[0] = Integer.valueOf(index, 16).byteValue();
        } else {
            logger.warn(errors[type]);
            throw new IOException(errors[type]);
        }
        return OctetString.fromByteArray(content);
    }

    /**
     * Get the specific attribute from the XML String
     *
     * @param msgStr    the Original content
     * @param attribute the xml attribute from which you want the content
     * @return the content from the attribute or an empty string if some error occurred
     */
    protected String getAttributeValue(String msgStr, String attribute) {
        try {
            int startIndex = msgStr.indexOf("\"", msgStr.indexOf(attribute)) + 1;
            return msgStr.substring(startIndex, msgStr.indexOf("\"", startIndex));
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * Get a specific content from an XML String
     *
     * @param msgStr the Original content
     * @param tag    the xml tag from which you want the content
     * @return the content between the given tag
     */
    protected String getContentValue(String msgStr, String tag) {
        try {
            return msgStr.substring(msgStr.indexOf(">", msgStr.indexOf(tag)) + 1, msgStr.indexOf("</" + tag));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get the content form the XML string, including the given tag
     *
     * @param msgStr the Original content
     * @param tag    the xml tag from which you want the content
     * @return the content including the given tags
     */
    protected String getImplicitContentValue(String msgStr, String tag) {
        try {
            int startIndex = msgStr.indexOf(tag) - 1;
            return msgStr.substring(startIndex, msgStr.indexOf("</" + tag) + 2 + tag.length() + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Convert a given epoch timestamp in MILLISECONDS to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time - the time in milliSeconds sins 1th jan 1970 00:00:00 GMT
     * @return the AXDRDateTime of the given time
     * @throws java.io.IOException when the entered time could not be parsed to a long value
     */
    public AXDRDateTime convertUnixToDeviceTimeZoneDateTime(Long time) throws IOException {
        AXDRDateTime dateTime = null;
        Calendar cal = Calendar.getInstance(protocol.getTimeZone());
        cal.setTimeInMillis(time);
        dateTime = new AXDRDateTime(cal);
        return dateTime;
    }
}