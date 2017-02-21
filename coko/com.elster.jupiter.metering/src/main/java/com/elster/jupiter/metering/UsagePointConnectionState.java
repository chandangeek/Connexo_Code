/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;


import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

public interface UsagePointConnectionState extends Effectivity {

    UsagePoint getUsagePoint();

    ConnectionState getConnectionState();

    void close(Instant closingDate);
}
