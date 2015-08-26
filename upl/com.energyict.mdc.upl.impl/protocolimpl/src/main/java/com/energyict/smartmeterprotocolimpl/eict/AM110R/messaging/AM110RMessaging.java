package com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20-jul-2011
 * Time: 17:13:53
 */
public class AM110RMessaging extends GenericMessaging implements MessageProtocol {

    private final AM110RMessageExecutor messageExecutor;

    public AM110RMessaging(final AM110RMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(getGPRSModemSetupCategorySpec());
        categories.add(getLogBookMessageCategorySpec());
        categories.add(getWebserverCategory());
        categories.add(getHanManagementMessageCategorySpec());
        categories.add(getFirmwareUpgradeMessageCategorySpec());
        return categories;
    }

    /**
     * MessageCategory containing relevant HAN management messages
     *
     * @return the newly created category
     */
    private MessageCategorySpec getHanManagementMessageCategorySpec() {
        MessageCategorySpec hanManagement = new MessageCategorySpec("HAN management");
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Create Han Network", RtuMessageConstant.CREATE_HAN_NETWORK, false, null));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Remove Han Network", RtuMessageConstant.REMOVE_HAN_NETWORK, false, null));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Backup ZigBee HAN parameters", RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS, false, null));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Restore ZigBee HAN parameters", RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS, false, null, RtuMessageConstant.RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Join ZigBee slave device", RtuMessageConstant.JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE, false, null, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_DEVICE_TYPE));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Remove ZigBee slave device", RtuMessageConstant.REMOVE_ZIGBEE_SLAVE, false, null, RtuMessageConstant.REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Remove all ZigBee slave devices", RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES, false, null));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Remove HAN mirror", RtuMessageConstant.REMOVE_ZIGBEE_MIRROR, false, RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_FORCE, RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_IEEE_ADDRESS));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Update HAN link key", RtuMessageConstant.UPDATE_HAN_LINK_KEY, false, null, RtuMessageConstant.UPDATE_HAN_LINK_KEY_SLAVE_IEEE_ADDRESS));
        hanManagement.addMessageSpec(addMsgWithValuesAndOptionalValue("Zigbee NCP firmware upgrade", RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE, false, RtuMessageConstant.ACTIVATE_DATE, RtuMessageConstant.FIRMWARE_USER_FILE));
        return hanManagement;
    }

    private MessageCategorySpec getLogBookMessageCategorySpec() {
        MessageCategorySpec logbookCategory = new MessageCategorySpec("Logbooks");
        logbookCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Read debug logbook", RtuMessageConstant.DEBUG_LOGBOOK, false, RtuMessageConstant.LOGBOOK_TO, RtuMessageConstant.LOGBOOK_FROM));
        logbookCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Read Elster specific logbook", RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK, false, RtuMessageConstant.LOGBOOK_TO, RtuMessageConstant.LOGBOOK_FROM));
        return logbookCategory;
    }

    private MessageCategorySpec getGPRSModemSetupCategorySpec() {
        MessageCategorySpec modemSetupCategory = new MessageCategorySpec("GPRS modem setup");
        modemSetupCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Configure connection mode", RtuMessageConstant.CONNECTION_MODE, false, null, RtuMessageConstant.CONNECT_MODE));
        modemSetupCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Configure wakeup parameters", RtuMessageConstant.WAKEUP_PARAMETERS, false, null, RtuMessageConstant.WAKEUP_CALLING_WINDOW_LENGTH, RtuMessageConstant.WAKEUP_IDLE_TIMEOUT));
        modemSetupCategory.addMessageSpec(addMsgWithValuesAndAllOptionalValues("Configure preferred network operator list", RtuMessageConstant.PREFERRED_NETWORK_OPERATORS_LIST, false,
                RtuMessageConstant.NETWORK_OPERATOR + "_1",
                RtuMessageConstant.NETWORK_OPERATOR + "_2",
                RtuMessageConstant.NETWORK_OPERATOR + "_3",
                RtuMessageConstant.NETWORK_OPERATOR + "_4",
                RtuMessageConstant.NETWORK_OPERATOR + "_5",
                RtuMessageConstant.NETWORK_OPERATOR + "_6",
                RtuMessageConstant.NETWORK_OPERATOR + "_7",
                RtuMessageConstant.NETWORK_OPERATOR + "_8",
                RtuMessageConstant.NETWORK_OPERATOR + "_9",
                RtuMessageConstant.NETWORK_OPERATOR + "_10"));
        return modemSetupCategory;
    }

    private MessageCategorySpec getFirmwareUpgradeMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec("Firmware");
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Upgrade firmware", RtuMessageConstant.FIRMWARE_UPGRADE, false, RtuMessageConstant.ACTIVATE_DATE, RtuMessageConstant.FIRMWARE_USER_FILE));
        return pricingInformationCategory;
    }

    protected MessageSpec addMsgWithValuesAndOptionalValue(final String description, final String tagName, final boolean advanced, String lastAttribute, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        if (lastAttribute != null) {
            tagSpec.add(new MessageAttributeSpec(lastAttribute, false));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addMsgWithValuesAndAllOptionalValues(final String description, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, false));
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
        if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.FIRMWARE_UPGRADE)) {
            return writeUserFileTag(msgTag, RtuMessageConstant.FIRMWARE_USER_FILE);
        } else if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE)) {
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
