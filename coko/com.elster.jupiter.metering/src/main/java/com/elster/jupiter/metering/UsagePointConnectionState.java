package com.elster.jupiter.metering;


import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

public interface UsagePointConnectionState extends Effectivity {

    UsagePoint getUsagePoint();

    ConnectionState getConnectionState();

    void close(Instant closingDate);
}
