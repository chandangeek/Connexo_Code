/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides initialization services that is typically used by classes that focus
 * on testing the correct implementation of the persistence aspects of entities in this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:58)
 */
public class InMemoryPersistence {

    private final Supplier<List<Module>> moduleSupplier;
    private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
    private ThreadSecurityModule threadSecurityModule;
    private Principal principal;
    private Injector injector;
    private TransactionService transactionService;
    private BundleContext bundleContext;
    private EventAdmin eventAdmin;
    private DataModel dataModel;
    private DeviceLifeCycleConfigurationServiceImpl lifeCycleService;
    private MeteringDataModelService meteringDataModelService;
    private LicenseService licenseService;
    private PropertySpecService propertySpecService;
    private SearchService searchService;
    private TimeService timeService;

    private InMemoryPersistence(Supplier<List<Module>> modulesSupplier) {
        super();
        this.moduleSupplier = modulesSupplier;
    }

    /**
     * Returns a new InMemoryPersistence that uses all the defaults
     * that are appropriate for the finite state machine bundle.
     *
     * @return The default InMemoryPersistence
     */
    public static InMemoryPersistence defaultPersistence() {
        return new InMemoryPersistence(InMemoryPersistence::defaultModules);
    }

    private static List<Module> defaultModules() {
        return Arrays.asList(
                new InMemoryMessagingModule(),
                new DataVaultModule(),
                new PartyModule(),
                new TransactionModule(),
                new H2OrmModule(),
                new TaskModule(),
                new CalendarModule(),
                new IdsModule(),
                new EventsModule(),
                new MeteringModule(),
                new PubSubModule(),
                new CustomPropertySetsModule(),
                new UserModule(),
                new UtilModule(),
                new BpmModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new AuditServiceModule(),
                new WebServicesModule(),
                new TokenModule(),
                new BlackListModule()
        );
    }

    public void initializeDatabase(String testName) {
        this.initializeMocks(testName);
        this.threadSecurityModule = new ThreadSecurityModule(this.principal);
        this.injector = Guice.createInjector(this.guiceModules());
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.injector.getInstance(OrmService.class);
            this.injector.getInstance(UserService.class);
            this.injector.getInstance(NlsService.class);
            this.injector.getInstance(EventService.class);
            this.injector.getInstance(CustomPropertySetService.class);
            this.injector.getInstance(PartyService.class);
            this.injector.getInstance(CalendarService.class);
            this.injector.getInstance(MeteringDataModelService.class);
            this.lifeCycleService = this.injector.getInstance(DeviceLifeCycleConfigurationServiceImpl.class);
            this.dataModel = this.lifeCycleService.getDataModel();
            TestMicroCheck.Factory factory = new TestMicroCheck.Factory();
            this.lifeCycleService.addMicroCheckFactory(factory);
            ctx.commit();
        }
    }

    private List<Module> guiceModules() {
        List<Module> modules = new ArrayList<>(this.moduleSupplier.get());
        modules.add(this.threadSecurityModule);
        modules.add(this.bootstrapModule);
        modules.add(new MockModule());
        return modules;
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class);
        this.meteringDataModelService = mock(MeteringDataModelService.class);
        this.licenseService = mock(LicenseService.class);
        this.propertySpecService = mock(PropertySpecService.class);
        this.searchService = mock(SearchService.class);
        this.timeService = mock(TimeService.class);
        when(this.principal.getName()).thenReturn(testName);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return this.transactionService;
    }

    public DeviceLifeCycleConfigurationServiceImpl getDeviceLifeCycleConfigurationService() {
        return this.lifeCycleService;
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(SearchService.class).toInstance(searchService);
            bind(TimeService.class).toInstance(timeService);
            bind(LicenseService.class).toInstance(licenseService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(HttpService.class).toInstance(mock(HttpService.class));
        }
    }
}
