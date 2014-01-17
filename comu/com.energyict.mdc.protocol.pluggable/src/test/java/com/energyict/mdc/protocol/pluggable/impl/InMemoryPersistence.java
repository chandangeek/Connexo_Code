package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides initialization services that is typically used by classes that focus
 * on testing the correct implementation of the persistence aspects of entities in this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (09:57)
 */
public class InMemoryPersistence {

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    private OrmService ormService;
    private EventService eventService;
    private RelationService relationService;
    private PropertySpecService propertySpecService;
    private ProtocolPluggableService protocolPluggableService;
    private Injector injector;

    public static InMemoryPersistence initializeDatabase()  {
        InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdmin eventAdmin = mock(EventAdmin.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence.mdc.protocol.pluggable");
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(bundleContext, eventAdmin),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new IssuesModule(),
                new MdcCommonModule(),
                new ProtocolsModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new ProtocolPluggableModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            inMemoryPersistence.ormService = injector.getInstance(OrmService.class);
            inMemoryPersistence.eventService = injector.getInstance(EventService.class);
            inMemoryPersistence.relationService = injector.getInstance(RelationService.class);
            inMemoryPersistence.propertySpecService= injector.getInstance(PropertySpecService.class);
            inMemoryPersistence.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            inMemoryPersistence.injector = injector;
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        return inMemoryPersistence;
    }

    public ProtocolPluggableService getProtocolPluggableService() {
        return protocolPluggableService;
    }

    public void cleanUpDataBase() throws SQLException {
        Environment environment = Environment.DEFAULT.get();
        if (environment != null) {
            Object bootstrapModule = environment.get(JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME);
            if (bootstrapModule != null) {
                deactivate(bootstrapModule);
            }
        }
    }

    private void deactivate(Object bootstrapModule) {
        if (bootstrapModule instanceof InMemoryBootstrapModule) {
            InMemoryBootstrapModule inMemoryBootstrapModule = (InMemoryBootstrapModule) bootstrapModule;
            inMemoryBootstrapModule.deactivate();
        }
    }

    private static class MockModule extends AbstractModule {

        private BundleContext bundleContext;

        private EventAdmin eventAdmin;

        private MockModule(BundleContext bundleContext, EventAdmin eventAdmin) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = eventAdmin;
        }

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
        }

    }

}