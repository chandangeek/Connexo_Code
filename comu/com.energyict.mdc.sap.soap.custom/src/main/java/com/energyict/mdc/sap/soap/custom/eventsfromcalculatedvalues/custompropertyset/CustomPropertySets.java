/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.custom.TranslationInstaller;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;


@Component(name = "com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CustomPropertySets",
        service = CustomPropertySets.class, immediate = true)
public class CustomPropertySets {
    static final String APPLICATION_NAME = "MultiSense";

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;

    private CustomPropertySet<Device, PowerFactorDomainExtension> powerFactorInfo;
    private CustomPropertySet<Device, MaxDemandDomainExtension> maxDemandInfo;
    private CustomPropertySet<Device, CTRatioDomainExtension> ctRatioInfo;

    public CustomPropertySets() {
        // for OSGi purposes
    }

    @Inject
    public CustomPropertySets(BundleContext bundleContext, DeviceService deviceService, CustomPropertySetService customPropertySetService,
                              PropertySpecService propertySpecService, OrmService ormService,
                              Thesaurus thesaurus) {
        setDeviceService(deviceService);
        setCustomPropertySetService(customPropertySetService);
        setPropertySpecService(propertySpecService);
        setOrmService(ormService);
        this.thesaurus = thesaurus;
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        customPropertySetService.addCustomPropertySet(powerFactorInfo = new PowerFactorCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetService.addCustomPropertySet(maxDemandInfo = new MaxDemandCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetService.addCustomPropertySet(ctRatioInfo = new CTRatioCustomPropertySet(propertySpecService, thesaurus));
    }

    @Deactivate
    public void deactivate() {
        customPropertySetService.removeCustomPropertySet(powerFactorInfo);
        customPropertySetService.removeCustomPropertySet(maxDemandInfo);
        customPropertySetService.removeCustomPropertySet(ctRatioInfo);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setThesaurus(TranslationInstaller translationInstaller) {
        this.thesaurus = translationInstaller.getThesaurus();
    }
}
