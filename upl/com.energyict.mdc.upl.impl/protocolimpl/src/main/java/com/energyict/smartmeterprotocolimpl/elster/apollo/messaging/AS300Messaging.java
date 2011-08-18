package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 14:32:04
 */
public class AS300Messaging extends GenericMessaging implements MessageProtocol, TimeOfUseMessaging, FirmwareUpdateMessaging {

    private final AS300MessageExecutor messageExecutor;

    public AS300Messaging(final AS300MessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(ProtocolMessageCategories.getPricingInformationCategory());
        categories.add(ProtocolMessageCategories.getChangeOfTenancyCategory());
        categories.add(ProtocolMessageCategories.getChangeOfSupplierCategory());
        return categories;
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

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return new AS300TimeOfUseMessageBuilder();
    }

    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        TimeOfUseMessagingConfig config = new TimeOfUseMessagingConfig();
        config.setNeedsName(true);
        config.setSupportsUserFiles(true);
        config.setSupportsCodeTables(true);
        config.setZipContent(true);
        return config;
    }

    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        FirmwareUpdateMessagingConfig config = new FirmwareUpdateMessagingConfig();
        config.setSupportsUserFiles(true);
        return config;
    }

    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return new AS300FirmwareUpdateMessageBuilder();
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        if (msgTag.getName().equals(RtuMessageConstant.UPDATE_PRICING_INFORMATION)) {

            int userFileId = 0;
            for (Object maObject : msgTag.getAttributes()) {
                MessageAttribute ma = (MessageAttribute) maObject;
                if (ma.getSpec().getName().equals(RtuMessageConstant.UPDATE_PRICING_INFORMATION_USERFILE_ID)) {
                    if (ma.getValue() != null && ma.getValue().length() != 0) {
                        userFileId = Integer.valueOf(ma.getValue());
                    }
                }
            }

            StringBuilder builder = new StringBuilder();
            addOpeningTag(builder, msgTag.getName());
            builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

            // This will generate a message that will make the RtuMessageContentParser inline the file.
            builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(userFileId).append("\"");
            builder.append(" ").append(CREATEZIP_ATTRIBUTE_TAG).append("=\"true\"");
            builder.append("/>");

            builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            addClosingTag(builder, msgTag.getName());
            return builder.toString();
        } else {
            return super.writeTag(msgTag);
        }
    }
}
