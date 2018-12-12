/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * This interface provides functionality for custom property set management on usage points.
 */
@ProviderType
public interface UsagePointCustomPropertySetExtension {

    /**
     * @return The usage point instance.
     */
    UsagePoint getUsagePoint();

    /**
     * @return List of all registered custom property sets for linked metrology configuration,
     * on an empty list if there is no linked metrology configuration
     */
    List<UsagePointPropertySet> getPropertySetsOnMetrologyConfiguration();

    /**
     * @return List of all registered custom property sets from service category.
     */
    List<UsagePointPropertySet> getPropertySetsOnServiceCategory();

    /**
     * @return List of all registered custom property sets which are available for that usage point.
     * In fact it is combination of {@link #getPropertySetsOnMetrologyConfiguration()} and
     * {@link #getPropertySetsOnServiceCategory()}
     */
    List<UsagePointPropertySet> getAllPropertySets();

    UsagePointPropertySet getPropertySet(long registeredCustomPropertySetId);

    UsagePointVersionedPropertySet getVersionedPropertySet(long registeredCustomPropertySetId);
}
