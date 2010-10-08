package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributeobjects.DayProfileActions;
import com.energyict.dlms.cosem.attributeobjects.DayProfiles;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.dlms.cosem.attributeobjects.WeekProfiles;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.parsing.CodeTableToXml;
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
import java.util.Calendar;
import java.util.TimeZone;

/**
 * ActivityCalendar implementation for the AS220 Devices
 * <p/>
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 6-okt-2010
 * Time: 16:45:12
 * To change this template use File | Settings | File Templates.
 */
public class AS220ActivityCalendarController implements ActivityCalendarController {

    private final AS220 as220;

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
    private Array specialDayArray;

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

    public AS220ActivityCalendarController(AS220 as220) {
        this.as220 = as220;
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

    protected Array getSpecialDayArray(){
        return specialDayArray;
    }

    /**
     * Parse the given content to a proper activityCalendar
     *
     * @param content the activityCalendar content
     * @throws IOException if a parsing exception occurred
     */
    public void parseContent(String content) throws IOException {
        String activationDate =       getAttributeValue(content, AS220Messaging.ACTIVATION_DATE);
        if("0".equalsIgnoreCase(activationDate) || "1".equalsIgnoreCase(activationDate)){
            activatePassiveCalendarTime = OctetString.fromString(activationDate);
        } else {
            activatePassiveCalendarTime = new OctetString(convertUnixToGMTDateTime(activationDate).getBEREncodedByteArray(),0);
        }
        passiveCalendarName = OctetString.fromString(getAttributeValue(content, AS220Messaging.CALENDAR_NAME));


        String actCodeTableContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + getImplicitContentValue(content, CodeTableToXml.rootActCodeTable);
        String spdCodeTableContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + getImplicitContentValue(content, CodeTableToXml.rootSpDCodeTable);


        try {
            // Create a DOM builder and parse the fragment
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(actCodeTableContent)));

            NodeList seasonProfileList = doc.getElementsByTagName(CodeTableToXml.seasonProfile);
            createSeasonProfiles(seasonProfileList);

            NodeList weekProfileList = doc.getElementsByTagName(CodeTableToXml.weekProfile);
            createWeekProfiles(weekProfileList);

            NodeList dayProfileList = doc.getElementsByTagName(CodeTableToXml.dayProfile);
            createDayProfiles(dayProfileList);

            doc = builder.parse(new InputSource(new StringReader(spdCodeTableContent)));

            NodeList specialDayList = doc.getElementsByTagName(CodeTableToXml.specialDayProfile);
            createSpecialDays(specialDayList);

            logger.debug(ParseUtils.decimalByteToString(seasonArray.getBEREncodedByteArray()));
            logger.debug(ParseUtils.decimalByteToString(weekArray.getBEREncodedByteArray()));
            logger.debug(ParseUtils.decimalByteToString(dayArray.getBEREncodedByteArray()));
            logger.debug(ParseUtils.decimalByteToString(specialDayArray.getBEREncodedByteArray()));

        } catch (ParserConfigurationException e) {
            logger.error("ActivityCalendar parser -> Could not create a DocumentBuilder.");
            throw new IOException("ActivityCalendar parser -> Could not create a DocumentBuilder. ParseConfigurationException message : " + e.getLocalizedMessage());
        } catch (SAXException e) {
            logger.error("ActivityCalendar parser -> A parse ERROR occurred.");
            throw new IOException("ActivityCalendar parser -> A parse ERROR occurred. SAXException message : " + e.getLocalizedMessage());
        }
    }

    /**
     * Write a given name to the Calendar
     *
     * @param name the name of the ActivityCalendar
     */
    public void writeCalendarName(String name) {
        // Currently not used
    }

    /**
     * Write the complete ActivityCalendar to the device
     */
    public void writeCalendar() throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeSeasonProfilePassive(getSeasonArray());
        ac.writeWeekProfileTablePassive(getWeekArray());
        ac.writeDayProfileTablePassive(getDayArray());


        //TODO uncomment this to write the given CalendarName
//        if(!"".equalsIgnoreCase(this.passiveCalendarName.stringValue())){
//            ac.writeCalendarNamePassive(this.passiveCalendarName);
//        } else {
//            logger.debug("No PassiveCalendarName will be written.");
//        }

        if("1".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())){
            // TODO do an immediate activation
            ac.activateNow();
        } else if("0".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())){
            ac.writeActivatePassiveCalendarTime(this.activatePassiveCalendarTime);
        } else {
            logger.trace("No passiveCalendar activation date was given.");
        }

        for(AbstractDataType specialDay : getSpecialDayArray().getAllDataTypes()){
            int retry = 0;
            while(retry  < 3){
                try {
                    getSpecialDayTable().insert((Structure) specialDay);
                    break;
                } catch (DataAccessResultException e) {
                    if(retry == 3){
                        throw e;
                    } else if(e.getDataAccessResult() == 2){ // Temporary Failure
                        logger.trace("Received failure, will retry");
                        retry++;
                    }
                }
            }
        }

