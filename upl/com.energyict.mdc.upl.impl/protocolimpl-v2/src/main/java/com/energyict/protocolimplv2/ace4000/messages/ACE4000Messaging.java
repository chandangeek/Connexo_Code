package com.energyict.protocolimplv2.ace4000.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ACE4000Messaging extends ProtocolMessages {
    private final ACE4000MessageExecutor messageExecutor;
    private List supportedMessages;

    public ACE4000Messaging(ACE4000MessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
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

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        return categories;
    }

    protected MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithValues(final String keyId, final String tagName, final boolean advanced, String... attributes) {
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attributes) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Object o1 : msgTag.getAttributes()) {
            MessageAttribute att = (MessageAttribute) o1;
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Object o : msgTag.getSubElements()) {
            MessageElement elt = (MessageElement) o;
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    @Override
    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();
            //Configuration messages
            supportedMessages.add(ACE4000ConfigurationMessages.SendShortDisplayMessage);
            supportedMessages.add(ACE4000ConfigurationMessages.SendLongDisplayMessage);
            supportedMessages.add(ACE4000ConfigurationMessages.DisplayMessage);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureLCDDisplay);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureLoadProfileDataRecording);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureSpecialDataMode);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureMaxDemandSettings);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureConsumptionLimitationsSettings);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureEmergencyConsumptionLimitation);
            supportedMessages.add(ACE4000ConfigurationMessages.ConfigureTariffSettings);

            //General messages
            supportedMessages.add(ACE4000GeneralMessages.FirmwareUpgrade);
            supportedMessages.add(ACE4000GeneralMessages.Connect);
            supportedMessages.add(ACE4000GeneralMessages.Disconnect);
        }


        return supportedMessages;
    }
}
