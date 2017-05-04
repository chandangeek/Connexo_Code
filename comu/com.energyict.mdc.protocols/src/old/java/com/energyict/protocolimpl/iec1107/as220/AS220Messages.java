/**
 * AS220Messages.java
 *
 * Created on 19-nov-2008, 13:15:45 by jme
 *
 */
package com.energyict.protocolimpl.iec1107.as220;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.base.ContactorController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jme
 *
 */
public class AS220Messages implements MessageProtocol {

	private static final AS220MessageType CONTACTOR_CLOSE = new AS220MessageType("CONTACTOR_CLOSE", 411, 0, "Contactor close");
	private static final AS220MessageType CONTACTOR_ARM = new AS220MessageType("CONTACTOR_ARM", 411, 0, "Contactor arm");
	private static final AS220MessageType CONTACTOR_OPEN = new AS220MessageType("CONTACTOR_OPEN", 411, 0, "Contactor open");

	private static final AS220MessageType DEMAND_RESET = new AS220MessageType("DEMAND_RESET", 0, 0, "Demand reset");
	private static final AS220MessageType POWER_OUTAGE_RESET = new AS220MessageType("POWER_OUTAGE_RESET", 0, 0, "Power outage counter reset");
	private static final AS220MessageType POWER_QUALITY_RESET = new AS220MessageType("POWER_QUALITY_RESET", 0, 0, "Power quality counters reset");
	private static final AS220MessageType ERROR_STATUS_RESET = new AS220MessageType("ERROR_STATUS_RESET", 0, 0, "Error status reset");

	private static final AS220MessageType REGISTERS_RESET = new AS220MessageType("REGISTERS_RESET", 0, 0, "Register data reset");
	private static final AS220MessageType LOAD_LOG_RESET = new AS220MessageType("LOAD_LOG_RESET", 0, 0, "Load profile and logfile reset");
	private static final AS220MessageType EVENT_LOG_RESET = new AS220MessageType("EVENT_LOG_RESET", 0, 0, "Event log register reset");

	private static final AS220MessageType SET_DISPLAY_MESSAGE = new AS220MessageType("SET_DISPLAY_MESSAGE", 0, 0, "Write a message to the LCD of the meter");
	private static final AS220MessageType CLEAR_DISPLAY_MESSAGE = new AS220MessageType("CLEAR_DISPLAY_MESSAGE", 0, 0,
	"Clear the message on the LCD of the meter");

	private AS220 as220 = null;

