/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

@RtuMessageDescription(category = "Miscellaneous", description = "Force sync clock", tag = "ForceSyncClock")
public interface ForceSyncClockMessage extends AnnotatedMessage {

    // No attributes, just the sync clock command

}
