/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * @author Stijn Vanhoorelbeke
 * @since 05.10.17 - 11:47
 */
public class DataEncryptionException extends CommunicationException {

    public DataEncryptionException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public DataEncryptionException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }
}