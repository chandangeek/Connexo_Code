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
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;

/**
 * @author jme
 *
 */
public class A1440Messages implements MessageProtocol {

	//	private static final A1440MessageType SPC_MESSAGE = new A1440MessageType("SPC_DATA", 4, 285 * 2, "Upload 'Switch Point Clock' settings (Class 4)");
	//	private static final A1440MessageType SPCU_MESSAGE = new A1440MessageType("SPCU_DATA", 34, 285 * 2, "Upload 'Switch Point Clock Update' settings (Class 32)");

	private static final A1440MessageType CONTACTOR_CLOSE = new A1440MessageType("CONTACTOR_CLOSE", 411, 0, "Contactor close");
	private static final A1440MessageType CONTACTOR_ARM = 	new A1440MessageType("CONTACTOR_ARM", 411, 0, "Contactor arm");
	private static final A1440MessageType CONTACTOR_OPEN = 	new A1440MessageType("CONTACTOR_OPEN", 411, 0, "Contactor open");

	private static final A1440MessageType DEMAND_RESET = new A1440MessageType("DEMAND_RESET", 0, 0, "Demand reset");
	private static final A1440MessageType POWER_OUTAGE_RESET = new A1440MessageType("POWER_OUTAGE_RESET", 0, 0, "Power outage counter reset");
	private static final A1440MessageType POWER_QUALITY_RESET = new A1440MessageType("POWER_QUALITY_RESET", 0, 0, "Power quality counters reset");
	private static final A1440MessageType ERROR_STATUS_RESET = new A1440MessageType("ERROR_STATUS_RESET", 0, 0, "Error status reset");

	private A1440 a1440 = null;

	public A1440Messages(A1440 a1440) {
		this.a1440 = a1440;
	}

	public List getMessageCategories() {
		List theCategories = new ArrayList();
		//MessageCategorySpec catTimeTable = new MessageCategorySpec("'Switch Point Clock' Messages");
		//catTimeTable.addMessageSpec(addBasicMsg(SPC_MESSAGE, false));
		//catTimeTable.addMessageSpec(addBasicMsg(SPCU_MESSAGE, false));

		MessageCategorySpec catContactor = new MessageCategorySpec("'Contacor' Messages");
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_CLOSE, false));
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_ARM, false));
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_OPEN, false));

		MessageCategorySpec catResetMessages = new MessageCategorySpec("'Reset' Messages");
		catResetMessages.addMessageSpec(addBasicMsg(DEMAND_RESET, false));
		catResetMessages.addMessageSpec(addBasicMsg(POWER_OUTAGE_RESET, false));
		catResetMessages.addMessageSpec(addBasicMsg(POWER_QUALITY_RESET, false));
		catResetMessages.addMessageSpec(addBasicMsg(ERROR_STATUS_RESET, false));

		//theCategories.add(catTimeTable);
		theCategories.add(catContactor);
		theCategories.add(catResetMessages);
		return theCategories;
	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		try {
			//			if (isThisMessage(messageEntry, SPC_MESSAGE)) {
			//				getLogger().fine("************************* " + SPC_MESSAGE.getDisplayName() + " *************************");
			//				A1440MeterclassWriter classWriter = new A1440MeterclassWriter(getA1440());
			//				classWriter.writeClassSettings(messageEntry, SPC_MESSAGE);
			//				return MessageResult.createSuccess(messageEntry);
			//			}
			//
			//			if (isThisMessage(messageEntry, SPCU_MESSAGE)) {
			//				getLogger().fine("************************* " + SPCU_MESSAGE + " *************************");
			//				A1440MeterclassWriter classWriter = new A1440MeterclassWriter(getA1440());
			//				classWriter.writeClassSettings(messageEntry, SPCU_MESSAGE);
			//				return MessageResult.createSuccess(messageEntry);
			//			}

			if (isThisMessage(messageEntry, CONTACTOR_ARM)) {
				doArmContactor();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, CONTACTOR_CLOSE)) {
				doCloseContactor();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, CONTACTOR_OPEN)) {
				doOpenContactor();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, DEMAND_RESET)) {
				doDemandReset();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, POWER_OUTAGE_RESET)) {
				doPowerOutageReset();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, POWER_QUALITY_RESET)) {
				doPowerQualityReset();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, ERROR_STATUS_RESET)) {
				doErrorStatusReset();
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

	public void applyMessages(List messageEntries) {}

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

	private static MessageSpec addBasicMsg(A1440MessageType abba220MessageType, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(abba220MessageType.getDisplayName(), advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(abba220MessageType.getTagName());
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private static boolean isThisMessage(MessageEntry messageEntry, A1440MessageType messagetype) {
		return (A1440Utils.getXMLAttributeValue(messagetype.getTagName(), messageEntry.getContent()) != null);
	}

	private Logger getLogger() {
		return getA1440().getLogger();
	}

	private A1440 getA1440() {
		return this.a1440;
	}

	public void doOpenContactor() throws IOException {
		System.out.println("Received contactor ARM message");
		A1440ContactorController cc = new A1440ContactorController(this.a1440);
		cc.doDisconnect();
	}

	public void doCloseContactor() throws IOException {
		System.out.println("Received contactor CONTACTOR_CLOSE");
		A1440ContactorController cc = new A1440ContactorController(this.a1440);
		cc.doConnect();
	}

	public void doArmContactor() throws IOException {
		System.out.println("Received contactor CONTACTOR_ARM");
		A1440ContactorController cc = new A1440ContactorController(this.a1440);
		cc.doArm();
	}

	public void doDemandReset() throws IOException {
		System.out.println("Received DEMAND_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.DEMAND_RESET_REGISTER , "");
	}

	public void doErrorStatusReset() throws IOException {
		System.out.println("Received ERROR_STATUS_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.ERROR_STATUS_REGISTER , "");
	}

	public void doPowerQualityReset() throws IOException {
		System.out.println("Received POWER_QUALITY_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.POWER_QUALITY_RESET_REGISTER , "");
	}

	public void doPowerOutageReset() throws IOException {
		System.out.println("Received POWER_OUTAGE_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.POWER_OUTAGE_RESET_REGISTER , "");
	}
}
