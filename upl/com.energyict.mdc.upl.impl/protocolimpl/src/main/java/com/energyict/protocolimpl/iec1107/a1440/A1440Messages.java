/**
 * A1440Messages.java
 * 
 * Created on 19-nov-2008, 13:15:45 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.a1440;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

/**
 * @author jme
 *
 */
public class A1440Messages implements MessageProtocol {

	private static final A1440MessageType SPC_MESSAGE = new A1440MessageType("SPC_DATA", 4, 285 * 2, "Upload 'Switch Point Clock' settings (Class 4)");
	private static final A1440MessageType SPCU = new A1440MessageType("SPCU_DATA", 34, 285 * 2, "Upload 'Switch Point Clock Update' settings (Class 32)");

	private static final A1440MessageType CONTACTOR_CLOSE = new A1440MessageType("CONTACTOR_CLOSE", 411, 0, "Contactor close");
	private static final A1440MessageType CONTACTOR_ARM = 	new A1440MessageType("CONTACTOR_ARM", 411, 0, "Contactor arm");
	private static final A1440MessageType CONTACTOR_OPEN = 	new A1440MessageType("CONTACTOR_OPEN", 411, 0, "Contactor open");

	private A1440 a1440 = null;

	public A1440Messages(A1440 a1440) {
		this.a1440 = a1440;
	}

	public List getMessageCategories() {
		List theCategories = new ArrayList();
		MessageCategorySpec catTimeTable = new MessageCategorySpec("'Switch Point Clock' Messages");
		catTimeTable.addMessageSpec(addBasicMsg(SPC_MESSAGE, false));
		catTimeTable.addMessageSpec(addBasicMsg(SPCU, false));

		MessageCategorySpec catContactor = new MessageCategorySpec("'Contacor' Messages");
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_CLOSE, false));
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_ARM, false));
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_OPEN, false));

		theCategories.add(catTimeTable);
		theCategories.add(catContactor);
		return theCategories;
	}

	public void applyMessages(List messageEntries) {
		Iterator it = messageEntries.iterator();
		while(it.hasNext()) {
			MessageEntry messageEntry = (MessageEntry)it.next();
			getLogger().finest(messageEntry.toString());
		}
	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		try {
			if (isThisMessage(messageEntry, SPC_MESSAGE)) {
				getLogger().fine("************************* " + SPC_MESSAGE.getDisplayName() + " *************************");
				writeClassSettings(messageEntry, SPC_MESSAGE);
				return MessageResult.createSuccess(messageEntry);
			}
			else if (isThisMessage(messageEntry, SPCU)) {
				getLogger().fine("************************* " + SPCU + " *************************");
				writeClassSettings(messageEntry, SPCU);
				return MessageResult.createSuccess(messageEntry);
			} else if (isThisMessage(messageEntry, CONTACTOR_ARM)) {
				System.out.println("Received contactor CONTACTOR_ARM");
				A1440ContactorController cc = new A1440ContactorController(this.a1440);
				cc.doArm();
				return MessageResult.createSuccess(messageEntry);
			} else if (isThisMessage(messageEntry, CONTACTOR_CLOSE)) {
				System.out.println("Received contactor CONTACTOR_CLOSE");
				A1440ContactorController cc = new A1440ContactorController(this.a1440);
				try {
					cc.doConnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return MessageResult.createSuccess(messageEntry);
			} else if (isThisMessage(messageEntry, CONTACTOR_OPEN)) {
				System.out.println("Received contactor ARM message");
				A1440ContactorController cc = new A1440ContactorController(this.a1440);
				cc.doDisconnect();
				return MessageResult.createSuccess(messageEntry);
			}

		}
		catch(IOException e) {
			e.printStackTrace();
		}

		return MessageResult.createFailed(messageEntry);
	}

	public String writeValue(MessageValue value) {
		return value.getValue();
	}

	public String writeMessage(Message msg) {
		return msg.write(this.a1440);
	}

	public String writeTag(MessageTag tag) {
		StringBuffer buf = new StringBuffer();

		// a. Opening tag
		buf.append("<");
		buf.append( tag.getName() );

		// b. Attributes
		for (Iterator it = tag.getAttributes().iterator(); it.hasNext();) {
			MessageAttribute att = (MessageAttribute)it.next();
			if ((att.getValue()==null) || (att.getValue().length()==0)) {
				continue;
			}
			buf.append(" ").append(att.getSpec().getName());
			buf.append("=").append('"').append(att.getValue()).append('"');
		}
		buf.append(">");

		// c. sub elements
		for (Iterator it = tag.getSubElements().iterator(); it.hasNext();) {
			MessageElement elt = (MessageElement)it.next();
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
		buf.append("\n\n</");
		buf.append( tag.getName() );
		buf.append(">");

		return buf.toString();

	}

	//--------------------------------------------------------------------------------------------------------------------------



	private static MessageSpec addBasicMsg(A1440MessageType abba220MessageType, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(abba220MessageType.getDisplayName(), advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(abba220MessageType.getTagName());
		msgSpec.add(tagSpec);
		return msgSpec;
	}


	private void writeClassSettings(MessageEntry messageEntry, A1440MessageType messageType) throws IOException {
		final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
		final int MAX_PACKETSIZE = 48;

		String returnValue = "";
		String iec1107Command = "";

		int first = 0;
		int last = 0;
		int offset = 0;
		int length = 0;

		if (this.a1440.getISecurityLevel() < 1) {
			throw new IOException("Message " + messageType.getDisplayName() + " needs at least security level 1. Current level: " + this.a1440.getISecurityLevel());
		}

		String message = A1440Utils.getXMLAttributeValue(messageType.getTagName(), messageEntry.getContent());
		message = A1440Utils.cleanAttributeValue(message);
		if (message.length() != messageType.getLength()) {
			throw new IOException("Wrong length !!! Length should be " + messageType.getLength() + " but was " + message.length());
		}
		if (!A1440Utils.containsOnlyTheseCharacters(message.toUpperCase(), "0123456789ABCDEF")) {
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

			getLogger().finest(	" classNumber: " + ProtocolUtils.buildStringHex(messageType.getClassnr(), 2) +
					" First: " + ProtocolUtils.buildStringHex(first, 4) +
					" Last: " + ProtocolUtils.buildStringHex(last, 4) +
					" Offset: " + ProtocolUtils.buildStringHex(offset, 4) +
					" Length: " + ProtocolUtils.buildStringHex(length, 4) +
					" Sending iec1107Command: [ W1." + iec1107Command + " ]"
			);

			returnValue = this.a1440.getFlagIEC1107Connection().sendRawCommandFrameAndReturn(WRITE1, iec1107Command.getBytes());
			if (returnValue != null) {
				throw new IOException(" Wrong response on iec1107Command: W1." + iec1107Command + "] expected 'null' but received " + ProtocolUtils.getResponseData(returnValue.getBytes()));
			}
			first = last;

		} while (first < message.length());

	}

	private static boolean isThisMessage(MessageEntry messageEntry, A1440MessageType messagetype) {
		return (A1440Utils.getXMLAttributeValue(messagetype.getTagName(), messageEntry.getContent()) != null);
	}

	private Logger getLogger() {
		return getA1440().getLogger();
	}

	public A1440 getA1440() {
		return this.a1440;
	}
}
