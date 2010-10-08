package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.protocolimpl.dlms.as220.parsing.CodeTableToXml;
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
 * Date: 6-okt-2010
 * Time: 17:16:26
 * To change this template use File | Settings | File Templates.
 */
public class AS220ActivityCalendarControllerTest {

    @Test
    public void testParseContent() throws Exception {
        String content = "<TimeOfUse name=\"ActGna1\" activationDate=\"0\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<CodeTableActCalendar><SeasonProfiles><SeasonProfile><SeasonProfileName>090131</SeasonProfileName>" +
                "<SeasonStart>090cffffff01ff00000000800000</SeasonStart><SeasonWeekName>090130</SeasonWeekName></SeasonProfile></SeasonProfiles><WeekProfiles><WeekProfile><WeekProfileName>090130</WeekProfileName><wkMonday>1101</wkMonday><wkTuesday>1101</wkTuesday><wkWednesday>1101</wkWednesday><wkThursday>1101</wkThursday><wkFriday>1101</wkFriday><wkSaturday>1102</wkSaturday><wkSunday>1102</wkSunday></WeekProfile></WeekProfiles><DayProfiles><DayProfile><DayProfileId>1101</DayProfileId><DayProfileSchedules><DayProfileSchedule><DayScheduleStartTime>090400000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120000</DayScheduleScriptSL></DayProfileSchedule><DayProfileSchedule><DayScheduleStartTime>090406000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120001</DayScheduleScriptSL></DayProfileSchedule><DayProfileSchedule><DayScheduleStartTime>090415000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120000</DayScheduleScriptSL></DayProfileSchedule></DayProfileSchedules></DayProfile><DayProfile><DayProfileId>1102</DayProfileId><DayProfileSchedules><DayProfileSchedule><DayScheduleStartTime>090400000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120000</DayScheduleScriptSL></DayProfileSchedule></DayProfileSchedules></DayProfile><DayProfile><DayProfileId>1103</DayProfileId><DayProfileSchedules><DayProfileSchedule><DayScheduleStartTime>090400000000</DayScheduleStartTime><DayScheduleScriptLN>0910302e302e31302e302e3130302e323535</DayScheduleScriptLN><DayScheduleScriptSL>120002</DayScheduleScriptSL></DayProfileSchedule></DayProfileSchedules></DayProfile></DayProfiles></CodeTableActCalendar>" +
                "<CodeTableSpecialDay><SpecialDayProfiles>" +
                "<SpecialDayProfile><SpecialDayEntryIndex>123af0</SpecialDayEntryIndex><SpecialDayEntryDate>090507db041807</SpecialDayEntryDate>" +
                "<SpecialDayEntryDayId>1103</SpecialDayEntryDayId></SpecialDayProfile><SpecialDayProfile><SpecialDayEntryIndex>120166</SpecialDayEntryIndex>" +
                "<SpecialDayEntryDate>0905ffff0c19ff</SpecialDayEntryDate><SpecialDayEntryDayId>1103</SpecialDayEntryDayId></SpecialDayProfile>" +
                "<SpecialDayProfile><SpecialDayEntryIndex>12013a</SpecialDayEntryIndex><SpecialDayEntryDate>0905ffff0b0bff</SpecialDayEntryDate>" +
                "<SpecialDayEntryDayId>1103</SpecialDayEntryDayId></SpecialDayProfile></SpecialDayProfiles></CodeTableSpecialDay>" +
                "</TimeOfUse>";

        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);
        aacc.parseContent(content);
        Array seasonArray = aacc.getSeasonArray();
    }

    @Test
    public void getContentValueTest(){
        String content = "ContentOfTheTest";
        String xmlContent = "<test>"+content+"</test>";

        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);
        assertEquals(content, aacc.getContentValue(xmlContent, "test"));

        String xmlContentWithAttributes = "<test attrb1='attrb1'>"+content+"</test>";
        assertEquals(content, aacc.getContentValue(xmlContentWithAttributes, "test"));
    }

    @Test
    public void getAttributeValueTest(){
        String content = "ContentOfTheTest";
        String xmlContentWithAttributes = "<test attrb1=\"attrb1\">"+content+"</test>";

        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);

        assertEquals("attrb1", aacc.getAttributeValue(xmlContentWithAttributes, "attrb1"));
    }

    @Test
    public void getImplicitContentValueTest(){
        String content = "<test>ContentOfTheTest</test>";
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+content;

        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);
        assertEquals(content, aacc.getImplicitContentValue(xmlContent, "test"));
    }
}
