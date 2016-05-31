package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.ReadingType;

import java.util.Collections;
import java.util.List;

/**
 * It is a stub class. Real functionality will be done in scope of CXO-608
 */
public final class EndDeviceCapabilities {
    private List<ReadingType> readingTypes;

    public EndDeviceCapabilities(List<ReadingType> readingTypes) {
        this.readingTypes = Collections.unmodifiableList(readingTypes);
    }

    public List<ReadingType> getConfiguredReadingTypes() {
        return this.readingTypes;
    }
}
