package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a caching mechanism for properties of {@link HasDynamicProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-16 (13:19)
 */
public class PropertyCache<T extends HasDynamicProperties, PT extends PluggableClassUsageProperty<T>> {

    /**
     * The TimePeriod during which all properties in this cache are active.
     */
    private IntermittentInterval activePeriod;
    private Date activeDate;
    private Map<String, PT> properties = new HashMap<>();
    private boolean dirty = false;
    private PropertyFactory<T, PT> factory;

    public PropertyCache (PropertyFactory<T, PT> factory) {
        super();
        this.factory = factory;
    }

    /**
     * Tests if any changes were made to the cache,
     * i.e. if any calls to {@link #put(Date, String, Object)} were executed.
     *
     * @return A flag that indicates if the contents of the cached was modified
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Clears this cache.
     */
    public void clear () {
        this.activePeriod = null;
        this.activeDate = null;
        this.properties = new HashMap<>();
    }

    /**
     * Tests if this cache contains properties
     * that are active on the specified Date.
     *
     * @param date The Date
     * @return A flag that indicates if this cache contains properties that are active on the specified Date
     */
    public boolean isCached (Date date) {
        return (this.activePeriod != null && this.activePeriod.toSpanningInterval().contains(date, Interval.EndpointBehavior.OPEN_CLOSED))
            || (this.activeDate != null && !this.activeDate.after(date));
    }

    /**
     * Tests if this cache contains properties
     * that are active in the specified TimePeriod.
     *
     * @param period The TimePeriod
     * @return A flag that indicates if this cache contains properties that are active in the specified TimePeriod
     */
    public boolean isCached (Interval period) {
        return this.activePeriod != null && this.activePeriod.toSpanningInterval().includes(period);
    }

    /**
     * Gets the properties that are active on the specified Date,
     * loading them if necessary.
     *
     * @return The properties that are active on the specified Date
     */
    public List<PT> get (Date date) {
        if (this.isCached(date)) {
            return this.filterByDate(date);
        }
        else {
            this.clearAndLoad(date);
            return this.filterByDate(date);
        }
    }

    private void clearAndLoad(Date date) {
        this.clear();
        this.addAll(this.factory.loadProperties(date), date);
        this.dirty = false;
    }

    public void put (Date date,  String propertyName, Object value) {
        if (!this.isCached(date)) {
            this.clearAndLoad(date);
        }
        this.properties.put(propertyName, this.factory.newProperty(propertyName, value, date));
        this.dirty = true;
    }

    public void remove (Date date,  String propertyName) {
        if (!this.isCached(date)) {
            this.clearAndLoad(date);
        }
        this.properties.remove(propertyName);
        this.dirty = true;
    }

    private List<PT> filterByDate (Date date) {
        List<PT> result = new ArrayList<>(this.properties.size());    // At most all cached properties are active on the specified Date
        for (PT property : this.properties.values()) {
            if (property.getActivePeriod().contains(date, Interval.EndpointBehavior.CLOSED_CLOSED)) {
                result.add(property);
            }
        }
        return result;
    }

    /**
     * Gets the properties that are active in the specified TimePeriod,
     * loading them if necessary.
     *
     * @return The properties that are active on the specified Date
     */
    public List<PT> get (Interval period) {
        if (this.isCached(period)) {
            return this.filterByPeriod(period);
        }
        else {
            this.clear();
            this.addAll(this.factory.loadProperties(period), period);
            return this.filterByPeriod(period);
        }
    }

    private List<PT> filterByPeriod (Interval period) {
        List<PT> result = new ArrayList<>(this.properties.size());    // At most all cached properties are active in the specified TimePeriod
        for (PT property : this.properties.values()) {
            if (period.overlaps(property.getActivePeriod())) {
                result.add(property);
            }
        }
        return result;
    }

    private void addAll (List<PT> properties, Date cacheHitDate) {
        if (properties.isEmpty()) {
            this.activeDate = cacheHitDate;
        }
        else {
            this.addAll(properties);
        }
    }

    private void addAll (List<PT> properties, Interval cacheHitPeriod) {
        if (properties.isEmpty()) {
            this.activePeriod = new IntermittentInterval(cacheHitPeriod);
        }
        else {
            this.addAll(properties);
        }
    }

    private void addAll (List<PT> properties) {
        for (PT property : properties) {
            this.add(property);
        }
    }

    private void add (PT property) {
        if (this.activePeriod != null) {
            this.extendActivePeriodIfNecessary(property.getActivePeriod());
        }
        else {
            this.activePeriod = new IntermittentInterval(property.getActivePeriod());
        }
        this.properties.put(property.getName(), property);
    }

    private void extendActivePeriodIfNecessary (Interval propertyActivePeriod) {
        this.activePeriod = this.activePeriod.addInterval(propertyActivePeriod);
    }

}