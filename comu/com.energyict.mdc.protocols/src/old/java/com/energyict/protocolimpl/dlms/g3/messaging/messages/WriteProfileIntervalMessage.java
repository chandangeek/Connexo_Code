/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

@RtuMessageDescription(category = "Miscellaneous", description = "Write load profile interval", tag = "WriteProfileInterval")
public interface WriteProfileIntervalMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "IntervalInSeconds", required = true)
    int getIntervalInSeconds();

}
