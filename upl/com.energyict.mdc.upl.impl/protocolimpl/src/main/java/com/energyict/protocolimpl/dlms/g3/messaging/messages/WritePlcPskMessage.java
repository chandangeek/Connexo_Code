package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 2:49 PM
 */
@RtuMessageDescription(category = "Miscellaneous", description = "Write PLC pre shared key (PSK)", tag = "WritePlcPsk")
public interface WritePlcPskMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "PSK", required = true)
    byte[] getPSK();

}
