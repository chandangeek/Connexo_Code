package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 12-aug-2011
 * Time: 13:24:10
 */
@RunWith(MockitoJUnitRunner.class)
public class AS300MessageExecutorTest {

    private static byte[] xmlContentBytes = new byte[]{60,85,112,100,97,116,101,95,80,114,105,99,105,110,103,95,73,110,102,111,114,109,97,116,105,111,110,62,60,73,110,99,108,117,100,101,100,70,105,108,101,62,72,52,115,73,65,65,65,65,65,65,65,65,65,72,49,83,84,85,118,68,81,66,66,100,65,111,111,82,118,79,112,49,50,84,43,81,116,69,51,98,121,54,90,81,76,89,85,101,114,71,74,67,76,53,54,50,54,97,65,76,43,88,74,51,65,118,89,47,57,85,102,53,66,122,120,53,13,10,54,100,70,116,107,112,97,78,111,103,116,122,101,102,80,101,109,56,102,77,55,106,55,74,87,97,87,73,56,51,121,55,43,55,114,90,110,49,47,69,72,119,52,104,55,121,85,104,122,106,87,80,107,108,102,73,103,67,53,70,66,105,71,76,81,83,79,100,82,103,80,102,102,50,81,84,13,10,101,110,88,112,56,114,115,105,75,119,88,75,116,85,119,108,98,109,118,73,53,102,101,65,111,79,74,116,67,98,111,66,87,111,81,101,111,74,67,49,99,114,111,67,112,87,87,82,104,54,122,72,118,70,98,111,47,86,84,121,87,73,108,99,105,119,81,78,48,85,67,117,50,57,104,70,13,10,103,69,47,119,86,112,107,119,55,81,65,98,87,104,89,113,69,43,109,120,52,102,73,112,111,112,76,114,67,109,69,71,79,108,71,121,120,69,75,100,109,105,90,47,75,114,82,101,98,79,104,75,112,74,85,74,53,47,116,57,110,49,72,80,73,105,120,121,106,83,74,80,119,79,97,89,13,10,78,122,80,86,109,56,43,55,51,78,77,111,105,122,121,121,75,100,122,55,76,119,50,118,78,90,98,102,81,52,75,65,107,82,72,107,76,48,101,47,52,84,65,89,57,84,102,68,89,68,119,89,66,49,51,110,106,112,104,55,102,121,122,69,98,114,82,81,115,47,114,117,112,103,43,110,13,10,57,88,55,100,49,111,106,114,51,122,68,53,66,107,53,81,108,74,107,120,65,103,65,65,60,47,73,110,99,108,117,100,101,100,70,105,108,101,62,60,47,85,112,100,97,116,101,95,80,114,105,99,105,110,103,95,73,110,102,111,114,109,97,116,105,111,110,62};
    private static String expectedResponse = "10000AC401C10001010f00";

    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private TariffCalendarExtractor calendarExtractor;
    @Mock
    private DeviceMessageFileFinder messageFileFinder;
    @Mock
    private DeviceMessageFileExtractor messageFileExtractor;
    @Mock
    private DateFormatter dateFormatter;

    @Test
    public void testExecuteMessageEntry() throws Exception {
        MessageEntry msgEntry = MessageEntry.fromContent(new String(xmlContentBytes, "US-ASCII")).trackingId("TrackingId").serialNumber("SerialNumber").finish();
        AS300 protocol = new AS300(calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, dateFormatter);
        DummyDLMSConnection connection = new DummyDLMSConnection();
        connection.setResponseByte(DLMSUtils.hexStringToByteArray(expectedResponse));
        protocol.getDlmsSession().setDlmsConnection(connection);
        AS300MessageExecutor mExecutor = new AS300MessageExecutor(protocol, calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, dateFormatter);
        MessageResult result = mExecutor.executeMessageEntry(msgEntry);
        assertTrue(result.isSuccess());
    }
}
