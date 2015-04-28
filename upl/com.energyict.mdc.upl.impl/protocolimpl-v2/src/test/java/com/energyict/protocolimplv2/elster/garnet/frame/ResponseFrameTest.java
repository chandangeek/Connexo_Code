package com.energyict.protocolimplv2.elster.garnet.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.structure.LogBookEventResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.logbook.LogBookEventCode;
import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author sva
 * @since 28/04/2015 - 14:07
 */
public class ResponseFrameTest {

    @Test
    public void testParseEmptyLogBookEventResponse() throws Exception {
        ResponseFrame responseFrame = new ResponseFrame(TimeZone.getDefault());
        byte[] rawResponse = ProtocolTools.getBytesFromHexString("000011000015042216532900000000000000000000000000000000000000000000000000000000000000006290", "");

        // Business method
        responseFrame.parse(rawResponse, 0);
        responseFrame.doParseData();

        // Asserts
        assertTrue(responseFrame.getData() instanceof LogBookEventResponseStructure);
        LogBookEventResponseStructure eventResponse = (LogBookEventResponseStructure) responseFrame.getData();
        assertEquals(1429714409000l, eventResponse.getDateTime().getDate().getTime());  // 22/04/2015 16:53:29 CEST 2015
        assertEquals(0, eventResponse.getTotalNrOfLogs().getNr());
        assertEquals(0, eventResponse.getLogNr().getNr());
        assertEquals(LogBookEventCode.EventCode.UNKNOWN, eventResponse.getEventCode().getEventCode());
    }
}