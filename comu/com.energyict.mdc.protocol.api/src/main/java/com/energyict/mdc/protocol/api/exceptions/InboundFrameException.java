/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.io.CommunicationException;

/**
 * Models the expected but exceptional situations that can occur with frames.
 *
 * Date: 16/10/12
 * Time: 12:01
 * Author: khe
 */
public final class InboundFrameException extends CommunicationException {

    public InboundFrameException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}