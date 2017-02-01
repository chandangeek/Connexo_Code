/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceControlTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateInstaller;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.BufferedReaderIterable;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides common functionality which can be used for clean install of 10.2 or for upgrade from 10.1 to 10.2
 */
public class InstallerV10_2Impl implements FullInstaller, PrivilegesProvider {

    //TODO update the enddevicecontroltypes csv file
    private static final String IMPORT_CONTROL_TYPES = "enddevicecontroltypes.csv";
    private static final String NOT_APPLICABLE = "n/a";

    private final ServerMeteringService meteringService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final UserService userService;

    @Inject
    public InstallerV10_2Impl(ServerMeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService, UserService userService) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.userService = userService;
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
        doTry(
                "Create End Device control types",
                () -> createEndDeviceControlTypes(logger),
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

    void createEndDeviceControlTypes(Logger logger) {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(getClass().getPackage().getName().replace('.', '/') + '/' + IMPORT_CONTROL_TYPES)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line : new BufferedReaderIterable(reader)) {
                String[] fields = line.split(",");

                for (EndDeviceType deviceType : endDeviceTypes(fields[0])) {
                    for (EndDeviceDomain domain : domains(fields[1])) {
                        for (EndDeviceSubDomain subDomain : subDomains(fields[2])) {
                            for (EndDeviceEventOrAction eventOrAction : eventOrActions(fields[3])) {
                                String code = EndDeviceControlTypeCodeBuilder
                                        .type(deviceType)
                                        .domain(domain)
                                        .subDomain(subDomain)
                                        .eventOrAction(eventOrAction)
                                        .toCode();
                                try {
                                    if (meteringService.getEndDeviceControlType(code).isPresent()) {
                                        logger.finer("Skipping code " + code + ": already exists");
                                    } else {
                                        logger.finer("adding code " + code);
                                        meteringService.createEndDeviceControlType(code);
                                    }
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, "Error creating EndDeviceType \'" + code + "\' : " + e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterable<EndDeviceEventOrAction> eventOrActions(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceEventOrAction.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceEventOrAction.NA);
            } else {
                return Collections.singletonList(EndDeviceEventOrAction.valueOf(sanitized(field)));
            }
        }
    }

    private String sanitized(String field) {
        return field.toUpperCase().replaceAll("[\\-%]", "");
    }

    private Iterable<EndDeviceSubDomain> subDomains(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceSubDomain.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceSubDomain.NA);
            } else {
                return Collections.singletonList(EndDeviceSubDomain.valueOf(sanitized(field)));
            }
        }
    }

    private Iterable<EndDeviceDomain> domains(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceDomain.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceDomain.NA);
            } else {
                return Collections.singletonList(EndDeviceDomain.valueOf(sanitized(field)));
            }
        }
    }

    private Iterable<EndDeviceType> endDeviceTypes(String field) {
        if ("*".equals(field)) {
            return Arrays.asList(EndDeviceType.values());
        } else {
            if (NOT_APPLICABLE.equalsIgnoreCase(field)) {
                return Collections.singletonList(EndDeviceType.NA);
            } else {
                return Collections.singletonList(EndDeviceType.valueOf(sanitized(field)));
            }
        }
    }

    @Override
    public String getModuleName() {
        return MeteringDataModelService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_USAGE_POINT.getKey(),
                        DefaultTranslationKey.RESOURCE_USAGE_POINT_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
                                Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT,
                                Privileges.Constants.ADMINISTER_USAGEPOINT_TIME_SLICED_CPS)));
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_READING_TYPE.getKey(),
                        DefaultTranslationKey.RESOURCE_READING_TYPE_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.ADMINISTER_READINGTYPE, Privileges.Constants.VIEW_READINGTYPE)));
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_SERVICE_CATEGORY.getKey(),
                        DefaultTranslationKey.RESOURCE_SERVICE_CATEGORY_DESCRIPTION.getKey(),
                        Collections.singletonList(Privileges.Constants.VIEW_SERVICECATEGORY)));

        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_METROLOGY_CONFIGURATION.getKey(),
                        DefaultTranslationKey.RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION,
                                Privileges.Constants.VIEW_METROLOGY_CONFIGURATION)));
        return resources;
    }

}
