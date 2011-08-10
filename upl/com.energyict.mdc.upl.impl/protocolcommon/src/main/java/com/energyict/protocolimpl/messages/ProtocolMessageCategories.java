package com.energyict.protocolimpl.messages;

import com.energyict.protocol.messaging.MessageCategorySpec;

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
        MessageCategorySpec hanManagement = new MessageCategorySpec(RtuMessageCategoryConstants.ZIG_BEE_SETUP);
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getCreateHanMessageSpecification());
        // ZigBee DLMS objects currently have no support to shutdown a previously created HAN network, should they have one?
        //hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveHanMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getJoinZigBeeSlaveSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveZigBeeSlaveSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getBackupZigBeeHanKeyMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRestoreZigBeeParametersMessageSpecification());
        return hanManagement;
    }

}
