package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.common.ApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Parser object to convert the structure of a CodeTable to an XML format
 * <p/>
 * Copyrights EnergyICT
 */
public class CodeTableXmlParsing {

    // RTUMessageTag
    public static final String rootTOUMessage = "TimeOfUse";
    public static final String rootActCalendarName = "CalendarName";
    public static final String rootPassiveCalendarActivationTime = "ActivationDate";

    // XML Tags
    public static final String rootActCodeTable = "CodeTableActCalendar";
    public static final String rootSpDCodeTable = "CodeTableSpecialDay";
//    public static final String codeTableName = "CodeTableName";
    public static final String codeTableDefinitionTimeZone = "CodeTableTimeZone";
    public static final String codeTableDestinationTimeZone = "CodeTableDestinationTimeZone";
    public static final String codeTableInterval = "CodeTableInterval";
    public static final String codeTableFromYear = "CodeTableFromYear";
    public static final String codeTableToYear = "CodeTableToYear";
    public static final String codeTableSeasonSetId = "CodeTableSeasonSetId";

    // Date Tags
    public static final String dYear = "Year";
    public static final String dMonth = "Month";
    public static final String dDay = "Day";
    public static final String dHour = "Hour";
    public static final String dMinutes = "Minutes";
    public static final String dSeconds = "Seconds";

    // SeasonTags
    public static final String seasonProfiles = "SeasonProfiles";
    public static final String seasonProfile = "SeasonProfile";
    public static final String seasonProfileName = "SeasonProfileName";
    public static final String seasonStart = "SeasonStart";
    public static final String seasonWeekName = "SeasonWeekName";

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
    public static final String[] weekProfileElements = new String[]{wkMonday, wkTuesday, wkWednesDay, wkThursday, wkFriday, wkSaturday, wkSunday};

    // DayTags
    public static final String dayProfiles = "DayProfiles";
    public static final String dayProfile = "DayProfile";
    public static final String dayId = "DayProfileId";
    public static final String dayTariffs = "DayProfileTariffs";
    public static final String dayTariff = "DayProfileTariff";
    public static final String dayTariffId = "DayProfileTariffId";
    public static final String dayTariffStartTime = "DayTariffStartTime";

    // SpecialDayTags
    public static final String specialDays = "SpecialDays";
    public static final String specialDay = "SpecialDay";
    public static final String specialDayEntryDate = "SpecialDayEntryDate";
    public static final String specialDayEntryDayId = "SpecialDayEntryDayId";

    protected static final Log logger = LogFactory.getLog(CodeTableXmlParsing.class);

