package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 2:49 PM
 */
@RtuMessageDescription(category = "Miscellaneous", description = "Force sync clock", tag = "ForceSyncClock")
public interface ForceSyncClockMessage extends AnnotatedMessage {

    // No attributes, just the sync clock command

}
