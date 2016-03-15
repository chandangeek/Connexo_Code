package com.elster.jupiter.metering.config;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

public interface MetrologyPurpose extends HasId, HasName {

    String getDescription();

    void delete();

    interface MetrologyPurposeBuilder {

        MetrologyPurpose fromDefaultMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose);

        MetrologyPurposeBuilder withName(String name);

        MetrologyPurposeBuilder withDescription(String description);

        MetrologyPurpose create();
    }
}
