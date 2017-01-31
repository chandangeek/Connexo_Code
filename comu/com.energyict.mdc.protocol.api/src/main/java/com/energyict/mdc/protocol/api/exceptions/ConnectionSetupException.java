/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.MessageSeeds;

public class ConnectionSetupException extends CommunicationException {

    public ConnectionSetupException(MessageSeed messageSeed, ConnectionException cause) {
        super(messageSeed, cause);
    }

    public static ConnectionSetupException disconnectFailed(ConnectionException cause) {
        return new ConnectionSetupException(MessageSeeds.CONNECTION_DISCONNECT_ERROR, cause);
    }
}