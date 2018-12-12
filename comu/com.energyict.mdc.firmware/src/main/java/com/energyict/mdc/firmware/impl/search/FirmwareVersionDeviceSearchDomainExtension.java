/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.search;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component(name = "com.energyict.mdc.firmware.impl.search.FirmwareVersionDeviceSearchDomainExtension", service = SearchDomainExtension.class, immediate = true)
public class FirmwareVersionDeviceSearchDomainExtension implements SearchDomainExtension {

    private volatile FirmwareServiceImpl firmwareService;

    @SuppressWarnings("unused")
    public FirmwareVersionDeviceSearchDomainExtension() {
    }

    @Inject
    public FirmwareVersionDeviceSearchDomainExtension(FirmwareService firmwareService) {
        this();
        this.setFirmwareService(firmwareService);
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = (FirmwareServiceImpl) firmwareService;
    }

    @Override
    public boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        return domain.getDomainClass().isAssignableFrom(Device.class) &&
                constrictions
                        .stream()
                        .map(SearchablePropertyConstriction::getConstrainingProperty)
                        .anyMatch(property -> property.hasName(DeviceFields.DEVICETYPE.fieldName()));
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        Optional<SearchablePropertyConstriction> deviceTypePropertyConstriction = constrictions
                .stream()
                .filter(constriction -> constriction.getConstrainingProperty().hasName(DeviceFields.DEVICETYPE.fieldName()))
                .findFirst();
        if (deviceTypePropertyConstriction.isPresent() && !deviceTypePropertyConstriction.get().getConstrainingValues().isEmpty()) {
            DataModel injector = this.firmwareService.getDataModel();
            FirmwareSearchablePropertyGroup firmwareVersionGroup = injector.getInstance(FirmwareSearchablePropertyGroup.class);
            FirmwareTypeSearchableProperty firmwareTypeSearchableProperty = injector.getInstance(FirmwareTypeSearchableProperty.class)
                    .init(firmwareVersionGroup, deviceTypePropertyConstriction.get().getConstrainingProperty());
            return Arrays.asList(
                    firmwareTypeSearchableProperty,
                    injector.getInstance(FirmwareVersionSearchableProperty.class)
                            .init(firmwareVersionGroup, deviceTypePropertyConstriction.get().getConstrainingProperty(), firmwareTypeSearchableProperty)
            );
        }
        return Collections.emptyList();
    }

    @Override
    public SqlFragment asFragment(List<SearchablePropertyCondition> conditions) {
        return this.firmwareService.getDataModel().query(ActivatedFirmwareVersion.class, FirmwareVersion.class)
                .asFragment(conditions.stream().reduce(Condition.TRUE, (ormCondition, searchCondition) -> ormCondition.and(searchCondition.getCondition()), Condition::and),
                        "device");
    }
}
