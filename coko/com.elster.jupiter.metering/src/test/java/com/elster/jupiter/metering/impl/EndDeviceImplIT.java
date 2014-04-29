package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
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
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceImplIT {

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
                new MeteringModule(),
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
                injector.getInstance(MeteringService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testGetEvents() {
        Date date = new DateTime(2014, 2, 5, 14, 15, 16, 0).toDate();
        EndDevice endDevice;
        EndDeviceEventRecord eventRecord;
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEventType> availableEndDeviceEventTypes = meteringService.getAvailableEndDeviceEventTypes();
            endDevice = meteringService.findAmrSystem(1).get().newEndDevice("1", "1");
            endDevice.save();
            eventRecord = endDevice.addEventRecord(availableEndDeviceEventTypes.get(0), date);
            eventRecord.save();
            context.commit();
        }
        List<EndDeviceEventRecord> deviceEvents = endDevice.getDeviceEvents(Interval.startAt(date));
        assertThat(deviceEvents).contains(eventRecord);
        List<EndDeviceEventType> endDeviceEventTypes = new ArrayList<>(meteringService.getAvailableEndDeviceEventTypes());
        deviceEvents = endDevice.getDeviceEvents(Interval.startAt(date), endDeviceEventTypes);
        assertThat(deviceEvents).contains(eventRecord);
        endDeviceEventTypes.remove(0);
        deviceEvents = endDevice.getDeviceEvents(Interval.startAt(date), endDeviceEventTypes);
        assertThat(deviceEvents).isEmpty();
    }


}
