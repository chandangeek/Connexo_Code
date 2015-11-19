package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;
import java.util.List;

public interface MeterConfiguration extends Effectivity {
    List<MeterReadingTypeConfiguration> getReadingTypeConfigs();

    void endAt(Instant endAt);
}
