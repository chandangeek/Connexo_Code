package com.energyict.protocolimpl.dlms.as220.parsing;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocolimpl.utils.Utilities;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 5-okt-2010
 * Time: 11:43:24
 * To change this template use File | Settings | File Templates.
 */
public class CodeTableToXmlTest {

    /**
     * This test uses a CodeTable from my database, Teamcity can't access that one
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testParse() throws Exception {
        Utilities.createEnvironment();
        MeteringWarehouse.createBatchContext(false);
        System.out.println(CodeTableToXml.parseSpecialDaysTable(1));
    }

    /**
     * Test the conversion of the Season{@link com.energyict.dlms.axrdencoding.Array} to XML
     */
    @Test
    public final void convertSeasonArrayToXmlTest() {

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CodeTableActCalendar><SeasonProfiles><SeasonProfile>" +
                "<SeasonProfileName>090131</SeasonProfileName><SeasonStart>090cffffff01ff00000000800000</SeasonStart>" +
                "<SeasonWeekName>090130</SeasonWeekName></SeasonProfile></SeasonProfiles></CodeTableActCalendar>";

        // an Array we copied from a converted codeTable
        byte[] berArray = new byte[]{1, 1, 2, 3, 9, 1, 49, 9, 12, -1, -1, -1, 1, -1, 0, 0, 0, 0, -128, 0, 0, 9, 1, 48};
        try {
            Array seasonArray = new Array(berArray, 0, 0);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(CodeTableToXml.rootActCodeTable);
            Element seasonXml = CodeTableToXml.convertSeasonArrayToXml(seasonArray, document);
            root.appendChild(seasonXml);
            document.appendChild(root);

            assertEquals(expectedXml, CodeTableToXml.documentToString(document));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
    }

    /**
     * Test the conversion of the Week{@link com.energyict.dlms.axrdencoding.Array} to XML
     */
    @Test
    public final void convertWeekArrayToXmlTest() {
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CodeTableActCalendar><WeekProfiles>" +
                "<WeekProfile><WeekProfileName>090130</WeekProfileName><wkMonday>1101</wkMonday><wkTuesday>1101</wkTuesday>" +
                "<wkWednesday>1101</wkWednesday><wkThursday>1101</wkThursday><wkFriday>1101</wkFriday><wkSaturday>1102</wkSaturday>" +
                "<wkSunday>1102</wkSunday></WeekProfile></WeekProfiles></CodeTableActCalendar>";

        // an Array we copied from a converted codeTable
        byte[] berArray = new byte[]{1, 1, 2, 8, 9, 1, 48, 17, 1, 17, 1, 17, 1, 17, 1, 17, 1, 17, 2, 17, 2};
        try {
            Array seasonArray = new Array(berArray, 0, 0);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(CodeTableToXml.rootActCodeTable);
            Element seasonXml = CodeTableToXml.convertWeekArrayToXml(seasonArray, document);
            root.appendChild(seasonXml);
            document.appendChild(root);

            assertEquals(expectedXml, CodeTableToXml.documentToString(document));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
    }

    /**
     * Test the conversion of the Day{@link com.energyict.dlms.axrdencoding.Array} to XML
     */
    @Test
    public final void convertDayArrayToXmlTest() {
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CodeTableActCalendar><DayProfiles>" +
                "<DayProfile><DayProfileId>1103</DayProfileId><DayProfileSchedules><DayProfileSchedule>" +
                "<DayScheduleStartTime>090400000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN>" +
                "<DayScheduleScriptSL>120002</DayScheduleScriptSL></DayProfileSchedule></DayProfileSchedules>" +
                "</DayProfile><DayProfile><DayProfileId>1101</DayProfileId><DayProfileSchedules><DayProfileSchedule>" +
                "<DayScheduleStartTime>090400000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN>" +
                "<DayScheduleScriptSL>120000</DayScheduleScriptSL></DayProfileSchedule><DayProfileSchedule>" +
                "<DayScheduleStartTime>090406000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN>" +
                "<DayScheduleScriptSL>120001</DayScheduleScriptSL></DayProfileSchedule><DayProfileSchedule>" +
                "<DayScheduleStartTime>090415000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN>" +
                "<DayScheduleScriptSL>120000</DayScheduleScriptSL></DayProfileSchedule></DayProfileSchedules>" +
                "</DayProfile><DayProfile><DayProfileId>1102</DayProfileId><DayProfileSchedules><DayProfileSchedule>" +
                "<DayScheduleStartTime>090400000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN>" +
                "<DayScheduleScriptSL>120000</DayScheduleScriptSL></DayProfileSchedule></DayProfileSchedules>" +
                "</DayProfile></DayProfiles></CodeTableActCalendar>";

        // an Array we copied from a converted codeTable
        byte[] berArray = new byte[]{1, 3, 2, 2, 17, 3, 1, 1, 2, 3, 9, 4, 0, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46, 50, 53, 53,
                18, 0, 2, 2, 2, 17, 1, 1, 3, 2, 3, 9, 4, 0, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46, 50, 53, 53, 18, 0, 0, 2, 3, 9, 4, 6, 0,
                0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46, 50, 53, 53, 18, 0, 1, 2, 3, 9, 4, 21, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46,
                48, 46, 49, 48, 48, 46, 50, 53, 53, 18, 0, 0, 2, 2, 17, 2, 1, 1, 2, 3, 9, 4, 0, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46,
                50, 53, 53, 18, 0, 0};
        try {
            Array seasonArray = new Array(berArray, 0, 0);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(CodeTableToXml.rootActCodeTable);
            Element dayXml = CodeTableToXml.convertDayArrayToXml(seasonArray, document);
            root.appendChild(dayXml);
            document.appendChild(root);

            assertEquals(expectedXml, CodeTableToXml.documentToString(document));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
    }

    @Test
    public final void convertAllThreeArraysTest() {

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CodeTableActCalendar><SeasonProfiles>" +
                "<SeasonProfile><SeasonProfileName>090131</SeasonProfileName><SeasonStart>090cffffff01ff00000000800000</SeasonStart>" +
                "<SeasonWeekName>090130</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile>" +
                "<WeekProfileName>090130</WeekProfileName><wkMonday>1101</wkMonday><wkTuesday>1101</wkTuesday>" +
                "<wkWednesday>1101</wkWednesday><wkThursday>1101</wkThursday><wkFriday>1101</wkFriday><wkSaturday>1102</wkSaturday>" +
                "<wkSunday>1102</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>1103</DayProfileId>" +
                "<DayProfileSchedules><DayProfileSchedule><DayScheduleStartTime>090400000000</DayScheduleStartTime>" +
                "<DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120002</DayScheduleScriptSL>" +
                "</DayProfileSchedule></DayProfileSchedules></DayProfile><DayProfile><DayProfileId>1101</DayProfileId>" +
                "<DayProfileSchedules><DayProfileSchedule><DayScheduleStartTime>090400000000</DayScheduleStartTime>" +
                "<DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120000</DayScheduleScriptSL>" +
                "</DayProfileSchedule><DayProfileSchedule><DayScheduleStartTime>090406000000</DayScheduleStartTime>" +
                "<DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120001</DayScheduleScriptSL>" +
                "</DayProfileSchedule><DayProfileSchedule><DayScheduleStartTime>090415000000</DayScheduleStartTime>" +
                "<DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120000</DayScheduleScriptSL>" +
                "</DayProfileSchedule></DayProfileSchedules></DayProfile><DayProfile><DayProfileId>1102</DayProfileId>" +
                "<DayProfileSchedules><DayProfileSchedule><DayScheduleStartTime>090400000000</DayScheduleStartTime>" +
                "<DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120000</DayScheduleScriptSL>" +
                "</DayProfileSchedule></DayProfileSchedules></DayProfile></DayProfiles></CodeTableActCalendar>";
        // a SeasonArray we copied from a converted codeTable
        byte[] berSeasonArray = new byte[]{1, 1, 2, 3, 9, 1, 49, 9, 12, -1, -1, -1, 1, -1, 0, 0, 0, 0, -128, 0, 0, 9, 1, 48};

        // a WeekArray we copied from a converted codeTable
        byte[] berWeekArray = new byte[]{1, 1, 2, 8, 9, 1, 48, 17, 1, 17, 1, 17, 1, 17, 1, 17, 1, 17, 2, 17, 2};

        // a DayArray we copied from a converted codeTable
        byte[] berDayArray = new byte[]{1, 3, 2, 2, 17, 3, 1, 1, 2, 3, 9, 4, 0, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46, 50, 53, 53,
                18, 0, 2, 2, 2, 17, 1, 1, 3, 2, 3, 9, 4, 0, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46, 50, 53, 53, 18, 0, 0, 2, 3, 9, 4, 6, 0,
                0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46, 50, 53, 53, 18, 0, 1, 2, 3, 9, 4, 21, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46,
                48, 46, 49, 48, 48, 46, 50, 53, 53, 18, 0, 0, 2, 2, 17, 2, 1, 1, 2, 3, 9, 4, 0, 0, 0, 0, 9, 16, 48, 46, 48, 46, 49, 48, 46, 48, 46, 49, 48, 48, 46,
                50, 53, 53, 18, 0, 0};

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(CodeTableToXml.rootActCodeTable);

            Array seasonArray = new Array(berSeasonArray, 0, 0);
            Element seasonXml = CodeTableToXml.convertSeasonArrayToXml(seasonArray, document);
            root.appendChild(seasonXml);

            Array weekArray = new Array(berWeekArray, 0, 0);
            Element weekXml = CodeTableToXml.convertWeekArrayToXml(weekArray, document);
            root.appendChild(weekXml);

            Array dayArray = new Array(berDayArray, 0, 0);
            Element dayXml = CodeTableToXml.convertDayArrayToXml(dayArray, document);
            root.appendChild(dayXml);

            document.appendChild(root);
            assertEquals(expectedXml, CodeTableToXml.documentToString(document));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
    }

    @Test
    public final void convertSpecialDayArrayToXmlTest() {
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CodeTableSpecialDay><SpecialDayProfiles>" +
                "<SpecialDayProfile><SpecialDayEntryIndex>123af0</SpecialDayEntryIndex><SpecialDayEntryDate>090507db041807</SpecialDayEntryDate>" +
                "<SpecialDayEntryDayId>1103</SpecialDayEntryDayId></SpecialDayProfile><SpecialDayProfile><SpecialDayEntryIndex>120166</SpecialDayEntryIndex>" +
                "<SpecialDayEntryDate>0905ffff0c19ff</SpecialDayEntryDate><SpecialDayEntryDayId>1103</SpecialDayEntryDayId></SpecialDayProfile>" +
                "<SpecialDayProfile><SpecialDayEntryIndex>12013a</SpecialDayEntryIndex><SpecialDayEntryDate>0905ffff0b0bff</SpecialDayEntryDate>" +
                "<SpecialDayEntryDayId>1103</SpecialDayEntryDayId></SpecialDayProfile></SpecialDayProfiles></CodeTableSpecialDay>";

        byte[] berSpecialDayArray = new byte[]{1, 3, 2, 3, 18, 58, -16, 9, 5, 7, -37, 4, 24, 7, 17, 3, 2, 3, 18, 1, 102, 9, 5, -1, -1, 12, 25, -1, 17, 3, 2, 3, 18, 1, 58, 9, 5, -1, -1, 11, 11, -1, 17, 3};

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(CodeTableToXml.rootSpDCodeTable);

            Array spdArray = new Array(berSpecialDayArray, 0, 0);
            Element seasonXml = CodeTableToXml.convertSpecialDayArrayToXml(spdArray, document);
            root.appendChild(seasonXml);

            document.appendChild(root);

            assertEquals(expectedXml, CodeTableToXml.documentToString(document));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail();
        }
    }

}
