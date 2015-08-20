package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsServiceImpl;
import com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.MeteringServiceImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
                    new TaskModule(),
                    new FiniteStateMachineModule(),
                    new MeteringGroupsModule(),
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
                    new TaskModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(YellowfinGroupsService.class);
                MeteringGroupsService meteringGroupsService = (MeteringGroupsServiceImpl) injector.getInstance(MeteringGroupsService.class);
                MeteringService meteringService = (MeteringServiceImpl) injector.getInstance(MeteringService.class);

                SimpleEndDeviceQueryProvider endDeviceQueryProvider = new SimpleEndDeviceQueryProvider();
                endDeviceQueryProvider.setMeteringService(meteringService);
                meteringGroupsService.addEndDeviceQueryProvider(endDeviceQueryProvider);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCaching() {
        EndDevice endDevice = null;
        try(TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_MRID);
            endDevice.save();
            ctx.commit();
        }

        QueryEndDeviceGroup queryEndDeviceGroup = null;
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            queryEndDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(Operator.EQUAL.compare("id", 15).or(Operator.EQUAL.compare("mRID", ED_MRID)));
            queryEndDeviceGroup.setMRID("mine");
            queryEndDeviceGroup.setName("mine");
            queryEndDeviceGroup.setQueryProviderName(SimpleEndDeviceQueryProvider.SIMPLE_ENDDEVICE_QUERYPRVIDER);
            queryEndDeviceGroup.save();
            ctx.commit();
        }

        Optional<DynamicDeviceGroupImpl> found = Optional.empty();
        YellowfinGroupsService yellowfinGroupsService = injector.getInstance(YellowfinGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            found = yellowfinGroupsService.cacheDynamicDeviceGroup("mine");
            ctx.commit();
        }

        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(DynamicDeviceGroupImpl.class);
        DynamicDeviceGroupImpl group = (DynamicDeviceGroupImpl) found.get();
        List<DynamicDeviceGroupImpl.DynamicEntryImpl> entries = group.getEntries();
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getGroupId()).isEqualTo(queryEndDeviceGroup.getId());
        assertThat(entries.get(0).getDeviceId()).isEqualTo(endDevice.getId());
    }
}
