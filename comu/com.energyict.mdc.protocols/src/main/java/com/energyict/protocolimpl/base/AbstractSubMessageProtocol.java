/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageElementSpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jme
 *
 */
public abstract class AbstractSubMessageProtocol implements SubMessageProtocol {

	private List<String> supportedMessageTags = new ArrayList<String>();

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
		for (Iterator iterator = messageSpec.getElements().iterator(); iterator.hasNext();) {
			MessageElementSpec messageElementSpec = (MessageElementSpec) iterator.next();
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
    	StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append( tag.getName() );

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue()==null) || (att.getValue().length()==0)) {
				continue;
			}
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator<MessageElement> it = tag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = it.next();
            if (elt.isTag()) {
				buf.append( writeTag((MessageTag)elt) );
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if ((value==null) || (value.length()==0)) {
					return "";
				}
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append( tag.getName() );
        buf.append(">");

        return buf.toString();
	}

	public String writeValue(MessageValue value) {
		return value.getValue();
	}

	/**
	 * @param messageEntry
	 * @return
	 */
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
     * Generate a {@link MessageSpec}, that can be added to the list of supported messages
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

	/**
	 * @param tag
	 * @param messageEntry
	 * @return
	 */
	protected boolean isMessageTag(String tag, MessageEntry messageEntry) {
		return (messageEntry.getContent().indexOf("<" + tag) >= 0);
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
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        for(String attribute : attributes){
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
