package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeOctetStringTest {

    @Test
    public void testDateTimeDeviation(){
        OctetString octetString = new OctetString(ProtocolTools.getBytesFromHexString("0070E000901000500B00001F0490FF010000"));

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Dubai");
        DateTimeOctetString dateTimeOctetString = new DateTimeOctetString(octetString, timeZone);

        assertEquals(dateTimeOctetString.getDeviation(), -240);
        assertEquals(dateTimeOctetString.getDeviation(),(-1)*timeZone.getRawOffset()/60000);
    }
}