/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.cps.UsagePointTestCustomPropertySet;
import com.elster.jupiter.transaction.TransactionContext;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationCPSUsageTestIT {
    private static final String METROLOGY_CONFIG_NAME = "Test metrology configuration";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();
    private static UsagePointTestCustomPropertySet customPropertySet;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        customPropertySet = new UsagePointTestCustomPropertySet(inMemoryBootstrapModule.getPropertySpecService());
        inMemoryBootstrapModule.getCustomPropertySetService().addCustomPropertySet(customPropertySet);
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @After
    public void afterTest() {
        inTransaction(ctx -> {
            findMetrologyConfiguration().ifPresent(MetrologyConfiguration::delete);
            ctx.commit();
        });
    }

    private void inTransaction(Consumer<TransactionContext> worker) {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            worker.accept(context);
        }
    }

    private MetrologyConfiguration getMetrologyConfiguration() {
        return findMetrologyConfiguration()
                .orElseGet(() -> {
                    Optional<ServiceCategory> serviceCategory = inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
                    return inMemoryBootstrapModule.getMetrologyConfigurationService().newMetrologyConfiguration(METROLOGY_CONFIG_NAME, serviceCategory.get()).create();
                });
    }

    private Optional<MetrologyConfiguration> findMetrologyConfiguration() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService().findMetrologyConfiguration(METROLOGY_CONFIG_NAME);
    }

    @Test
    public void testCPSSuccessfullyRegistered() {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = inMemoryBootstrapModule.getCustomPropertySetService()
                .findActiveCustomPropertySet(customPropertySet.getId());
        assertThat(registeredCustomPropertySet).isPresent();
    }

    private RegisteredCustomPropertySet getRegisteredCPS() {
        return inMemoryBootstrapModule.getCustomPropertySetService().findActiveCustomPropertySet(customPropertySet.getId()).get();
    }

    @Test
    public void testAddCPSToMetrologyConfig() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            ctx.commit();
        });

        MetrologyConfiguration metrologyConfiguration = findMetrologyConfiguration().get();
        assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(1);
        assertThat(metrologyConfiguration.getCustomPropertySets().get(0).getCustomPropertySet().getId())
                .isEqualTo(customPropertySet.getId());
    }

    @Test
    public void testAddTheSameCPSTwice() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            ctx.commit();
        });

        MetrologyConfiguration metrologyConfiguration = findMetrologyConfiguration().get();
        assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(1);
        assertThat(metrologyConfiguration.getCustomPropertySets().get(0).getCustomPropertySet().getId())
                .isEqualTo(customPropertySet.getId());
    }

    @Test
    public void testAddCPSIncreasesMetrologyConfigVersion() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            long version = metrologyConfiguration.getVersion();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            assertThat(metrologyConfiguration.getVersion()).isEqualTo(version + 1);
        });
    }

    @Test(expected = CannotManageCustomPropertySetOnActiveMetrologyConfiguration.class)
    public void testAddCPSToActiveMetrologyConfig() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.activate();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            ctx.commit();
        });
    }

    @Test
    public void testRemoveNotAddedCPS() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            metrologyConfiguration.removeCustomPropertySet(getRegisteredCPS());
            ctx.commit();
        });

        MetrologyConfiguration metrologyConfiguration = findMetrologyConfiguration().get();
        assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(0);
        // plus we expect no exceptions
    }

    @Test
    public void testAddCPSAndRemoveAfterThat() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            RegisteredCustomPropertySet registeredCPS = getRegisteredCPS();
            metrologyConfiguration.addCustomPropertySet(registeredCPS);
            metrologyConfiguration.removeCustomPropertySet(registeredCPS);
            ctx.commit();
        });

        MetrologyConfiguration metrologyConfiguration = findMetrologyConfiguration().get();
        assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(0);
    }

    @Test
    public void testAddCPSAndRemoveItTwice() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            RegisteredCustomPropertySet registeredCPS = getRegisteredCPS();
            metrologyConfiguration.addCustomPropertySet(registeredCPS);
            metrologyConfiguration.removeCustomPropertySet(registeredCPS);
            metrologyConfiguration.removeCustomPropertySet(registeredCPS);
            ctx.commit();
        });

        MetrologyConfiguration metrologyConfiguration = findMetrologyConfiguration().get();
        assertThat(metrologyConfiguration.getCustomPropertySets()).hasSize(0);
        // plus we expect no exceptions
    }

    @Test
    public void testRemoveCPSIncreasesMetrologyConfigVersion() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            long version = metrologyConfiguration.getVersion();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            metrologyConfiguration.removeCustomPropertySet(getRegisteredCPS());
            assertThat(metrologyConfiguration.getVersion()).isEqualTo(version + 2);
        });
    }

    @Test(expected = CannotManageCustomPropertySetOnActiveMetrologyConfiguration.class)
    public void testRemoveCPSFromActiveMetrologyConfig() {
        inTransaction(ctx -> {
            MetrologyConfiguration metrologyConfiguration = getMetrologyConfiguration();
            RegisteredCustomPropertySet registeredCPS = getRegisteredCPS();
            metrologyConfiguration.addCustomPropertySet(registeredCPS);
            metrologyConfiguration.activate();
            metrologyConfiguration.addCustomPropertySet(getRegisteredCPS());
            ctx.commit();
        });
    }
}
