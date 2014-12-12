package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 11:02 AM
 */
public interface NetworkInformationMessages {

    public String CATEGORY = "Network information";

    @RtuMessageDescription(category = CATEGORY, description = "Read topology", tag = "ReadTopology")
    interface ReadTopologyMessage extends AnnotatedMessage {
        // No attributes, just the command
    }

    @RtuMessageDescription(category = CATEGORY, description = "Path request", tag = "PathRequest")
    interface PathRequestMessage extends AnnotatedMessage {
        @RtuMessageAttribute(tag = "groupId", defaultValue = "", required = false)
        String getGroupId();
    }
}