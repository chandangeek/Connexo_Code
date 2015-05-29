package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceServiceImpl;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchProvider;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link SearchDomain}
 * interface that supports {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:32)
 */
public class DeviceSearchDomain implements SearchDomain {

    private final DeviceServiceImpl deviceService;
    private final PropertySpecService propertySpecService;
    private final Clock clock;

    public DeviceSearchDomain(DeviceServiceImpl deviceService, PropertySpecService propertySpecService, Clock clock) {
        super();
        this.deviceService = deviceService;
        this.propertySpecService = propertySpecService;
        this.clock = clock;
    }

    @Override
    public String getId() {
        return Device.class.getName();
    }

    @Override
    public boolean supports(Class aClass) {
        return Device.class.equals(aClass);
    }

    @Override
    public SearchProvider getProvider() {
        return this.deviceService;
    }

    @Override
    public List<SearchableProperty> getProperties() {
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this, this.propertySpecService);
        return Arrays.asList(
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService),
                deviceTypeSearchableProperty,
                new StateNameSearchableProperty(this, deviceTypeSearchableProperty, this.propertySpecService));
    }

    @Override
    public Finder<Device> finderFor(List<SearchablePropertyCondition> conditions) {
        return new DeviceFinder(
                new DeviceSearchSqlBuilder(this.deviceService.dataModel(), conditions, this.clock.instant()),
                this.deviceService.dataModel());
    }

}