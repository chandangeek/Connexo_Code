/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;

@ProviderType
/**
 * Provides shortcuts for custom property set management for the specific usage point and custom property set
 */
public interface UsagePointPropertySet extends RegisteredCustomPropertySet {
    @Override
    CustomPropertySet<UsagePoint, ?> getCustomPropertySet();

    /**
     * @return Property set values actual at this time for the custom property set. Can be <code>null</code> for time-sliced
     * properties if there is no actual version at this moment.
     */
    CustomPropertySetValues getValues();

    /**
     * Sets property set values.
     *
     * @param value custom property set values
     */
    void setValues(CustomPropertySetValues value);

    /**
     * @return The usage point which owns the property set.
     */
    UsagePoint getUsagePoint();
}
