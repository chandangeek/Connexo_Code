package com.elster.insight.usagepoint.data;


import java.util.Optional;

public interface UsagePointDataService {
    String COMPONENT_NAME = "UDC";

    /**
     * Returns an extended usage point which supports functionality which relates to metrology configurations.
     * @param mrid usage point mrid
     * @return A wrapped usage point.
     */
    // TODO remove when {@link MetrologyConfiguration} will be moved to the metering bundle.
    Optional<UsagePointExtended> findUsagePointByMrid(String mrid);
}