package com.energyict.protocolimpl.messaging.examples;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 11:16 PM
 */
@RtuMessageDescription(category = "Connect/Disconnect", tag = "CloseContactor", description = "Close contactor", advanced = true)
public interface CloseContactorMessage extends AnnotatedMessage {

}
