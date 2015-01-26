package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.google.common.collect.Range;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedEndDeviceGroupImplIT {

    //    private static final String UP_MRID = "15-451785-45 ";
    private static final String ED_MRID = " ( ";
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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(false),
                new MeteringGroupsModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule()
        );
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(MeteringGroupsService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistence() {
        EndDevice endDevice = null;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_MRID);
            endDevice.save();
            ctx.commit();
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("Mine");
            enumeratedEndDeviceGroup.setMRID("mine");
            enumeratedEndDeviceGroup.add(endDevice, Range.atLeast(Instant.EPOCH));
            enumeratedEndDeviceGroup.save();
            ctx.commit();
        }

        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("mine");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(EnumeratedEndDeviceGroup.class);
        EnumeratedEndDeviceGroup group = (EnumeratedEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(ZonedDateTime.of(2014, 1, 23, 14, 54, 0, 0, ZoneId.systemDefault()).toInstant());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(endDevice.getId());
    }


    @Test
    public void testAmrIdQuey() {
        int NUMBER_OF_DEVICES_IN_GROUP = 24;
        List<EndDevice> endDevices = new ArrayList<>();
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        AmrSystem MDC = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalStateException::new);
        AmrSystem EAMS = meteringService.findAmrSystem(KnownAmrSystem.ENERGY_AXIS.getId()).orElseThrow(IllegalStateException::new);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            for (int i = 0; i < 2 * NUMBER_OF_DEVICES_IN_GROUP; i++) {
                EndDevice endDevice = (i % 2 == 0 ? MDC : EAMS).newMeter("" + i, "MRID" + i);
                endDevice.save();
                endDevices.add(endDevice);
            }
            ctx.commit();
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("Mine");
            enumeratedEndDeviceGroup.setMRID("mine");
            for (int i = 0; i < NUMBER_OF_DEVICES_IN_GROUP; i++) {
                EndDevice endDevice = endDevices.get(i);
                enumeratedEndDeviceGroup.add(endDevice, Range.atLeast(Instant.EPOCH));
            }
            enumeratedEndDeviceGroup.save();
            ctx.commit();
        }


        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("mine");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(EnumeratedEndDeviceGroup.class);
        EnumeratedEndDeviceGroup group = (EnumeratedEndDeviceGroup) found.get();


        // get all
        Subquery subQuery = group.getAmrIdSubQuery();
        List<EndDevice> deviceList = meteringService.getEndDeviceQuery().select(ListOperator.IN.contains(subQuery, "amrId"));
        assertThat(deviceList).hasSize(NUMBER_OF_DEVICES_IN_GROUP);

        subQuery = group.getAmrIdSubQuery(MDC);
        deviceList = meteringService.getEndDeviceQuery().select(ListOperator.IN.contains(subQuery, "amrId"));
        assertThat(deviceList).hasSize(NUMBER_OF_DEVICES_IN_GROUP / 2);

    }

}
