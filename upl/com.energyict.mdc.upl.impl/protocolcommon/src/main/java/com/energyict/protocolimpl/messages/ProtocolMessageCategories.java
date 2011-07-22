package com.energyict.protocolimpl.messages;

import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;

/**
 * Contains a summary of possible {@link com.energyict.protocol.messaging.MessageCategorySpec} for a protocol to implement
 */
public class ProtocolMessageCategories {

    /**
     * Creates a MessageCategory for a default DemandReset
     *
     * @return a messageCategory for a default DemandReset (no arguments)
     */
    public static MessageCategorySpec getDemandResetCategory() {
        MessageCategorySpec demandReset = new MessageCategorySpec(RtuMessageCategoryConstants.DEMANDRESET);
        demandReset.addMessageSpec(ProtocolMessageSpecifications.getDemandResetMessageSpecification());
        return demandReset;
    }

    /**
     * MessageCategory containing relevant HAN management messages
     *
     * @return the newly created category
     */
    public static MessageCategorySpec getHanManagementCategory() {
        MessageCategorySpec hanManagement = new MessageCategorySpec(RtuMessageConstant.HanManagement);
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getCreateHanMessageSpecification());
        return hanManagement;
    }

}
