package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.messages.ProtocolMessageSpecifications;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 6/11/2014 - 17:12
 */
public class NXT4Messages implements MessageProtocol {

    private final NXT4 meterProtocol;

    public NXT4Messages(NXT4 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

        MessageCategorySpec catResetMessages = new MessageCategorySpec(RtuMessageCategoryConstants.DEMANDRESET);
        catResetMessages.addMessageSpec(ProtocolMessageSpecifications.getDemandResetMessageSpecification());
        theCategories.add(catResetMessages);
        return theCategories;
    }

    public MessageResult queryMessage(MessageEntry messageEntry) {
        try {
            getLogger().fine("Received message with tracking ID " + messageEntry.getTrackingId());

            if (isThisMessage(messageEntry, RtuMessageConstant.DEMAND_RESET)) {
                doDemandReset();
                return MessageResult.createSuccess(messageEntry);
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
        }

        return MessageResult.createFailed(messageEntry);
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public String writeMessage(Message msg) {
        return msg.write(this.meterProtocol);
    }

    public void applyMessages(List messageEntries) {
    }

    public String writeTag(MessageTag tag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(tag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("\n\n</");
        builder.append(tag.getName());
        builder.append(">");

        return builder.toString();

    }

    private static boolean isThisMessage(MessageEntry messageEntry, String messageTag) {
        return messageEntry.getContent().contains(messageTag);
    }

    private Logger getLogger() {
        return getMeterProtocol().getLogger();
    }

    private NXT4 getMeterProtocol() {
        return this.meterProtocol;
    }

    /**
     * After receiving the 'Demand Reset' command the meter executes a demand
     * reset by doing a snap shot of all energy and demand registers.
     *
     * @throws IOException
     */
    public void doDemandReset() throws IOException {
        getLogger().fine("Received DEMAND_RESET");
        getMeterProtocol().getRegistry().setRegister(NXT4Registry.DEMAND_RESET_REGISTER, "");
    }
}
