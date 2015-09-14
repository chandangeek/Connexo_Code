package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCycleModule;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
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
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:58)
 */
public class InMemoryPersistence {

    private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
    private ThreadSecurityModule threadSecurityModule;
    private Principal principal;
    private Injector injector;
    private final Supplier<List<Module>> moduleSupplier;
    private TransactionService transactionService;
    private BundleContext bundleContext;
    private EventAdmin eventAdmin;
    private PartyService partyService;
    private MeteringService meteringService;
    private MeteringGroupsService meteringGroupService;
    private ValidationService validationService;
    private EstimationService estimationService;
    private KpiService kpiService;
    private com.elster.jupiter.tasks.TaskService jupiterTaskService;
    private TaskService mdcTaskService;
    private DeviceConfigurationService deviceConfigurationService;
    private RelationService relationService;
    private ProtocolPluggableService protocolPluggableService;
    private EngineConfigurationService engineConfigurationService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private SchedulingService schedulingService;
    private IssueService issueService;
    private PropertySpecService propertySpecService;
    private TopologyService topologyService;

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
                new DeviceLifeCycleModule()
        );
    }

    private InMemoryPersistence(Supplier<List<Module>> modulesSupplier) {
        super();
        this.moduleSupplier = modulesSupplier;
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
            this.injector.getInstance(FiniteStateMachineService.class);
            this.injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.injector.getInstance(DeviceLifeCycleService.class);
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
        this.partyService = mock(PartyService.class);
        this.meteringService = mock(MeteringService.class);
        this.meteringGroupService = mock(MeteringGroupsService.class);
        this.validationService = mock(ValidationService.class);
        this.estimationService = mock(EstimationService.class);
        this.kpiService = mock(KpiService.class);
        this.jupiterTaskService = mock(com.elster.jupiter.tasks.TaskService.class);
        this.mdcTaskService = mock(TaskService.class);
        this.engineConfigurationService = mock(EngineConfigurationService.class);
        this.deviceConfigurationService = mock(DeviceConfigurationService.class);
        this.relationService = mock(RelationService.class);
        this.protocolPluggableService = mock(ProtocolPluggableService.class);
        this.deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);
        this.schedulingService = mock(SchedulingService.class);
        this.issueService = mock(IssueService.class);
        this.propertySpecService = mock(PropertySpecService.class);
        this.topologyService = mock(TopologyService.class);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return this.transactionService;
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(PartyService.class).toInstance(partyService);
            bind(MeteringService.class).toInstance(meteringService);
            bind(MeteringGroupsService.class).toInstance(meteringGroupService);
            bind(ValidationService.class).toInstance(validationService);
            bind(EstimationService.class).toInstance(estimationService);
            bind(KpiService.class).toInstance(kpiService);
            bind(com.elster.jupiter.tasks.TaskService.class).toInstance(jupiterTaskService);
            bind(TaskService.class).toInstance(mdcTaskService);
            bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
            bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
            bind(RelationService.class).toInstance(relationService);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
            bind(SchedulingService.class).toInstance(schedulingService);
            bind(IssueService.class).toInstance(issueService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(TopologyService.class).toInstance(topologyService);
        }

    }

}