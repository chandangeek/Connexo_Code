package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
/**
 * Provides shortcuts for custom property set management for the specific usage point and time-sliced custom property set
 */
public interface UsagePointVersionedPropertySet extends UsagePointPropertySet {

    /**
     * Returns property set values for the specific version.
     *
     * @param anyTimeInVersionInterval pointer for the specific property set version
     * @return Property set values.
     */
    CustomPropertySetValues getVersionValues(Instant anyTimeInVersionInterval);

    /**
     * SSets property set values for the specific version. It also updates version's boundaries by effective range from values.
     *
     * @param anyTimeInVersionInterval pointer for the specific property set version
     * @param values                   property set values
     */
    void setVersionValues(Instant anyTimeInVersionInterval, CustomPropertySetValues values);

    /**
     * @return All property set versions values
     */
    List<CustomPropertySetValues> getAllVersionValues();

    /**
     * @return Default interval for the new property set version.
     */
    Range<Instant> getNewVersionInterval();
}
