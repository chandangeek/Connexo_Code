/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private RangeSet<Instant> activePeriod;
    private Instant activeDate;
    private Map<String, PT> properties = new HashMap<>();
    private boolean dirty = false;
    private PropertyFactory<T, PT> factory;

    public PropertyCache (PropertyFactory<T, PT> factory) {
        super();
        this.factory = factory;
    }

    /**
     * Tests if any changes were made to the cache,
     * i.e. if any calls to {@link #put(Instant, String, Object)} were executed.
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
    public boolean isCached (Instant date) {
        return (this.activePeriod != null && this.activePeriod.span().contains(date))
                || (this.activeDate != null && !this.activeDate.isAfter(date));
    }

    /**
     * Tests if this cache contains properties
     * that are active in the specified TimePeriod.
     *
     * @param period The TimePeriod
     * @return A flag that indicates if this cache contains properties that are active in the specified TimePeriod
     */
    public boolean isCached (Range<Instant> period) {
        return this.activePeriod != null && this.activePeriod.span().encloses(period);
    }

    /**
     * Gets the properties that are active on the specified Date,
     * loading them if necessary.
     *
     * @return The properties that are active on the specified Date
     */
    public List<PT> get (Instant date) {
        if (this.isCached(date)) {
            return this.filterByDate(date);
        }
        else {
            this.clearAndLoad(date);
            return this.filterByDate(date);
        }
    }

    private void clearAndLoad(Instant date) {
        this.clear();
        this.addAll(this.factory.loadProperties(date), date);
        this.dirty = false;
    }

    public void put (Instant date,  String propertyName, Object value) {
        if (!this.isCached(date)) {
            this.clearAndLoad(date);
        }
        this.properties.put(propertyName, this.factory.newProperty(propertyName, value, date));
        this.dirty = true;
    }

    public void remove (Instant date,  String propertyName) {
        if (!this.isCached(date)) {
            this.clearAndLoad(date);
        }
        this.properties.remove(propertyName);
        this.dirty = true;
    }

    private List<PT> filterByDate (Instant date) {
        return this.properties
                .values()
                .stream()
                .filter(property -> property.getActivePeriod().contains(date))
                .collect(Collectors.toList());
    }

    private void addAll (List<PT> properties, Instant cacheHitDate) {
        if (properties.isEmpty()) {
            this.activeDate = cacheHitDate;
        }
        else {
            this.addAll(properties);
        }
    }

    private void addAll (List<PT> properties) {
        properties.forEach(this::add);
    }

    private void add (PT property) {
        if (this.activePeriod != null) {
            this.extendActivePeriodIfNecessary(property.getActivePeriod());
        }
        else {
            this.activePeriod = TreeRangeSet.create();
            activePeriod.add(property.getActivePeriod());
        }
        this.properties.put(property.getName(), property);
    }

    private void extendActivePeriodIfNecessary (Range<Instant> propertyActivePeriod) {
        this.activePeriod.add(propertyActivePeriod);
    }

}