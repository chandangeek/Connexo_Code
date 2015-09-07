package com.elster.jupiter.metering.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.sql.SQLException;

import com.elster.jupiter.bpm.impl.BpmModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;

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
    	final ServerMeteringService meteringService = getMeteringService();
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode();
                EndDeviceEventTypeImpl endDeviceEventType = meteringService.createEndDeviceEventType(code);
                Optional<EndDeviceEventType> found = meteringService.getDataModel().mapper(EndDeviceEventType.class).getOptional(code);
                assertThat(found.get()).isEqualTo(endDeviceEventType);
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

    private EndDeviceEventTypeImpl createEndDeviceEventType() {
    	return new EndDeviceEventTypeImpl(dataModel, thesaurus);
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = createEndDeviceEventType().init(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode());
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return createEndDeviceEventType().init(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode());
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
        		createEndDeviceEventType().init(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.GAS_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode()),
        		createEndDeviceEventType().init(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.CLOCK).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.DECREASED).toCode()),
        		createEndDeviceEventType().init(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.TIME).eventOrAction(EndDeviceEventorAction.DECREASED).toCode()),
        		createEndDeviceEventType().init(EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER).domain(EndDeviceDomain.BATTERY).subDomain(EndDeviceSubDomain.CHARGE).eventOrAction(EndDeviceEventorAction.INCREASED).toCode())
        );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
