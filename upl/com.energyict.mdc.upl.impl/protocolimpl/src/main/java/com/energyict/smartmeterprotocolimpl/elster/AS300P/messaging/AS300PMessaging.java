package com.energyict.smartmeterprotocolimpl.elster.AS300P.messaging;

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
 * Date: 8-aug-2011
 * Time: 14:32:04
 */
public class AS300PMessaging extends GenericMessaging implements MessageProtocol {

    private final AS300PMessageExecutor messageExecutor;

    public AS300PMessaging(final AS300PMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        categories.add(getTariffMessageCategorySpec());
        categories.add(getPricingInformationMessageCategorySpec());
        categories.add(getCoTSMessageCategorySpec());
        categories.add(getConnectDisconnectMessageCategorySpec());
        categories.add(getFirmwareUpgradeMessageCategorySpec());
        categories.add(getSecurityMessageCategorySpec());
        return categories;
    }

    private MessageCategorySpec getTariffMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec(RtuMessageCategoryConstants.TIMEOFUSE);
        pricingInformationCategory.addMessageSpec(addBasicMsgWithAttributes("Send new TOU tariff", RtuMessageConstant.TOU_SEND_NEW_TARIFF, false, RtuMessageConstant.TOU_TARIFF_USER_FILE));
        return pricingInformationCategory;
    }

    private MessageCategorySpec getSecurityMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec(RtuMessageCategoryConstants.SECURITY);
        pricingInformationCategory.addMessageSpec(addMsgWithValues("Set Engineer menu PIN", RtuMessageConstant.SET_ENGINEER_PIN, false, false, RtuMessageConstant.ENGINEER_PIN, RtuMessageConstant.ENGINEER_PIN_TIMEOUT, RtuMessageConstant.ACTIVATION_DATE));
        return pricingInformationCategory;
    }

    private MessageCategorySpec getPricingInformationMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec(RtuMessageCategoryConstants.PRICING_INFORMATION);
        pricingInformationCategory.addMessageSpec(addBasicMsgWithAttributes("Send new price matrix", RtuMessageConstant.SEND_NEW_PRICE_MATRIX, false, RtuMessageConstant.PRICE_MATRIX_USER_FILE));
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set standing charge", RtuMessageConstant.SET_STANDING_CHARGE, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.STANDING_CHARGE));
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Set currency", RtuMessageConstant.SET_CURRENCY, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.CURRENCY));
        return pricingInformationCategory;
    }

    private MessageCategorySpec getConnectDisconnectMessageCategorySpec() {
        MessageCategorySpec connectDisconnectCat = new MessageCategorySpec("Connect/disconnect");
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Disconnect Control - Reconnect", RtuMessageConstant.DISCONNECT_CONTROL_RECONNECT, false, false));
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Disconnect Control - Disconnect", RtuMessageConstant.DISCONNECT_CONTROL_DISCONNECT, false, false));
        connectDisconnectCat.addMessageSpec(addMsgWithValues("Disconnect Control - Write control mode", RtuMessageConstant.SET_DISCONNECT_CONTROL_MODE, false, true, RtuMessageConstant.CONTROL_MODE));
        return connectDisconnectCat;
    }

    private MessageCategorySpec getFirmwareUpgradeMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec("Firmware");
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Upgrade firmware", RtuMessageConstant.FIRMWARE_UPGRADE, false, RtuMessageConstant.ACTIVATION_DATE, RtuMessageConstant.FIRMWARE_USER_FILE));
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
                        "Change Of Supplier A+", RtuMessageConstant.CHANGE_OF_SUPPLIER_IMPORT_ENERGY,
                        false,
                        RtuMessageConstant.ACTIVATION_DATE,
                        RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED
                ));
        coTSMessageCat.addMessageSpec(
                addMsgWithValuesAndOptionalValue(
                        "Change Of Supplier A-", RtuMessageConstant.CHANGE_OF_SUPPLIER_EXPORT_ENERGY,
                        false,
                        RtuMessageConstant.ACTIVATION_DATE,
                        RtuMessageConstant.TENANT_REFERENCE, RtuMessageConstant.SUPPLIER_REFERENCE, RtuMessageConstant.SUPPLIER_ID, RtuMessageConstant.SCRIPT_EXECUTED
                ));
        return coTSMessageCat;
    }

    protected MessageSpec addMsgWithValues(final String description, final String tagName, final boolean advanced, boolean required, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, required));
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

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        //currently nothing to implement
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
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

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf     The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuilder buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }
}
