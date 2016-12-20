package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.impl.identifiers.DeviceMessageIdentifierForAlreadyKnownMessage;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 30/09/13 - 9:09
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateDeviceMessageTest {

    private final long MESSAGE_ID = 1;

    @Mock
    private DeviceMessage deviceMessage;

    @Before
    public void initBefore() {
        when(deviceMessage.getId()).thenReturn(MESSAGE_ID);
    }

    @Test
    public void testToJournalMessageDescriptionOnInfoLogLevel() throws Exception {
        final MessageIdentifier messageIdentifier = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage);
        final DeviceProtocolMessageAcknowledgement messageAcknowledgement = new DeviceProtocolMessageAcknowledgement(messageIdentifier);
        messageAcknowledgement.setDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        messageAcknowledgement.setProtocolInfo("Additional ProtocolInfo");
        UpdateDeviceMessage command = new UpdateDeviceMessage(messageAcknowledgement, null, new NoDeviceCommandServices());

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{messageIdentifier: message having id 1; message status: confirmed}");
    }

    @Test
    public void testToJournalMessageDescriptionOnDebugLogLevel() throws Exception {
        Instant sentDate = Instant.now();
        final MessageIdentifier messageIdentifier = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage);
        final DeviceProtocolMessageAcknowledgement messageAcknowledgement = new DeviceProtocolMessageAcknowledgement(messageIdentifier);
        messageAcknowledgement.setDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        messageAcknowledgement.setProtocolInfo("Additional ProtocolInfo");
        messageAcknowledgement.setSentDate(sentDate);
        UpdateDeviceMessage command = new UpdateDeviceMessage(messageAcknowledgement, null, new NoDeviceCommandServices());

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).contains("{messageIdentifier: message having id 1; message status: confirmed; sent date: " + sentDate.toString() + "; protocolInfo: Additional ProtocolInfo}");
    }
}
