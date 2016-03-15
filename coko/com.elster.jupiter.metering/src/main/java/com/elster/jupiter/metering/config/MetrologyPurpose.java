package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

@ProviderType
public interface MetrologyPurpose extends HasId, HasName {

    String getDescription();

    void delete();

    @ProviderType
    interface MetrologyPurposeBuilder {

        MetrologyPurpose fromDefaultMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose);

        MetrologyPurposeBuilder withName(String name);

        MetrologyPurposeBuilder withDescription(String description);

        MetrologyPurpose create();
    }
}
