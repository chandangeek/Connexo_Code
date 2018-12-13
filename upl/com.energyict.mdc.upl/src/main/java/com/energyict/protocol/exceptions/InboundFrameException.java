/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * @author Stijn Vanhoorelbeke
 * @since 05.10.17 - 11:47
 */
public class InboundFrameException extends CommunicationException {

    public InboundFrameException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public InboundFrameException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }
}