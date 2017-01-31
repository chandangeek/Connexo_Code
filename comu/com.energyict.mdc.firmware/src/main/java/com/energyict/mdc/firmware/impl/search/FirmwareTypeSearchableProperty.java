/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareType;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FirmwareTypeSearchableProperty implements SearchableProperty {

    static final String PROPERTY_NAME = "FIRMWARE.firmwareVersion.firmwareType";

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private SearchablePropertyGroup group;
    private SearchableProperty deviceTypeProperty;
    private FirmwareType[] firmwareTypes = new FirmwareType[0];

    @Inject
    public FirmwareTypeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    FirmwareTypeSearchableProperty init(SearchablePropertyGroup group, SearchableProperty deviceTypeProperty) {
        this.group = group;
        this.deviceTypeProperty = deviceTypeProperty;
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
                .specForValuesOf(new EnumFactory(FirmwareType.class))
                .named("firmwareVersion.firmwareType", PropertyTranslationKeys.FIRMWARE_TYPE)
                .fromThesaurus(this.thesaurus)
                .addValues(firmwareTypes)
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
        return this.thesaurus.getFormat(PropertyTranslationKeys.FIRMWARE_TYPE).format();
    }

    @Override
    public String toDisplay(Object value) {
        String type = ((FirmwareType) value).name();
        return thesaurus.getString("firmware.type." + type, type);
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.deviceTypeProperty);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        constrictions
                .stream()
                .filter(constriction -> constriction.getConstrainingProperty().hasName(this.deviceTypeProperty.getName()))
                .forEach(constriction -> {
                    if (constriction.getConstrainingValues()
                            .stream()
                            .map(DeviceType.class::cast)
                            .anyMatch(deviceType -> deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol() != null
                                    && deviceProtocolPluggableClass.getDeviceProtocol().supportsCommunicationFirmwareVersion()).orElse(false))) {
                        this.firmwareTypes = new FirmwareType[]{FirmwareType.METER, FirmwareType.COMMUNICATION};
                    } else {
                        this.firmwareTypes = new FirmwareType[]{FirmwareType.METER};
                    }
                });
    }
}
