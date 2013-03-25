package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.*;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 11:02 AM
 */
public interface ReadTopologyMessages {

    @RtuMessageDescription(category = "Read topology", description = "Read topology", tag = "ReadTopology")
    interface ReadTopologyMessage extends AnnotatedMessage {
        // No attributes, just the reset command
    }
}