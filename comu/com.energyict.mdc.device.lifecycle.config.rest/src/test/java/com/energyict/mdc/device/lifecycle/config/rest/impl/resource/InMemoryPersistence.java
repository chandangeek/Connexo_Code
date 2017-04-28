/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleModule;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCycleModule;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
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

    private InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
    private ThreadSecurityModule threadSecurityModule;
    private User principal;
    private Injector injector;
    private final Supplier<List<Module>> moduleSupplier;
    private TransactionService transactionService;
    private BundleContext bundleContext;
    private EventAdmin eventAdmin;
    private MeteringGroupsService meteringGroupService;
    private ValidationService validationService;
    private EstimationService estimationService;
    private KpiService kpiService;
    private com.elster.jupiter.tasks.TaskService jupiterTaskService;
    private TaskService mdcTaskService;
    private DeviceConfigurationService deviceConfigurationService;
    private ProtocolPluggableService protocolPluggableService;
    private EngineConfigurationService engineConfigurationService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private SchedulingService schedulingService;
    private IssueService issueService;
    private PropertySpecService propertySpecService;
    private TopologyService topologyService;
    private DataVaultService dataVaultService;
    private CustomPropertySetService customPropertySetService;
    private LicenseService licenseService;
    private SearchService searchService;
    private TimeService timeService;

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
                new PartyModule(),
                new OrmModule(),
                new CalendarModule(),
                new EventsModule(),
                new IdsModule(),
                new PubSubModule(),
                new UserModule(),
                new BpmModule(),
                new MeteringModule(),
                new UtilModule(),
                new DomainUtilModule(),
                new CustomPropertySetsModule(),
                new NlsModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceLifeCycleModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new UsagePointLifeCycleModule()
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
            this.injector.getInstance(CustomPropertySetService.class);
            this.injector.getInstance(FiniteStateMachineService.class);
            this.injector.getInstance(PartyService.class);
            this.injector.getInstance(MeteringDataModelService.class);
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
        this.principal = mock(User.class);
        GrantPrivilege superGrant = mock(GrantPrivilege.class);
        when(superGrant.canGrant(any())).thenReturn(true);
        Group superUser = mock(Group.class);
        when(superUser.getPrivileges()).thenReturn(ImmutableMap.of("", asList(superGrant)));
        when(this.principal.getGroups()).thenReturn(asList(superUser));
        when(this.principal.getName()).thenReturn(testName);
        this.meteringGroupService = mock(MeteringGroupsService.class);
        this.validationService = mock(ValidationService.class);
        this.estimationService = mock(EstimationService.class);
        this.kpiService = mock(KpiService.class);
        this.jupiterTaskService = mock(com.elster.jupiter.tasks.TaskService.class);
        RecurrentTaskBuilder recurrentTaskBuilder = mock(RecurrentTaskBuilder.class, RETURNS_DEEP_STUBS);
        when(this.jupiterTaskService.newBuilder()).thenReturn(recurrentTaskBuilder);
        this.mdcTaskService = mock(TaskService.class);
        this.engineConfigurationService = mock(EngineConfigurationService.class);
        this.deviceConfigurationService = mock(DeviceConfigurationService.class);
        this.protocolPluggableService = mock(ProtocolPluggableService.class);
        this.deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);
        this.schedulingService = mock(SchedulingService.class);
        this.issueService = mock(IssueService.class);
        this.propertySpecService = mock(PropertySpecService.class);
        this.topologyService = mock(TopologyService.class);
        this.dataVaultService = mock(DataVaultService.class);
        this.licenseService = mock(LicenseService.class);
        this.searchService = mock(SearchService.class);
        this.timeService = mock(TimeService.class);
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
            bind(MeteringGroupsService.class).toInstance(meteringGroupService);
            bind(ValidationService.class).toInstance(validationService);
            bind(EstimationService.class).toInstance(estimationService);
            bind(KpiService.class).toInstance(kpiService);
            bind(com.elster.jupiter.tasks.TaskService.class).toInstance(jupiterTaskService);
            bind(TaskService.class).toInstance(mdcTaskService);
            bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
            bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
            bind(SchedulingService.class).toInstance(schedulingService);
            bind(IssueService.class).toInstance(issueService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(TopologyService.class).toInstance(topologyService);
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(SearchService.class).toInstance(searchService);
            bind(TimeService.class).toInstance(timeService);
            bind(LicenseService.class).toInstance(licenseService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }

    }

}