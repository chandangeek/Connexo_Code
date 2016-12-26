package com.energyict.protocolimpl.messages.codetableparsing;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 21/12/11
 * Time: 10:30
 */

import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalender;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Parser object to convert the structure of a CodeTable to an XML format.
 * <p/>
 * Copyrights EnergyICT
 */
public class CodeTableXml extends CodeTableXmlParsing {

    public CodeTableXml(TariffCalendarFinder finder) {
        super(finder);
    }

    /**
     * Parse the given CodeTable to a proper xml format for the ActivityCalendar AND SpecialDayTable.
     *
     * @param id             the id of the {@link com.energyict.mdw.core.Code}
     * @param activationTime the time to activate the new calendar(epoch time) Possible values:
     *                       <ul>
     *                       <li> <code>0</code> : calendar isn't activated
     *                       <li> <code>1</code> : passive calendar will be switched to active calendar immediately
     *                       <li> correct epoch time : time is written
     *                       </ul>
     * @return the complete xml for the RTUMessage
     * @throws javax.xml.parsers.ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     */
    public String parseActivityCalendarAndSpecialDayTable(String id, long activationTime) throws ParserConfigurationException {
        TariffCalender calendar = getCalendar(id).orElseThrow(() -> new IllegalArgumentException("Tariff calendar with id " + id + " not found!"));

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
            root.appendChild(createSingleElement(document, rootActCalendarName, calendar.getName()));
            root.appendChild(createSingleElement(document, codeTableDefinitionTimeZone, calendar.getDefinitionTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableDestinationTimeZone, calendar.getDestinationTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableInterval, Integer.toString(calendar.getIntervalInSeconds())));
            root.appendChild(createSingleElement(document, codeTableFromYear, Integer.toString(calendar.getYearFrom())));
            root.appendChild(createSingleElement(document, codeTableToYear, Integer.toString(calendar.getYearTo())));
            root.appendChild(createSingleElement(document, codeTableSeasonSetId, Integer.toString(calendar.getSeasonSetId())));
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
