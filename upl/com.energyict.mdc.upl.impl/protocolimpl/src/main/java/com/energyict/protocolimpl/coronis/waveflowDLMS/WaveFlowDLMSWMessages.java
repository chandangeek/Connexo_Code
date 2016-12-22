package com.energyict.protocolimpl.coronis.waveflowDLMS;

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

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveFlowDLMSWMessages implements MessageProtocol {

    private static final ObisCode DISCONNECT_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    AbstractDLMS abstractDLMS;

    WaveFlowDLMSWMessages(AbstractDLMS abstractDLMS) {
        this.abstractDLMS = abstractDLMS;
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<PairMeter")) {
                abstractDLMS.getLogger().info("************************* PairMeter *************************");

                try {
                    int baudrate = WaveflowProtocolUtils.parseInt(getTagContents("PairMeter", messageEntry.getContent()));
                    return abstractDLMS.pairWithEMeter(baudrate) ? MessageResult.createSuccess(messageEntry) : MessageResult.createFailed(messageEntry);
                } catch (IOException e) {
                    abstractDLMS.getLogger().severe("Unable to pair meter: " + e.getMessage());
                    return MessageResult.createFailed(messageEntry);
                }

            } else if (messageEntry.getContent().contains("<ForceTimeSync")) {
                abstractDLMS.getLogger().info("************************* ForceTimeSync (e-meter time)*************************");
                abstractDLMS.forceSetTime();
                return MessageResult.createSuccess(messageEntry);
            } else if (messageEntry.getContent().contains("<RemoteDisconnect")) {
                abstractDLMS.getLogger().info("************************* RemoteDisconnect *************************");
                if (!abstractDLMS.isOptimizeChangeContactorStatus()) {
                    return doDisconnect(messageEntry);
                } else {
                    AbstractDataType abstractDataType = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(DISCONNECT_OBISCODE, 3);
                    TypeEnum controlState = abstractDataType.getTypeEnum();
                    if (controlState.getValue() == 0) {
                        abstractDLMS.getLogger().info("Disconnect message failed, meter should be in connected or ready_for_connection state");
                        return MessageResult.createFailed(messageEntry);
                    } else {
                        int controlMode;
                        try {
                            controlMode = Integer.parseInt(getTagContents("RemoteDisconnect", messageEntry.getContent()).trim());
                        } catch (NumberFormatException e) {
                            controlMode = 1;
                        }
                        abstractDLMS.getLogger().info("Setting the control mode to " + controlMode);
                        setControlMode(controlMode);
                        return doDisconnect(messageEntry);
                    }
                }
            } else if (messageEntry.getContent().contains("<RemoteConnect")) {
                abstractDLMS.getLogger().info("************************* RemoteConnect *************************");
                if (!abstractDLMS.isOptimizeChangeContactorStatus()) {
                    return doReconnect(messageEntry);
                } else {
                    AbstractDataType abstractDataType = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(DISCONNECT_OBISCODE, 3);
                    TypeEnum controlState = abstractDataType.getTypeEnum();

                    //Reconnection is only allowed if the current control state is 'disconnected'
                    if (controlState.getValue() == 0) {
                        int controlMode;
                        try {
                            controlMode = Integer.parseInt(getTagContents("RemoteConnect", messageEntry.getContent()).trim());
                        } catch (NumberFormatException e) {
                            controlMode = 1;
                        }
                        abstractDLMS.getLogger().info("Setting the control mode to " + controlMode);
                        setControlMode(controlMode);
                        return doReconnect(messageEntry);
                    } else {
                        abstractDLMS.getLogger().info("Reconnect message failed, meter should be in disconnected state");
                        return MessageResult.createFailed(messageEntry);
                    }
                }
            } else if (messageEntry.getContent().contains("<SyncWaveFlowRTC")) {
                abstractDLMS.getLogger().info("************************* SyncWaveFlowRTC (waveflow100mW time)*************************");
                abstractDLMS.setWaveFlowTime();
                return MessageResult.createSuccess(messageEntry);
            } else if (messageEntry.getContent().contains("<SetAlarmConfig")) {
                abstractDLMS.getLogger().info("************************* SetAlarmConfig *************************");
                int alarmConfigValue = WaveflowProtocolUtils.parseInt(getTagContents("SetAlarmConfig", messageEntry.getContent()));
                abstractDLMS.getRadioCommandFactory().setAlarmRoute(alarmConfigValue);
                if (abstractDLMS.getParameterFactory().readAlarmConfiguration() == alarmConfigValue) {
                    return MessageResult.createSuccess(messageEntry);
                } else {
                    abstractDLMS.getLogger().severe("Message set alarm configuration readback failed!");
                    return MessageResult.createFailed(messageEntry);
                }
            } else if (messageEntry.getContent().contains("<SetDisconnectorControlMode")) {
                abstractDLMS.getLogger().info("************************* SetDisconnectorControlMode *************************");
                int controlMode = WaveflowProtocolUtils.parseInt(getTagContents("SetDisconnectorControlMode", messageEntry.getContent()));
                abstractDLMS.getLogger().info("Setting disconnector control mode to " + controlMode);
                setControlMode(controlMode);
                return MessageResult.createSuccess(messageEntry);
            } else if (messageEntry.getContent().contains("<SetOperatingMode")) {
                abstractDLMS.getLogger().info("************************* SetOperatingMode *************************");
                int operatingModeValue = WaveflowProtocolUtils.parseInt(getTagContents("SetOperatingMode", messageEntry.getContent()));
                abstractDLMS.getParameterFactory().writeOperatingMode(operatingModeValue);
                if (abstractDLMS.getParameterFactory().readOperatingMode() == operatingModeValue) {
                    return MessageResult.createSuccess(messageEntry);
                } else {
                    abstractDLMS.getLogger().severe("Message set operating mode readback failed!");
                    return MessageResult.createFailed(messageEntry);
                }
            } else if (messageEntry.getContent().contains("<SetApplicationStatus")) {
                abstractDLMS.getLogger().info("************************* SetApplicationStatus *************************");
                int applicationStatusValue = WaveflowProtocolUtils.parseInt(getTagContents("SetApplicationStatus", messageEntry.getContent()));
                abstractDLMS.getParameterFactory().writeApplicationStatus(applicationStatusValue);
                return MessageResult.createSuccess(messageEntry);
            } else if (messageEntry.getContent().contains("<InitializeKey")) {
                abstractDLMS.getLogger().info("************************* InitializeKey *************************");
                abstractDLMS.initializeEncryption(getTagContents("InitializeKey", messageEntry.getContent()));
                return MessageResult.createSuccess(messageEntry);
            } else if (messageEntry.getContent().contains("<RenewKey")) {
                abstractDLMS.getLogger().info("************************* RenewKey *************************");
                String content = getTagContents("RenewKey", messageEntry.getContent());
                String[] keys = content.split(",");
                if (keys.length != 2) {
                    abstractDLMS.getLogger().severe("Invalid content for renew key message [" + content + "]");
                    return MessageResult.createFailed(messageEntry);
                }
                abstractDLMS.renewEncryptionKey(keys[0], keys[1]);
                return MessageResult.createSuccess(messageEntry);
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        } catch (Exception e) {
            abstractDLMS.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void setControlMode(int controlMode) throws IOException {
        abstractDLMS.getTransparantObjectAccessFactory().writeObjectAttribute(DISCONNECT_OBISCODE, 4, new TypeEnum(controlMode));
    }

    private MessageResult doReconnect(MessageEntry messageEntry) throws IOException {
        try {
            abstractDLMS.getLogger().info("Executing reconnect");
            abstractDLMS.getTransparantObjectAccessFactory().executeObjectAction(DISCONNECT_OBISCODE, 2);
        } catch (DataAccessResultException e) {
            abstractDLMS.getLogger().info("Tried to execute remote reconnect method, but it failed. It is probably not allowed by the disconnector control mode: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult doDisconnect(MessageEntry messageEntry) throws IOException {
        try {
            abstractDLMS.getLogger().info("Executing disconnect");
            abstractDLMS.getTransparantObjectAccessFactory().executeObjectAction(DISCONNECT_OBISCODE, 1);
        } catch (DataAccessResultException e) {
            abstractDLMS.getLogger().info("Tried to execute remote disconnect method, but it failed. It is probably not allowed by the disconnector control mode: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        return MessageResult.createSuccess(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

        MessageCategorySpec cat1 = new MessageCategorySpec("WaveflowDLMS messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set the applicationstatus (0 to reset)", "SetApplicationStatus", false, "0"));
        cat1.addMessageSpec(addBasicMsg("Force sync the e-meter time", "ForceTimeSync", false));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("WaveflowDLMS advanced messages");
        cat2.addMessageSpec(addBasicMsgWithValue("Remote disconnect", "RemoteDisconnect", true, " "));
        cat2.addMessageSpec(addBasicMsgWithValue("Remote connect", "RemoteConnect", true, " "));
        cat2.addMessageSpec(addBasicMsgWithValue("Set disconnector control mode", "SetDisconnectorControlMode", true, null));
        cat2.addMessageSpec(addBasicMsgWithValue("Set alarm configuration (0..7)", "SetAlarmConfig", true, "7"));
        cat2.addMessageSpec(addBasicMsgWithValue("Set the operating mode (0..7)", "SetOperatingMode", true, "7"));
        cat2.addMessageSpec(addBasicMsgWithValue("Detect e-meter (pair) (9600 or 19200 (default) baud)", "PairMeter", true, "19200"));
        cat2.addMessageSpec(addBasicMsg("Force sync the waveflow time", "SyncWaveFlowRTC", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Initialize key (new 128 bit key) ", "InitializeKey", true, "new 128 bit key in hex notation"));
        cat2.addMessageSpec(addBasicMsgWithValue("Renew key (old 128 bit key, new 128 bit key)", "RenewKey", true, "old 128 bit key in hex notation,new 128 bit key in hex notation"));
        theCategories.add(cat2);

        return theCategories;
    }

    private MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced, String defaultValue) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(defaultValue));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {
    }


    /**
     * Gets the contents of the given tag.
     *
     * @param tagName         The name of the tag.
     * @param messageContents The contents of the message to extract the data from.
     * @return The contents, <code>null</code> if for some reason the contents cannot be extracted. A warning will be issued in that case.
     */
    private String getTagContents(final String tagName, final String messageContents) {
        final int startIndex = messageContents.indexOf("<" + tagName + ">") + tagName.length() + 2;
        final int endIndex = messageContents.indexOf("</" + tagName + ">");

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return messageContents.substring(startIndex, endIndex).trim();
        } else {
            abstractDLMS.getLogger().warning("Cannot get contents of tag [" + tagName + "] out of message [" + messageContents + "], startIndex is [" + startIndex + "], endIndex is [" + endIndex + "]");
        }

        return null;
    }
}