    /**
     * Parse the given {@link Calendar} to a proper xml format for the ActivityCalendar AND SpecialDayTable.
     *
     * @param calendar The Calendar
     * @param activationTime the time to activate the new calendar(epoch time) Possible values:
     *        <ul>
     *        <li> <code>0</code> : calendar isn't activated
     *        <li> <code>1</code> : passive calendar will be switched to active calendar immediately
     *        <li> correct epoch time : time is written
     *        </ul>
     * @param name The name for the ActivityCalendar
     * @return the complete xml for the RTUMessage
     * @throws javax.xml.parsers.ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     */
    public static String parseActivityCalendarAndSpecialDayTable(Calendar calendar, long activationTime, String name) throws ParserConfigurationException {
        CodeTableParser ctp = new CodeTableParser(calendar);
        try {

            ctp.parse();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(rootTOUMessage);

            /*
             We use the name of the calendar as activityCalendarName
             This way we can track the calendars in the devices
              */
            root.appendChild(createSingleElement(document, rootActCalendarName, name));
            root.appendChild(createSingleElement(document, codeTableDefinitionTimeZone, calendar.getTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableDestinationTimeZone, calendar.getTimeZone().getDisplayName()));
            /* Todo: Extract the hard code 15 min to a parameter for this method.
                     com.energyict.mdc.upl.messages.legacy.Extractor#intervalInSeconds implementation
                     may need to access the available reading types,
                     sort them as:
                     favour Electricity, favour Wh over W, favour smaller intervals over larger intervals
                     Use the interval of the best match. */
            root.appendChild(createSingleElement(document, codeTableInterval, Integer.toString(900)));
            root.appendChild(createSingleElement(document, codeTableFromYear, Integer.toString(calendar.getStartYear().getValue())));
            root.appendChild(createSingleElement(document, codeTableToYear, Integer.toString(calendar.getEndYear().getValue())));
            root.appendChild(createSingleElement(document, codeTableSeasonSetId, "0"));
            root.appendChild(createSingleElement(document, rootPassiveCalendarActivationTime, String.valueOf(activationTime)));

            Element rootActCalendar = document.createElement(rootActCodeTable);
            rootActCalendar.appendChild(convertSeasonProfileToXml(ctp.getSeasonProfiles(), document));
            rootActCalendar.appendChild(convertWeekProfileToXml(ctp.getWeekProfiles(), document));
            rootActCalendar.appendChild(convertDayProfileToXml(ctp.getDayProfiles(), document));
            root.appendChild(rootActCalendar);

            Element rootSpdCalendar = document.createElement(rootSpDCodeTable);
            rootSpdCalendar.appendChild(convertSpecialDayProfileToXml(ctp.getSpecialDaysProfile(), document));
            root.appendChild(rootSpdCalendar);

            document.appendChild(root);
            return getXmlWithoutDocType(document);
        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Convert the given SeasonProfile to an XML format. One season will look like this :<br>
     * <pre>{@code
     * <SeasonProfiles>
     * <SeasonProfile>
     * <SeasonProfileName>1</SeasonProfileName>
     * <SeasonStart>
     * <Year>-1</Year>
     * <Month>-1</Month>
     * <Day>-1</Day>
     * </SeasonStart>
     * <SeasonWeekName>1</SeasonWeekName>
     * </SeasonProfile>
     * </SeasonProfiles>
     * }</pre>
     *
     * @param constructedSeasonProfile the constructedSeasonProfile-Map from the codeTable
     * @param document                 the document to create an Element for
     */
    protected static Element convertSeasonProfileToXml(Map<Integer, SeasonStartDates> constructedSeasonProfile, Document document) {

        Element sProfiles = document.createElement(seasonProfiles);

        for (int seasonId : constructedSeasonProfile.keySet()) {
            Element sProfile = document.createElement(seasonProfile);

            // Create the SeasonProfileName element
            Element sProfileName = document.createElement(seasonProfileName);
            sProfileName.setTextContent(String.valueOf(seasonId));
            sProfile.appendChild(sProfileName);

            // Create the SeasonProfileStart element
            Element sStart = document.createElement(seasonStart);
            Element ssYear = document.createElement(dYear);
            Element ssMonth = document.createElement(dMonth);
            Element ssDay = document.createElement(dDay);
            ssYear.setTextContent(String.valueOf(constructedSeasonProfile.get(seasonId).getYear()));
            ssMonth.setTextContent(String.valueOf(constructedSeasonProfile.get(seasonId).getMonth()));
            ssDay.setTextContent(String.valueOf(constructedSeasonProfile.get(seasonId).getDay()));
            sStart.appendChild(ssYear);
            sStart.appendChild(ssMonth);
            sStart.appendChild(ssDay);
            sProfile.appendChild(sStart);

            Element spWeekName = document.createElement(seasonWeekName);
            spWeekName.setTextContent(constructedSeasonProfile.get(seasonId).getWeekName());
            sProfile.appendChild(spWeekName);

            sProfiles.appendChild(sProfile);

        }
        return sProfiles;
    }

    /**
     * Convert the given WeekProfile to an XML format. One week will look like the following:<br>
     * <pre>{@code
     * <WeekProfiles>
     * <WeekProfile>
     * <WeekProfileName>1</WeekProfileName>
     * <wkMonday>1</wkMonday>
     * <wkTuesday>1</wkTuesday>
     * <wkWednesday>1</wkWednesday>
     * <wkThursday>1</wkThursday>
     * <wkFriday>1</wkFriday>
     * <wkSaturday>2</wkSaturday>
     * <wkSunday>2</wkSunday>
     * </WeekProfile>
     * </WeekProfiles>
     * }</pre>
     *
     * @param constructedWeekProfile the constructedWeekProfile-Map from the codeTable
     * @param document               the document to create an Element for
     */
    protected static Element convertWeekProfileToXml(Map<Integer, List<WeekDayDefinitions>> constructedWeekProfile, Document document) {
        Element wProfiles = document.createElement(weekProfiles);

        for (int weekId : constructedWeekProfile.keySet()) {
            Element wProfile = document.createElement(weekProfile);

            // Create WeekProfileName element
            Element wpName = document.createElement(weekProfileName);
            wpName.setTextContent(String.valueOf(weekId));
            wProfile.appendChild(wpName);

            Element[] weekDayTypes = new Element[7];
            int anyDayTypeValue = -1;
            // Create DayOfWeekIndexes
            for (WeekDayDefinitions wdd : constructedWeekProfile.get(weekId)) {
                if (wdd.getDayOfWeek() != -1 && wdd.getDayOfWeek() - 1 < weekProfileElements.length) {
                    weekDayTypes[wdd.getDayOfWeek() - 1] = document.createElement(weekProfileElements[wdd.getDayOfWeek() - 1]);
                    weekDayTypes[wdd.getDayOfWeek() - 1].setTextContent(String.valueOf(wdd.getDayTypeId()));
                } else {
                    anyDayTypeValue = wdd.getDayTypeId();
                }
            }
            // All the dayTypes that are empty correspond with the "ANY" dayType
            for (int wdtIndex = 0; wdtIndex < weekDayTypes.length; wdtIndex++) {
                if (weekDayTypes[wdtIndex] == null) {
                    weekDayTypes[wdtIndex] = document.createElement(weekProfileElements[wdtIndex]);
                    weekDayTypes[wdtIndex].setTextContent(String.valueOf(anyDayTypeValue));
                }
                wProfile.appendChild(weekDayTypes[wdtIndex]);
            }

            wProfiles.appendChild(wProfile);
        }
        return wProfiles;
    }

    /**
     * Convert the given DayArray to an XML format. One day will look like the following:<br>
     * <pre>{@code
     * <DayProfiles>
     * <DayProfile>
     * <DayProfileId>2</DayProfileId>
     * <DayProfileTariffs>
     * <DayProfileTariff>
     * <DayProfileTariffId>0</DayProfileTariffId>
     * <DayTariffStartTime>
     * <Hour>0</Hour>
     * <Minutes>0</Minutes>
     * <Seconds>0</Seconds>
     * </DayTariffStartTime>
     * </DayProfileTariff>
     * </DayProfileTariffs>
     * </DayProfile>
     * </DayProfiles>
     * }</pre>
     *
     * @param constructedDayProfile the constructedDayProfile from the codeTable (actually from the DLMS objects)
     * @param document              the document to create an Element for
     */
    protected static Element convertDayProfileToXml(Map<Integer, List<DayTypeDefinitions>> constructedDayProfile, Document document) {
        Element dProfiles = document.createElement(dayProfiles);

        for (int dayProfileId : constructedDayProfile.keySet()) {
            Element dProfile = document.createElement(dayProfile);

            // The DayProfileId
            Element dpName = document.createElement(dayId);
            dpName.setTextContent(String.valueOf(dayProfileId));
            dProfile.appendChild(dpName);

            // The DayProfileSchedules
            Element dpSchedules = document.createElement(dayTariffs);
            for (DayTypeDefinitions dtd : constructedDayProfile.get(dayProfileId)) {
                Element dpSchedule = document.createElement(dayTariff);

                Element dpTariffCode = document.createElement(dayTariffId);
                dpTariffCode.setTextContent(String.valueOf(dtd.getTariffcode()));
                dpSchedule.appendChild(dpTariffCode);

                // The StartTime
                Element dpsStartTime = document.createElement(dayTariffStartTime);
                Element dpSTHour = document.createElement(dHour);
                Element dpSTMinutes = document.createElement(dMinutes);
                Element dpSTSeconds = document.createElement(dSeconds);
                dpSTHour.setTextContent(String.valueOf(dtd.getHour()));
                dpSTMinutes.setTextContent(String.valueOf(dtd.getMinute()));
                dpSTSeconds.setTextContent(String.valueOf(dtd.getSeconds()));

                dpsStartTime.appendChild(dpSTHour);
                dpsStartTime.appendChild(dpSTMinutes);
                dpsStartTime.appendChild(dpSTSeconds);

                dpSchedule.appendChild(dpsStartTime);

                dpSchedules.appendChild(dpSchedule);

                dProfile.appendChild(dpSchedules);
            }
            dProfiles.appendChild(dProfile);
        }

        return dProfiles;
    }

    /**
     * Convert the given (DLMS) SpecialDayArray to an XML format. One SpecialDay will look like:<br>
     * <pre>{@code
     * <SpecialDays>
     * <SpecialDay>
     * <SpecialDayEntryDate>
     * <Year>-1</Year>
     * <Month>12</Month>
     * <Day>25</Day>
     * </SpecialDayEntryDate>
     * <SpecialDayEntryDayId>0</SpecialDayEntryDayId>
     * </SpecialDay>
     * </SpecialDays>
     * }</pre>
     *
     * @param constructedSpecialDays the constructedSpecialDays from the codeTable
     * @param document               the document to create an Element for
     * @return The specialDay {@link org.w3c.dom.Element} to put in the {@link org.w3c.dom.Document}
     */
    protected static Element convertSpecialDayProfileToXml(List<SpecialDayDefinition> constructedSpecialDays, Document document) {
        Element sDays = document.createElement(specialDays);

        for (SpecialDayDefinition sdd : constructedSpecialDays) {
            Element sDay = document.createElement(specialDay);

            Element sdDate = document.createElement(specialDayEntryDate);
            Element sddYear = document.createElement(dYear);
            Element sddMonth = document.createElement(dMonth);
            Element sddDay = document.createElement(dDay);
            sddYear.setTextContent(String.valueOf(sdd.getYear()));
            sddMonth.setTextContent(String.valueOf(sdd.getMonth()));
            sddDay.setTextContent(String.valueOf(sdd.getDay()));
            sdDate.appendChild(sddYear);
            sdDate.appendChild(sddMonth);
            sdDate.appendChild(sddDay);
            sDay.appendChild(sdDate);

            Element sdDayId = document.createElement(specialDayEntryDayId);
            sdDayId.setTextContent(String.valueOf(sdd.getDayTypeId()));
            sDay.appendChild(sdDayId);

            sDays.appendChild(sDay);
        }
        return sDays;
    }

    /**
     * Getter for the {@link Calendar}
     *
     * @param id the ID of the Calendar
     * @return the Calendar
     */
    protected static Calendar getCalendar(long id) {
        // Todo: port Code to jupiter, return null as the previous code would have returned null too.
        return null;
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

    /**
     * Prints the document to a {@link String}, without the docType (this way we can put it in the OldDeviceMessage)
     *
     * @param doc the {@link org.w3c.dom.Document} to converted
     * @return the XML String from the Document
     */
    public static String getXmlWithoutDocType(Document doc) {
        String codeTableXml = documentToString(doc);
        int index = codeTableXml.indexOf("?>");
        return (index != -1) ? codeTableXml.substring(index + 2) : codeTableXml;
    }

    /**
     * Prints an {@link org.w3c.dom.Element} to a xml format
     *
     * @param domElement the element for print out
     * @return the xml formatted element as a readable string
     */
    public static String domElementToString(Element domElement) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(domElement), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Create a single Element for the given Document
     *
     * @param document the document for the element
     * @param name     the name of the Element
     * @param value    the textvalue of the element
     * @return the newly created element
     */
    public static Element createSingleElement(Document document, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value);
        return element;
    }

}