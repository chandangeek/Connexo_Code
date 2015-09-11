package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.UtilModule;
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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeGeneratorForElectricityTest {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Subscriber topicHandler;

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
                new BpmModule(),
                new FiniteStateMachineModule(),
                new NlsModule());
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringServiceImpl.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void generateTest() {
        getTransactionService().execute(new VoidTransaction(){
            @Override
            protected void doPerform() {
                assertThat(getMeteringService().getAvailableReadingTypes()).hasSize(0).overridingErrorMessage("We should have started with 0 reading types");

                ReadingTypeGeneratorForElectricity readingTypeGeneratorForElectricity = new ReadingTypeGeneratorForElectricity();
                List<Pair<String, String>> readingTypes = readingTypeGeneratorForElectricity.generateReadingTypes();
                getMeteringService().createAllReadingTypes(readingTypes);

                assertThat(getMeteringService().getAvailableReadingTypes()).hasSize(readingTypes.size()).overridingErrorMessage("Expected " + readingTypes.size() + " reading types");
            }
        });

    }

    @Test
    public void aliasPrefixTest() {
        getTransactionService().execute(new VoidTransaction(){
            @Override
            protected void doPerform() {

                ReadingTypeGeneratorForElectricity readingTypeGeneratorForElectricity = new ReadingTypeGeneratorForElectricity();
                List<Pair<String, String>> readingTypes = readingTypeGeneratorForElectricity.generateReadingTypes();
                getMeteringService().createAllReadingTypes(readingTypes);

                getMeteringService().getAvailableReadingTypes().stream().forEach(readingType ->
                {
                    if(readingType.getAccumulation().equals(Accumulation.DELTADELTA)){
                        assertThat(readingType.getAliasName().startsWith("Delta "));
                    } else if(readingType.getAccumulation().equals(Accumulation.BULKQUANTITY)){
                        assertThat(readingType.getAliasName().startsWith("Bulk "));
                    } else if(readingType.getAccumulation().equals(Accumulation.SUMMATION)){
                        assertThat(readingType.getAliasName().startsWith("Sum "));
                    }
                });
            }
        });
    }

    private MeteringServiceImpl getMeteringService() {
        return injector.getInstance(MeteringServiceImpl.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }
}