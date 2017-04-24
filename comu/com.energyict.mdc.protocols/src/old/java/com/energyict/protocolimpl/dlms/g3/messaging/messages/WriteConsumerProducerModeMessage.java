/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

@RtuMessageDescription(category = "Miscellaneous", description = "Write Consumer/Producer Mode", tag = "WriteConsumerProducerMode")
public interface WriteConsumerProducerModeMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "Mode", required = true)
    int getMode();

}