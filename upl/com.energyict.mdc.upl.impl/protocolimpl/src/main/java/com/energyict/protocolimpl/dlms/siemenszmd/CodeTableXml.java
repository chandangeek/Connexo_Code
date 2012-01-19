package com.energyict.protocolimpl.dlms.siemenszmd;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 11/01/12
 * Time: 15:30
 **/

import com.energyict.mdw.core.Code;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableParser;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.*;

/**
 * Parser object to convert the structure of a CodeTable to an XML format
 * <p/>
 * Copyrights EnergyICT
 */
public class CodeTableXml extends CodeTableXmlParsing {

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
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     */
    public static String parseActivityCalendarAndSpecialDayTable(int id, long activationTime) throws ParserConfigurationException {
        Code codeTable = getCode(id);

        CodeTableParser ctp = new CodeTableParser(codeTable);
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
            root.appendChild(createSingleElement(document, rootActCalendarName, codeTable.getName()));
            root.appendChild(createSingleElement(document, codeTableDefinitionTimeZone, codeTable.getDefinitionTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableDestinationTimeZone, codeTable.getDestinationTimeZone().getDisplayName()));
            root.appendChild(createSingleElement(document, codeTableInterval, Integer.toString(codeTable.getIntervalInSeconds())));
            root.appendChild(createSingleElement(document, codeTableFromYear, Integer.toString(codeTable.getYearFrom())));
            root.appendChild(createSingleElement(document, codeTableToYear, Integer.toString(codeTable.getYearTo())));
            root.appendChild(createSingleElement(document, codeTableSeasonSetId, Integer.toString(codeTable.getSeasonSetId())));
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