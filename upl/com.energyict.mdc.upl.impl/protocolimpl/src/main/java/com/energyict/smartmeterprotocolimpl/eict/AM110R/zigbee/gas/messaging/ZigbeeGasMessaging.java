package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.messaging;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:55
 */
public class ZigbeeGasMessaging extends GenericMessaging implements MessageProtocol {

    private final ZigbeeMessageExecutor messageExecutor;

    public ZigbeeGasMessaging(final ZigbeeMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    public void applyMessages(List messageEntries) throws IOException {
        // Nothing to do here
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        categories.add(getTariffMessageCategorySpec());
        categories.add(getPricingInformationMessageCategorySpec());
        categories.add(getCoTSMessageCategorySpec());
        categories.add(getGasConversionMessageCategorySpec());
        categories.add(getFirmwareUpgradeMessageCategorySpec());
        return categories;
    }

    private MessageCategorySpec getTariffMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec(RtuMessageCategoryConstants.TIMEOFUSE);
        pricingInformationCategory.addMessageSpec(addBasicMsgWithAttributes("Send new TOU tariff", RtuMessageConstant.TOU_SEND_NEW_TARIFF, false, RtuMessageConstant.TOU_TARIFF_USER_FILE));
        return pricingInformationCategory;
    }

    private MessageCategorySpec getPricingInformationMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec(RtuMessageCategoryConstants.PRICING_INFORMATION);
        pricingInformationCategory.addMessageSpec(addBasicMsgWithAttributes("Send new price matrix", RtuMessageConstant.SEND_NEW_PRICE_MATRIX, false, RtuMessageConstant.PRICE_MATRIX_USER_FILE));
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set standing charge", RtuMessageConstant.SET_STANDING_CHARGE, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.STANDING_CHARGE));
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set currency", RtuMessageConstant.SET_CURRENCY, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.CURRENCY));
        return pricingInformationCategory;
    }

    private MessageCategorySpec getFirmwareUpgradeMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec("Firmware");
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Upgrade firmware", RtuMessageConstant.FIRMWARE_UPGRADE, false, RtuMessageConstant.ACTIVATE_DATE, RtuMessageConstant.FIRMWARE_USER_FILE));
        return pricingInformationCategory;
    }

    private MessageCategorySpec getCoTSMessageCategorySpec() {
        MessageCategorySpec coTSMessageCat = new MessageCategorySpec("Change of Tenancy or Supplier");
        coTSMessageCat.addMessageSpec(
                addMsgWithValuesAndOptionalValue(
                        "Change Of Tenant", RtuMessageConstant.CHANGE_OF_TENANT,
                        false,
                        RtuMessageConstant.ACTIVATION_DATE,
                        RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED
                ));
        coTSMessageCat.addMessageSpec(
                addMsgWithValuesAndOptionalValue(
                        "Change Of Supplier", RtuMessageConstant.CHANGE_OF_SUPPLIER,
                        false,
                        RtuMessageConstant.ACTIVATION_DATE,
                        RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED
                ));
        return coTSMessageCat;
    }

    private MessageCategorySpec getGasConversionMessageCategorySpec() {
        MessageCategorySpec cat2 = new MessageCategorySpec("CV & CF information");
        cat2.addMessageSpec(addMsgWithValuesAndOptionalValue("Set calorific value", RtuMessageConstant.SET_CALORIFIC_VALUE, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.CALORIFIC_VALUE));
        cat2.addMessageSpec(addMsgWithValuesAndOptionalValue("Set conversion factor", RtuMessageConstant.SET_CONVERSION_FACTOR, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.CONVERSION_FACTOR));
        return cat2;
    }

    protected MessageSpec addBasicMsgWithAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addMsgWithValuesAndOptionalValue(final String description, final String tagName, final boolean advanced, String lastAttribute, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        tagSpec.add(new MessageAttributeSpec(lastAttribute, false));
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    // One attribute is set required - all others are optional
    protected MessageSpec addMsgWithValuesAndRequiredValue(final String description, final String tagName, final boolean advanced, String requiredAttribute, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(requiredAttribute, true));
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, false));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_SEND_NEW_TARIFF)) {
            return writeUserFileTag(msgTag, RtuMessageConstant.TOU_TARIFF_USER_FILE);
        } else if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.SEND_NEW_PRICE_MATRIX)) {
            return writeUserFileTag(msgTag, RtuMessageConstant.PRICE_MATRIX_USER_FILE);
        } else if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.FIRMWARE_UPGRADE)) {
            return writeUserFileTag(msgTag, RtuMessageConstant.FIRMWARE_USER_FILE);
        } else {
            return super.writeTag(msgTag);
        }
    }

    private String writeUserFileTag(MessageTag msgTag, String userFileAttributeName) {
        StringBuilder builder = new StringBuilder();
        String content = "";
        String activationDate = null;

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());
        builder.append(">");

        // b. Attributes
        for (Object o1 : msgTag.getAttributes()) {
            MessageAttribute att = (MessageAttribute) o1;
            if (userFileAttributeName.equalsIgnoreCase(att.getSpec().getName())) {
                if (att.getValue() != null) {
                    content = att.getValue();
                }
            } else if (RtuMessageConstant.ACTIVATION_DATE.equalsIgnoreCase(att.getSpec().getName())) {
                if (att.getValue() != null) {
                    activationDate = att.getValue();
                }
            }
        }


        builder.append("<IncludedFile>" + content + "</IncludedFile>");

        if (activationDate != null) {
            builder.append("<ActivationDate>" + activationDate + "</ActivationDate>");
        }


        // c. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");
        return builder.toString();
    }

}