package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.engine.impl.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author sva
 * @since 30/09/13 - 9:09
 */
public class UpdateDeviceMessageTest {

    private final int MESSAGE_ID = 1;

    @Test
    public void testToJournalMessageDescriptiononInfoLogLevel() throws Exception {
        final MessageIdentifier messageIdentifier = new DeviceMessageIdentifierById(MESSAGE_ID);
        final DeviceProtocolMessageAcknowledgement messageAcknowledgement = new DeviceProtocolMessageAcknowledgement(messageIdentifier);
        messageAcknowledgement.setDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        messageAcknowledgement.setProtocolInfo("Additional ProolInfo");
        UpdateDeviceMessage command = new UpdateDeviceMessage(messageAcknowledgement);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(UpdateDeviceMessage.class.getSimpleName() + " {messageIdentifier: messageId = 1; message status: confirmed}");
    }

    @Test
    public void testToJournalMessageDescriptionOnDebugLogLevel() throws Exception {
        final MessageIdentifier messageIdentifier = new DeviceMessageIdentifierById(MESSAGE_ID);
        final DeviceProtocolMessageAcknowledgement messageAcknowledgement = new DeviceProtocolMessageAcknowledgement(messageIdentifier);
        messageAcknowledgement.setDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        messageAcknowledgement.setProtocolInfo("Additional ProtocolInfo");
        UpdateDeviceMessage command = new UpdateDeviceMessage(messageAcknowledgement);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).isEqualTo(UpdateDeviceMessage.class.getSimpleName() + " {messageIdentifier: messageId = 1; message status: confirmed; protocolInfo: Additional ProtocolInfo}");
    }
}
