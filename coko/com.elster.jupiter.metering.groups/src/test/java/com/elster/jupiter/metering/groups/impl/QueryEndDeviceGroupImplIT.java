package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroupBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.impl.MeteringModule;
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
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryEndDeviceGroupImplIT {

    private Injector injector;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private BundleContext bundleContext;
    @Mock
    private ServiceRegistration serviceRegistration;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private SearchDomain searchDomain;

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
        when(this.bundleContext.registerService(any(Class.class), anyObject(), any(Dictionary.class))).thenReturn(this.serviceRegistration);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
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
                    new DataVaultModule(),
                    new CustomPropertySetsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
                    injector.getInstance(FiniteStateMachineService.class);
                    injector.getInstance(MeteringGroupsService.class);
                    injector.getInstance(MeteringService.class);

                    when(searchDomain.getId()).thenReturn("Device");
                    injector.getInstance(SearchService.class).register(searchDomain);

                    return null;
                }
        );
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testSaveQueryEndDeviceGroup() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder groupBuilder = meteringGroupsService.createQueryEndDeviceGroup();
            groupBuilder.setName("QueryEndDeviceGroup");
            groupBuilder.setMRID("MRID");
            groupBuilder.setQueryProviderName("DeviceQueryProvider");
            groupBuilder.setSearchDomain(searchDomain);
            groupBuilder.withConditions(mockSearchablePropertyValue("mRID", SearchablePropertyOperator.EQUAL, Arrays.asList("DME*", "*000001")));
            groupBuilder.withConditions(mockSearchablePropertyValue("serialNumber", SearchablePropertyOperator.BETWEEN, Arrays.asList("1000", "2000")));
            groupBuilder.create();

            assertThat(meteringGroupsService.findEndDeviceGroups()).hasSize(1);
            QueryEndDeviceGroupImpl endDeviceGroup = (QueryEndDeviceGroupImpl) meteringGroupsService.findEndDeviceGroups().get(0);
            assertThat(endDeviceGroup.getName()).isEqualTo("QueryEndDeviceGroup");
            assertThat(endDeviceGroup.getMRID()).isEqualTo("MRID");
            assertThat(endDeviceGroup.isDynamic()).isTrue();
            assertThat(endDeviceGroup.getSearchDomain().getId()).isEqualTo(searchDomain.getId());
            List<QueryEndDeviceGroupCondition> conditions = endDeviceGroup.getConditions();
            assertThat(conditions).hasSize(2);

            assertThat(conditions.get(0).getSearchableProperty()).isEqualTo("mRID");
            assertThat(conditions.get(0).getOperator()).isEqualTo(SearchablePropertyOperator.EQUAL);
            assertThat(conditions.get(0).getConditionValues()).hasSize(2);
            assertThat(conditions.get(0).getConditionValues().get(0).getValue()).isEqualTo("DME*");
            assertThat(conditions.get(0).getConditionValues().get(1).getValue()).isEqualTo("*000001");

            assertThat(conditions.get(1).getSearchableProperty()).isEqualTo("serialNumber");
            assertThat(conditions.get(1).getOperator()).isEqualTo(SearchablePropertyOperator.BETWEEN);
            assertThat(conditions.get(1).getConditionValues()).hasSize(2);
            assertThat(conditions.get(1).getConditionValues().get(0).getValue()).isEqualTo("1000");
            assertThat(conditions.get(1).getConditionValues().get(1).getValue()).isEqualTo("2000");
        }
    }

    @Test
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.DUPLICATE_NAME + "}")
    public void testSaveGroupWithDuplicateName() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder groupBuilder = meteringGroupsService.createQueryEndDeviceGroup();
            groupBuilder.setName("group");
            groupBuilder.setMRID("MRID");
            groupBuilder.setQueryProviderName("DeviceQueryProvider");
            groupBuilder.setSearchDomain(searchDomain);
            groupBuilder.create();

            assertThat(meteringGroupsService.findEndDeviceGroups()).hasSize(1);
            Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName("group");
            assertThat(endDeviceGroup).isPresent();

            //create the another group with the same name
            groupBuilder = meteringGroupsService.createQueryEndDeviceGroup();
            groupBuilder.setName("group");
            groupBuilder.setMRID("MRID");
            groupBuilder.setQueryProviderName("DeviceQueryProvider");
            groupBuilder.setSearchDomain(searchDomain);
            groupBuilder.create();
        }
    }

    @Test
    public void testUpdateQueryEndDeviceGroup() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder groupBuilder = meteringGroupsService.createQueryEndDeviceGroup();
            groupBuilder.setName("QueryEndDeviceGroupV1");
            groupBuilder.setMRID("MRID:V1");
            groupBuilder.setQueryProviderName("DeviceQueryProvider");
            groupBuilder.setSearchDomain(searchDomain);
            groupBuilder.withConditions(mockSearchablePropertyValue("mRID", SearchablePropertyOperator.EQUAL, Arrays.asList("DME*", "*000001")));
            groupBuilder.withConditions(mockSearchablePropertyValue("serialNumber", SearchablePropertyOperator.BETWEEN, Arrays.asList("1000", "2000")));
            QueryEndDeviceGroup queryEndDeviceGroup = groupBuilder.create();

            //update of persisted group
            queryEndDeviceGroup.setName("QueryEndDeviceGroupV2");
            queryEndDeviceGroup.setMRID("MRID:V2");
            queryEndDeviceGroup.setConditions(Arrays.asList(mockSearchablePropertyValue("deviceType", SearchablePropertyOperator.EQUAL, Arrays.asList("1"))));
            queryEndDeviceGroup.update();

            assertThat(meteringGroupsService.findEndDeviceGroups()).hasSize(1);
            QueryEndDeviceGroupImpl endDeviceGroup = (QueryEndDeviceGroupImpl) meteringGroupsService.findEndDeviceGroups().get(0);
            assertThat(endDeviceGroup.getName()).isEqualTo("QueryEndDeviceGroupV2");
            assertThat(endDeviceGroup.getMRID()).isEqualTo("MRID:V2");
            assertThat(endDeviceGroup.isDynamic()).isTrue();
            assertThat(endDeviceGroup.getSearchDomain().getId()).isEqualTo(searchDomain.getId());
            List<QueryEndDeviceGroupCondition> conditions = endDeviceGroup.getConditions();
            assertThat(conditions).hasSize(1);

            assertThat(conditions.get(0).getSearchableProperty()).isEqualTo("deviceType");
            assertThat(conditions.get(0).getOperator()).isEqualTo(SearchablePropertyOperator.EQUAL);
            assertThat(conditions.get(0).getConditionValues()).hasSize(1);
            assertThat(conditions.get(0).getConditionValues().get(0).getValue()).isEqualTo("1");
        }
    }

    @Test
    public void testRemoveQueryEndDeviceGroup() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder groupBuilder = meteringGroupsService.createQueryEndDeviceGroup();
            groupBuilder.setName("QueryEndDeviceGroupV1");
            groupBuilder.setMRID("MRID:V1");
            groupBuilder.setQueryProviderName("DeviceQueryProvider");
            groupBuilder.setSearchDomain(searchDomain);
            groupBuilder.withConditions(mockSearchablePropertyValue("mRID", SearchablePropertyOperator.EQUAL, Arrays.asList("DME*", "*000001")));
            groupBuilder.withConditions(mockSearchablePropertyValue("serialNumber", SearchablePropertyOperator.BETWEEN, Arrays.asList("1000", "2000")));
            QueryEndDeviceGroup queryEndDeviceGroup = groupBuilder.create();

            Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup(queryEndDeviceGroup.getId());
            assertThat(found).isPresent();

            found.get().delete();

            Optional<EndDeviceGroup> removed = meteringGroupsService.findEndDeviceGroup(found.get().getId());
            assertThat(removed).isEmpty();
        }
    }

    private SearchablePropertyValue mockSearchablePropertyValue(String property, SearchablePropertyOperator operator, List<String> values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = values;
        SearchableProperty searchableProperty = mock(SearchableProperty.class);
        when(searchableProperty.getName()).thenReturn(property);
        return new SearchablePropertyValue(searchableProperty, valueBean);
    }
}
