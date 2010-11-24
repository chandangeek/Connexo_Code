package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.protocolimpl.utils.Utilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 6-okt-2010
 * Time: 17:16:26
 * To change this template use File | Settings | File Templates.
 */
public class AS220ActivityCalendarControllerTest {

    static final Log logger = LogFactory.getLog(AS220ActivityCalendarControllerTest.class);

    @Test
    public void testParseContent() throws Exception {

        String seasonArray = "01010203090100090cffff0101ffffffffffffffff090100";
        String weekArray = "010102080901001101110111011101110111021102";
        String dayArray = "01030202110201010203090400000000090600000a0064ff1200000202110101030203090400000000090600000a0064ff1200000203090406000000090600000a0064ff1200010203090415000000090600000a0064ff1200000202110001010203090400000000090600000a0064ff120001";
        String specialDayArray = "010302031200000905ffff0b0bff110002031200010905ffff0c19ff11000203120002090507db0418ff1100";

        String msgXml = new String(Utilities.readResource("com/energyict/protocolimpl/dlms/as220/parsing/ActivityCalendar.xml"));

        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);
        aacc.parseContent(msgXml);

        assertArrayEquals(DLMSUtils.hexStringToByteArray(seasonArray), aacc.getSeasonArray().getBEREncodedByteArray());
        assertArrayEquals(DLMSUtils.hexStringToByteArray(weekArray), aacc.getWeekArray().getBEREncodedByteArray());
        assertArrayEquals(DLMSUtils.hexStringToByteArray(dayArray), aacc.getDayArray().getBEREncodedByteArray());
        assertArrayEquals(DLMSUtils.hexStringToByteArray(specialDayArray), aacc.getSpecialDayArray().getBEREncodedByteArray());
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

    @Test
    public void createSeasonNameTest(){
        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);
        try {
            assertArrayEquals(new byte[]{0x09, 0x01, 0x00},aacc.createSeasonName("0").getBEREncodedByteArray());
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail();
        }

        try {
            aacc.createSeasonName("11").getBEREncodedByteArray();
        } catch (IOException e) {
            assertEquals("ActivityCalendar did not contain a valid SEASONName.", e.getMessage());
        }
    }

    @Test
    public final void constructEightByteCalendarNameTest(){
        byte[] expected = new byte[]{0x43, 0x61, 0x6c, 0x20, 0x20, 0x20, 0x20, 0x20};
        AS220ActivityCalendarController aacc = new AS220ActivityCalendarController(null);
        assertArrayEquals(expected, aacc.constructEightByteCalendarName("Cal"));
    }

}
