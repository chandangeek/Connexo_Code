/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;

import java.util.List;

public interface CollectedMessageList extends CollectedData {

    /**
     * Can be used as protocolInformation to indicate that a DeviceMessage
     * was picked up by a certain ComTask, but due to an error before executing
     * this Message, we did not process this message. Next time another
     * ComTask will pick up this message and try again.
     */
    String REASON_FOR_PENDING_STATE = "This message was picked up by a ComTask, but not executed due to an error before executing this message.";

    void addCollectedMessages(CollectedMessage collectedMessage);

    List<CollectedMessage> getCollectedMessages();

    List<CollectedMessage> getCollectedMessages(MessageIdentifier messageIdentifier);

}