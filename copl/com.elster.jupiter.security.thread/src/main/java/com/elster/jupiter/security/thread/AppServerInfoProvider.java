/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.security.thread;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface AppServerInfoProvider {
    Optional<String> getServerName();
}
