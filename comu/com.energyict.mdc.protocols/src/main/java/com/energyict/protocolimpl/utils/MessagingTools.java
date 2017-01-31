/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.utils;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

/**
 * @author jme
 *
 */
public final class MessagingTools {

	private MessagingTools() {
		// Hide the constructor of this util class
	}

	/**
	 * @param messageEntry
	 * @param tagName
	 * @return
	 */
	public static String getContentOfAttribute(MessageEntry messageEntry, String tagName) {
		if ((tagName != null) && (tagName.length() > 0)) {
			String messageContent = messageEntry.getContent();
			String startingTag = tagName + "=\"";
			if (messageContent.indexOf(startingTag) != -1) {
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
        MessageValueSpec valueSpec = new MessageValueSpec();
        valueSpec.setValue(" ");
        return valueSpec;
    }

    public static MessageTagSpec getAttributesOnlyMessageTagSpec(String tagName) {
        MessageTagSpec messageTagSpec = new MessageTagSpec(tagName);
        messageTagSpec.add(getEmptyMessageValueSpec());
        return messageTagSpec;
    }

}
