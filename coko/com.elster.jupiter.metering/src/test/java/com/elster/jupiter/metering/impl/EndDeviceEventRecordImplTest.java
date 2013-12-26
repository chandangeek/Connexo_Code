package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private EndDeviceEventRecordImpl instanceA;

    @Mock
    private EndDevice endDevice, endDevice2;
    @Mock
    private EndDeviceEventType endDeviceEventType, endDeviceEventType2;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EndDeviceEventRecord.class).to(EndDeviceEventRecordImpl.class);
            bind(EndDevice.class).to(EndDeviceImpl.class);
            bind(EndDeviceEventType.class).to(EndDeviceEventTypeImpl.class);
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
                new TransactionModule());
        when(principal.getName()).thenReturn("Test");
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(MeteringService.class);
                return null;
            }
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
                Date date = new DateMidnight(2001, 1, 1).toDate();
                String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode();
                EndDeviceEventTypeImpl eventType = new EndDeviceEventTypeImpl(code);
                eventType.persist();

                AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
                EndDevice endDevice = getMeteringService().createEndDevice(amrSystem, "amrID", "mRID");
                endDevice.save();
                EndDeviceEventRecord endDeviceEventRecord = endDevice.addEventRecord(eventType, date);
                endDeviceEventRecord.save();

                assertThat(Bus.getOrmClient().getEndDeviceEventRecordFactory().getOptional(endDevice.getId(), eventType.getMRID(), date)).contains(endDeviceEventRecord);
            }
        });

    }

    @Test
    public void testPersistWithProperties() throws SQLException {

        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Date date = new DateMidnight(2001, 1, 1).toDate();
                String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode();
                EndDeviceEventTypeImpl eventType = new EndDeviceEventTypeImpl(code);
                eventType.persist();

                AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
                EndDevice endDevice = getMeteringService().createEndDevice(amrSystem, "amrID", "mRID");
                endDevice.save();
                EndDeviceEventRecord endDeviceEventRecord = endDevice.addEventRecord(eventType, date);
                endDeviceEventRecord.addProperty("A", "C");
                endDeviceEventRecord.addProperty("D", "C");
                endDeviceEventRecord.save();

                Optional<EndDeviceEventRecord> found = Bus.getOrmClient().getEndDeviceEventRecordFactory().getOptional(endDevice.getId(), eventType.getMRID(), date);
                assertThat(found).contains(endDeviceEventRecord);
                assertThat(found.get().getProperties()).contains(entry("A", "C"), entry("D", "C"));
            }
        });

    }

    private MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
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
        if (instanceA == null) {
            when(endDevice.getId()).thenReturn(END_DEVICE_ID);
            when(endDevice2.getId()).thenReturn(END_DEVICE_ID + 1);
            when(endDeviceEventType.getMRID()).thenReturn("A");
            when(endDeviceEventType2.getMRID()).thenReturn("B");
            instanceA = new EndDeviceEventRecordImpl(endDevice, endDeviceEventType, new DateTime(2013, 12, 17, 14, 41, 0).toDate());
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new EndDeviceEventRecordImpl(endDevice, endDeviceEventType, new DateTime(2013, 12, 17, 14, 41, 0).toDate());
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                new EndDeviceEventRecordImpl(endDevice2, endDeviceEventType, new DateTime(2013, 12, 17, 14, 41, 0).toDate()),
                new EndDeviceEventRecordImpl(endDevice, endDeviceEventType2, new DateTime(2013, 12, 17, 14, 41, 0).toDate()),
                new EndDeviceEventRecordImpl(endDevice, endDeviceEventType, new DateTime(2013, 12, 17, 14, 42, 0).toDate())
        );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
