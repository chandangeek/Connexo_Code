package com.energyict.protocolimpl.utils;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

/**
 * @author jme
 *
 */
public final class MessagingTools {

	private MessagingTools() {
		// Hide the constructor of this util class
	}

	public static String getContentOfAttribute(MessageEntry messageEntry, String tagName) {
		if ((tagName != null) && (!tagName.isEmpty())) {
			String messageContent = messageEntry.getContent();
			String startingTag = tagName + "=\"";
			if (messageContent.contains(startingTag)) {
				int begin = messageContent.indexOf(startingTag) + tagName.length() + 2;
				if (begin <= messageContent.length()) {
					String value = messageContent.substring(begin);
					int end = value.indexOf('"');
					if ((end != -1) && (end <= value.length())) {
						return value.substring(0, end);
					}
				}
			}
		}
		return null;
	}

    public static MessageValueSpec getEmptyMessageValueSpec() {
	    return new MessageValueSpec(" ");
    }

    public static MessageTagSpec getAttributesOnlyMessageTagSpec(String tagName) {
        MessageTagSpec messageTagSpec = new MessageTagSpec(tagName);
        messageTagSpec.add(getEmptyMessageValueSpec());
        return messageTagSpec;
    }

}
