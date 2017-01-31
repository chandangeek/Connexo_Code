/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import com.elster.jupiter.util.exception.MessageSeed;

public class ModemException extends CommunicationException {

    public ModemException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

}