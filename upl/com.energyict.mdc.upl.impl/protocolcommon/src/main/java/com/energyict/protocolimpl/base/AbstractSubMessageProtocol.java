package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageElementSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jme
 *
 */
public abstract class AbstractSubMessageProtocol implements SubMessageProtocol {

	private List<String> supportedMessageTags = new ArrayList<>();

	public List<String> getSupportedMessageTags() {
		return supportedMessageTags;
	}

	public void addSupportedMessageTag(String tag) {
		getSupportedMessageTags().add(tag);
	}

	public boolean canHandleMessage(MessageEntry messageEntry) {
		for (String tag : getSupportedMessageTags()) {
			if (isMessageTag(tag, messageEntry)) {
				return true;
			}
		}
		return false;
	}

	public boolean canHandleMessage(MessageSpec messageSpec) {
		for (Iterator<MessageElementSpec> iterator = messageSpec.getElements().iterator(); iterator.hasNext();) {
			MessageElementSpec messageElementSpec = iterator.next();
			if (messageElementSpec.isTag()) {
				MessageTagSpec mts = (MessageTagSpec) messageElementSpec;
				if (canHandleMessage(mts.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean canHandleMessage(String messageTag) {
		for (String tag : getSupportedMessageTags()) {
			if (tag.equals(messageTag)) {
				return true;
			}
		}
		return false;
	}

	public String writeMessage(Message msg) {
		return msg.write(this);
	}

	public String writeTag(MessageTag tag) {
    	StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append( tag.getName() );

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue()==null) || (att.getValue().isEmpty())) {
				continue;
			}
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator<MessageElement> it = tag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = it.next();
            if (elt.isTag()) {
				builder.append( writeTag((MessageTag)elt) );
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if ((value==null) || (value.isEmpty())) {
					return "";
				}
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append( tag.getName() );
        builder.append(">");

        return builder.toString();
	}

	public String writeValue(MessageValue value) {
		return value.getValue();
	}

	protected String getMessageEntryContent(MessageEntry messageEntry) {
		String returnValue = "";
		if ((messageEntry != null) && (messageEntry.getContent() != null)) {
			String content = messageEntry.getContent();
			int firstPos = content.indexOf('>');
			int lastPosPos = content.lastIndexOf('<');
			if ((firstPos != -1) && (lastPosPos != -1) && ((firstPos + 1) < lastPosPos)) {
				returnValue = content.substring(firstPos + 1, lastPosPos);
			}
		}
		return returnValue;
	}

    /**
     * Generate a {@link MessageSpec}, that can be added to the list of supported messages.
     *
     * @param keyId
     * @param tagName
     * @param advanced
     * @return
     */
    protected MessageSpec createMessageSpec(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

	protected boolean isMessageTag(String tag, MessageEntry messageEntry) {
		return (messageEntry.getContent().contains("<" + tag));
	}

	public void applyMessages(List messageEntries) throws IOException {
		// TODO Auto-generated method stub
	}


    /**
     * Create a MessageSpec with an EMPTY value-field but with one or several attributes added
     *
     * @param keyId the KeyId of the message
     * @param tagName the xml tag name for this message
     * @param advanced an indication whether this is an advanced message
     * @param attributes a list of attributes which will be applied to the xml message
     * @return a messageSpecification
     */
    protected MessageSpec createValueMessage(String keyId, String tagName, boolean advanced, String... attributes){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);

        // We should add this messageSpec, otherwise the other attributeSpecs wont show up in eiserver. Bug??
        MessageValueSpec msgVal = new MessageValueSpec(" ");
        tagSpec.add(msgVal);
        for (String attribute : attributes) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
