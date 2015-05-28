package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceServiceImpl;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchProvider;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;

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

    public DeviceSearchDomain(DeviceServiceImpl deviceService, PropertySpecService propertySpecService) {
        super();
        this.deviceService = deviceService;
        this.propertySpecService = propertySpecService;
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
        return DefaultFinder.of(Device.class, this.toCondition(conditions), this.deviceService.dataModel());
    }

    private Condition toCondition(List<SearchablePropertyCondition> conditions) {
        return conditions
                .stream()
                .map(ConditionBuilder::new)
                .reduce(
                    Condition.TRUE,
                    (underConstruction, builder) -> underConstruction.and(builder.build()),
                    Condition::and);
    }

    private class ConditionBuilder {
        private final SearchablePropertyCondition spec;
        private final SearchableDeviceProperty property;

        private ConditionBuilder(SearchablePropertyCondition spec) {
            super();
            this.spec = spec;
            this.property = (SearchableDeviceProperty) spec.getProperty();
        }

        private Condition build() {
            return this.property.toCondition(this.spec.getCondition());
        }

    }

}