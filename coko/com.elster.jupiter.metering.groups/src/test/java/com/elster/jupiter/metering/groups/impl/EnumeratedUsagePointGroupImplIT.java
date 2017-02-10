/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Clock;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedUsagePointGroupImplIT {

    private static final String UP_NAME = " ( ";
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactional = new TransactionalRule(injector.getInstance(TransactionService.class));

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(mock(UserService.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule(),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new CalendarModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule()

            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringGroupsService.class);
            setupDefaultUsagePointLifeCycle();
            return null;
        });
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private static void setupDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
        usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
    }

    @Test
    @Transactional
    public void testPersistence() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        UsagePoint usagePoint = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get().newUsagePoint(UP_NAME, Instant.EPOCH).create();

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedUsagePointGroup()
                .setName("Mine")
                .setMRID("mine")
                .at(Instant.EPOCH)
                .containing(usagePoint)
                .create();

        Optional<UsagePointGroup> found = meteringGroupsService.findUsagePointGroup("mine");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(EnumeratedUsagePointGroup.class);
        EnumeratedUsagePointGroup group = (EnumeratedUsagePointGroup) found.get();
        List<UsagePoint> members = group.getMembers(Year.of(2014).atMonth(Month.JANUARY).atDay(23).atTime(14, 54).atZone(ZoneId.systemDefault()).toInstant());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(usagePoint.getId());
    }

    @Test
    @Transactional
    public void testUsagePointDeletionHandler() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        Clock clock = injector.getInstance(Clock.class);

        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usage point", Instant.EPOCH).create();
        EnumeratedUsagePointGroup enumeratedUsagePointGroup = meteringGroupsService.createEnumeratedUsagePointGroup(usagePoint).setName("test").create();
        Instant activeMemberTime = clock.instant().minusMillis(1);
        assertThat(enumeratedUsagePointGroup.getMemberCount(activeMemberTime)).isEqualTo(1);
        usagePoint.makeObsolete();

        UsagePointDeletionEventHandler usagePointDeletionEventHandler = injector.getInstance(UsagePointDeletionEventHandler.class);
        LocalEvent usagePointDeletionEvent = mock(LocalEvent.class);
        when(usagePointDeletionEvent.getSource()).thenReturn(usagePoint);

        // Business method
        usagePointDeletionEventHandler.handle(usagePointDeletionEvent);

        // Assert
        enumeratedUsagePointGroup = meteringGroupsService.findEnumeratedUsagePointGroup(enumeratedUsagePointGroup.getId()).get();
        Instant inactiveMemberTime = clock.instant().plusMillis(1);

        assertThat(enumeratedUsagePointGroup.getMemberCount(inactiveMemberTime)).isEqualTo(0);
        assertThat(enumeratedUsagePointGroup.getMembers(inactiveMemberTime)).isEmpty();

        assertThat(enumeratedUsagePointGroup.getMemberCount(activeMemberTime)).isEqualTo(1);
        assertThat(enumeratedUsagePointGroup.getMembers(activeMemberTime)).contains(usagePoint);
    }
}
