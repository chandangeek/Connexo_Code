/**
 * A1440Messages.java
 *
 * Created on 19-nov-2008, 13:15:45 by jme
 *
 */
package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jme
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

    //PQ threshold messages
    private static final A1440MessageType PQ_THRESHOLD = new A1440MessageType("CLASS_37_UPDATE", 37, 180 * 2, "Update MeterClass 37");

    private A1440 a1440 = null;

    public A1440Messages(A1440 a1440) {
        this.a1440 = a1440;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

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

        MessageCategorySpec catPowerQuality = new MessageCategorySpec("'Meter class update' Messages");
        catPowerQuality.addMessageSpec(addValueMsg(PQ_THRESHOLD, false));

        theCategories.add(catContactor);
        theCategories.add(catResetMessages);
        theCategories.add(catPowerQuality);
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

            if (isThisMessage(messageEntry, PQ_THRESHOLD)) {
                doPowerQualityLimitMessage(messageEntry);
                return MessageResult.createSuccess(messageEntry);
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
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
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(tag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("\n\n</");
        builder.append(tag.getName());
        builder.append(">");

        return builder.toString();

    }

    private static MessageSpec addBasicMsg(A1440MessageType abba220MessageType, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(abba220MessageType.getDisplayName(), advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(abba220MessageType.getTagName());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private static MessageSpec addValueMsg(final A1440MessageType msgType, final boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(msgType.getDisplayName(), advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(msgType.getTagName());
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
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
     *
     * @throws IOException
     */
    public void doOpenContactor() throws IOException {
        getLogger().fine("Received contactor CONTACTOR_OPEN");
        ContactorController cc = new A1440ContactorController(getA1440());
        cc.doDisconnect();
    }

    /**
     * This command tries to switch on (connect) the contactor in the A1440
     * device.
     *
     * @throws IOException
     */
    public void doCloseContactor() throws IOException {
        getLogger().fine("Received contactor CONTACTOR_CLOSE");
        ContactorController cc = new A1440ContactorController(getA1440());
        cc.doConnect();
    }

    /**
     * This command tries to switch the contactor to ARMED mode for the A1440
     * device. The armed-status allows the customer to switch the relay back on
     * by pressing the meter button for at least 4 seconds.
     *
     * @throws IOException
     */
    public void doArmContactor() throws IOException {
        getLogger().fine("Received contactor CONTACTOR_ARM");
        ContactorController cc = new A1440ContactorController(getA1440());
        cc.doArm();
    }

    /**
     * After receiving the 'Demand Reset' command the meter executes a demand
     * reset by doing a snap shot of all energy and demand registers.
     *
     * @throws IOException
     */
    public void doDemandReset() throws IOException {
        getLogger().fine("Received DEMAND_RESET");
        getA1440().getA1440Registry().setRegister(A1440Registry.DEMAND_RESET_REGISTER, "");
    }

    /**
     * With that command the error status of the meter can be reset.
     *
     * @throws IOException
     */
    public void doErrorStatusReset() throws IOException {
        getLogger().fine("Received ERROR_STATUS_RESET");
        getA1440().getA1440Registry().setRegister(A1440Registry.ERROR_STATUS_RESET_REGISTER, "");
    }

    /**
     * With that command the power quality counters (in class 26) can be set to
     * zero
     *
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
     *
     * @throws IOException
     */
    public void doPowerOutageReset() throws IOException {
        getLogger().fine("Received POWER_OUTAGE_RESET");
        getA1440().getA1440Registry().setRegister(A1440Registry.POWER_OUTAGE_RESET_REGISTER, "");
    }

    /**
     * With this command all registers of the meter (energy, demand register, ...)
     * will be reset to zero.
     *
     * @throws IOException
     */
    public void doRegistersReset() throws IOException {
        getLogger().fine("Received REGISTERS_RESET");
        getA1440().getA1440Registry().setRegister(A1440Registry.REGISTERS_RESET_REGISTER, "");
    }

    /**
     * Resets the event logs of class 25.
     *
     * @throws IOException
     */
    public void doEventLogReset() throws IOException {
        getLogger().fine("Received EVENT_LOG_RESET");
        getA1440().getA1440Registry().setRegister(A1440Registry.EVENT_LOG_RESET_REGISTER, "");
    }

    /**
     * With that command the load profile and log file will be reset.
     *
     * @throws IOException
     */
    public void doLoadLogReset() throws IOException {
        getLogger().fine("Received LOAD_LOG_RESET");
        getA1440().getA1440Registry().setRegister(A1440Registry.LOAD_LOG_RESET_REGISTER, "");
    }

    /**
     * Write the powerQuality lower and upper thresholds via raw dataBlock
     *
     * @throws IOException
     */
    public void doPowerQualityLimitMessage(MessageEntry messageEntry) throws IOException {
        getLogger().fine("Received message : " + PQ_THRESHOLD.getDisplayName());
        writeClassSettings(messageEntry, PQ_THRESHOLD);
    }

    private void writeClassSettings(MessageEntry messageEntry, A1440MessageType messageType) throws IOException {
        final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
        final int MAX_PACKETSIZE = 48;

        String returnValue;
        String iec1107Command;

        int first = 0;
        int last;
        int offset;
        int length;

        if (a1440.getISecurityLevel() < 1) {
            throw new IOException("Message " + messageType.getDisplayName() + " needs at least security level 1. Current level: " + a1440.getISecurityLevel());
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


            returnValue = a1440.getFlagIEC1107Connection().sendRawCommandFrameAndReturn(WRITE1, iec1107Command.getBytes());
            if (returnValue != null) {
                throw new IOException(" Wrong response on iec1107Command: W1." + iec1107Command + "] expected 'null' but received " + ProtocolUtils.getResponseData(returnValue.getBytes()));
            }
            first = last;

        } while (first < message.length());

    }
}
