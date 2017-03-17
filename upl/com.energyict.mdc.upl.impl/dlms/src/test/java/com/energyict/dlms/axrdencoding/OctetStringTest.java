/**
 *
 */
package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.energyict.dlms.DLMSUtils.getBytesFromHexString;
import static org.junit.Assert.*;

/**
 * @author jme
 */
public class OctetStringTest {

    /**
     * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromString(java.lang.String)}.
     */
    @Test
    public final void testFromStringString() {
        final String testString = "TestString";
        OctetString os = OctetString.fromString(testString);
        assertNotNull(os);
        assertEquals(testString.length() + 2, os.getDecodedSize());
    }

    @Test
    public final void fromIpAddressStringTest() {
        final OctetString oc = OctetString.fromIPv4Address("192.168.18.255");
        assertNotNull(oc);
        assertArrayEquals(getBytesFromHexString("$09$04$C0$A8$12$FF"), oc.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$C0$A8$12$FF"), oc.getContentByteArray());
        assertArrayEquals(getBytesFromHexString("$C0$A8$12$FF"), oc.getOctetStr());
        assertArrayEquals(getBytesFromHexString("$12$FF"), oc.getContentBytes());
        assertNotNull(oc.stringValue());
        assertEquals(6, oc.getDecodedSize());
        assertEquals(6, oc.size());
    }

    @Test
    public final void testFromByteArray() {
        final byte[] testArray = "TestString".getBytes();
        OctetString os = OctetString.fromByteArray(testArray, testArray.length);
        assertNotNull(os);
        assertEquals(testArray.length + 2, os.getDecodedSize());
    }

    @Test
    public final void fromBerDataAndOffset() throws IOException {
        final OctetString oc = new OctetString(getBytesFromHexString("$FF$FF$FF$09$04$C0$A8$12$FF$01$02$03$04$05"), 3);
        assertNotNull(oc);
        assertArrayEquals(getBytesFromHexString("$09$04$C0$A8$12$FF"), oc.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$C0$A8$12$FF"), oc.getContentByteArray());
        assertArrayEquals(getBytesFromHexString("$C0$A8$12$FF"), oc.getOctetStr());
        assertArrayEquals(getBytesFromHexString("$12$FF"), oc.getContentBytes());
        assertNotNull(oc.stringValue());
        assertEquals(6, oc.getDecodedSize());
        assertEquals(6, oc.size());
    }

    @Test
    public final void fromBerDataOffsetAndNotFixed() throws IOException {
        final OctetString oc = new OctetString(getBytesFromHexString("$09$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), 0, true);
        assertNotNull(oc);
        assertArrayEquals(getBytesFromHexString("$09$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getContentByteArray());
        assertArrayEquals(getBytesFromHexString("$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getOctetStr());
        assertArrayEquals(getBytesFromHexString("$07$DC$01$1B$05$07$33$09$13$00$00"), oc.getContentBytes());
        assertNotNull(oc.stringValue());
        assertEquals(14, oc.getDecodedSize());
        assertEquals(14, oc.size());
    }

    @Test
    public final void fromBerDataOffsetAndFixed() throws IOException {
        final OctetString oc = new OctetString(getBytesFromHexString("$09$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), 0, false);
        assertNotNull(oc);
        assertArrayEquals(getBytesFromHexString("$09$0D$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getContentByteArray());
        assertArrayEquals(getBytesFromHexString("$0C$07$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getOctetStr());
        assertArrayEquals(getBytesFromHexString("$DC$01$1B$05$07$33$09$13$00$00$00"), oc.getContentBytes());
        assertNotNull(oc.stringValue());
        assertEquals(14, oc.getDecodedSize());
        assertEquals(14, oc.size());
    }

    @Test
    public final void fromDateTime() throws IOException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(getDateFromYYYYMMddhhmmss("2010-05-07 15:30:45 +0200"));
        AXDRDateTime dateTime = new AXDRDateTime(cal);
        System.out.println(DLMSUtils.getHexStringFromBytes(dateTime.getBEREncodedByteArray()));

        OctetString oc = new OctetString(dateTime.getBEREncodedByteArray(), 0, false);
        System.out.println(DLMSUtils.getHexStringFromBytes(oc.getBEREncodedByteArray()));

        assertNotNull(oc);
        assertArrayEquals(getBytesFromHexString("$09$0D$0C$07$DA$05$07$05$0D$1E$2D$00$00$00$00"), oc.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$0C$07$DA$05$07$05$0D$1E$2D$00$00$00$00"), oc.getContentByteArray());
        assertArrayEquals(getBytesFromHexString("$0C$07$DA$05$07$05$0D$1E$2D$00$00$00$00"), oc.getOctetStr());
        assertArrayEquals(getBytesFromHexString("$DA$05$07$05$0D$1E$2D$00$00$00$00"), oc.getContentBytes());
        assertNotNull(oc.stringValue());
        assertEquals(14, oc.getDecodedSize());
        assertEquals(14, oc.size());
    }

    /**
     * Generate a new date object, derived from the given string.
     * This string should allways have the following format: yyyy-MM-dd hh:mm:ss
     *
     * @param yyyyMMddhhmmss the date in string format (yyyy-MM-dd hh:mm:ss) or null if the string vas invalid
     * @return The new date object
     */
    private static Date getDateFromYYYYMMddhhmmss(String yyyyMMddhhmmss) {
        try {
            if (yyyyMMddhhmmss != null) {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(yyyyMMddhhmmss);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            } else {
                return null;
            }
        } catch (ParseException e) {
            return null;
        }
    }

}
