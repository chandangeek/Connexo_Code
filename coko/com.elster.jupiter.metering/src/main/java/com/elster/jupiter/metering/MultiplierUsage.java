/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.util.Optional;

public interface MultiplierUsage {
    ReadingType getMeasured();

    Optional<ReadingType> getCalculated();

    MultiplierType getMultiplierType();
}
