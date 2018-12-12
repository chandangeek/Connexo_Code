/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface UsagePointConnectionState extends Effectivity {

    UsagePoint getUsagePoint();

    ConnectionState getConnectionState();

    String getConnectionStateDisplayName();

    /**
     * Does nothing, it was put in the interface by mistake, to be dropped asap
     *
     * @deprecated because it should not be public
     */
    @Deprecated
    void close(Instant closingDate);
}
