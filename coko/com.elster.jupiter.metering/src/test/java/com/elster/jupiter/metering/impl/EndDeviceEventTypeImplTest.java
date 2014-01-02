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
import com.elster.jupiter.metering.MeteringService;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Date;

import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceEventTypeImplTest extends EqualsContractTest {

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

    private EndDeviceEventTypeImpl instanceA;


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
                EndDeviceEventTypeImpl endDeviceEventType = new EndDeviceEventTypeImpl(code);
                endDeviceEventType.persist();

                Optional<EndDeviceEventType> found = Bus.getOrmClient().getEndDeviceEventTypeFactory().getOptional(code);
                assertThat(found).contains(new EndDeviceEventTypeImpl(code));
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
            instanceA = new EndDeviceEventTypeImpl(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode());
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new EndDeviceEventTypeImpl(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode());
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                new EndDeviceEventTypeImpl(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.GAS_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode()),
                new EndDeviceEventTypeImpl(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.CLOCK).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode()),
                new EndDeviceEventTypeImpl(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.TIME).eventOrAction(EndDeviceEventorAction.DECREASED).toCode()),
                new EndDeviceEventTypeImpl(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.INCREASED).toCode())
        );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
