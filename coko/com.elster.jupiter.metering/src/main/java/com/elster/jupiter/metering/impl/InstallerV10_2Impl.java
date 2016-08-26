package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateInstaller;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Provides common functionality which can be used for clean install of 10.2 or for upgrade from 10.1 to 10.2
 */
public class InstallerV10_2Impl implements FullInstaller {

    private final ServerMeteringService meteringService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public InstallerV10_2Impl(ServerMeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create SQL Aggregation Components",
                this::createSqlAggregationComponents,
                logger
        );
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
    }

    private void createSqlAggregationComponents() {
        if (!SqlDialect.H2.equals(this.meteringService.getDataModel().getSqlDialect())) {
            SqlExecuteFromResourceFile
                    .executeAll(
                            this.meteringService.getDataModel(),
                            "type.Flags_Array.sql",
                            "function.aggFlags.sql");
        }
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

}
