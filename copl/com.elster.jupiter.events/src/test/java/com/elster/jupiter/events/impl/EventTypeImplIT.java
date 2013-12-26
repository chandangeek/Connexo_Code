package com.elster.jupiter.events.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
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

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EventTypeImplIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule());
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(EventService.class);
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
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                EventTypeImpl eventType = new EventTypeImpl("topic");
                eventType.setComponent("A");
                eventType.setPublish(true);
                eventType.setName("name");
                eventType.setCategory("category");
                eventType.setScope("scope");
                eventType.addProperty("A", ValueType.STRING, "C");
                eventType.save();

                Optional<EventType> optional = Bus.getOrmClient().getEventTypeFactory().getOptional(eventType.getTopic());

                assertThat(optional).isPresent();
                EventType read = optional.get();

                assertThat(read.getComponent()).isEqualTo("A");
                assertThat(read.shouldPublish()).isTrue();
                assertThat(read.getName()).isEqualTo("name");
                assertThat(read.getCategory()).isEqualTo("category");
                assertThat(read.getScope()).isEqualTo("scope");
                assertThat(read.getPropertyTypes()).hasSize(1);
            }
        });
    }

}