//        getSpecialDayTable().writeSpecialDays(getSpecialDayArray());
    }

    /**
     * Write the SpecialDays table to the device
     */
    public void writeSpecialDaysTable() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Write a time from which the new ActivityCalendar should be active
     *
     * @param activationDate the given time
     */
    public void writeCalendarActivationTime(Calendar activationDate) {
        // Currently not used
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
    private ActivityCalendar getActivityCalendar() throws IOException {
        return this.as220.getCosemObjectFactory().getActivityCalendar(this.as220.getMeterConfig().getActivityCalendar().getObisCode());
    }


    private SpecialDaysTable getSpecialDayTable() throws IOException {
        return this.as220.getCosemObjectFactory().getSpecialDaysTable(this.as220.getMeterConfig().getSpecialDaysTable().getObisCode());
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
     * @throws IOException
     */
    private void createSeasonProfiles(NodeList seasonProfileList) throws IOException {
        Node seasonProfile;
        for (int i = 0; i < seasonProfileList.getLength(); i++) {
            seasonProfile = seasonProfileList.item(i);
            SeasonProfiles sp = new SeasonProfiles();
            for (int j = 0; j < seasonProfile.getChildNodes().getLength(); j++) {
                Node seasonNode = seasonProfile.getChildNodes().item(j);
                if (seasonNode.getNodeName().equalsIgnoreCase(CodeTableToXml.seasonProfileName)) {
                    System.out.println("SeasonProfileName : " + seasonNode.getTextContent());
                    sp.setSeasonProfileName(new OctetString(ParseUtils.hexStringToByteArray(seasonNode.getTextContent()), 0));
                } else if (seasonNode.getNodeName().equalsIgnoreCase(CodeTableToXml.seasonStart)) {
                    System.out.println("SeasonStart : " + seasonNode.getTextContent());
                    sp.setSeasonStart(new OctetString(ParseUtils.hexStringToByteArray(seasonNode.getTextContent()), 0));
                } else if (seasonNode.getNodeName().equalsIgnoreCase(CodeTableToXml.seasonWeekName)) {
                    System.out.println("SeasonWeekName : " + seasonNode.getTextContent());
                    sp.setWeekName(new OctetString(ParseUtils.hexStringToByteArray(seasonNode.getTextContent()), 0));
                }
            }
            sp.addDataType(new Unsigned16(i));
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
     * @throws IOException
     */
    private void createWeekProfiles(NodeList weekProfileList) throws IOException {
        Node weekProfile;
        for (int i = 0; i < weekProfileList.getLength(); i++) {
            weekProfile = weekProfileList.item(i);
            WeekProfiles wp = new WeekProfiles();
            for (int j = 0; j < weekProfile.getChildNodes().getLength(); j++) {
                Node weekNode = weekProfile.getChildNodes().item(j);
                if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.weekProfileName)) {
                    System.out.println("WeekProfileName : " + weekNode.getTextContent());
                    wp.setWeekProfileName(new OctetString(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkMonday)) {
                    System.out.println("Monday : " + weekNode.getTextContent());
                    wp.setMonday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkTuesday)) {
                    System.out.println("Tuesday : " + weekNode.getTextContent());
                    wp.setTuesday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkWednesDay)) {
                    System.out.println("WednesDay : " + weekNode.getTextContent());
                    wp.setWednesday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkThursday)) {
                    System.out.println("Thursday : " + weekNode.getTextContent());
                    wp.setThursday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkFriday)) {
                    System.out.println("Friday : " + weekNode.getTextContent());
                    wp.setFriday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkSaturday)) {
                    System.out.println("Saturday : " + weekNode.getTextContent());
                    wp.setSaturday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                } else if (weekNode.getNodeName().equalsIgnoreCase(CodeTableToXml.wkSunday)) {
                    System.out.println("Sunday : " + weekNode.getTextContent());
                    wp.setSunday(new Unsigned8(ParseUtils.hexStringToByteArray(weekNode.getTextContent()), 0));
                }
            }
            wp.addDataType(new Unsigned16(i));
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
     * @throws IOException
     */
    private void createDayProfiles(NodeList dayProfileList) throws IOException {
        Node dayProfile;
        for (int i = 0; i < dayProfileList.getLength(); i++) {
            dayProfile = dayProfileList.item(i);
            Structure dp = new Structure();
//            DayProfiles dp = new DayProfiles();
            for (int j = 0; j < dayProfile.getChildNodes().getLength(); j++) {
                if (dayProfile.getChildNodes().item(j).getNodeName().equalsIgnoreCase(CodeTableToXml.dayId)) {
                    System.out.println("DayId : " + dayProfile.getChildNodes().item(j).getTextContent());
//                    dp.setDayId(new Unsigned8(DLMSUtils.hexStringToByteArray(dayProfile.getChildNodes().item(j).getTextContent()), 0));
                    // This is an odd thing to do, but we do it because the AM500 has a WRONG dayProfile ID dataType
                    dp.addDataType(new Unsigned16(new Unsigned8(DLMSUtils.hexStringToByteArray(dayProfile.getChildNodes().item(j).getTextContent()), 0).getValue()));
                } else if (dayProfile.getChildNodes().item(j).getNodeName().equalsIgnoreCase(CodeTableToXml.daySchedules)) {
                    Node dayProfileSchedules = dayProfile.getChildNodes().item(j);
                    System.out.println("NrOfDaySchedules : " + dayProfileSchedules.getChildNodes().getLength());
                    Array dpsArray = new Array();
                    for (int k = 0; k < dayProfileSchedules.getChildNodes().getLength(); k++) {
                        Node dayProfileSchedule = dayProfileSchedules.getChildNodes().item(k);
                        System.out.println("DayScheduleItems : " + dayProfileSchedule.getChildNodes().getLength());
                        DayProfileActions dpa = new DayProfileActions();
                        for (int l = 0; l < dayProfileSchedule.getChildNodes().getLength(); l++) {
                            Node schedule = dayProfileSchedule.getChildNodes().item(l);
                            if (schedule.getNodeName().equalsIgnoreCase(CodeTableToXml.daySchStartTime)) {
                                System.out.println("DayScheduleStartTime : " + schedule.getTextContent());
                                dpa.setStartTime(new OctetString(ParseUtils.hexStringToByteArray(schedule.getTextContent()), 0));
                            } else if (schedule.getNodeName().equalsIgnoreCase(CodeTableToXml.daySchScriptLN)) {
                                System.out.println("DayScheduleLogicalName : " + schedule.getTextContent());
                                dpa.setScriptLogicalName(new OctetString(ParseUtils.hexStringToByteArray(schedule.getTextContent()), 0));
                            } else if (schedule.getNodeName().equalsIgnoreCase(CodeTableToXml.daySchScriptSL)) {
                                System.out.println("DayScheduleScriptSelector : " + schedule.getTextContent());
                                dpa.setScriptSelector(new Unsigned16(DLMSUtils.hexStringToByteArray(schedule.getTextContent()), 0));
                            }
                        }
                        dpsArray.addDataType(dpa);
                    }
//                    dp.setDayProfileActions(dpsArray);
                    dp.addDataType(dpsArray);
                }
            }
            dayArray.addDataType(dp);
        }

    }


    private void createSpecialDays(NodeList specialDayList) throws IOException {
        Node specialDayProfile;
        for (int i = 0; i < specialDayList.getLength(); i++) {
            specialDayProfile = specialDayList.item(i);
            Structure sds = new Structure();
            for (int j = 0; j < specialDayProfile.getChildNodes().getLength(); j++) {
                Node specialDayNode = specialDayProfile.getChildNodes().item(j);
                if (specialDayNode.getNodeName().equalsIgnoreCase(CodeTableToXml.specialDayEntryIndex)) {
                    System.out.println("SpecialDayEntryIndex : " + specialDayNode.getTextContent());
                    sds.addDataType(new Unsigned16(ParseUtils.hexStringToByteArray(specialDayNode.getTextContent()), 0));
                } else if (specialDayNode.getNodeName().equalsIgnoreCase(CodeTableToXml.specialDayEntryDate)) {
                    System.out.println("SpecialDayEntryDate : " + specialDayNode.getTextContent());
                    sds.addDataType(new OctetString(ParseUtils.hexStringToByteArray(specialDayNode.getTextContent()), 0));
                } else if (specialDayNode.getNodeName().equalsIgnoreCase(CodeTableToXml.specialDayEntryDayId)) {
                    System.out.println("SpecialDayEntryDayId : " + specialDayNode.getTextContent());
                   sds.addDataType(new Unsigned8(ParseUtils.hexStringToByteArray(specialDayNode.getTextContent()), 0));
                }
            }
            specialDayArray.addDataType(sds);
        }
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
     * Convert a given epoch timestamp in SECONDS to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time - the time in seconds sins 1th jan 1970 00:00:00 GMT
     * @return the AXDRDateTime of the given time
     * @throws IOException when the entered time could not be parsed to a long value
     */
    public AXDRDateTime convertUnixToGMTDateTime(String time) throws IOException {
        try {
            AXDRDateTime dateTime = null;
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.setTimeInMillis(Long.parseLong(time) * 1000);
            dateTime = new AXDRDateTime(cal);
            return dateTime;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + time + " to a long value");
		}
	}
}
