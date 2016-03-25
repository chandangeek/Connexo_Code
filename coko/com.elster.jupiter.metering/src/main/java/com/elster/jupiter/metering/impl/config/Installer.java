package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

public class Installer {

    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;

    Installer(MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public void install() {
        ExceptionCatcher.executing(
                this::createMeterRoles,
                this::createMetrologyPurposes,
                this::createReadingTypeTemplates
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createMeterRoles() {
        metrologyConfigurationService.newMeterRole(DefaultMeterRole.DEFAULT.getNlsKey());//available to all service categories, so no need to attach
        MeterRole production = metrologyConfigurationService.newMeterRole(DefaultMeterRole.PRODUCTION.getNlsKey());
        MeterRole consumption = metrologyConfigurationService.newMeterRole(DefaultMeterRole.CONSUMPTION.getNlsKey());
        MeterRole main = metrologyConfigurationService.newMeterRole(DefaultMeterRole.MAIN.getNlsKey());
        MeterRole check = metrologyConfigurationService.newMeterRole(DefaultMeterRole.CHECK.getNlsKey());

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

    private void createMetrologyPurposes() {
        metrologyConfigurationService.createMetrologyPurpose().fromDefaultMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        metrologyConfigurationService.createMetrologyPurpose().fromDefaultMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        metrologyConfigurationService.createMetrologyPurpose().fromDefaultMetrologyPurpose(DefaultMetrologyPurpose.VOLTAGE_MONITORING);
    }

    private void createReadingTypeTemplates() {
        new ReadingTypeTemplateInstaller(metrologyConfigurationService).install();
    }
}