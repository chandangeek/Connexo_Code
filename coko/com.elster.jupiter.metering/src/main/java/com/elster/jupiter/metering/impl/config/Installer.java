package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;

public class Installer {

    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;

    Installer(MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public void install() {
        createMeterRoles();
    }

    private void createMeterRoles() {
        metrologyConfigurationService.newMeterRole(DefaultMeterRole.DEFAULT);//available to all service categories, so no need to attach
        MeterRole production = metrologyConfigurationService.newMeterRole(DefaultMeterRole.PRODUCTION);
        MeterRole consumption = metrologyConfigurationService.newMeterRole(DefaultMeterRole.CONSUMPTION);
        MeterRole main = metrologyConfigurationService.newMeterRole(DefaultMeterRole.MAIN);
        MeterRole check = metrologyConfigurationService.newMeterRole(DefaultMeterRole.CHECK);

        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get(), production, consumption, main, check);
        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.GAS).get(), consumption, main, check);
        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.WATER).get(), consumption, main, check);
        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.HEAT).get(), consumption, main, check);
    }

    private void attachMeterRolesToServiceCategory(ServiceCategory serviceCategory, MeterRole... meterRoles) {
        for (MeterRole meterRole : meterRoles) {
            serviceCategory.addMeterRole(meterRole);
        }
    }
}