/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

@RtuMessageDescription(category = "Miscellaneous", description = "Write PLC pre shared key (PSK)", tag = "WritePlcPsk")
public interface WritePlcPskMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "PSK", required = true)
    byte[] getPSK();

}
