package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;


public class DemoUsagePointMeterActivationValidator implements CustomUsagePointMeterActivationValidator {

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Thesaurus thesaurus;


    @Override
    public void validateActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) {
        if ((usagePoint.getServiceCategory().getKind() == ServiceKind.ELECTRICITY) && ((UsagePointMetrologyConfiguration) usagePoint.getMetrologyConfiguration().get())
                .getUsagePointRequirements()
                .stream()
                .anyMatch(r -> r.toValueBean().propertyName.equals("prepay") &&
                        r.toValueBean().operator == SearchablePropertyOperator.EQUAL &&
                        r.toValueBean().values.stream().anyMatch(v -> v.equals("true")))) {
            if (!checkConditions(meter, usagePoint)) {
                throw new CustomUsagePointMeterActivationValidationException(thesaurus, MessageSeeds.WRONG_PREPAY_AND_METERMECHANISM_EXCEPTION);
            }
        }
    }

    private boolean checkConditions(Meter meter, UsagePoint usagePoint) {
        Device device = deviceService.findByUniqueMrid(meter.getMRID()).get();
        return usagePoint.forCustomProperties()
                .getAllPropertySets()
                .stream()
                .anyMatch(cas -> (cas.getValues().getProperty("prepay") != null)
                        && (cas.getValues().getProperty("prepay").equals("PP")))
                && device.getDeviceType()
                .getCustomPropertySets()
                .stream()
                .anyMatch(cas -> cas.getCustomPropertySet().getName().equals("CAS2") && customPropertySetService.getUniqueValuesFor(cas.getCustomPropertySet(), device)
                        .getProperty("meterMechanism")
                        .equals("CR"));
    }

    @Inject
    public DemoUsagePointMeterActivationValidator(DeviceService deviceService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.deviceService = deviceService;
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
    }

}
