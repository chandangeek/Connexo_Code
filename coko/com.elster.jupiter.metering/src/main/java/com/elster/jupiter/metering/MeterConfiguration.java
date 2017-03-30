/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.streams.Predicates.on;

public interface MeterConfiguration extends Effectivity {
    List<MeterReadingTypeConfiguration> getReadingTypeConfigs();

    void endAt(Instant endAt);

    default Optional<MeterReadingTypeConfiguration> getReadingTypeConfiguration(ReadingType readingType) {
        return getReadingTypeConfigs()
                .stream()
                .filter(on(MeterReadingTypeConfiguration::getMeasured).test(readingType::equals))
                .findFirst();
    }
}
