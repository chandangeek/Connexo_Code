package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.events.impl.LocalEventImpl;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.pubsub.impl.PublisherImpl;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceEventRecordImplTest extends EqualsContractTest {

    private static final long END_DEVICE_ID = 185L;
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private EndDeviceEventRecordImpl instanceA;

    @Mock
    private EndDevice endDevice, endDevice2;
    @Mock
    private EndDeviceEventType endDeviceEventType, endDeviceEventType2;
    @Mock
    private DataModel dataModel;
    @Mock
    private Subscriber subscriber;


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
                new OrmModule(),
                new IdsModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new UtilModule(),
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new NlsModule());
        when(principal.getName()).thenReturn("Test");
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersist() throws SQLException {

        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                when(subscriber.getClasses()).thenReturn(new Class[]{LocalEventImpl.class});
                ((PublisherImpl) injector.getInstance(Publisher.class)).addHandler(subscriber);
                ServerMeteringService meteringService = getMeteringService();
                DataModel dataModel = meteringService.getDataModel();
                Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
                String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode();
                EndDeviceEventTypeImpl eventType = meteringService.createEndDeviceEventType(code);

                AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
                EndDevice endDevice = amrSystem.newEndDevice("amrID", "mRID");
                endDevice.save();
                EndDeviceEventRecord endDeviceEventRecord = endDevice.addEventRecord(eventType, date);
                endDeviceEventRecord.save();

                assertThat(dataModel.mapper(EndDeviceEventRecord.class).getOptional(endDevice.getId(), eventType.getMRID(), date).get()).isEqualTo(endDeviceEventRecord);
                ArgumentCaptor<LocalEvent> localEventCapture = ArgumentCaptor.forClass(LocalEvent.class);
                verify(subscriber, times(2)).handle(localEventCapture.capture());

                LocalEvent localEvent = localEventCapture.getAllValues().get(1);
                Assertions.assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.END_DEVICE_EVENT_CREATED.topic());
                Event event = localEvent.toOsgiEvent();
                Assertions.assertThat(event.containsProperty("endDeviceId")).isTrue();
                Assertions.assertThat(event.containsProperty("endDeviceEventType")).isTrue();
                Assertions.assertThat(event.containsProperty("eventTimestamp")).isTrue();
            }
        });

    }

    @Test
    public void testPersistWithProperties() throws SQLException {

        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                ServerMeteringService meteringService = getMeteringService();
                DataModel dataModel = meteringService.getDataModel();
                Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
                String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode();
                EndDeviceEventTypeImpl eventType = meteringService.createEndDeviceEventType(code);

                AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
                EndDevice endDevice = amrSystem.newEndDevice("amrID", "mRID");
                endDevice.save();
                EndDeviceEventRecord endDeviceEventRecord = endDevice.addEventRecord(eventType, date);
                endDeviceEventRecord.addProperty("A", "C");
                endDeviceEventRecord.addProperty("D", "C");
                endDeviceEventRecord.save();

                Optional<EndDeviceEventRecord> found = dataModel.mapper(EndDeviceEventRecord.class).getOptional(endDevice.getId(), eventType.getMRID(), date);
                assertThat(found.get()).isEqualTo(endDeviceEventRecord);
                assertThat(found.get().getProperties()).contains(entry("A", "C"), entry("D", "C"));
            }
        });

    }

    private ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        DataModel dataModel = mock(DataModel.class);
        if (instanceA == null) {
            when(endDevice.getId()).thenReturn(END_DEVICE_ID);
            when(endDevice2.getId()).thenReturn(END_DEVICE_ID + 1);
            when(endDeviceEventType.getMRID()).thenReturn("A");
            when(endDeviceEventType2.getMRID()).thenReturn("B");
            instanceA = new EndDeviceEventRecordImpl(dataModel, null).init(endDevice, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant());
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        DataModel dataModel = mock(DataModel.class);
        when(dataModel.getInstance(EndDeviceEventRecordImpl.class)).thenReturn(new EndDeviceEventRecordImpl(dataModel, null));
        return new EndDeviceEventRecordImpl(dataModel, null).init(endDevice, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant());
    }

    EndDeviceEventRecordImpl createEndDeviceEvent() {
        return new EndDeviceEventRecordImpl(dataModel, null);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                createEndDeviceEvent().init(endDevice2, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant()),
                createEndDeviceEvent().init(endDevice, endDeviceEventType2, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant()),
                createEndDeviceEvent().init(endDevice, endDeviceEventType,ZonedDateTime.of(2013, 12, 17, 14, 42, 0, 0, ZoneId.systemDefault()).toInstant())
        );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
