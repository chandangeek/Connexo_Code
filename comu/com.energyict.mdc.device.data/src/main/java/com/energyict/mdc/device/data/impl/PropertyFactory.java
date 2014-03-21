package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.dynamic.HasDynamicProperties;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

import java.util.Date;
import java.util.List;

/**
 * Defines the behavior of a component that will provide factory
 * services for properties of {@link HasDynamicProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-16 (13:36)
 */
public interface PropertyFactory<T extends HasDynamicProperties, PT extends PluggableClassUsageProperty<T>> {

    /**
     * Loads the properties that are active on the specified Date.
     *
     * @param date The Date
     * @return The properties that are active on the specified Date.
     */
    public List<PT> loadProperties (Date date);

    /**
     * Loads the properties that are active in the specified TimePeriod.
     *
     * @param period The TimePeriod
     * @return The properties that are active in the specified TimePeriod.
     */
    public List<PT> loadProperties (Interval period);

    /**
     * Creates a new PT with the specified name and value
     * that is active from the specified active date.
     *
     * @param name The name of the Property
     * @param value The value of the Property
     * @param activeDate The Date on which the value becomes active
     * @return The newly created PT
     */
    public PT newProperty(String name, Object value, Date activeDate);

}