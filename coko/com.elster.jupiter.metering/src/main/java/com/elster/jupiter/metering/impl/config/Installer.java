package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final MeteringService meteringService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    Installer(MeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public void install(DataModelUpgrader upgrader, Logger logger) {
        doTry(
                "Create Meter Roles",
                this::createMeterRoles,
                logger
        );
        doTry(
                "Create Metrology Purposes",
                this::createMetrologyPurposes,
                logger
        );
        doTry(
                "Create Reading Type templates",
                this::createReadingTypeTemplates,
                logger
        );
        doTry(
                "Create Metrology Configurations",
                this::createMetrologyConfigurations,
                logger
        );
    }

    private void createMeterRoles() {
        metrologyConfigurationService.newMeterRole(DefaultMeterRole.DEFAULT.getNlsKey());//available to all service categories, so no need to attach
        MeterRole production = metrologyConfigurationService.newMeterRole(DefaultMeterRole.PRODUCTION.getNlsKey());
        MeterRole consumption = metrologyConfigurationService.newMeterRole(DefaultMeterRole.CONSUMPTION.getNlsKey());
        MeterRole main = metrologyConfigurationService.newMeterRole(DefaultMeterRole.MAIN.getNlsKey());
        MeterRole check = metrologyConfigurationService.newMeterRole(DefaultMeterRole.CHECK.getNlsKey());
        MeterRole peakConsumption = metrologyConfigurationService.newMeterRole(DefaultMeterRole.PEAK_CONSUMPTION.getNlsKey());
        MeterRole offPeakConsumption = metrologyConfigurationService.newMeterRole(DefaultMeterRole.OFF_PEAK_CONSUMPTION.getNlsKey());

        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get(), production, consumption, main, check);
        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.GAS).get(), consumption, main, check);
        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.WATER).get(), consumption, main, check, peakConsumption, offPeakConsumption);
        attachMeterRolesToServiceCategory(meteringService.getServiceCategory(ServiceKind.HEAT).get(), consumption, main, check);
    }

    private void attachMeterRolesToServiceCategory(ServiceCategory serviceCategory, MeterRole... meterRoles) {
        for (MeterRole meterRole : meterRoles) {
            serviceCategory.addMeterRole(meterRole);
        }
    }

    private void createMetrologyPurposes() {
        metrologyConfigurationService.createMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        metrologyConfigurationService.createMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        metrologyConfigurationService.createMetrologyPurpose(DefaultMetrologyPurpose.VOLTAGE_MONITORING);
    }

    private void createReadingTypeTemplates() {
        new ReadingTypeTemplateInstaller(metrologyConfigurationService).install();
    }

    private void createMetrologyConfigurations() {
        new MetrologyConfigurationInstaller(metrologyConfigurationService, meteringService).install();
    }
}