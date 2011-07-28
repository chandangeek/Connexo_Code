package com.energyict.genericprotocolimpl.actarisace4000;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ACE4000Messages implements MessageProtocol {

    private ACE4000 ace4000;

    public ACE4000Messages(ACE4000 ace4000) {
        this.ace4000 = ace4000;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        MessageCategorySpec cat1 = new MessageCategorySpec("ACE4000 general messages");
        cat1.addMessageSpec(addBasicMsg("Read all events", "ReadEvents", false));
        cat1.addMessageSpec(addBasicMsgWith1Value("Read profile data from...", "ReadProfileData", false, "From date (dd/mm/yyyy hh:mm:ss)"));
        cat1.addMessageSpec(addBasicMsgWith2Values("Read profile data from... to...", "ReadProfileData", false, "From date (dd/mm/yyyy hh:mm:ss)", "To date (dd/mm/yyyy hh:mm:ss)"));
        categories.add(cat1);

        return categories;
    }

    protected MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith1Value(final String keyId, final String tagName, final boolean advanced, String attr1) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith2Values(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

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

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<ReadEvents")) {
                return iets(messageEntry);
            } else if (messageEntry.getContent().contains("<SimpleRestartDataLogging")) {
                return null;
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        } catch (Exception e) {
            ace4000.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }


    private MessageResult iets(MessageEntry messageEntry) throws IOException {
        ace4000.getLogger().info("Iets");
        int mode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (mode < 1 || mode > 3) {
            return MessageResult.createFailed(messageEntry);
        }

        //TODO

        return MessageResult.createSuccess(messageEntry);
    }
}