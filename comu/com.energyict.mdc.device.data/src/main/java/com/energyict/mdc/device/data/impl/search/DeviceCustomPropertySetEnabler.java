package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetSearchEnabler;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(name = "com.energyict.mdc.device.data.impl.search.DeviceCustomPropertySetEnabler", service = CustomPropertySetSearchEnabler.class, immediate = true)
public class DeviceCustomPropertySetEnabler implements CustomPropertySetSearchEnabler {
    @Override
    public Class getDomainClass() {
        return Device.class;
    }

    @Override
    public boolean enableWhen(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        return constrictions
                .stream()
                .filter(constriction -> constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME))
                .flatMap(constriction -> constriction.getConstrainingValues().stream())
                .map(DeviceType.class::cast)
                .distinct()
                .flatMap(dt -> dt.getCustomPropertySets().stream())
                .map(rcps -> rcps.getCustomPropertySet().getId())
                .anyMatch(id -> id.equals(customPropertySet.getId()));
    }
}
