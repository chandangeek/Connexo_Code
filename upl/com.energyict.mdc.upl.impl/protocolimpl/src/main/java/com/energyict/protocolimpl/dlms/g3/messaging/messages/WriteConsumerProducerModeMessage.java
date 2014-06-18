package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 2:49 PM
 */
@RtuMessageDescription(category = "Miscellaneous", description = "Write Consumer/Producer Mode", tag = "WriteConsumerProducerMode")
public interface WriteConsumerProducerModeMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "Mode", required = true)
    int getMode();

}