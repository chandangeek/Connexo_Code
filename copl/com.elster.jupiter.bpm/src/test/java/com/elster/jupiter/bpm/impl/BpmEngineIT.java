package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;

import static org.assertj.guava.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BpmEngineIT {
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new BpmModule());
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(BpmService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistenceBpmDirectory() {
//        TransactionService transactionService = injector.getInstance(TransactionService.class);
//        BpmService bpmService = injector.getInstance(BpmService.class);
//        try (TransactionContext context = transactionService.getContext()) {
//            BpmEngine bpmEngine = bpmService.createBpmDirectory("MyEngine");
//            bpmEngine.setLocation("MyLocation");
//            bpmEngine.save();
//            context.commit();
//        }
//
//        BpmEngine bpmEngine = bpmService.findBpmDirectory("MyEngine");
//
//        Assertions.assertThat(bpmEngine).isInstanceOf(BpmEngineImpl.class);
//        Assertions.assertThat(bpmEngine.getName()).isEqualTo("MyEngine");
//        Assertions.assertThat(bpmEngine.getLocation()).isEqualTo("MyLocation");
    }
}
