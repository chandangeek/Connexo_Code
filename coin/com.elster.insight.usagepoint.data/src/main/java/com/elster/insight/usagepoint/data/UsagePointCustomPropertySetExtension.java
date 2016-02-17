package com.elster.insight.usagepoint.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.insight.usagepoint.data.impl.exceptions.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    List<RegisteredCustomPropertySet> getCustomPropertySetsOnMetrologyConfiguration();

    /**
     * @return List of all registered custom property sets from service category.
     */
    List<RegisteredCustomPropertySet> getCustomPropertySetsOnServiceCategory();

    /**
     * @return List of all registered custom property sets which are available for that usage point.
     * In fact it is combination of {@link #getCustomPropertySetsOnMetrologyConfiguration()} and
     * {@link #getCustomPropertySetsOnServiceCategory()}
     */
    List<RegisteredCustomPropertySet> getAllCustomPropertySets();

    /**
     * Returns a map with values for provided custom properties sets. If there is no persisted value for
     * the given set, the resulting map will contain the <code>null</code> value. If a registered custom property
     * set doesn't relate to the usage point the <code>null</code> value will be returned. If a registered custom property
     * set has different domain (not a {@link UsagePoint}) the {@link UsagePointCustomPropertySetValuesManageException} will be thrown.
     *
     * @param registeredCustomPropertySets Custom property sets whose values we want to read.
     * @return Values for provided custom properties sets.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(List<RegisteredCustomPropertySet> registeredCustomPropertySets);

    /**
     * Returns a map with values for provided custom properties sets. If there is no persisted value for
     * the given set, the resulting map will contain the <code>null</code> value. If a registered custom property
     * set doesn't relate to the usage point the <code>null</code> value will be returned. If a registered custom property
     * set has different domain (not a {@link UsagePoint}) the {@link IllegalArgumentException} will be thrown.
     *
     * @param registeredCustomPropertySets Custom property sets whose values we want to read.
     * @param effectiveTimeStamp           The point in time for time-sliced custom property sets values.
     * @return Values for provided custom properties sets.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(List<RegisteredCustomPropertySet> registeredCustomPropertySets, Instant effectiveTimeStamp);

    /**
     * Sets values for custom property sets
     *
     * @param customPropertySet      custom property sets whose values we want to save.
     * @param customPropertySetValue filled values for a custom properties set.
     * @throws UsagePointCustomPropertySetValuesManageException in cases:
     *                                                          <li>there is no linked custom property set</li>
     *                                                          <li>current user has not sufficient privileges</li>
     */
    void setCustomPropertySetValue(CustomPropertySet<UsagePoint, ?> customPropertySet, CustomPropertySetValues customPropertySetValue);

}
