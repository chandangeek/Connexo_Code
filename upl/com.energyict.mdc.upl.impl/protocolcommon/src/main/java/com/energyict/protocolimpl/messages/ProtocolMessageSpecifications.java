package com.energyict.protocolimpl.messages;

import com.energyict.protocol.messaging.*;

/**
 * Contains a summary of {@link com.energyict.protocol.messaging.MessageSpec} for the protocol to use in the {@link com.energyict.protocolimpl.messages.ProtocolMessageCategories}
 */
public class ProtocolMessageSpecifications {

    /**
     * Creates a MessageSpecification for a <i>Demand Reset</i> message. The message does not contain any values or attributes.
     * @return the requested messageSpecification.
     */
    public static MessageSpec getDemandResetMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.DEMANDRESET, false);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.DEMAND_RESET);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
