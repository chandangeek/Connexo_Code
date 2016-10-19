package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Parser object to convert the structure of a CodeTable to an XML format
 * <p/>
 * Copyrights EnergyICT
 * User: sva
 * Date: 21/12/11
 * Time: 10:30
 */
public class CodeTableXml extends CodeTableXmlParsing {

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
     * @return the complete xml for the RTUMessage
     * @throws javax.xml.parsers.ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     */
    public static String parseActivityCalendarAndSpecialDayTable(Calendar calendar, long activationTime) throws ParserConfigurationException {
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
            root.appendChild(createSingleElement(document, codeTableDefinitionTimeZone, ZoneId.systemDefault().getDisplayName(TextStyle.FULL, Locale
                    .getDefault())));
            root.appendChild(createSingleElement(document, codeTableDestinationTimeZone, ZoneId.systemDefault().getDisplayName(TextStyle.FULL, Locale.getDefault())));
            /* Todo: Extract the hard code 15 min to a parameter for this method.
                     Calling context, i.e. the protocol or a protocol adapater
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
}
