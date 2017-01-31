/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class FirmwareVersionSearchableProperty implements SearchableProperty {

    static final String PROPERTY_NAME = "FIRMWARE.firmwareVersion";

    private final FirmwareServiceImpl firmwareService;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private SearchablePropertyGroup group;
    private SearchableProperty deviceTypeProperty;
    private SearchableProperty firmwareTypeProperty;

    private FirmwareVersion[] firmwareVersions = new FirmwareVersion[0];

    @Inject
    public FirmwareVersionSearchableProperty(PropertySpecService propertySpecService, FirmwareService firmwareService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.firmwareService = (FirmwareServiceImpl) firmwareService;
        this.thesaurus = thesaurus;
    }

    FirmwareVersionSearchableProperty init(SearchablePropertyGroup group, SearchableProperty deviceTypeProperty, SearchableProperty firmwareTypeSearchableProperty) {
        this.group = group;
        this.deviceTypeProperty = deviceTypeProperty;
        this.firmwareTypeProperty = firmwareTypeSearchableProperty;
        return this;
    }

    @Override
    public SearchDomain getDomain() {
        return null;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .referenceSpec(FirmwareVersion.class)
                .named("firmwareVersion", PropertyTranslationKeys.FIRMWARE_VERSION)
                .fromThesaurus(this.thesaurus)
                .addValues(this.firmwareVersions)
                .markExhaustive()
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.FIRMWARE_VERSION).format();
    }

    @Override
    public String toDisplay(Object value) {
        return ((FirmwareVersion) value).getFirmwareVersion();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Arrays.asList(this.deviceTypeProperty, this.firmwareTypeProperty);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        List<FirmwareType> firmwareTypes = constrictions
                .stream()
                .filter(constriction -> constriction.getConstrainingProperty().hasName(this.firmwareTypeProperty.getName()))
                .flatMap(constriction -> constriction.getConstrainingValues().stream())
                .map(FirmwareType.class::cast)
                .collect(Collectors.toList());
        List<DeviceType> deviceTypes = constrictions
                .stream()
                .filter(constriction -> constriction.getConstrainingProperty().hasName(this.deviceTypeProperty.getName()))
                .flatMap(constriction -> constriction.getConstrainingValues().stream())
                .map(DeviceType.class::cast)
                .collect(Collectors.toList());
        if (firmwareTypes.isEmpty() && deviceTypes.isEmpty()) {
            return;
        }
        Condition condition = Condition.TRUE;
        if (!firmwareTypes.isEmpty()) {
            condition = condition.and(where("firmwareType").in(firmwareTypes));
        }
        if (!deviceTypes.isEmpty()) {
            condition = condition.and(where("deviceType").in(deviceTypes));
        }
        List<FirmwareVersion> firmwareVersions = this.firmwareService.getDataModel().query(FirmwareVersion.class).select(condition);
        this.firmwareVersions = firmwareVersions.toArray(new FirmwareVersion[firmwareVersions.size()]);
    }
}
