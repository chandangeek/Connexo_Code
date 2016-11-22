package com.energyict.mdc.upl;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Groups all the services that universal protocols will be needing.
 * As the platform starts up, it will want/need to provide implementations
 * for the services by calling the corresponding setter method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (09:29)
 */
public class Services {

    private static AtomicReference<PropertySpecService> PROPERTY_SPEC_SERVICE = new AtomicReference<>();
    private static AtomicReference<NlsService> NLS_SERVICE = new AtomicReference<>();

    public static PropertySpecService propertySpecService() {
        return PROPERTY_SPEC_SERVICE.get();
    }

    public static void propertySpecService(PropertySpecService propertySpecService) {
        PROPERTY_SPEC_SERVICE.set(propertySpecService);
    }

    public static NlsService nlsService() {
        return NLS_SERVICE.get();
    }

    public static void nlsService(NlsService nlsService) {
        NLS_SERVICE.set(nlsService);
    }

}