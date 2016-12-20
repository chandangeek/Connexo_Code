package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.util.List;

/**
 * A CollectedMessageList identifies a list of {@link com.energyict.mdc.upl.messages.DeviceMessage}s
 * executed by the device and the result from the execution.
 * <p>
 * Copyrights EnergyICT
 * Date: 21/03/13
 * Time: 16:31
 */
public interface CollectedMessageList extends CollectedData {

    /**
     * Can be used as protocolInformation to indicate that a DeviceMessage
     * was picked up by a certain ComTask, but due to an error before executing
     * this Message, we did not process this message. Next time another
     * ComTask will pick up this message and try again.
     */
    String REASON_FOR_PENDING_STATE = "This message was picked up by a ComTask, but not executed due to an error before executing this message.";

    void addCollectedMessage(CollectedMessage collectedMessage);

    void addCollectedMessages(CollectedMessageList collectedMessages);

    List<CollectedMessage> getCollectedMessages();

    List<CollectedMessage> getCollectedMessages(MessageIdentifier messageIdentifier);

}