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
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.search.UsagePointSearchDomain;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryUsagePointGroupImplIT {
    private static final Instant NOW = ZonedDateTime
            .of(2014, 1, 23, 14, 54, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final String UP_NAME = " ( ";
    private static Injector injector;

    private static BundleContext bundleContext = mock(BundleContext.class);
    private static ServiceRegistration serviceRegistration = mock(ServiceRegistration.class);
    private static UserService userService = mock(UserService.class);
    private static NlsService nlsService = mock(NlsService.class);
    private static LicenseService licenseService = mock(LicenseService.class);
    private static EventAdmin eventAdmin = mock(EventAdmin.class);
    private static SearchDomain searchDomain;
    private static UsagePoint usagePoint;

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(NlsService.class).toInstance(nlsService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(licenseService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        License license = mock(License.class);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(license));
        when(bundleContext.registerService(any(Class.class), anyObject(), any(Dictionary.class)))
                .thenReturn(serviceRegistration);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
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
                    new CalendarModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            setupDefaultUsagePointLifeCycle();
            injector.getInstance(MeteringGroupsService.class).addQueryProvider(injector.getInstance(SimpleUsagePointQueryProvider.class));
            searchDomain = injector.getInstance(UsagePointSearchDomain.class);
            injector.getInstance(SearchService.class).register(searchDomain);
            usagePoint = injector.getInstance(MeteringService.class)
                    .getServiceCategory(ServiceKind.ELECTRICITY).get()
                    .newUsagePoint(UP_NAME, Instant.EPOCH).create();
            usagePoint.setVirtual(false);
            usagePoint.setSdp(false);
            return null;
        });
    }

    private static void setupDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
        usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testWithNameSearchableProperty() {
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createQueryUsagePointGroup(
                mockSearchablePropertyValue("name", SearchablePropertyOperator.EQUAL,
                        Collections.singletonList("*(*")))
                .setMRID("MDM:mine")
                .setName("mine")
                .setSearchDomain(searchDomain)
                .setQueryProviderName(SimpleUsagePointQueryProvider.SIMPLE_USAGE_POINT_QUERY_PROVIDER)
                .create();

        UsagePointGroup found = meteringGroupsService.findUsagePointGroup("MDM:mine")
                .orElseThrow(() -> new NoSuchElementException("The group is created but not found afterwards"));
        assertThat(found).isInstanceOf(QueryUsagePointGroup.class);
        QueryUsagePointGroup group = (QueryUsagePointGroup) found;
        assertThat(group.getName()).isEqualTo("mine");
        List<UsagePoint> members = group.getMembers(NOW);
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(usagePoint.getId());
    }

    @Test
    @Transactional
    public void testWithTypeSearchableProperty() {
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createQueryUsagePointGroup(
                mockSearchablePropertyValue("type", SearchablePropertyOperator.EQUAL,
                        Collections.singletonList(UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name())))
                .setMRID("MDM:mine")
                .setName("mine")
                .setSearchDomain(searchDomain)
                .setQueryProviderName(SimpleUsagePointQueryProvider.SIMPLE_USAGE_POINT_QUERY_PROVIDER)
                .create();

        UsagePointGroup found = meteringGroupsService.findUsagePointGroup("MDM:mine")
                .orElseThrow(() -> new NoSuchElementException("The group is created but not found afterwards"));
        assertThat(found).isInstanceOf(QueryUsagePointGroup.class);
        QueryUsagePointGroup group = (QueryUsagePointGroup) found;
        assertThat(group.getName()).isEqualTo("mine");
        List<UsagePoint> members = group.getMembers(NOW);
        assertThat(members).isEmpty();

        usagePoint.setSdp(true);
        usagePoint.update();
        members = group.getMembers(NOW);
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(usagePoint.getId());
    }

    private SearchablePropertyValue mockSearchablePropertyValue(String property,
                                                                SearchablePropertyOperator operator,
                                                                List<String> values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = values;
        SearchableProperty searchableProperty = mock(SearchableProperty.class);
        when(searchableProperty.getName()).thenReturn(property);
        return new SearchablePropertyValue(searchableProperty, valueBean);
    }
}
