/**
 * A1440Messages.java
 * <p>
 * Created on 19-nov-2008, 13:15:45 by jme
 */
package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;
import java.math.BigDecimal;
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

    private static final A1440MessageType DISABLE_LOAD_LIMIT = new A1440MessageType("DISABLE_LOAD_LIMIT", 0, 0, "Disable load limit");
    private static final A1440MessageType LOADLIMIT_DURATION = new A1440MessageType("SET_LOAD_LIMIT_DURATION", 0, 0, "Set load limit duration");
    private static final A1440MessageType LOADLIMIT_THRESHOLD = new A1440MessageType("SET_LOAD_LIMIT_TRESHOLD", 0, 0, "Set load limit threshold");
    private static final A1440MessageType LOADLIMIT_CONFIGURATION = new A1440MessageType("CONFIGURE_LOAD_LIMIT", 0, 0, "Configure the load limit settings");
    private static final String DURATION_ATTRIBUTE = "Duration";
    private static final String THRESHOLD_ATTRIBUTE = "Threshold";
    private static final String UNIT_ATTRIBUTE = "Unit";
    private static final String UNDEFINED = "undefined";

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

        MessageCategorySpec catPowerQuality = new MessageCategorySpec("'Meter class update' Messages");
        catPowerQuality.addMessageSpec(addValueMsg(PQ_THRESHOLD, false));

        //TODO: Note that these messages are for the Elster AS3000 device (which extends from A1440) - once we have a dedicated protocol for the AS3000, these messages should be moved to there.
        MessageCategorySpec catLoadLimit = new MessageCategorySpec("'Load limitation' Messages");
        catLoadLimit.addMessageSpec(addValueMsg(DISABLE_LOAD_LIMIT, false));
        catLoadLimit.addMessageSpec(addBasicMsgWithAttributes(LOADLIMIT_THRESHOLD, false, THRESHOLD_ATTRIBUTE, UNIT_ATTRIBUTE));
        catLoadLimit.addMessageSpec(addBasicMsgWithAttributes(LOADLIMIT_CONFIGURATION, false, true, THRESHOLD_ATTRIBUTE, UNIT_ATTRIBUTE, DURATION_ATTRIBUTE));

        theCategories.add(catContactor);
        theCategories.add(catResetMessages);
        theCategories.add(catPowerQuality);
        theCategories.add(catLoadLimit);
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

            if (isThisMessage(messageEntry, DISABLE_LOAD_LIMIT)) {
                disableLoadLimit(messageEntry);
                return MessageResult.createSuccess(messageEntry);
            }
            if (isThisMessage(messageEntry, LOADLIMIT_THRESHOLD)) {
                doLoadLimitThresholdMessage(messageEntry);
                return MessageResult.createSuccess(messageEntry);
            }
            if (isThisMessage(messageEntry, LOADLIMIT_CONFIGURATION)) {
                doLoadLimitConfiguration(messageEntry);
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
        for (Iterator it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext(); ) {
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
        builder.append("</");
        builder.append(tag.getName());
        builder.append(">");

        return builder.toString();

    }

    private static MessageSpec addBasicMsg(A1440MessageType a1440MessageType, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(a1440MessageType.getDisplayName(), advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(a1440MessageType.getTagName());
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

    private static MessageSpec addBasicMsgWithAttributes(A1440MessageType messageType, final boolean advanced, String requiredAttribute, String... optionalAttributes) {
        return addBasicMsgWithAttributes(messageType, advanced, false, requiredAttribute, optionalAttributes);
    }

    private static MessageSpec addBasicMsgWithAttributes(A1440MessageType messageType, final boolean advanced, boolean optionalAttributesRequired, String requiredAttribute, String... optionalAttributes) {
        MessageSpec msgSpec = new MessageSpec(messageType.getDisplayName(), advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(messageType.getTagName());

        MessageAttributeSpec addAttribute = new MessageAttributeSpec(requiredAttribute, true);
        tagSpec.add(addAttribute);
        for (String attribute : optionalAttributes) {
            addAttribute = new MessageAttributeSpec(attribute, optionalAttributesRequired);
            tagSpec.add(addAttribute);
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private static boolean isThisMessage(MessageEntry messageEntry, A1440MessageType messagetype) {
        return messageEntry.getContent().contains(messagetype.getTagName());
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
        checkSecurityLevelSufficient();

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

    private void disableLoadLimit(MessageEntry messageEntry) throws IOException {
        getLogger().fine("Received DISABLE_LOAD_LIMIT");
        checkSecurityLevelSufficient();
        doSetLoadLimitThreshold(BigDecimal.ZERO.toString(), UNDEFINED); // Disable load limitation, by writing limit '0' to device
    }

    private void doLoadLimitThresholdMessage(MessageEntry messageEntry) throws IOException {
        doSetLoadLimitThreshold(
                MessagingTools.getContentOfAttribute(messageEntry, THRESHOLD_ATTRIBUTE),
                MessagingTools.getContentOfAttribute(messageEntry, UNIT_ATTRIBUTE));
    }

    private void doSetLoadLimitThreshold(String threshold, String unitAcronym) throws IOException {
        getLogger().fine("Received SET_LOAD_LIMIT_TRESHOLD");
        checkSecurityLevelSufficient();
        try {
            LoadControlMeasurementQuantity measurementQuantity = LoadControlMeasurementQuantity.getLoadControlMeasurementQuantityForQuantityCode(
                    (String) getA1440().getA1440Registry()
                            .getRegister(A1440Registry.LOAD_CONTROL_MEASUREMENT_QUANTITY_REGISTER)
            );
            String value = measurementQuantity.format(Float.parseFloat(threshold), convertToUnit(unitAcronym));

            getA1440().getA1440Registry().setRegister(A1440Registry.LOAD_CONTROL_THRESHOLD_REGISTER, value);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse the threshold value: " + e.getMessage());
        }
    }

    private Unit convertToUnit(String unitAcronym) throws IOException {
        Unit unit = unitAcronym.equals(UNDEFINED) ? Unit.getUndefined() : Unit.get(unitAcronym.trim());
        if (unit == null) {
            throw new IOException("Encountered invalid unit '" + unitAcronym + "'.");
        }
        return unit;
    }

    private void doLoadLimitConfiguration(MessageEntry messageEntry) throws IOException {
        getLogger().fine("Received CONFIGURE_LOAD_LIMIT");
        checkSecurityLevelSufficient();
        String threshold = MessagingTools.getContentOfAttribute(messageEntry, THRESHOLD_ATTRIBUTE);
        String unit = MessagingTools.getContentOfAttribute(messageEntry, UNIT_ATTRIBUTE);
        String duration = MessagingTools.getContentOfAttribute(messageEntry, DURATION_ATTRIBUTE);

        doSetLoadLimitThreshold(threshold, unit);
        doSetLoadLimitDuration(duration);
    }

    private void doSetLoadLimitDuration(String loadLimitDuration) throws IOException {
        try {
            String value = String.format("%02X", Integer.parseInt(loadLimitDuration) / 5);
            getA1440().getA1440Registry().setRegister(A1440Registry.LOAD_CONTROL_ACTION_DELAY_REGISTER, value);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse duration '" + loadLimitDuration + "' - duration should contain a number indicating the number of seconds");
        }
    }

    private void checkSecurityLevelSufficient() throws IOException {
        if (a1440.getISecurityLevel() < 1) {
            throw new IOException("Message " + LOADLIMIT_DURATION.getDisplayName() + " needs at least security level 1. Current level: " + a1440
                    .getISecurityLevel());
        }
    }
}
