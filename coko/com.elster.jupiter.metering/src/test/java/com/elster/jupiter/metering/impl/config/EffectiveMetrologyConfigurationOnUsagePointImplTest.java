/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

public class EffectiveMetrologyConfigurationOnUsagePointImplTest {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
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

    private ServiceCategory getServiceCategory() {
        return inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
    }

    @Test
    @Transactional
    public void testLinkIsInactiveByDefault() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("test", serviceCategory)
                .create();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("test", inMemoryBootstrapModule.getClock().instant())
                .create();
        usagePoint.apply(metrologyConfiguration);

        EffectiveMetrologyConfigurationOnUsagePoint link = inMemoryBootstrapModule.getMeteringService().getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("usagePoint").isEqualTo(usagePoint))
                .get(0);

        assertThat(link.isActive()).isFalse();
    }

    @Test
    @Transactional
    public void testLinkCanBeActivated() {
        ServiceCategory serviceCategory = getServiceCategory();
        UsagePointMetrologyConfiguration metrologyConfiguration = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration("test", serviceCategory)
                .create();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("test", inMemoryBootstrapModule.getClock().instant())
                .create();
        usagePoint.apply(metrologyConfiguration);
        inMemoryBootstrapModule.getMeteringService().getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("usagePoint").isEqualTo(usagePoint))
                .get(0)
                .activate();

        EffectiveMetrologyConfigurationOnUsagePoint link = inMemoryBootstrapModule.getMeteringService().getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(where("usagePoint").isEqualTo(usagePoint))
                .get(0);

        assertThat(link.isActive()).isTrue();
    }
}
