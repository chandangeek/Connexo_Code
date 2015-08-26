package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.messaging;

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
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessaging extends GenericMessaging implements MessageProtocol {

    private InHomeDisplayMessageExecutor messageExecutor;

    public InHomeDisplayMessaging(InHomeDisplayMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do here
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.queryMessage(messageEntry);
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(getFirmwareUpgradeMessageCategorySpec());
        return categories;
    }

    private MessageCategorySpec getFirmwareUpgradeMessageCategorySpec() {
        MessageCategorySpec pricingInformationCategory = new MessageCategorySpec("Firmware");
        pricingInformationCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Upgrade firmware", RtuMessageConstant.FIRMWARE_UPGRADE, false, RtuMessageConstant.ACTIVATE_DATE, RtuMessageConstant.FIRMWARE));
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

    @Override
    public String writeTag(final MessageTag msgTag) {
        if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.FIRMWARE_UPGRADE)) {
            return writeUserFileTag(msgTag, RtuMessageConstant.FIRMWARE);
        } else if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE)) {
            return writeUserFileTag(msgTag, RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_FILE_ID);
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