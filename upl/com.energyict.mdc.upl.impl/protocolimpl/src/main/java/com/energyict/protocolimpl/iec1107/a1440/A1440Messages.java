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

	private static final A1440MessageType CONTACTOR_CLOSE = new A1440MessageType("CONTACTOR_CLOSE", 411, 0, "Contactor close");
	private static final A1440MessageType CONTACTOR_ARM = new A1440MessageType("CONTACTOR_ARM", 411, 0, "Contactor arm");
	private static final A1440MessageType CONTACTOR_OPEN = new A1440MessageType("CONTACTOR_OPEN", 411, 0, "Contactor open");

	private static final A1440MessageType DEMAND_RESET = new A1440MessageType("DEMAND_RESET", 0, 0, "Demand reset");
	private static final A1440MessageType POWER_OUTAGE_RESET = new A1440MessageType("POWER_OUTAGE_RESET", 0, 0, "Power outage counter reset");
	private static final A1440MessageType POWER_QUALITY_RESET = new A1440MessageType("POWER_QUALITY_RESET", 0, 0, "Power quality counters reset");
	private static final A1440MessageType ERROR_STATUS_RESET = new A1440MessageType("ERROR_STATUS_RESET", 0, 0, "Error status reset");

	private static final A1440MessageType REGISTERS_RESET = new A1440MessageType("REGISTERS_RESET", 0, 0, "Register data reset");
	private static final A1440MessageType LOAD_LOG_RESET = new A1440MessageType("LOAD_LOG_RESET", 0, 0, "Load profile and logfile reset");
	private static final A1440MessageType EVENT_LOG_RESET = new A1440MessageType("EVENT_LOG_RESET", 0, 0, "Event log register reset");

	private A1440 a1440 = null;

	public A1440Messages(A1440 a1440) {
		this.a1440 = a1440;
	}

	public List getMessageCategories() {
		List theCategories = new ArrayList();

		MessageCategorySpec catContactor = new MessageCategorySpec("'Contacor' Messages");
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_CLOSE, false));
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_ARM, false));
		catContactor.addMessageSpec(addBasicMsg(CONTACTOR_OPEN, false));

		MessageCategorySpec catResetMessages = new MessageCategorySpec("'Reset' Messages");
		catResetMessages.addMessageSpec(addBasicMsg(DEMAND_RESET, false));
		catResetMessages.addMessageSpec(addBasicMsg(POWER_OUTAGE_RESET, true));
		catResetMessages.addMessageSpec(addBasicMsg(POWER_QUALITY_RESET, true));
		catResetMessages.addMessageSpec(addBasicMsg(ERROR_STATUS_RESET, true));
		catResetMessages.addMessageSpec(addBasicMsg(REGISTERS_RESET, true));
		catResetMessages.addMessageSpec(addBasicMsg(LOAD_LOG_RESET, true));
		catResetMessages.addMessageSpec(addBasicMsg(EVENT_LOG_RESET, true));

		theCategories.add(catContactor);
		theCategories.add(catResetMessages);
		return theCategories;
	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		try {

			getLogger().fine("Received message with tracking ID " + messageEntry.getTrackingId());

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

			if (isThisMessage(messageEntry, REGISTERS_RESET)) {
				doRegistersReset();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, LOAD_LOG_RESET)) {
				doLoadLogReset();
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, EVENT_LOG_RESET)) {
				doEventLogReset();
				return MessageResult.createSuccess(messageEntry);
			}

		} catch (IOException e) {
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

	public void applyMessages(List messageEntries) {
	}

	public String writeTag(MessageTag tag) {
		StringBuffer buf = new StringBuffer();

		// a. Opening tag
		buf.append("<");
		buf.append(tag.getName());

		// b. Attributes
		for (Iterator it = tag.getAttributes().iterator(); it.hasNext();) {
			MessageAttribute att = (MessageAttribute) it.next();
			if ((att.getValue() == null) || (att.getValue().length() == 0)) {
				continue;
			}
			buf.append(" ").append(att.getSpec().getName());
			buf.append("=").append('"').append(att.getValue()).append('"');
		}
		buf.append(">");

		// c. sub elements
		for (Iterator it = tag.getSubElements().iterator(); it.hasNext();) {
			MessageElement elt = (MessageElement) it.next();
			if (elt.isTag()) {
				buf.append(writeTag((MessageTag) elt));
			} else if (elt.isValue()) {
				String value = writeValue((MessageValue) elt);
				if ((value == null) || (value.length() == 0)) {
					return "";
				}
				buf.append(value);
			}
		}

		// d. Closing tag
		buf.append("\n\n</");
		buf.append(tag.getName());
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

	/**
	 * This command tries to switch off (disconnect) the contactor in the A1440
	 * device.
	 * @throws IOException
	 */
	public void doOpenContactor() throws IOException {
		getLogger().fine("Received contactor ARM");
		A1440ContactorController cc = new A1440ContactorController(this.a1440);
		cc.doDisconnect();
	}

	/**
	 * This command tries to switch on (connect) the contactor in the A1440
	 * device.
	 * @throws IOException
	 */
	public void doCloseContactor() throws IOException {
		getLogger().fine("Received contactor CONTACTOR_CLOSE");
		A1440ContactorController cc = new A1440ContactorController(this.a1440);
		cc.doConnect();
	}

	/**
	 * This command tries to switch the contactor to ARMED mode for the A1440
	 * device. The armed-status allows the customer to switch the relay back on
	 * by pressing the meter button for at least 4 seconds.
	 * @throws IOException
	 */
	public void doArmContactor() throws IOException {
		getLogger().fine("Received contactor CONTACTOR_ARM");
		A1440ContactorController cc = new A1440ContactorController(this.a1440);
		cc.doArm();
	}

	/**
	 * After receiving the “Demand Reset” command the meter executes a demand
	 * reset by doing a snap shot of all energy and demand registers.
	 * @throws IOException
	 */
	public void doDemandReset() throws IOException {
		getLogger().fine("Received DEMAND_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.DEMAND_RESET_REGISTER, "");
	}

	/**
	 * With that command the error status of the meter can be reset.
	 * @throws IOException
	 */
	public void doErrorStatusReset() throws IOException {
		getLogger().fine("Received ERROR_STATUS_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.ERROR_STATUS_RESET_REGISTER, "");
	}

	/**
	 * With that command the power quality counters (in class 26) can be set to
	 * zero
	 * @throws IOException
	 */
	public void doPowerQualityReset() throws IOException {
		getLogger().fine("Received POWER_QUALITY_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.POWER_QUALITY_RESET_REGISTER, "");
	}

	/**
	 * With that command the following registers can be set to zero:
	 * <ul>
	 * <li>Counter for power outages</li>
	 * <li>Event registers (class 25)</li>
	 * <li>Power Fail, Reverse Power</li>
	 * <ul>
	 * @throws IOException
	 */
	public void doPowerOutageReset() throws IOException {
		getLogger().fine("Received POWER_OUTAGE_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.POWER_OUTAGE_RESET_REGISTER, "");
	}

	/**
	 * With this command all registers of the meter (energy, demand register, ...)
	 * will be reset to zero.
	 * @throws IOException
	 */
	public void doRegistersReset() throws IOException {
		getLogger().fine("Received REGISTERS_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.REGISTERS_RESET_REGISTER, "");
	}

	/**
	 * Resets the event logs of class 25.
	 * @throws IOException
	 */
	public void doEventLogReset()  throws IOException {
		getLogger().fine("Received EVENT_LOG_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.EVENT_LOG_RESET_REGISTER, "");
	}

	/**
	 * With that command the load profile and log file will be reset.
	 * @throws IOException
	 */
	public void doLoadLogReset()  throws IOException {
		getLogger().fine("Received LOAD_LOG_RESET");
		getA1440().getA1440Registry().setRegister(A1440Registry.LOAD_LOG_RESET_REGISTER, "");
	}

}
