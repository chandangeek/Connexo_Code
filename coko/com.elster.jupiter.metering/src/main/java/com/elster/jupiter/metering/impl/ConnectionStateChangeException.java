/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class ConnectionStateChangeException extends LocalizedException {

    private ConnectionStateChangeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static ConnectionStateChangeException stateChangeTimeShouldBeAfterInstallationTime(Thesaurus thesaurus) {
        return new ConnectionStateChangeException(thesaurus, MessageSeeds.CONNECTION_STATE_CHANGE_BEFORE_INSTALLATION_TIME);
    }

    public static ConnectionStateChangeException stateChangeTimeShouldBeAfterLatestConnectionStateChange(Thesaurus thesaurus) {
        return new ConnectionStateChangeException(thesaurus, MessageSeeds.CONNECTION_STATE_CHANGE_BEFORE_LATEST_CHANGE);
    }
}
