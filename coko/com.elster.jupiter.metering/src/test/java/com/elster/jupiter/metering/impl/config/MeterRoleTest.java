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
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.TableSpecs;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.callback.InstallService;

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

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

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
        //Business method
        ((InstallService) getMetrologyConfigurationService()).install();

        //Asserts
        Optional<MeterRole> defaultMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey());
        assertThat(defaultMeterRole).isPresent();
        assertThat(defaultMeterRole.get().getName()).isEqualTo(DefaultMeterRole.DEFAULT.getDefaultFormat());

        Optional<MeterRole> consumptionMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.CONSUMPTION.getKey());
        assertThat(consumptionMeterRole).isPresent();
        assertThat(consumptionMeterRole.get().getName()).isEqualTo(DefaultMeterRole.CONSUMPTION.getDefaultFormat());

        Optional<MeterRole> productionMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.PRODUCTION.getKey());
        assertThat(productionMeterRole).isPresent();
        assertThat(productionMeterRole.get().getName()).isEqualTo(DefaultMeterRole.PRODUCTION.getDefaultFormat());

        Optional<MeterRole> mainMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.MAIN.getKey());
        assertThat(mainMeterRole).isPresent();
        assertThat(mainMeterRole.get().getName()).isEqualTo(DefaultMeterRole.MAIN.getDefaultFormat());

        Optional<MeterRole> checkMeterRole = getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.CHECK.getKey());
        assertThat(checkMeterRole).isPresent();
        assertThat(checkMeterRole.get().getName()).isEqualTo(DefaultMeterRole.CHECK.getDefaultFormat());
    }

    @Test
    @Transactional
    public void testCreateMeterRole() {
        //Business method
        getMetrologyConfigurationService().newMeterRole(new TranslationKey() {
            @Override
            public String getKey() {
                return "meter.role.new";
            }

            @Override
            public String getDefaultFormat() {
                return "New meter role";
            }
        });

        //Asserts
        Optional<MeterRole> meterRole = getMetrologyConfigurationService().findMeterRole("meter.role.new");
        assertThat(meterRole).isPresent();
        assertThat(meterRole.get().getName()).isEqualTo("meter.role.new");
    }

    @Test
    @Transactional
    public void testOutOfTheBoxMeterRolesAttachingToServiceCategory() {
        //Business method
        ((InstallService) getMetrologyConfigurationService()).install();

        //Asserts
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<MeterRole> meterRoles;
        meterRoles = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat(), DefaultMeterRole.PRODUCTION
                        .getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.GAS).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.WATER).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.HEAT).get().getMeterRoles();
        assertThat(meterRoles.stream().map(MeterRole::getName).collect(Collectors.toList()))
                .containsOnly(DefaultMeterRole.CONSUMPTION.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat(), DefaultMeterRole.CHECK.getDefaultFormat());

        meterRoles = meteringService.getServiceCategory(ServiceKind.INTERNET).get().getMeterRoles();
        assertThat(meterRoles).isEmpty();
    }

    @Test
    @Transactional
    public void testAttachMeterRoleToServiceCategory() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        assertThat(serviceCategory.getMeterRoles()).isEmpty();
        MeterRole meterRole = getMetrologyConfigurationService().newMeterRole(DefaultMeterRole.DEFAULT);

        //Business method
        serviceCategory.addMeterRole(meterRole);

        //Asserts
        serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        assertThat(serviceCategory.getMeterRoles()).contains(meterRole);
    }

    @Test
    @Transactional
    public void testDetachMeterRoleFromServiceCategory() {
        ServiceCategory serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        assertThat(serviceCategory.getMeterRoles()).isEmpty();
        MeterRole meterRole = getMetrologyConfigurationService().newMeterRole(DefaultMeterRole.DEFAULT);
        serviceCategory.addMeterRole(meterRole);

        //Business method
        serviceCategory.removeMeterRole(meterRole);

        //Asserts
        serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        assertThat(serviceCategory.getMeterRoles()).isEmpty();
    }
}
