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
    List<RegisteredCustomPropertySet> getMetrologyCustomPropertySets();

    /**
     * Returns a map with values for custom properties sets inherited from linked metrology configuration.
     * For time-sliced custom property sets it returns values which are actual at this moment.
     * If the usage point has no linked metrology configuration, the empty map will be returned.
     *
     * @return Values for custom properties sets inherited from linked metrology configuration.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyConfigurationCustomPropertySetValues();

    /**
     * Returns a map with values for custom properties sets inherited from linked metrology configuration.
     * If the usage point has no linked metrology configuration, the empty map will be returned.
     *
     * @param effectiveTimeStamp The point in time for time-sliced custom property sets values.
     * @return Values for custom properties sets inherited from linked metrology configuration.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyConfigurationCustomPropertySetValues(Instant effectiveTimeStamp);

    /**
     * Sets values for custom property sets which was inherited from linked metrology configuration.
     *
     * @param customPropertySet      custom property sets whose values we want to save.
     * @param customPropertySetValue filled values for a custom properties set.
     * @throws UsagePointCustomPropertySetValuesManageException in cases:
     * <li>there is no linked metrology configuration</li>
     * <li>there is no linked custom property set on metrology configuration</li>
     * <li>current user has not sufficient privileges</li>
     */
    void setMetrologyConfigurationCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue);

    /**
     * @return List of all registered custom property sets from service category.
     */
    List<RegisteredCustomPropertySet> getServiceCategoryPropertySets();

    /**
     * Returns a map with values for custom properties sets from service category.
     * For time-sliced custom property sets it returns values which are actual at this moment.
     *
     * @return Values for custom properties sets from service category.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getServiceCategoryCustomPropertySetValues();

    /**
     * Returns a map with values for custom properties sets from service category.
     *
     * @param effectiveTimeStamp The point in time for time-sliced custom property sets values.
     * @return Values for custom properties sets from service category.
     */
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getServiceCategoryCustomPropertySetValues(Instant effectiveTimeStamp);

    /**
     * Sets values for custom property sets on service category.
     *
     * @param customPropertySet      custom property sets whose values we want to save.
     * @param customPropertySetValue filled values for a custom properties set.
     * @throws UsagePointCustomPropertySetValuesManageException in cases:
     * <li>there is no linked custom property set on service category</li>
     * <li>current user has not sufficient privileges</li>
     */
    void setServiceCategoryCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue);
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues();
    Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(Instant effectiveTimeStamp);
}
