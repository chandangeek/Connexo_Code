package com.energyict.protocolimpl.messages;

import com.energyict.protocol.messaging.MessageCategorySpec;

/**
 * Contains a summary of possible {@link com.energyict.protocol.messaging.MessageCategorySpec} for a protocol to implement
 */
public class ProtocolMessageCategories {

    public static MessageCategorySpec getDemandResetCategory() {
        MessageCategorySpec demandReset = new MessageCategorySpec(RtuMessageCategoryConstants.DEMANDRESET);
        demandReset.addMessageSpec(ProtocolMessageSpecifications.getDemandResetMessageSpecification());
        return demandReset;
    }

}
