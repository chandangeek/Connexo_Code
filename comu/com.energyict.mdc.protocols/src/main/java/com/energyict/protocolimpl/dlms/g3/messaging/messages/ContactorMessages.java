/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

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