	public AS220Messages(AS220 as220) {
		this.as220 = as220;
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

		MessageCategorySpec catDisplay = new MessageCategorySpec("'Display' Messages");
		catDisplay.addMessageSpec(addBasicMsg(SET_DISPLAY_MESSAGE, false));
		catDisplay.addMessageSpec(addBasicMsg(CLEAR_DISPLAY_MESSAGE, false));

		theCategories.add(catContactor);
		theCategories.add(catResetMessages);
		theCategories.add(catDisplay);
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

			if (isThisMessage(messageEntry, SET_DISPLAY_MESSAGE)) {
				doWriteMessageToDisplay(getContentBetweenTags(messageEntry.getContent()));
				return MessageResult.createSuccess(messageEntry);
			}

			if (isThisMessage(messageEntry, CLEAR_DISPLAY_MESSAGE)) {
				doClearDisplay();
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
		return msg.write(getAS220());
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

	private static MessageSpec addBasicMsg(AS220MessageType abba220MessageType, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(abba220MessageType.getDisplayName(), advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(abba220MessageType.getTagName());
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	private static boolean isThisMessage(MessageEntry messageEntry, AS220MessageType messagetype) {
		return (AS220Utils.getXMLAttributeValue(messagetype.getTagName(), messageEntry.getContent()) != null);
	}

	private static String getContentBetweenTags(String value) {
		String returnValue = value;
		int startPos = returnValue.indexOf('>') + 1;
		int endPos = returnValue.lastIndexOf('<');
		return returnValue.substring(startPos, endPos);
	}

	private Logger getLogger() {
		return getAS220().getLogger();
	}

	private AS220 getAS220() {
		return this.as220;
	}

	/**
	 * This command tries to switch off (disconnect) the contactor in the AS220
	 * device.
	 * @throws IOException
	 */
	public void doOpenContactor() throws IOException {
		getLogger().fine("Received contactor CONTACTOR_OPEN");
		ContactorController cc = new AS220ContactorController(getAS220());
		cc.doDisconnect();
	}

	/**
	 * This command tries to switch on (connect) the contactor in the AS220
	 * device.
	 * @throws IOException
	 */
	public void doCloseContactor() throws IOException {
		getLogger().fine("Received contactor CONTACTOR_CLOSE");
		ContactorController cc = new AS220ContactorController(getAS220());
		cc.doConnect();
	}

	/**
	 * This command tries to switch the contactor to ARMED mode for the AS220
	 * device. The armed-status allows the customer to switch the relay back on
	 * by pressing the meter button for at least 4 seconds.
	 * @throws IOException
	 */
	public void doArmContactor() throws IOException {
		getLogger().fine("Received contactor CONTACTOR_ARM");
		ContactorController cc = new AS220ContactorController(getAS220());
		cc.doArm();
	}

	/**
	 * After receiving the 'Demand Reset' command the meter executes a demand
	 * reset by doing a snap shot of all energy and demand registers.
	 * @throws IOException
	 */
	public void doDemandReset() throws IOException {
		getLogger().fine("Received DEMAND_RESET");
		getAS220().getAS220Registry().setRegister(AS220Registry.DEMAND_RESET_REGISTER, "");
	}

	/**
	 * With that command the error status of the meter can be reset.
	 * @throws IOException
	 */
	public void doErrorStatusReset() throws IOException {
		getLogger().fine("Received ERROR_STATUS_RESET");
		getAS220().getAS220Registry().setRegister(AS220Registry.ERROR_STATUS_RESET_REGISTER, "");
	}

	/**
	 * With that command the power quality counters (in class 26) can be set to
	 * zero
	 * @throws IOException
	 */
	public void doPowerQualityReset() throws IOException {
		getLogger().fine("Received POWER_QUALITY_RESET");
		getAS220().getAS220Registry().setRegister(AS220Registry.POWER_QUALITY_RESET_REGISTER, "");
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
		getAS220().getAS220Registry().setRegister(AS220Registry.POWER_OUTAGE_RESET_REGISTER, "");
	}

	/**
	 * With this command all registers of the meter (energy, demand register,
	 * ...) will be reset to zero.
	 * @throws IOException
	 */
	public void doRegistersReset() throws IOException {
		getLogger().fine("Received REGISTERS_RESET");
		getAS220().getAS220Registry().setRegister(AS220Registry.REGISTERS_RESET_REGISTER, "");
	}

	/**
	 * Resets the event logs of class 25.
	 * @throws IOException
	 */
	public void doEventLogReset() throws IOException {
		getLogger().fine("Received EVENT_LOG_RESET");
		getAS220().getAS220Registry().setRegister(AS220Registry.EVENT_LOG_RESET_REGISTER, "");
	}

	/**
	 * With that command the load profile and log file will be reset.
	 * @throws IOException
	 */
	public void doLoadLogReset() throws IOException {
		getLogger().fine("Received LOAD_LOG_RESET");
		getAS220().getAS220Registry().setRegister(AS220Registry.LOAD_LOG_RESET_REGISTER, "");
	}

	/**
	 * This command clears the message on the display of the meter.
	 * @throws IOException
	 */
	public void doClearDisplay() throws IOException {
		AS220DisplayController displayController = new AS220DisplayController(getAS220());
		displayController.clearDisplay();
	}

	/**
	 * This command sends a message onto the display of the meter. This message
	 * has the highest priority. This means that all other messages are
	 * overwritten by this message in scrollmode.
	 * @param message The message to show on the LCD of the device
	 * @throws IOException
	 */
	public void doWriteMessageToDisplay(String message) throws IOException {
		AS220DisplayController displayController = new AS220DisplayController(getAS220());
		displayController.writeMessage(message);
	}

}
