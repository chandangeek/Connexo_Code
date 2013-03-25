package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 10:59 AM
 */
public interface ContactorMessages {

    String DISCONNECT_CONTROL_CATEGORY = "Disconnect control";

    @RtuMessageDescription(category = DISCONNECT_CONTROL_CATEGORY, description = "Close main contactor", tag = "CloseMainContactor")
    interface CloseContactorMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = DISCONNECT_CONTROL_CATEGORY, description = "Arm main contactor", tag = "ArmMainContactor")
    interface ArmContactorMessage extends AnnotatedMessage {

    }

    @RtuMessageDescription(category = DISCONNECT_CONTROL_CATEGORY, description = "Open main contactor", tag = "OpenMainContactor")
    interface OpenContactorMessage extends AnnotatedMessage {

    }
}
