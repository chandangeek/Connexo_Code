package com.energyict.protocolimpl.messages;

import com.energyict.protocol.messaging.*;

/**
 * Contains a summary of {@link com.energyict.protocol.messaging.MessageSpec} for the protocol to use in the {@link com.energyict.protocolimpl.messages.ProtocolMessageCategories}
 */
public class ProtocolMessageSpecifications {

    /**
     * Creates a MessageSpecification for a <i>Demand Reset</i> message. The message does not contain any values or attributes.
     *
     * @return the requested messageSpecification.
     */
    public static MessageSpec getDemandResetMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.DEMANDRESET, false);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.DEMAND_RESET);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public static MessageSpec getChangeZigBeeHanSASMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.CHANGE_ZIGBEE_HAN_SAS, false);
        // MessageTagSpec tagSpec = MessagingTools.getAttributesOnlyMessageTagSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);
        // TODO Don't BackPort this to version below 8.11, use the above snippet instead
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.CHANGE_HAN_SAS);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.HAN_SAS_PAN_ID, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.HAN_SAS_CHANNEL, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.HAN_SAS_INSECURE_JOIN, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for creating a HAN network message. The message does not contain any values or attributes.
     *
     * @return the requested messageSpecification
     */
    public static MessageSpec getCreateHanMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.CREATE_HAN, false);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.CREATE_HAN_NETWORK);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for removing a HAN network message. The message does not contain any values or attributes.
     *
     * @return
     */
    public static MessageSpec getRemoveHanMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.REMOVE_HAN, false);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.REMOVE_HAN_NETWORK);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for adding a ZigBee device (IHD, Gas, ...) to the network.
     *
     * @return the requested messageSpecification
     */
    public static MessageSpec getJoinZigBeeSlaveSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.JOIN_ZIGBEE_SLAVE, false);
        // MessageTagSpec tagSpec = MessagingTools.getAttributesOnlyMessageTagSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);
        // TODO Don't BackPort this to version below 8.11, use the above snippet instead
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for removing a ZigBee device (IHD, Gas, ...) from the network.
     *
     * @return
     */
    public static MessageSpec getRemoveZigBeeSlaveSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.REMOVE_ZIGBEE_SLAVE, false);
        // MessageTagSpec tagSpec = MessagingTools.getAttributesOnlyMessageTagSpec(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE);
        // TODO Don't BackPort this to version below 8.11, use the above snippet instead
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for removing all ZigBee devices at once (IHD, Gas, ...) from the network.
     *
     * @return
     */
    public static MessageSpec getRemoveAllZigBeeSlavesSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.REMOVE_ALL_ZIGBEE_SLAVES, false);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for backing up the ZigBee HAN Keys. The message does not contain any values or attributes.
     *
     * @return the requested messageSpecification
     */
    public static MessageSpec getBackupZigBeeHanKeyMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.BACKUP_ZIGBEE_HAN_PARAMETERS);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpecification for creating a Restore ZigBee HAN parameters message
     *
     * @return the requested messageSpecification
     */
    public static MessageSpec getRestoreZigBeeParametersMessageSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.RESTORE_ZIGBEE_HAN_PARAMETERS);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
