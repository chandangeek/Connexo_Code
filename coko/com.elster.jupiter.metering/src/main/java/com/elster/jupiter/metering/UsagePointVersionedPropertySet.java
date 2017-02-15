/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    @Override
    /**
     * Sets property set values for active version. The same as #setVersionValues(now(), value).
     *
     * @param value custom property set values
     * @see #setVersionValues(Instant, CustomPropertySetValues)
     */
    void setValues(CustomPropertySetValues value);

    /**
     * Sets property set values for the specific version. It also updates version's boundaries by effective range from values.
     * If the anyTimeInVersionInterval parameter is null then new version will be created
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
