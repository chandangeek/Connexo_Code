/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link Builder} that creates a {@link EndDeviceGroup} for the given device types
 */
public class DeviceGroupBuilder extends NamedBuilder<EndDeviceGroup, DeviceGroupBuilder> {

    public static final String PROPERTY_DEVICE_NAME = "name";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";

    private final MeteringGroupsService meteringGroupsService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final SearchService searchService;

    private String namePrefix;
    private List<String> deviceTypes;
    private List<String> searchablePropertyNames = new ArrayList<>();

    @Inject
    public DeviceGroupBuilder(MeteringGroupsService meteringGroupsService, DeviceConfigurationService deviceConfigurationService, SearchService searchService) {
        super(DeviceGroupBuilder.class);
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.searchService = searchService;
    }

    public DeviceGroupBuilder withNamePrefix(String namePrefix) {
        if (namePrefix == null) {
            searchablePropertyNames.remove(PROPERTY_DEVICE_NAME);
        } else {
            searchablePropertyNames.add(PROPERTY_DEVICE_NAME);
        }

        this.namePrefix = namePrefix;
        return this;
    }

    public DeviceGroupBuilder withDeviceTypes(List<String> deviceTypes) {
        searchablePropertyNames.add(PROPERTY_DEVICE_TYPE);
        this.deviceTypes = deviceTypes;
        return this;
    }

    @Override
    public Optional<EndDeviceGroup> find() {
        return meteringGroupsService.findEndDeviceGroupByName(getName());
    }

    @Override
    public EndDeviceGroup create() {
        Log.write(this);
        EndDeviceGroup endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(getSearchablePropertyValues())
                .setName(getName())
                .setSearchDomain(findDeviceSearchDomain())
                .setMRID("MDC:" + getName())
                .setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider")
                .create();
        applyPostBuilders(endDeviceGroup);
        return endDeviceGroup;
    }

    private SearchDomain findDeviceSearchDomain() {
        return searchService.findDomain(Device.class.getName()).orElseThrow(() -> new UnableToCreate("Unable to find device search domain"));
    }

    protected SearchablePropertyValue[] getSearchablePropertyValues() {
        List<SearchablePropertyValue> values = new ArrayList<>(searchablePropertyNames.size());
        if (searchablePropertyNames.contains(PROPERTY_DEVICE_NAME)) {
            values.add(createSearchablePropertyValue(PROPERTY_DEVICE_NAME, Collections.singletonList(namePrefix)));
        }
        if (searchablePropertyNames.contains(PROPERTY_DEVICE_TYPE)) {
            values.add(createSearchablePropertyValue(PROPERTY_DEVICE_TYPE, getDeviceTypes().stream().map(HasId::getId).map(Object::toString).collect(Collectors.toList())));
        }
        return values.toArray(new SearchablePropertyValue[searchablePropertyNames.size()]);
    }

    private SearchablePropertyValue createSearchablePropertyValue(String searchableProperty, List<String> values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = searchableProperty;
        valueBean.operator = SearchablePropertyOperator.EQUAL;
        valueBean.values = values;
        return new SearchablePropertyValue(null, valueBean);
    }

    private List<DeviceType> getDeviceTypes() {
        List<DeviceType> result = new ArrayList<>();
        for (String deviceType : deviceTypes) {
            DeviceType deviceTypeByName = deviceConfigurationService.findDeviceTypeByName(deviceType)
                    .orElseThrow(() -> new UnableToCreate("Unable to find device type with name " + deviceType));
            result.add(deviceTypeByName);
        }
        return result;
    }
}
