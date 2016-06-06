package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ReadingType;

import java.util.Collections;
import java.util.List;

public final class EndDeviceCapabilities {
    private List<ReadingType> readingTypes;
    private List<EndDeviceControlType> controlTypes;

    public EndDeviceCapabilities(List<ReadingType> readingTypes, List<EndDeviceControlType> controlTypes) {
        // TODO : secure this : do not assign parameter to the actual member
        this.readingTypes = readingTypes;
        this.controlTypes = controlTypes;
    }

    public List<ReadingType> getConfiguredReadingTypes() {
       return Collections.unmodifiableList(readingTypes);
    }

    public List<EndDeviceControlType> getSupportedControlTypes() {
        return Collections.unmodifiableList(controlTypes);
    }
}
