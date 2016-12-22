package com.energyict.protocolimpl.messages;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;

/**
 * Contains a summary of possible {@link MessageCategorySpec} for a protocol to implement
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
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveHanMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveMirrorMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getJoinZigBeeSlaveSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveZigBeeSlaveSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRemoveAllZigBeeSlavesSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getBackupZigBeeHanKeyMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getRestoreZigBeeParametersMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getReadZigBeeStatusMessageSpecification());
        hanManagement.addMessageSpec(ProtocolMessageSpecifications.getZigbeeNCPFirmwareUpgradeSpecification());
        return hanManagement;
    }

    /**
     * MessageCategory containing the GPRS Modem Ping Setup message
     *
     * @return the newly created category
     */
    public static MessageCategorySpec getGPRSModemCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec(RtuMessageCategoryConstants.GPRS_MODEM_PING_SETUP);
        categorySpec.addMessageSpec(ProtocolMessageSpecifications.getModemPingSetupMessageSpecification());
        return categorySpec;
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


    /**
     * MessageCategory containing relevant functionality to change the administrative state of a DSMR4.0 meter.
     *
     * @return the newly created category
     */
    public static MessageCategorySpec getChangeAdministrativeStatusCategory() {
        MessageCategorySpec casc = new MessageCategorySpec(RtuMessageCategoryConstants.CHANGE_ADMINISTRATIVE_STATUS);
        casc.addMessageSpec(ProtocolMessageSpecifications.getChangeAdministrativeStatusMessageSpecification());
        return casc;
    }
}
