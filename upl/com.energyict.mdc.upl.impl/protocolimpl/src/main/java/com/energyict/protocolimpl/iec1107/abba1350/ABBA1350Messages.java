/**
 * ABBA1350Messages.java
 *
 * Created on 19-nov-2008, 13:15:45 by jme
 *
 */
package com.energyict.protocolimpl.iec1107.abba1350;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jme
 *
 */
public class ABBA1350Messages {

	private static final int DEBUG = 0;

	private ABBA1350 abba1350 = null;
    private static final ABBA1350MessageType SPC_MESSAGE = new ABBA1350MessageType("SPC_DATA", 4, 285 * 2, "Upload 'Switch Point Clock' settings (Class 4)");
    private static final ABBA1350MessageType SPCU = new ABBA1350MessageType("SPCU_DATA", 34, 285 * 2, "Upload 'Switch Point Clock Update' settings (Class 32)");

	public ABBA1350Messages(ABBA1350 abba1350) {
		this.abba1350 = abba1350;
	}

//--------------------------------------------------------------------------------------------------------------------------

	public List getMessageCategories() {
		sendDebug("getMessageCategories()");

		List<MessageCategorySpec> theCategories = new ArrayList<>();
        MessageCategorySpec cat = new MessageCategorySpec("'Switch Point Clock' Messages");

        cat.addMessageSpec(addBasicMsg(SPC_MESSAGE, false));
        cat.addMessageSpec(addBasicMsg(SPCU, false));

        theCategories.add(cat);
        return theCategories;
	}

	public void applyMessages(List messageEntries) {
		sendDebug("applyMessages(List messageEntries)");
        if (DEBUG >= 2) {
    		Iterator it = messageEntries.iterator();
            while (it.hasNext()) {
                MessageEntry messageEntry = (MessageEntry)it.next();
                sendDebug(messageEntry.toString());
            }
        }
	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		sendDebug("queryMessage(MessageEntry messageEntry)");

		try {
			if (isThisMessage(messageEntry, SPC_MESSAGE)) {
				sendDebug("************************* " + SPC_MESSAGE.getDisplayName() + " *************************");
				writeClassSettings(messageEntry, SPC_MESSAGE);
				return MessageResult.createSuccess(messageEntry);
			}
			else if (isThisMessage(messageEntry, SPCU)) {
				sendDebug("************************* " + SPCU + " *************************");
				writeClassSettings(messageEntry, SPCU);
				return MessageResult.createSuccess(messageEntry);
			}

		}
		catch(IOException e) {
			sendDebug(e.getMessage());
		}

		return MessageResult.createFailed(messageEntry);
	}

	public String writeValue(MessageValue value) {
		sendDebug("writeValue(MessageValue value)");
		return value.getValue();
	}

	public String writeMessage(Message msg) {
		sendDebug("writeMessage(Message msg)");
		return msg.write(this.abba1350);
	}

	public String writeTag(MessageTag tag) {
		sendDebug("writeTag(MessageTag tag)");

        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append( tag.getName() );

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if (att.getValue()==null || att.getValue().isEmpty()) {
	            continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag()) {
	            builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if (value==null || value.isEmpty()) {
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

//--------------------------------------------------------------------------------------------------------------------------



    private static MessageSpec addBasicMsg(ABBA1350MessageType abba1350MessageType, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(abba1350MessageType.getDisplayName(), advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(abba1350MessageType.getTagName());
        msgSpec.add(tagSpec);
        return msgSpec;
    }


	private void writeClassSettings(MessageEntry messageEntry, ABBA1350MessageType messageType) throws IOException {
		final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
		final int MAX_PACKETSIZE = 48;

		String returnValue;
		String iec1107Command;

		int first = 0;
		int last;
		int offset;
		int length;

		if (abba1350.getISecurityLevel() < 1) {
			throw new IOException("Message " + messageType.getDisplayName() + " needs at least security level 1. Current level: " + abba1350.getISecurityLevel());
		}

		String message = ABBA1350Utils.getXMLAttributeValue(messageType.getTagName(), messageEntry.getContent());
		message = ABBA1350Utils.cleanAttributeValue(message);
		sendDebug("Cleaned attribute value: " + message);
		if (message.length() != messageType.getLength()) {
			throw new IOException("Wrong length !!! Length should be " + messageType.getLength() + " but was " + message.length());
		}
		if (!ABBA1350Utils.containsOnlyTheseCharacters(message.toUpperCase(), "0123456789ABCDEF")) {
			throw new IOException("Invalid characters in message. Only the following characters are allowed: '0123456789ABCDEFabcdef'");
		}

		do {
			last = first + MAX_PACKETSIZE;
			if (last >= message.length()) {
				last = message.length();
			}
			String rawdata = message.substring(first, last);

			length = rawdata.length() / 2;
			offset = first / 2;

			iec1107Command = "C" + ProtocolUtils.buildStringHex(messageType.getClassnr(), 2);
			iec1107Command += ProtocolUtils.buildStringHex(length, 4);
			iec1107Command += ProtocolUtils.buildStringHex(offset, 4);
			iec1107Command += "(" + rawdata + ")";

			sendDebug(	" classNumber: " + ProtocolUtils.buildStringHex(messageType.getClassnr(), 2) +
						" First: " + ProtocolUtils.buildStringHex(first, 4) +
						" Last: " + ProtocolUtils.buildStringHex(last, 4) +
						" Offset: " + ProtocolUtils.buildStringHex(offset, 4) +
						" Length: " + ProtocolUtils.buildStringHex(length, 4) +
						" Sending iec1107Command: [ W1." + iec1107Command + " ]"
			);

			returnValue = abba1350.getFlagIEC1107Connection().sendRawCommandFrameAndReturn(WRITE1, iec1107Command.getBytes());
			if (returnValue != null) {
				throw new IOException(" Wrong response on iec1107Command: W1." + iec1107Command + "] expected 'null' but received " + ProtocolUtils.getResponseData(returnValue.getBytes()));
			}
			first = last;

		} while (first < message.length());

	}

	private static boolean isThisMessage(MessageEntry messageEntry, ABBA1350MessageType messagetype) {
		return (ABBA1350Utils.getXMLAttributeValue(messagetype.getTagName(), messageEntry.getContent()) != null);
	}

	private void sendDebug(String string) {
		if (DEBUG >= 1) {
			this.abba1350.sendDebug(string);
		}
	}

}
