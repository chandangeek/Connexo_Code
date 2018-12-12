/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable;

import com.elster.jupiter.properties.HasDynamicProperties;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Holds the value of a property of a {@link PluggableClass}.
 * Note that values of properties are versioned over time
 * so a PluggableClassUsageProperty has a time interval during
 * which the property was active.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-10 (09:34)
 */
public interface PluggableClassUsageProperty<T extends HasDynamicProperties> {

    /**
     * Gets the {@link PluggableClass}
     * that represents the {@link T} and its property configuration.
     *
     * @return The PluggableClass
     */
    PluggableClass getPluggableClass();

    /**
     * Gets the name of the property for which a value is held.
     *
     * @return The name of the property
     */
    String getName();

    /**
     * Gets the value of the property.
     *
     * @return The value
     */
    Object getValue();

    /**
     * Tests if this PluggableClassUsageProperty was inherited from
     * a higher level and is therefore not defined on the object that returned it.
     * Note that not all higher level object have activity periods for
     * properties and that therefore the activity period of an inherited
     * property may not be defined. In that case, a TimePeriod(null, null)
     * will be returned, i.e. a TimePeriod that spans from the early
     * big bang until forever.
     *
     * @return A flag that indicates if the property was inherited
     */
    boolean isInherited();

    /**
     * Gets the time Interval during which this PluggableClassUsageProperty was active.
     *
     * @return The activity period
     */
    Range<Instant> getActivePeriod();

}