package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Sole purpose is to have OSGi inject the {@link PropertySpecService}
 * so that I can publish it on a bus for the various enum classes
 * that need a PropertySpecService but can't get it injected through guice.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-16 (13:35)
 */
@Component(name = "com.energyict.mdc.service.propertyspecservicedependency", service = PropertySpecServiceDependency.class, immediate = true)
public class PropertySpecServiceDependency {

    private volatile PropertySpecService propertySpecService;

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate () {
        Bus.setPropertySpecService(this.propertySpecService);
    }

    @Deactivate
    public void deactivate () {
        Bus.clearPropertySpecService(this.propertySpecService);
    }

}