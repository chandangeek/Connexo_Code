/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.orm.History;

import java.time.Instant;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HistoryIT {

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

    @Test
    @Transactional
    public void testHistory() throws InterruptedException {
        ServiceCategoryImpl serviceCategory = (ServiceCategoryImpl) inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.GAS).get();
        Instant first = Instant.now();
        Thread.sleep(5000);
        serviceCategory.setAliasName("GGG");
        serviceCategory.update();

        Instant second = Instant.now();
        Thread.sleep(5000);
        serviceCategory.setAliasName("HHH");
        serviceCategory.update();

        History<? extends ServiceCategory> history = serviceCategory.getHistory();

        Optional<? extends ServiceCategory> versionAt = history.getVersionAt(first);
        assertThat(versionAt).isPresent();
        ServiceCategory version = versionAt.get();
        assertThat(version.getVersion()).isEqualTo(1);
        assertThat(version.getAliasName()).isNull();

        versionAt = history.getVersionAt(second);
        assertThat(versionAt).isPresent();
        version = versionAt.get();
        assertThat(version.getVersion()).isEqualTo(2);
        assertThat(version.getAliasName()).isEqualTo("GGG");

        versionAt = history.getVersionAt(Instant.now());
        assertThat(versionAt).isPresent();
        version = versionAt.get();
        assertThat(version.getVersion()).isEqualTo(3);
        assertThat(version.getAliasName()).isEqualTo("HHH");
    }
}