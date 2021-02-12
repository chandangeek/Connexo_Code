package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

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
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
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
 * Sadly this is a copy paste from @see {@link com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging.ActivityCalendarController}.
 * We should not use this but rather a provider from string to bytes needed to write but we have no schema and as can be seen this relies on manual parsing of the XML...
 */
public class ActivityCalendarHelper {

    private static final String RAW_CONTENT_TAG = "Activity_Calendar";
    private static final String xmlDocType = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private static final String ACTIVATION_DATE = "ActivationDate";
    private static final String CALENDAR_NAME = "CalendarName";

    private static final String[] errors = {"ActivityCalendar did not contain a valid SEASONName.",
            "ActivityCalendar did not contain a valid WEEKPROFILEName."};

    private static final int indexDtYearHigh = 0;
    private static final int indexDtYearLow = 1;
    private static final int indexDtMonth = 2;
    private static final int indexDtDayOfMonth = 3;
    private static final byte[] initialDateTimeArray = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            0, 0, 0, 0, (byte) 0x80, 0, 0};

    // Indexes of the Time OctetString
    private static final int indexTHour = 0;
    private static final int indexTMinutes = 1;
    private static final int indexTSeconds = 2;
    private static final byte[] initialDayTariffStartTimeArray = new byte[]{0x00, 0x00, 0x00, 0x00};

    // Indexes of the Date OctetString
    private static final int indexDYearHigh = 0;
    private static final int indexDYearLow = 1;
    private static final int indexDMonth = 2;
    private static final int indexDDayOfMonth = 3;
    private static final byte[] initialSpecialDayDateArray = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    private static final ObisCode activityCalendar = ObisCode.fromString("0.0.13.0.0.255");
    private static final ObisCode specialDayTable = ObisCode.fromString("0.0.11.0.0.255");

    private final List<String> tariffIds = new ArrayList<>();

    private final AbstractDlmsProtocol protocol;

    /**
     * The {@link Array} containing the {@link SeasonProfiles}
     */
    private final Array seasonArray;
    /**
     * The {@link Array} containing the {@link WeekProfiles}
     */
    private final Array weekArray;
    /**
     * The {@link Array} containing the {@link DayProfiles}
     */
    private final Array dayArray;
    /**
     * The {@link Array} containing the {@link SpecialDaysTable}
     */
    private Array specialDayArray;
    /**
     * A temporary Map to hold the specialDayNodes
     */
    private final Map<Long, Node> tempSpecialDayMap = new HashMap<>();
    /**
     * Sorted list containing the specialDayEntryDatesNode ...
     */
    private final List<Node> sortedSpecialDayNodes = new ArrayList<>();

    /**
     * The time when to active the passive Calendar
     */
    private OctetString activatePassiveCalendarTime;

    /**
     * The name of the passive Calendar
     */
    private OctetString passiveCalendarName;

    /**
     * The current {@link Log}
     */
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Contains a map of given DayProfile Ids and usable DayProfile Ids. The ApolloMeter does not allow a dayId starting from <b>0</b>
     */
    private final Map<String, Integer> tempShiftedDayIdMap = new HashMap<>();

    public ActivityCalendarHelper(AbstractDlmsProtocol protocol, String xml) throws IOException {
        this.protocol = protocol;
        this.seasonArray = new Array();
        this.weekArray = new Array();
        this.dayArray = new Array();
        this.specialDayArray = new Array();
        this.activatePassiveCalendarTime = OctetString.fromString("");
        this.passiveCalendarName = OctetString.fromString("");
        parseContent(xml);
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
     * Write a given name to the Calendar
     *
     */
    public void writeCalendarNamePassive() throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeCalendarNamePassive(this.passiveCalendarName);
    }

    /**
     * Getter for the {@link #seasonArray}
     *
     * @return the current {@link #seasonArray}
     */
    private Array getSeasonArray() {
        return seasonArray;
    }

    /**
     * Getter for the {@link #weekArray}
     *
     * @return the current {@link #weekArray}
     */
    private Array getWeekArray() {
        return weekArray;
    }

    /**
     * Getter for the {@link #dayArray}
     *
     * @return the current {@link #dayArray}
     */
    private Array getDayArray() {
        return dayArray;
    }

    private Array getSpecialDayArray() {
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
     * @throws IOException if a parsing exception occurred
     */
    private void parseContent(String xmlContent) throws IOException {

        final String openingTag = "<" + RAW_CONTENT_TAG + ">";
        final String closingTag = "</" + RAW_CONTENT_TAG + ">";
        String compressedBase64Content = getCompressedBase64Content(xmlContent, openingTag, closingTag);
        String content = openingTag + ProtocolTools.decompress(compressedBase64Content) + closingTag;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlDocType.concat(content))));

            NodeList actDateList = doc.getElementsByTagName(ACTIVATION_DATE);

            long activationDate = Long.parseLong(actDateList.item(0).getTextContent());
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
     * Construct a map of shifted dayProfile ID's. We are reusing the {@link CodeTableXml}, but this results
     * in dayIds with values of 0, which the device does not allow ...
     * This method will most likely result in a shift of all the dayProfileId's (all ID's +1)
     *
     * @param dayProfileList the given list of dayProfiles
     */
    private void createShiftedDayIdMap(final NodeList dayProfileList) {
        Node dayProfile;
        int dayIdCounter = 0;
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
    private byte[] constructEightByteCalendarName(String name) {
        byte[] calName = new byte[]{0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        System.arraycopy(name.getBytes(), 0, calName, 0, (name.getBytes().length > 8) ? 8 : name.getBytes().length);
        return calName;
    }

    /**
     * Getter for the LOCAL {@link ActivityCalendar}
     *
     * @return the current local {@link ActivityCalendar}
     */
    private ActivityCalendar getActivityCalendar() throws IOException {
        return this.protocol.getDlmsSession().getCosemObjectFactory().getActivityCalendar(activityCalendar);
    }

    private SpecialDaysTable getSpecialDayTable() throws IOException {
        return this.protocol.getDlmsSession().getCosemObjectFactory().getSpecialDaysTable(specialDayTable);
    }

    /**
     * Create the Season{@link Array}. The season Array to write to the meter should contain :<br>
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
     * @throws IOException
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
                    logger.debug("Entering SeasonStart Element");
                    for (int k = 0; k < seasonNode.getChildNodes().getLength(); k++) {
                        Node startTimeElement = seasonNode.getChildNodes().item(k);
                        if (startTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dYear)) {
                            logger.debug("SeasonStartYear : " + startTimeElement.getTextContent());
                            startTime[indexDtYearLow] = (byte) (2012 & 0x00FF);
                            startTime[indexDtYearHigh] = (byte) ((2012 & 0xFF00) >> 8);
                        } else if (startTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dMonth)) {
                            logger.debug("SeasonStartMonth : " + startTimeElement.getTextContent());
                            startTime[indexDtMonth] = (byte) (1 & 0xFF);
                        } else if (startTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dDay)) {
                            logger.debug("SeasonStartDay : " + startTimeElement.getTextContent());
                            startTime[indexDtDayOfMonth] = (byte) (1 & 0xFF);
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
        addPaddingSeasonProfiles(seasonProfileList.getLength());
    }

    /**
     * Create the Week{@link Array}. The week Array to write to the meter should contain :<br>
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
     * @throws IOException
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
        addPaddingWeeProfiles(weekProfileList.getLength());
    }

    /**
     * Create the Day{@link Array}. The day Array to write to the meter should contain :<br>
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
     * @throws IOException
     */
    private void createDayProfiles(NodeList dayProfileList) throws IOException {
        Node dayProfile;
        for (int i = 0; i < dayProfileList.getLength(); i++) {
            dayProfile = dayProfileList.item(i);
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
                                Integer value = Integer.valueOf(schedule.getTextContent());
                                if (value != 0) {
                                    value -= 1; // Tariff IDs should be transmitted 0-based.
                                    dpa.setScriptSelector(new Unsigned16(value));
                                    if (!tariffIds.contains(Integer.toString(value))) {
                                        tariffIds.add(Integer.toString(value));
                                    }
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
    private void createTempSortedDateList(NodeList specialDayEntryDates) {
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

    private void sortSpecialDays() throws IOException {
        SortedSet<Long> sortedSet = new TreeSet<Long>(tempSpecialDayMap.keySet());
        Iterator<Long> it = sortedSet.iterator();
        while (it.hasNext()) {
            sortedSpecialDayNodes.add(tempSpecialDayMap.get(it.next()));
        }
    }

    /**
     * @param specialDayList
     * @throws IOException
     */
    private void createSpecialDays(NodeList specialDayList) throws IOException {
        this.specialDayArray = new Array(100);
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
                        sds.addDataType(new Unsigned16(0)); // Index is not used (always set to zero)
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
        addPaddingSpecialDays(sortedSpecialDayNodes.size());
    }

    /**
     * Construct the SeasonProfileName according to the additional requirements of Andrea.
     * The SeasonName should be an OctetString encoded as follow:
     * 0x09, 0x01, 0x0Z -> where Z = 0, 1, ..., 9
     *
     * @param seasonIndex the index of the season
     * @return the SeasonName.
     */
    private OctetString createSeasonName(String seasonIndex) throws IOException {
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
     * @throws IOException if the size of the index is not equal to 1
     */
    private OctetString createByteName(String index, int type) throws IOException {
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
     * The Season_profile_passive entry is an array of fixed size 12
     * The entries not used must be filled up with a dummy season entries
     * @param usedSeasons
     */
    private void addPaddingSeasonProfiles(int usedSeasons) {
        int index = usedSeasons;
        for (int i = usedSeasons; i < 12; i++) {
            Structure sds = new Structure();
            byte[] indexBytes = {(byte) index++};
            OctetString indexOctet = OctetString.fromByteArray(indexBytes);
            sds.addDataType(indexOctet);
            byte[] dummyUTC = {(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF};
            sds.addDataType(new OctetString(dummyUTC));
            sds.addDataType(indexOctet);

            seasonArray.addDataType(sds);
        }
    }

    /**
     * The Week_profile_passive entry is an array of fixed size 12
     * The entries not used must be filled up with a dummy week entries
     * @param usedWeeks
     */
    private void addPaddingWeeProfiles(int usedWeeks) {
        int index = usedWeeks;
        for (int i = usedWeeks; i < 12; i++) {
            Structure sds = new Structure();
            byte[] indexBytes = {(byte) index++};
            OctetString indexOctet = OctetString.fromByteArray(indexBytes);
            sds.addDataType(indexOctet);
            for (int y = 0; y < 7; y++) {
                Unsigned8 dummyDayProfileName = new Unsigned8(0xFF);
                sds.addDataType(dummyDayProfileName);
            }
            weekArray.addDataType(sds);
        }
    }

     /**
     * The SpecialDaysParameters entry is an array of fixed size 100
     * The entries not used must be filled up with a dummy special day entry
     * @param usedDays
     */
    private void addPaddingSpecialDays(int usedDays) {
        int index = usedDays;
        for (int i = usedDays; i < 100; i++) {
            Structure sds = new Structure();
            sds.addDataType(new Unsigned16(0)); // Index is not used (always set to zero)
            byte[] dummyDate = {0x13, 0x5C, 0x01, 0x01, (byte) 0xFF};
            sds.addDataType(new OctetString(dummyDate));
            sds.addDataType(new Unsigned8(0xFF));

            specialDayArray.setDataType(index++, sds);
        }
    }

    /**
     * Convert a given epoch timestamp in MILLISECONDS to an {@link AXDRDateTime} object
     *
     * @param time - the time in milliSeconds sins 1th jan 1970 00:00:00 GMT
     * @return the AXDRDateTime of the given time
     * @throws IOException when the entered time could not be parsed to a long value
     */
    private AXDRDateTime convertUnixToDeviceTimeZoneDateTime(Long time) throws IOException {
        AXDRDateTime dateTime = null;
        Calendar cal = Calendar.getInstance(protocol.getTimeZone());
        cal.setTimeInMillis(time);
        dateTime = new AXDRDateTime(cal);
        return dateTime;
    }

}