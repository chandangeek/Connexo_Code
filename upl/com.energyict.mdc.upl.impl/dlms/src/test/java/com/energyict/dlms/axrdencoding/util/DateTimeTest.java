package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeTest {

    @Test
    public void testDateTimeDeviation(){
        OctetString octetString = new OctetString(ProtocolTools.getBytesFromHexString("0070E000901000500B00001F0490FF010000"));

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Dubai");
        DateTime dateTime= new DateTime(octetString, timeZone);

        assertEquals(dateTime.getDeviation(), -240);
        assertEquals(dateTime.getDeviation(),(-1)*timeZone.getRawOffset()/60000);
    }
}