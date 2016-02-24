package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.google.common.collect.Range;

import java.time.Instant;
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
     * Returns a value for provided custom properties set.
     * <ul>
     * <li>If there is no persisted value for the given set, the <code>null</code> value will be returned.</li>
     * <li>If a registered custom property set doesn't relate to the usage point, the <code>null</code> value will be returned.</li>
     * <li>If a registered custom property set domain differs from {@link UsagePoint} the
     * {@link UsagePointCustomPropertySetValuesManageException} will be thrown.</li>
     * </ul>
     *
     * @param registeredCustomPropertySet Custom property set whose value we want to read.
     * @return Value for provided custom properties set.
     */
    CustomPropertySetValues getCustomPropertySetValue(RegisteredCustomPropertySet registeredCustomPropertySet);

    /**
     * Returns a value for provided custom properties set.
     * <ul>
     * <li>If there is no active version value for the given versionStartTime, the <code>null</code> value will be returned.</li>
     * <li>If a registered custom property set doesn't relate to the usage point, the <code>null</code> value will be returned.</li>
     * <li>If a registered custom property set domain differs from {@link UsagePoint} the
     * {@link UsagePointCustomPropertySetValuesManageException} will be thrown.</li>
     * </ul>
     *
     * @param registeredCustomPropertySet Custom property set whose value we want to read.
     * @param versionStartTime            The version start time for time-sliced custom attribute sets.
     *                                    For regular custom property sets this parameter is ignored.
     * @return Value for provided custom properties set.
     */
    CustomPropertySetValues getCustomPropertySetValue(RegisteredCustomPropertySet registeredCustomPropertySet, Instant versionStartTime);

    /**
     * Sets values for custom property sets
     *
     * @param customPropertySet      Custom property sets whose values we want to save.
     * @param customPropertySetValue Filled values for a custom properties set.
     * @throws UsagePointCustomPropertySetValuesManageException in cases:
     *                                                          <li>there is no linked custom property set</li>
     *                                                          <li>current user has not sufficient privileges</li>
     */
    void setCustomPropertySetValue(CustomPropertySet<UsagePoint, ?> customPropertySet, CustomPropertySetValues customPropertySetValue);

    void setCustomPropertySetValue(CustomPropertySet<UsagePoint, ?> customPropertySet, CustomPropertySetValues customPropertySetValue, Instant effectiveTimeStamp);

    /**
     * Provides an interval for new version on time-sliced custom attribute set.
     *
     * @param registeredCustomPropertySet Custom property set for whom we want to calculate the interval.
     * @return Interval for new version on time-sliced custom attribute set.
     * @throws UsagePointCustomPropertySetValuesManageException in cases:
     *                                                          <li>there is no linked custom property set</li>
     *                                                          <li>custom property set is not versioned (time-sliced)</li>
     *                                                          <li>custom property set domain differs from {@link UsagePoint}</li>
     */
    Range<Instant> getCurrentInterval(RegisteredCustomPropertySet registeredCustomPropertySet);
}
