package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 5:37 PM
 */
@RtuMessageDescription(category = "Miscellaneous", description = "Log object list", tag = "LogObjectList", advanced = true)
public interface LogObjectListMessage extends AnnotatedMessage {

}
