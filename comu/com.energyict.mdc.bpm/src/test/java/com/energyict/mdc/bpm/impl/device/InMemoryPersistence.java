package com.energyict.mdc.bpm.impl.device;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.bpm.impl.issue.datacollection.IssueProcessAssociationProvider;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Provides initialization services that is typically used by classes that focus
 * on testing the correct implementation of the persistence aspects of entities in this bundle.
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
    //private DataModel dataModel;
    private DeviceProcessAssociationProvider deviceProvider;
    private IssueProcessAssociationProvider issueProvider;

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
                new TransactionModule(),
                new OrmModule(),
                new EventsModule(),
                new PubSubModule(),
                new UserModule(),
                new UtilModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new MeteringModule(),
                new PartyModule(),
                new IdsModule(),
                new CustomPropertySetsModule(),
                new DeviceDataModule(),
                new DeviceConfigurationModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new TaskModule(),
                new IssueDataCollectionModule(),
                new ProtocolPluggableModule(),
                new SchedulingModule(),
                new ProtocolApiModule(),
                new MasterDataModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new IssuesModule(),
                new TasksModule(),
                new TopologyModule(),
                new KpiModule(),
                new EngineModule(),
                new EngineModelModule(),
                new MdcIOModule()
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
            this.injector.getInstance(BpmService.class);
            this.injector.getInstance(PropertySpecService.class);
            this.injector.getInstance(FiniteStateMachineService.class);
            this.injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.injector.getInstance(IssueDataCollectionService.class);
            this.injector.getInstance(CustomPropertySetService.class);
            this.injector.getInstance(MeteringGroupsService.class);
            this.injector.getInstance(MasterDataService.class);
            this.deviceProvider = this.injector.getInstance(DeviceProcessAssociationProvider.class);
            this.issueProvider = this.injector.getInstance(IssueProcessAssociationProvider.class);
            //this.dataModel = this.lifeCycleService.getDataModel();
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
        this.principal = mock(Principal.class, withSettings().extraInterfaces(User.class));
        when(this.principal.getName()).thenReturn(testName);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return this.transactionService;
    }

    public ProcessAssociationProvider getDeviceAssociationProvider() {
        return this.deviceProvider;
    }

    public ProcessAssociationProvider getIssueAssociationProvider() {
        return this.issueProvider;
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            //bind(DataModel.class).toProvider(() -> dataModel);
        }

    }

}