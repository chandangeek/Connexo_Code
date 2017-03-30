/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;
import java.util.List;

public interface UsagePointConfiguration extends Effectivity {
    List<UsagePointReadingTypeConfiguration> getReadingTypeConfigs();

    void endAt(Instant instant);
}
