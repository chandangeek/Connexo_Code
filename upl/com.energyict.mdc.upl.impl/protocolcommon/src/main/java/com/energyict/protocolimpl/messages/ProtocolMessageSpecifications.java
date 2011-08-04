package com.energyict.protocolimpl.messages;

import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;

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
     * Creates a MessageSpecification for creating a HAN network message. The message does not contain any values or attributes.
     *
     * @return the requested messageSpecification
     */
    public static MessageSpec getJoinZigBeeSlaveSpecification() {
        MessageSpec msgSpec = new MessageSpec(RtuMessageKeyIdConstants.JOIN_ZIGBEE_SLAVE, false);
        MessageTagSpec tagSpec = MessagingTools.getAttributesOnlyMessageTagSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
