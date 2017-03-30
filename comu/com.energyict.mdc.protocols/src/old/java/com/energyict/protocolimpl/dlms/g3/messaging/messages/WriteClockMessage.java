/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

@RtuMessageDescription(category = "Miscellaneous", description = "Set clock date and time", tag = "WriteClockDateTime")
public interface WriteClockMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "DateTime", required = true)
    String getDateTime();

}