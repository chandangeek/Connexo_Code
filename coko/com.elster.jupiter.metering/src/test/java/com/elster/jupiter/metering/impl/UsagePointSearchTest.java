/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.StreetDetail;
import com.elster.jupiter.cbo.TownDetail;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.search.NameSearchableProperty;
import com.elster.jupiter.metering.impl.search.UsagePointSearchDomain;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.impl.SearchBuilderImpl;
import com.elster.jupiter.search.impl.SearchMonitor;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.units.Unit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointSearchTest {

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static TransactionService transactionService;
    private static User user;
    private static MeteringService meteringService;
    private static UsagePointSearchDomain usagePointSearchDomain;
    private static PropertySpecService propertySpecService;
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static SearchMonitor dummyMonitor;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new IdsModule(),
                new MeteringModule(),
                new BasicPropertiesModule(),
                new TimeModule(),
                new PartyModule(),
                new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new UserModule(),
                new TransactionModule(false),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new DataVaultModule(),
                new NlsModule(),
                new CalendarModule(),
                new CustomPropertySetsModule(),
                new BasicPropertiesModule(),
                new UsagePointLifeCycleConfigurationModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext context = transactionService.getContext()) {
            injector.getInstance(EventService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            meteringService = injector.getInstance(MeteringService.class);
            user = injector.getInstance(UserService.class).findUser("admin")
                    .orElseThrow(() -> new NoSuchElementException("User 'admin' is not found"));
            injector.getInstance(ThreadPrincipalService.class).set(user);
            injector.getInstance(MeteringTranslationService.class);
            usagePointSearchDomain = injector.getInstance(UsagePointSearchDomain.class);
            propertySpecService = injector.getInstance(PropertySpecService.class);
            createDefaultUsagePointLifeCycle();
            context.commit();
        }
        ArgumentCaptor<TranslationKey> translationKeyCaptor = ArgumentCaptor.forClass(TranslationKey.class);
        dummyMonitor = mock(SearchMonitor.class);
        ExecutionTimer timer = mock(ExecutionTimer.class);
        doAnswer(invocation -> ((Callable)invocation.getArguments()[0]).call()).when(timer).time(any(Callable.class));
        when(dummyMonitor.searchTimer(usagePointSearchDomain)).thenReturn(timer);
    }

    private static void createDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
        usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(transactionService);

    @Test
    @Transactional
    public void test() throws Exception {
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        ServiceLocation location = meteringService.newServiceLocation()
                .setMainAddress(new StreetAddress(new StreetDetail("Spinnerijstraat", "101"), new TownDetail("8500", "Kortrijk", "BE")))
                .setName("EnergyICT")
                .create();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("UP_0", Instant.EPOCH).withServiceLocation(location).create();
        ElectricityDetailImpl detail = (ElectricityDetailImpl) serviceCategory.newUsagePointDetail(usagePoint, Instant.now());
        detail.setRatedPower(Unit.WATT.amount(BigDecimal.valueOf(1000), 3));
        usagePoint.addDetail(detail);
//        usagePoint.save();
        Query<UsagePoint> query = meteringService.getUsagePointQuery();
        Condition condition = where("serviceLocation.mainAddress.townDetail.country").isEqualTo("BE");
        condition = condition.and(where("detail.ratedPower.value").between(BigDecimal.valueOf(999)).and(BigDecimal.valueOf(1001)));
        assertThat(query.select(condition)).hasSize(1);
        assertThat(query.select(condition).get(0).getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        query.setEager();
        assertThat(query.select(condition)).hasSize(1);
        for (int i = 0; i < 10; i++) {
            usagePoint = serviceCategory.newUsagePoint("UP_" + (10 - i), Instant.EPOCH).create();
        }
        assertThat(query.select(Condition.TRUE)).hasSize(11);
        assertThat(query.select(Condition.TRUE, 1, 5)).hasSize(5);
        assertThat(query.select(meteringService.hasAccountability())).isEmpty();
        PartyService partyService = injector.getInstance(PartyService.class);
        Party party = partyService.newOrganization("Electrabel").create();
        PartyRole role = partyService.getRole(MarketRoleKind.ENERGYSERVICECONSUMER.name()).get();
        party.assumeRole(role, Instant.now());
        query.setLazy();
        assertThat(query.select(meteringService.hasAccountability())).isEmpty();
        party.appointDelegate(user, Instant.now());
        party = partyService.getParty("Electrabel").get();
        usagePoint.addAccountability(role, party, Instant.now());
        assertThat(query.select(meteringService.hasAccountability())).isNotEmpty();

        List<String> sortedResult = Arrays.asList("UP_0", "UP_1", "UP_10", "UP_2", "UP_3", "UP_4", "UP_5", "UP_6", "UP_7", "UP_8", "UP_9");
        assertThat(query.select(Condition.TRUE, Order.descending("name").toUpperCase(), Order.ascending("id"))
                .stream()
                .map(UsagePoint::getName)
                .collect(Collectors.toList()))
                .containsOnlyElementsOf(sortedResult)
                .isSortedAccordingTo(Comparator.<String>naturalOrder().reversed());
        assertThat(usagePoint.getCustomer(Instant.now()).get().getMRID()).isEqualTo("Electrabel");

        Finder<UsagePoint> finder = (Finder<UsagePoint>)usagePointSearchDomain.finderFor(Collections.emptyList());
        assertThat(finder.stream().map(UsagePoint::getName).collect(Collectors.toList()))
                .isEqualTo(sortedResult);
        finder.paged(0, 4);
        assertThat(finder.stream().map(UsagePoint::getName).collect(Collectors.toList()))
                .isEqualTo(sortedResult.subList(0, 5));
        finder.paged(4, 4);
        assertThat(finder.stream().map(UsagePoint::getName).collect(Collectors.toList()))
                .isEqualTo(sortedResult.subList(4, 9));
        finder.paged(8, 4);
        assertThat(finder.stream().map(UsagePoint::getName).collect(Collectors.toList()))
                .isEqualTo(sortedResult.subList(8, 11));
        finder = new SearchBuilderImpl(usagePointSearchDomain, dummyMonitor)
                .where(new NameSearchableProperty(usagePointSearchDomain, propertySpecService, thesaurus))
                .in(sortedResult.subList(1, 11)).toFinder();
        assertThat(finder.stream().map(UsagePoint::getName).collect(Collectors.toList()))
                .isEqualTo(sortedResult.subList(1, 11));
        finder.paged(4, 4);
        assertThat(finder.stream().map(UsagePoint::getName).collect(Collectors.toList()))
                .isEqualTo(sortedResult.subList(5, 10));
    }
}
