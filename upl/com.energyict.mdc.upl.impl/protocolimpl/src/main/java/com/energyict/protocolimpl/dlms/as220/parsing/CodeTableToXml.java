package com.energyict.protocolimpl.dlms.as220.parsing;

import com.energyict.cbo.ApplicationException;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.core.*;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Parser object to convert the structure of a CodeTable to an XML format
 * <p/>
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 5-okt-2010
 * Time: 11:24:20
 */
public class CodeTableToXml {

    // RTUMessageTag
    public static final String rootTOUMessage = AS220Messaging.ACTIVITY_CALENDAR;
    public static final String rootActCalendarName = AS220Messaging.CALENDAR_NAME;
    public static final String rootPassiveCalendarActivationTime = AS220Messaging.ACTIVATION_DATE;

    // XML Tags
    public static final String rootActCodeTable = "CodeTableActCalendar";
    public static final String rootSpDCodeTable = "CodeTableSpecialDay";

    // SeasonTags
    public static final String seasonProfiles = "SeasonProfiles";
    public static final String seasonProfile = "SeasonProfile";
    public static final String seasonProfileName = "SeasonProfileName";
    public static final String seasonStart = "SeasonStart";
    public static final String seasonWeekName = "SeasonWeekName";
    public static final String[] seasonProfileElements = new String[]{seasonProfileName, seasonStart, seasonWeekName};

    // WeekTags
    public static final String weekProfiles = "WeekProfiles";
    public static final String weekProfile = "WeekProfile";
    public static final String weekProfileName = "WeekProfileName";
    public static final String wkMonday = "wkMonday";
    public static final String wkTuesday = "wkTuesday";
    public static final String wkWednesDay = "wkWednesday";
    public static final String wkThursday = "wkThursday";
    public static final String wkFriday = "wkFriday";
    public static final String wkSaturday = "wkSaturday";
    public static final String wkSunday = "wkSunday";
    public static final String[] weekProfileElements = new String[]{weekProfileName, wkMonday, wkTuesday, wkWednesDay, wkThursday, wkFriday, wkSaturday, wkSunday};

    // DayTags
    public static final String dayProfiles = "DayProfiles";
    public static final String dayProfile = "DayProfile";
    public static final String dayId = "DayProfileId";
    public static final String daySchedules = "DayProfileSchedules";
    public static final String daySchedule = "DayProfileSchedule";
    public static final String daySchStartTime = "DayScheduleStartTime";
    public static final String daySchScriptLN = "DayScheduleScriptLN";
    public static final String daySchScriptSL = "DayScheduleScriptSL";
    public static final String[] dayScheduleElements = new String[]{daySchStartTime, daySchScriptLN, daySchScriptSL};

    // SpecialDayTags
    public static final String specialDayProfiles = "SpecialDayProfiles";
    public static final String specialDayProfile = "SpecialDayProfile";
    public static final String specialDayEntryIndex = "SpecialDayEntryIndex";
    public static final String specialDayEntryDate = "SpecialDayEntryDate";
    public static final String specialDayEntryDayId = "SpecialDayEntryDayId";
    public static final String[] specialDayEntryElements = new String[]{specialDayEntryIndex, specialDayEntryDate, specialDayEntryDayId};

    /**
     * The ID of the used {@link com.energyict.mdw.core.Code}
     */
    private int id;

    /**
     * The used {@link com.energyict.mdw.core.Code}
     */
    private Code codeTable;

    /**
     * Constructor with codeTable id
     */
    public CodeTableToXml() {
    }

    /**
     * Getter for the current {@link com.energyict.mdw.core.MeteringWarehouse}
     *
     * @return the current {@link com.energyict.mdw.core.MeteringWarehouse}
     */
    private static MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

