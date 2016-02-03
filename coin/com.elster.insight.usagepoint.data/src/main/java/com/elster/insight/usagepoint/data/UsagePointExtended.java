package com.elster.insight.usagepoint.data;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.Map;

/**
 * This temporary interface extension, the sole purpose is provide functionality which relates to metrology configuration.
 * When metrology configuration will be moved to the metering bundle all this methods should be moved to the
 * {@link UsagePoint} interface.
 */
// TODO move all functionality to the UsagePoint interface
public interface UsagePointExtended extends UsagePoint {

    /**
     * Returns an unmodifiable map with values for custom properties sets inherited from linked metrology configuration.
     * For time-sliced custom property sets it returns values which are actual at this moment.
     * If the usage point has no linked metrology configuration, the empty map will be returned.
     *
     * @return Values for custom properties sets inherited from linked metrology configuration.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyCustomPropertySetValues();

    /**
     * Returns an unmodifiable map with values for custom properties sets inherited from linked metrology configuration.
     * If the usage point has no linked metrology configuration, the empty map will be returned.
     *
     * @param effectiveTimeStamp The point in time for time-sliced custom property sets values.
     * @return Values for custom properties sets inherited from linked metrology configuration.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyCustomPropertySetValues(Instant effectiveTimeStamp);

    /**
     * Sets values for custom property sets which was inherited from linked metrology configuration.
     *
     * @param customPropertySet      custom property sets whose values we want to save.
     * @param customPropertySetValue filled values for a custom properties set.
     * @throws
     */
    void setMetrologyCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue);
}
