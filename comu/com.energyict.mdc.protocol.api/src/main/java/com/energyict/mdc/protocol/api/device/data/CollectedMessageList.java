package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;

import java.util.List;

/**
 * A <Code>CollectedMessageList</Code> identifies a list of DeviceMessages
 * executed by the device and the result from the execution.
 * <p/>
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
    public static final String REASON_FOR_PENDING_STATE = "This message was picked up by a ComTask, but not executed due to an error before executing this message.";

    public void addCollectedMessages(CollectedMessage collectedMessage);

    public List<CollectedMessage> getCollectedMessages();

    public List<CollectedMessage> getCollectedMessages(MessageIdentifier messageIdentifier);

}