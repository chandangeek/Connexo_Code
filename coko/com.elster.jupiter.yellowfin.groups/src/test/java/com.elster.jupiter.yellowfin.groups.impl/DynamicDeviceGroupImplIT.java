package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroupBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamicDeviceGroupImplIT {

    private static final String ED_MRID = "DYNAMIC_GROUP_MRID";

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new TaskModule(),
                    new FiniteStateMachineModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new YellowfinGroupsModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new TaskModule(),
                    new DataVaultModule(),
                    new CustomPropertySetsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(YellowfinGroupsService.class);
            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            MeteringService meteringService = injector.getInstance(MeteringService.class);

            SimpleEndDeviceQueryProvider endDeviceQueryProvider = new SimpleEndDeviceQueryProvider();
            endDeviceQueryProvider.setMeteringService(meteringService);
            meteringGroupsService.addEndDeviceQueryProvider(endDeviceQueryProvider);

            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCaching() {
        EndDevice endDevice;
        try(TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            endDevice = meteringService.findAmrSystem(1).get().newMeter("1").setMRID(ED_MRID).create();
            ctx.commit();
        }

        QueryEndDeviceGroup queryEndDeviceGroup;
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder builder = meteringGroupsService.createQueryEndDeviceGroup();
            builder.setMRID("mine");
            builder.setName("mine");
            builder.setQueryProviderName(SimpleEndDeviceQueryProvider.SIMPLE_ENDDEVICE_QUERYPRVIDER);
            SearchDomain searchDomain = mockSearchDomain(EndDevice.class);
            builder.setSearchDomain(searchDomain);
            SearchableProperty mrid = mockSearchableProperty("mRID");
            when(searchDomain.getProperties()).thenReturn(Collections.singletonList(mrid));
            builder.withConditions(mockSearchablePropertyValue(mrid, SearchablePropertyOperator.EQUAL, Collections.singletonList(ED_MRID)));
            queryEndDeviceGroup = builder.create();
            ctx.commit();
        }

        Optional<DynamicDeviceGroupImpl> found;
        YellowfinGroupsService yellowfinGroupsService = injector.getInstance(YellowfinGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            found = yellowfinGroupsService.cacheDynamicDeviceGroup("mine");
            ctx.commit();
        }

        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(DynamicDeviceGroupImpl.class);
        DynamicDeviceGroupImpl group = found.get();
        List<DynamicDeviceGroupImpl.DynamicEntryImpl> entries = group.getEntries();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getGroupId()).isEqualTo(queryEndDeviceGroup.getId());
        assertThat(entries.get(0).getDeviceId()).isEqualTo(endDevice.getId());
    }

    private SearchDomain mockSearchDomain(Class<?> clazz) {
        SearchService searchService = injector.getInstance(SearchService.class);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchDomain.getId()).thenReturn(clazz.getName());
        searchService.register(searchDomain);
        return searchDomain;
    }

    private SearchableProperty mockSearchableProperty(String name) {
        SearchableProperty property = mock(SearchableProperty.class, RETURNS_DEEP_STUBS);
        when(property.getName()).thenReturn(name);
        when(property.getSpecification().getValueFactory()).thenReturn(new StringFactory());
        return property;
    }

    private SearchablePropertyValue mockSearchablePropertyValue(SearchableProperty searchableProperty, SearchablePropertyOperator operator, List<String> values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.operator = operator;
        valueBean.propertyName = searchableProperty.getName();
        valueBean.values = values;
        return new SearchablePropertyValue(searchableProperty, valueBean);
    }
}
