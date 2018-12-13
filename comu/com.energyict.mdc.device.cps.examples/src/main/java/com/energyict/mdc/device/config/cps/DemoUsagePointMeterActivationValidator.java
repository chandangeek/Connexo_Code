/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.device.config.cps.DemoUsagePointMeterActivationValidator",
        service = {CustomUsagePointMeterActivationValidator.class}, immediate = true)
@SuppressWarnings("unused")
public class DemoUsagePointMeterActivationValidator implements CustomUsagePointMeterActivationValidator {

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Thesaurus thesaurus;

    public DemoUsagePointMeterActivationValidator() {
    }

    @Inject
    public DemoUsagePointMeterActivationValidator(DeviceService deviceService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.setDeviceService(deviceService);
        this.setCustomPropertySetService(customPropertySetService);
        this.thesaurus = thesaurus;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus("CPS", Layer.DOMAIN);
    }

    @Override
    public void validateActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) {
        if ((usagePoint.getServiceCategory().getKind() == ServiceKind.ELECTRICITY)) {
            if (checkUsagePointConditions(usagePoint)) {
                if (!checkMeterConditions(meter)) {
                    throw new CustomUsagePointMeterActivationValidationException(thesaurus, MessageSeeds.WRONG_METERMECHANISM_VALUE_EXCEPTION, "Prepay", "Meter mechanism");
                }
            }
        }
    }

    private boolean checkMeterConditions(Meter meter) {
        Device device = deviceService.findDeviceById(Long.parseLong(meter.getAmrId())).get();
        return device.getDeviceType()
                .getCustomPropertySets()
                .stream()
                .anyMatch(cas -> "MeterSpecs".equals(cas.getCustomPropertySet().getName())
                        && "Credit".equals(customPropertySetService.getUniqueValuesFor(cas.getCustomPropertySet(), device).getProperty("meterMechanism")));
    }

    private boolean checkUsagePointConditions(UsagePoint usagePoint) {
        return usagePoint.forCustomProperties()
                .getAllPropertySets()
                .stream()
                .filter(cas -> cas.getCustomPropertySet().getId().equals("com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension"))
                .anyMatch(cas -> cas.getValues() != null
                        && Boolean.TRUE.equals(cas.getValues().getProperty("prepay")));
    }
}
