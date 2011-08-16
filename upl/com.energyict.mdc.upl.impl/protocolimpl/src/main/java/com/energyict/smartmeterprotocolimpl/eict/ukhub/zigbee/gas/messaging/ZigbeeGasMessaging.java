package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.mdw.core.*;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:55
 */
public class ZigbeeGasMessaging extends GenericMessaging implements TimeOfUseMessaging {

    private final ZigbeeMessageExecutor messageExecutor;

    public ZigbeeGasMessaging(final ZigbeeMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    public void applyMessages(List messageEntries) throws IOException {
        // Nothing to do here
    }

    private TimeOfUseMessageBuilder messageBuilder = null;

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(ProtocolMessageCategories.getPricingInformationCategory());
        categories.add(ProtocolMessageCategories.getChangeOfTenancyCategory());
        categories.add(ProtocolMessageCategories.getChangeOfSupplierCategory());
        return categories;
    }

    public boolean needsName() {
        return true;
    }

    public boolean supportsCodeTables() {
        return true;
    }

    public boolean supportsUserFiles() {
        return false;
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.UserFile} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsUserFiles()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return false;   // we need to inline the userFile
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.Code} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsCodeTables()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a codeTable ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsCodeTableReferences() {
        return false;   // we need to inline the codeTable
    }

    /**
     * Indicates whether the content of the {@link com.energyict.mdw.core.UserFile} of the {@link com.energyict.mdw.core.Code} must be zipped when it is inlined in the
     * RtuMessage. This is only taken into account when {@link #supportsCodeTableReferences()} or {@link #supportsUserFileReferences()} is false.</br>
     * <b><u>NOTE:</u> If the content is zipped, then Base64 Encoding is also applied!</b>
     *
     * @return true if the content needs to be zipped, false otherwise
     */
    public boolean zipContent() {
        return true;
    }

    /**
     * Indicate whether the content of the {@link com.energyict.mdw.core.UserFile} or {@link com.energyict.mdw.core.Code} must be Base64 Encoded.
     * <b><u>NOTE:</u> Base64 encoding will be automatically applied if {@link #zipContent()} returns true</b>
     *
     * @return true if the content needs to be Base64 encoded, false otherwise
     */
    public boolean encodeContentToBase64() {
        return false;   // gets neglected because zipContent() is true
    }

    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        if (messageBuilder == null) {
            this.messageBuilder = new ZigbeeTimeOfUseMessageBuilder();
        }
        return messageBuilder;
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
