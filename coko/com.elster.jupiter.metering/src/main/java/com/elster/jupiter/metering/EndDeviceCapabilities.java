package com.elster.jupiter.metering;

import java.util.List;


public interface EndDeviceCapabilities {
    List<ReadingType> getConfiguredReadingTypes();
}
