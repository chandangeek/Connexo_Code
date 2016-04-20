package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetSearchEnabler;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import org.osgi.service.component.annotations.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.data.impl.search.DeviceCustomPropertySetEnabler", service = CustomPropertySetSearchEnabler.class, immediate = true)
public class DeviceCustomPropertySetEnabler implements CustomPropertySetSearchEnabler {
    @Override
    public Class getDomainClass() {
        return Device.class;
    }

    private Set<DeviceType> getDeviceTypesFromConstrictions(List<SearchablePropertyConstriction> constrictions) {
        Set<DeviceType> deviceTypes = new HashSet<>();
        for (SearchablePropertyConstriction constriction : constrictions) {
            if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
                constriction.getConstrainingValues().stream().map(DeviceType.class::cast).forEach(deviceTypes::add);
            }
            if (constriction.getConstrainingProperty().hasName(DeviceConfigurationSearchableProperty.PROPERTY_NAME)) {
                constriction.getConstrainingValues()
                        .stream()
                        .map(DeviceConfiguration.class::cast)
                        .map(DeviceConfiguration::getDeviceType)
                        .forEach(deviceTypes::add);
            }
        }
        return deviceTypes;
    }

    @Override
    public boolean enableWhen(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        if (constrictions.isEmpty()) {
            return false;
        }
        return getDeviceTypesFromConstrictions(constrictions)
                .stream()
                .flatMap(dt -> dt.getCustomPropertySets().stream())
                .map(rcps -> rcps.getCustomPropertySet().getId())
                .anyMatch(id -> id.equals(customPropertySet.getId()));
    }
}
