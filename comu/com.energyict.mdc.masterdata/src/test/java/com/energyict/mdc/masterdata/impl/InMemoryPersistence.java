package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
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

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private MeteringService meteringService;
    private NlsService nlsService;
    private MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private MasterDataServiceImpl masterDataService;
    private DataModel dataModel;
    private Injector injector;
    private InMemoryBootstrapModule bootstrapModule;

    public void initializeDatabase(String testName, boolean showSqlLogging, boolean createDefaults) {
        this.initializeMocks(testName);
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(true), // this module currently really needs ALL reading types, explicetly tested
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            injector.getInstance(UserService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.mdcReadingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.dataModel = this.createNewMasterDataService(createDefaults);
            ctx.commit();
        }
    }

    private DataModel createNewMasterDataService(boolean createDefaults) {
        this.masterDataService = new MasterDataServiceImpl(this.ormService, this.eventService, this.nlsService, this.meteringService, this.injector.getInstance(Publisher.class), this.mdcReadingTypeUtilService, createDefaults);
        return this.masterDataService.getDataModel();
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class);
        when(this.principal.getName()).thenReturn(testName);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public MdcReadingTypeUtilService getReadingTypeUtilService() {
        return mdcReadingTypeUtilService;
    }

    public MasterDataServiceImpl getMasterDataService() {
        return masterDataService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    public Injector getInjector() {
        return injector;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DataModel.class).toProvider(() -> dataModel);
        }

    }

}