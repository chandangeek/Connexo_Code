package com.energyict.protocolimpl.messages.codetableparsing;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 21/12/11
 * Time: 10:30
 */

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.protocol.api.codetables.Code;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Parser object to convert the structure of a CodeTable to an XML format
 * <p/>
 * Copyrights EnergyICT
 */
public class CodeTableXml extends CodeTableXmlParsing {

    /**
     * Parse the given CodeTable to a proper xml format for the ActivityCalendar AND SpecialDayTable.
     *
     * @param id             the id of the {@link Code}
     * @param activationTime the time to activate the new calendar(epoch time) Possible values:
     *                       <ul>
     *                       <li> <code>0</code> : calendar isn't activated
     *                       <li> <code>1</code> : passive calendar will be switched to active calendar immediately
     *                       <li> correct epoch time : time is written
     *                       </ul>
     * @return the complete xml for the RTUMessage
     * @throws javax.xml.parsers.ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     */
    public static String parseActivityCalendarAndSpecialDayTable(int id, long activationTime) throws ParserConfigurationException {
        Calendar calendar = getCalendar(id);

        CodeTableParser ctp = new CodeTableParser(calendar);
        try {
            ctp.parse();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(rootTOUMessage);

            /*
             We use the name of the codeTable as activityCalendarName
             This way we can track the calendars in the devices
              */
            root.appendChild(createSingleElement(document, rootActCalendarName, calendar.getName()));
            root.appendChild(createSingleElement(document, codeTableDefinitionTimeZone, calendar.getTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableDestinationTimeZone, calendar.getTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableInterval, Integer.toString(calendar.getIntervalInSeconds())));
            root.appendChild(createSingleElement(document, codeTableFromYear, Integer.toString(calendar.getStart().getValue())));
            root.appendChild(createSingleElement(document, codeTableToYear, Integer.toString(calendar.getEnd().getValue())));
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
}
