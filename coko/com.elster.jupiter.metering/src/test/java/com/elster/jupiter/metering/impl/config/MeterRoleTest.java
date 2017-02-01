/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.TableSpecs;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterRoleTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @After
    public void after() {
        inMemoryBootstrapModule.getOrmService().invalidateCache(MeteringService.COMPONENTNAME, TableSpecs.MTR_SERVICECATEGORY.name());
    }

    private MetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    @Test
    @Transactional
    public void testOutOfTheBoxMeterRoles() {
        //Asserts
        Optional<MeterRole> defaultMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey());
        assertThat(defaultMeterRole).isPresent();
        assertThat(defaultMeterRole.get().getDisplayName()).isEqualTo(DefaultMeterRole.DEFAULT.getDefaultFormat());

        Optional<MeterRole> consumptionMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.CONSUMPTION.getKey());
        assertThat(consumptionMeterRole).isPresent();
        assertThat(consumptionMeterRole.get().getDisplayName()).isEqualTo(DefaultMeterRole.CONSUMPTION.getDefaultFormat());

        Optional<MeterRole> productionMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.PRODUCTION.getKey());
        assertThat(productionMeterRole).isPresent();
        assertThat(productionMeterRole.get().getDisplayName()).isEqualTo(DefaultMeterRole.PRODUCTION.getDefaultFormat());

        Optional<MeterRole> mainMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.MAIN.getKey());
        assertThat(mainMeterRole).isPresent();
        assertThat(mainMeterRole.get().getDisplayName()).isEqualTo(DefaultMeterRole.MAIN.getDefaultFormat());

        Optional<MeterRole> checkMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.CHECK.getKey());
        assertThat(checkMeterRole).isPresent();
        assertThat(checkMeterRole.get().getDisplayName()).isEqualTo(DefaultMeterRole.CHECK.getDefaultFormat());
    }

    @Test
    @Transactional
    public void testCreateMeterRole() {
        //Business method
        getMetrologyConfigurationService().newMeterRole(SimpleNlsKey
                .key(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN, "meter.role.new")
                .defaultMessage("New meter role"));

        //Asserts
        Optional<MeterRole> meterRole = getMetrologyConfigurationService().findMeterRole("meter.role.new");
        assertThat(meterRole).isPresent();
        assertThat(meterRole.get().getDisplayName()).isEqualTo("New meter role");
    }

    @Test
    @Transactional
    public void testOutOfTheBoxMeterRolesAttachingToServiceCategory() {
        //Asserts
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<MeterRole> meterRoles;
        meterRoles = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getDisplayName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat(), DefaultMeterRole.PRODUCTION
                        .getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.GAS).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getDisplayName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.WATER).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getDisplayName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(),
                        DefaultMeterRole.CHECK.getDefaultFormat(), DefaultMeterRole.PEAK_CONSUMPTION.getDefaultFormat(), DefaultMeterRole.OFF_PEAK_CONSUMPTION.getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.HEAT).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getDisplayName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.INTERNET).get().getMeterRoles();
        assertThat(meterRoles).isEmpty();
    }

    @Test
    @Transactional
    public void testAttachMeterRoleToServiceCategory() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.INTERNET).get();
        assertThat(serviceCategory.getMeterRoles()).isEmpty();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();

        //Business method
        serviceCategory.addMeterRole(meterRole);
        serviceCategory.addMeterRole(meterRole);

        //Asserts
        serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.INTERNET).get();
        assertThat(serviceCategory.getMeterRoles()).containsExactly(meterRole);
    }

    @Test
    @Transactional
    public void testDetachMeterRoleFromServiceCategory() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.INTERNET).get();
        MeterRole meterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();

        //Business method
        serviceCategory.removeMeterRole(meterRole);

        //Asserts
        serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.INTERNET).get();
        assertThat(serviceCategory.getMeterRoles()).isEmpty();
    }
}