    /**
     * Parse the given CodeTable to a proper xml format for the ActivityCalendar AND SpecialDayTable.
     *
     * @param id             the id of the {@link com.energyict.mdw.core.Code}
     * @param calendarName   the new <i>PASSIVE</i> calendar name
     * @param activationTime the time to activate the new calendar(epoch time) Possible values:
     *                       <ul>
     *                       <li> <code>null</code> : calendar isn't activated
     *                       <li> correct epoch time : time is written
     *                       <li> 0 : passive calendar will be switched to active calendar immediately
     *                       </ul>
     * @return the complete xml for the RTUMessage
     */
    public static String parseActivityCalendarAndSpecialDayTable(int id, String calendarName, long activationTime) throws IOException, ParserConfigurationException {
        Code codeTable = getCode(id);

        AS220ActivityCalendar acm = new AS220ActivityCalendar(codeTable, null);
        try {
            acm.parse();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(rootTOUMessage);
            root.setAttribute(rootActCalendarName, calendarName);
            root.setAttribute(rootPassiveCalendarActivationTime, String.valueOf(activationTime));

            Element rootActCalendar = document.createElement(rootActCodeTable);
            rootActCalendar.appendChild(convertSeasonArrayToXml(acm.getSeasonProfile(), document));
            rootActCalendar.appendChild(convertWeekArrayToXml(acm.getWeekProfile(), document));
            rootActCalendar.appendChild(convertDayArrayToXml(acm.getDayProfile(), document));
            root.appendChild(rootActCalendar);

            Element rootSpdCalendar = document.createElement(rootSpDCodeTable);
            rootSpdCalendar.appendChild(convertSpecialDayArrayToXml(getSpecialDayArray(codeTable.getCalendars(), acm.getTempDayProfileMap()), document));
            root.appendChild(rootSpdCalendar);

            document.appendChild(root);
            return documentToString(document);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Parse the given CodeTable to a proper xml format.
     *
     * @param id             the id of the {@link com.energyict.mdw.core.Code}
     * @param calendarName   the new <i>PASSIVE</i> calendar name
     * @param activationTime the time to activate the new calendar
     * @return the xml for the RTUMessage
     */
    public static String parseActivityCalendar(int id, String calendarName, long activationTime) throws IOException, ParserConfigurationException {
        Code codeTable = getCode(id);

        AS220ActivityCalendar acm = new AS220ActivityCalendar(codeTable, null);
        try {
            acm.parse();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(rootTOUMessage);
            root.setAttribute(rootActCalendarName, calendarName);
            root.setAttribute(rootPassiveCalendarActivationTime, String.valueOf(activationTime));

            Element rootActCalendar = document.createElement(rootActCodeTable);
            rootActCalendar.appendChild(convertSeasonArrayToXml(acm.getSeasonProfile(), document));
            rootActCalendar.appendChild(convertWeekArrayToXml(acm.getWeekProfile(), document));
            rootActCalendar.appendChild(convertDayArrayToXml(acm.getDayProfile(), document));
            root.appendChild(rootActCalendar);

            document.appendChild(root);
            return documentToString(document);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * The fabulous parse method for the ActivityCalendar
     *
     * @param id the ID of the {@link com.energyict.mdw.core.Code}
     * @return the XML for the RTUMessage
     */
    protected static String parseActivityCalendar(int id) throws ParserConfigurationException, IOException {
        Code codeTable = getCode(id);

        AS220ActivityCalendar acm = new AS220ActivityCalendar(codeTable, null);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(rootActCodeTable);

            acm.parse();

            root.appendChild(convertSeasonArrayToXml(acm.getSeasonProfile(), document));
            root.appendChild(convertWeekArrayToXml(acm.getWeekProfile(), document));
            root.appendChild(convertDayArrayToXml(acm.getDayProfile(), document));
            document.appendChild(root);
            return documentToString(document);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Convert the given (DLMS) SeasonArray to an XML format. One season contains:<br>
     * <code>
     * seasonProfile::=structure{}
     * - season_profilename :   OctetString<br>
     * - season_start      :   OctetString<br>
     * - week_name         :   OctetString<br>
     * }
     * </code>
     *
     * @param seasonArray the seasonArray from the codeTable
     * @param document    the document to create an Element for
     */
    protected static Element convertSeasonArrayToXml(Array seasonArray, Document document) {

        Element sProfiles = document.createElement(seasonProfiles);

        for (AbstractDataType season : seasonArray.getAllDataTypes()) {
            if (season.isStructure()) {
                Element sProfile = document.createElement(seasonProfile);

                // loop over the elements
                Structure seasonStruct = (Structure) season;
                for (int i = 0; i < seasonStruct.nrOfDataTypes(); i++) {
                    Element sProfileElement = document.createElement(seasonProfileElements[i]);
                    AbstractDataType content = seasonStruct.getDataType(i);
                    sProfileElement.setTextContent(ParseUtils.decimalByteToString(content.getBEREncodedByteArray()));
                    sProfile.appendChild(sProfileElement);
                }

                sProfiles.appendChild(sProfile);
            } else {
                throw new ApplicationException("CodeTableParsing -> SeasonProfiles should contain structures.");
            }
        }
        return sProfiles;
    }

    /**
     * Convert the given (DLMS) WeekArray to an XML format. One week contains:<br>
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
     *
     * @param weekArray the weekArray from the codeTable (actually from the DLMS objects)
     * @param document  the document to create an Element for
     */
    protected static Element convertWeekArrayToXml(Array weekArray, Document document) {
        Element sProfiles = document.createElement(weekProfiles);

        for (AbstractDataType week : weekArray.getAllDataTypes()) {
            if (week.isStructure()) {
                Element sProfile = document.createElement(weekProfile);

                // loop over the elements
                Structure weekStruct = (Structure) week;
                for (int i = 0; i < weekStruct.nrOfDataTypes(); i++) {
                    Element sProfileElement = document.createElement(weekProfileElements[i]);
                    AbstractDataType content = weekStruct.getDataType(i);
                    sProfileElement.setTextContent(ParseUtils.decimalByteToString(content.getBEREncodedByteArray()));
                    sProfile.appendChild(sProfileElement);
                }

                sProfiles.appendChild(sProfile);
            } else {
                throw new ApplicationException("CodeTableParsing -> WeekProfiles should contain structures.");
            }
        }
        return sProfiles;
    }

    /**
     * Convert the given (DLMS) DayArray to an XML format. One day contains:<br>
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
     * @param dayArray the dayArray from the codeTable (actually from the DLMS objects)
     * @param document the document to create an Element for
     */
    protected static Element convertDayArrayToXml(Array dayArray, Document document) {
        Element sProfiles = document.createElement(dayProfiles);

        for (AbstractDataType day : dayArray.getAllDataTypes()) {
            if (day.isStructure()) {
                Element sProfile = document.createElement(dayProfile);

                Structure dayStruct = (Structure) day;
                Element sProfileElement = document.createElement(dayId);
                sProfileElement.setTextContent(ParseUtils.decimalByteToString(dayStruct.getDataType(0).getBEREncodedByteArray()));
                sProfile.appendChild(sProfileElement);

                // loop over the elements
                Array scriptArray = (Array) dayStruct.getDataType(1);
                Element sScriptSchedule = document.createElement(daySchedules);
                for (AbstractDataType script : scriptArray.getAllDataTypes()) {
                    if (script.isStructure()) {
                        Element sScript = document.createElement(daySchedule);

                        Structure scriptStruct = (Structure) script;
                        for (int i = 0; i < scriptStruct.nrOfDataTypes(); i++) {
                            Element sProfileScriptElement = document.createElement(dayScheduleElements[i]);
                            AbstractDataType content = scriptStruct.getDataType(i);
                            sProfileScriptElement.setTextContent(ParseUtils.decimalByteToString(content.getBEREncodedByteArray()));
                            sScript.appendChild(sProfileScriptElement);
                        }
                        sScriptSchedule.appendChild(sScript);
                    } else {
                        throw new ApplicationException("CodeTableParsing -> DayProfiles-ScriptSchedules should contain structures.");
                    }
                }
                sProfile.appendChild(sScriptSchedule);

                sProfiles.appendChild(sProfile);
            } else {
                throw new ApplicationException("CodeTableParsing -> DayProfiles should contain structures.");
            }
        }
        return sProfiles;
    }

//    /**
//     * The fabulous parse method for the ActivityCalendar
//     *
//     * @param id the ID of the {@link com.energyict.mdw.core.Code}
//     * @return the XML for the RTUMessage
//     * @throws IOException                  when a logical error occurred
//     * @throws ParserConfigurationException if the XML parsing order is not correct
//     */
//    public static String parseSpecialDaysTable(int id) throws IOException, ParserConfigurationException {
//        Code codeTable = getCode(id);
//
//        Array specialDayArray = getSpecialDayArray(codeTable.getCalendars(), acm.getTempDayProfileMap());
//
//        try {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document document = builder.newDocument();
//            Element root = document.createElement(rootActCodeTable);
//
//            root.appendChild(convertSpecialDayArrayToXml(specialDayArray, document));
//            document.appendChild(root);
//            return documentToString(document);
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }

    /**
     * Convert the given (DLMS) SpecialDayArray to an XML format. One SpecialDay Structure contains:<br>
     * <code>
     * special_day_entry::=structure{}
     * - index           :   long-unsigned<br>
     * - specailday_date :   OctetString<br>
     * - day_id          :   unsigned<br>
     * }
     * </code>
     *
     * @param specialDayArray the specialDayArray from the codeTable (actually from the DLMS objects)
     * @param document        the document to create an Element for
     * @return The specialDay {@link org.w3c.dom.Element} to put in the {@link org.w3c.dom.Document}
     */
    protected static Element convertSpecialDayArrayToXml(Array specialDayArray, Document document) {
        Element sProfiles = document.createElement(specialDayProfiles);

        for (AbstractDataType spd : specialDayArray.getAllDataTypes()) {
            if (spd.isStructure()) {
                Element sProfile = document.createElement(specialDayProfile);

                // loop over the elements
                Structure spdStruct = (Structure) spd;
                for (int i = 0; i < spdStruct.nrOfDataTypes(); i++) {
                    Element sProfileElement = document.createElement(specialDayEntryElements[i]);
                    AbstractDataType content = spdStruct.getDataType(i);
                    sProfileElement.setTextContent(ParseUtils.decimalByteToString(content.getBEREncodedByteArray()));
                    sProfile.appendChild(sProfileElement);
                }

                sProfiles.appendChild(sProfile);
            } else {
                throw new ApplicationException("CodeTableParsing -> SpecialDayEntries should contain structures.");
            }
        }
        return sProfiles;
    }

    /**
     * Converts a List of {@link com.energyict.mdw.core.CodeCalendar}s to a DLMS {@link com.energyict.dlms.axrdencoding.Array} object
     *
     * @param calendars the list of {@link com.energyict.mdw.core.CodeCalendar}s from the {@link com.energyict.mdw.core.Code}
     * @param tempDayProfileMap
     * @return an {@link com.energyict.dlms.axrdencoding.Array} containing 1 or more {@link com.energyict.dlms.axrdencoding.Structure}s of SpecialDayEntries
     * @throws IOException if an invalid identifier occurred in the {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} parsing
     */
    protected static Array getSpecialDayArray(List<CodeCalendar> calendars, HashMap<CodeDayType, Integer> tempDayProfileMap) throws IOException {
        Array sdArray = new Array();

        int count = 0;

        for (int i = 0; i < calendars.size(); i++) {
            CodeCalendar cc = calendars.get(i);
            if (cc.getSeason() == 0) {
                OctetString os = new OctetString(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                        (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek())});
                Unsigned8 dayType = new Unsigned8(tempDayProfileMap.get(cc.getDayType()));
                Structure struct = new Structure();
                AXDRDateTime dt = new AXDRDateTime(new byte[]{(byte) 0x09, (byte) 0x0C, (byte) ((cc.getYear() == -1) ? 0x07 : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xB2 : (cc.getYear()) & 0xFF),
                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                        (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});

                long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24; //creates a unique ID for each specialDayEntry

                struct.addDataType(new Unsigned16(count++));
                struct.addDataType(os);
                struct.addDataType(dayType);
                sdArray.addDataType(struct);
            }
        }
        return sdArray;
    }

    /**
     * Getter for the {@link com.energyict.mdw.core.Code}
     *
     * @param id the ID of the CodeTable
     * @return the desired CodeTable
     */
    private static Code getCode(int id) {
        return mw().getCodeFactory().find(id);
    }

    /**
     * Prints the document to a {@link String}
     *
     * @param doc the {@link org.w3c.dom.Document} to converted
     * @return the XML String from the Document
     */
    public static String documentToString(Document doc) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            try {
                StreamResult result = new StreamResult(new StringWriter());
                DOMSource source = new DOMSource(doc);
                transformer.transform(source, result);
                return result.getWriter().toString();
            } catch (TransformerException e) {
                throw new ApplicationException(e);
            }
        } catch (TransformerConfigurationException e) {
            throw new ApplicationException(e);
        }
    }


}