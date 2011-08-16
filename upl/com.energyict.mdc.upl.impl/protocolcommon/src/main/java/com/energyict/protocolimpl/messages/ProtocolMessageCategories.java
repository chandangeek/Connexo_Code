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
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getChangeZigBeeHanSASMessageSpecification());
        // ZigBee DLMS objects currently have no support to shutdown a previously created HAN network, should they have one?
        //hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveHanMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getJoinZigBeeSlaveSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveZigBeeSlaveSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveAllZigBeeSlavesSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getBackupZigBeeHanKeyMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRestoreZigBeeParametersMessageSpecification());
        return hanManagement;
    }

    /**
     * MessageCategory containing relevant Pricing Information messages
     *
     * @return the newly created category
     */
    public static MessageCategorySpec getPricingInformationCategory() {
        MessageCategorySpec pricing = new MessageCategorySpec(RtuMessageCategoryConstants.PRICING_INFORMATION);
        pricing.addMessageSpec(ProtocolMessageSpecifications.getUpdatePricingMessageSpecification());
        return pricing;
    }

    /**
     * MessageCategory containing relevant functionality to inform a device a 'Change Of Tenancy' will occur.
     *
     * @return the newly created category
     */
    public static MessageCategorySpec getChangeOfTenancyCategory() {
        MessageCategorySpec cot = new MessageCategorySpec(RtuMessageCategoryConstants.CHANGE_OF_TENANCY);
        cot.addMessageSpec(ProtocolMessageSpecifications.getChangeOfTenancyMessageSpecification());
        return cot;
    }

    /**
     * MessageCategory containing relevant functionality to inform a device that a 'Supplier change' will occur.
     *
     * @return the newly created category
     */
    public static MessageCategorySpec getChangeOfSupplierCategory() {
        MessageCategorySpec cos = new MessageCategorySpec(RtuMessageCategoryConstants.CHANGE_OF_SUPPLIER);
        cos.addMessageSpec(ProtocolMessageSpecifications.getChangeOfSupplierMessageSpecification());
        return cos;
    }
}
