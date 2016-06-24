package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.properties.PropertySpecService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;

@Component(name = "com.energyict.mdc.device.config.cps.CustomPropertySetsDemoInstaller", service = {CustomPropertySetsDemoInstaller.class}, property = "name=CPD", immediate = true)
public class CustomPropertySetsDemoInstaller {

    public static final String COMPONENT_NAME = "CPD";

    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private List<CustomPropertySet> sets = new ArrayList<>();

    public CustomPropertySetsDemoInstaller() {
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCustomPropertySet(CustomPropertySet customPropertySet) {
        if (customPropertySet.getClass().getName().startsWith(this.getClass().getPackage().getName())) {
            sets.add(customPropertySet);
        }
        customPropertySetService.addCustomPropertySet(customPropertySet);
    }

    public void removeCustomPropertySet(CustomPropertySet customPropertySet) {
        sets.remove(customPropertySet);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }
}