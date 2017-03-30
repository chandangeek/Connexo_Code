/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.messaging.examples;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

@RtuMessageDescription(category = "Connect/Disconnect", tag = "OpenContactor", description = "Open contactor")
public interface OpenContactorMessage extends AnnotatedMessage {

}
