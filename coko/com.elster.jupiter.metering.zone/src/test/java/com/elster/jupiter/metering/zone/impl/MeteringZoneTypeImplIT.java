/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

import java.util.Dictionary;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeteringZoneTypeImplIT {

    private static Injector injector;

    private static BundleContext bundleContext = mock(BundleContext.class);
    private static ServiceRegistration serviceRegistration = mock(ServiceRegistration.class);
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static final String ZONE_NAME_1 = "ZoneName1";
    private static final String ZONE_NAME_2 = "ZoneName2";
    private static final long ZONE_ID = 1L;
    private static final String APPLICATION_1 = "APPNAME1";
    private static final String APPLICATION_2 = "APPNAME2";
    private static final String ZONE_TYPE_NAME_1 = "ZoneTypeName1";
    private static final String ZONE_TYPE_NAME_2 = "ZoneTypeName2";
    private static final long ZONE_TYPE_ID = 10L;
    private static final long VERSION = 1L;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactionRule = new TransactionalRule(injector.getInstance(TransactionService.class));


    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
        }
    }

    @BeforeClass
    public static void setUp() {
        when(bundleContext.registerService(any(Class.class), anyObject(), any(Dictionary.class))).thenReturn(serviceRegistration);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new TransactionModule(),
                    new InMemoryMessagingModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new MeteringZoneModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new NlsModule()

            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
                    injector.getInstance(MeteringZoneService.class);
                    return null;
                }
        );
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testSave() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_2, APPLICATION_1);

        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes(APPLICATION_1);
        assertThat(zoneTypes).hasSize(2);
        assertThat(zoneTypes.get(0).getName()).isEqualTo(ZONE_TYPE_NAME_1);
        assertThat(zoneTypes.get(1).getName()).isEqualTo(ZONE_TYPE_NAME_2);
    }

    @Test
    @Transactional
    public void testGetOrderByName() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_2, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);

        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes(APPLICATION_1);
        assertThat(zoneTypes).hasSize(2);
        assertThat(zoneTypes.get(0).getName()).isEqualTo(ZONE_TYPE_NAME_1);
        assertThat(zoneTypes.get(1).getName()).isEqualTo(ZONE_TYPE_NAME_2);
    }

    @Test
    @Transactional
    public void testGetFilterByAppName() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_2, APPLICATION_2);

        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes(APPLICATION_1);
        assertThat(zoneTypes).hasSize(1);
        assertThat(zoneTypes.get(0).getName()).isEqualTo(ZONE_TYPE_NAME_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "typeName", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithEmptyName() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, "", APPLICATION_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "typeName", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithLargeName() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, "123456789012345678901234567890123456789012345678901234567890123456789012345678901", APPLICATION_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "typeName", messageId = "{" + MessageSeeds.Constants.ZONE_TYPE_NAME_REQUIRED + "}")
    public void testSaveWithNULLName() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, null, APPLICATION_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "application", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_10 + "}")
    public void testSaveWithEmptyApplication() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, "");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "application", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_10 + "}")
    public void testSaveWithLargeApplication() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, "01234567890");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "application", messageId = "{" + MessageSeeds.Constants.ZONE_TYPE_APP_REQUIRED + "}")
    public void testSaveWithNULLApplication() {
        MeteringZoneService meteringZoneService = injector.getInstance(MeteringZoneService.class);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, null);
    }

    private ZoneType createZoneType(MeteringZoneService meteringZoneService, String name, String application) {
        return meteringZoneService
                .newZoneTypeBuilder()
                .withName(name)
                .withApplication(application)
                .create();
    }
}
