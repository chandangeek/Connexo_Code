/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
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
    private User principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private EventServiceImpl eventService;
    private MasterDataService masterDataService;
    private TaskService taskService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private DataModel dataModel;
    private Injector injector;
    private ValidationService validationService;
    private EstimationService estimationService;
    private CalendarService calendarService;
    private PluggableService pluggableService;
    private CustomPropertySetService customPropertySetService;
    private SecurityManagementService securityManagementService;

    private boolean mockProtocolPluggableService;
    private ProtocolPluggableService protocolPluggableService;
    private PropertySpecService propertySpecService;
    private LogBookTypeUpdateEventHandler logBookTypeUpdateEventHandler;
    private ComTaskDeletionEventHandler comTaskDeletionEventHandler;
    private LogBookTypeDeletionEventHandler logBookTypeDeletionEventHandler;
    private LoadProfileTypeUpdateEventHandler loadProfileTypeUpdateEventHandler;
    private LoadProfileTypeDeletionEventHandler loadProfileTypeDeletionEventHandler;
    private MeasurementTypeUpdateEventHandler measurementTypeUpdateEventHandler;
    private MeasurementTypeDeletionEventHandler measurementTypeDeletionEventHandler;
    private ChannelTypeDeleteFromLoadProfileTypeEventHandler channelTypeDeleteFromLoadProfileTypeEventHandler;
    private LicenseService licenseService;
    private LicensedProtocolService licensedProtocolService;
    private ConnectionTypeService connectionTypeService;
    private InMemoryBootstrapModule bootstrapModule;
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private FiniteStateMachineService finiteStateMachineService;

    public void initializeDatabaseWithMockedProtocolPluggableService(String testName, boolean showSqlLogging) {
        this.initializeDatabase(testName, showSqlLogging, true);
    }

    public void initializeDatabaseWithRealProtocolPluggableService(String testName, boolean showSqlLogging) {
        this.initializeDatabase(testName, showSqlLogging, false);
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return finiteStateMachineService;
    }

    private void initializeDatabase(String testName, boolean showSqlLogging, boolean mockedProtocolPluggableService) {
        this.initializeMocks(testName, mockedProtocolPluggableService);
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(this.guiceModules(showSqlLogging, mockedProtocolPluggableService, bootstrapModule));
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            injector.getInstance(UserService.class);
            injector.getInstance(Publisher.class);
            this.eventService = (EventServiceImpl) injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            this.securityManagementService = this.injector.getInstance(SecurityManagementService.class);
            finiteStateMachineService = injector.getInstance(FiniteStateMachineService.class);
            deviceLifeCycleConfigurationService = injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.customPropertySetService = injector.getInstance(CustomPropertySetService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            injector.getInstance(EngineConfigurationService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.taskService = injector.getInstance(TaskService.class);
            this.validationService = injector.getInstance(ValidationService.class);
            this.estimationService = injector.getInstance(EstimationService.class);
            this.calendarService = injector.getInstance(CalendarService.class);
            this.propertySpecService = injector.getInstance(PropertySpecService.class);
            this.pluggableService = this.injector.getInstance(PluggableService.class);
            if (!mockedProtocolPluggableService) {
                this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
                this.protocolPluggableService.addLicensedProtocolService(this.licensedProtocolService);
                this.protocolPluggableService.addConnectionTypeService(this.connectionTypeService);
            }
            this.dataModel = this.createNewDeviceConfigurationService();
            ctx.commit();
        }
    }

    private Module[] guiceModules(boolean showSqlLogging, boolean mockedProtocolPluggableService, InMemoryBootstrapModule bootstrapModule) {
        List<Module> modules = new ArrayList<>();
        modules.addAll(Arrays.asList(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new CustomPropertySetsModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new PkiModule(),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "11.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "11.0.0.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new H2OrmModule(),
                new DataVaultModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new DeviceLifeCycleConfigurationModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new TaskModule(),
                new DeviceConfigurationModule(),
                new EngineModelModule(),
                new PluggableModule(),
                new SchedulingModule(),
                new TimeModule(),
                new CustomPropertySetsModule(),
                new CalendarModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new FileImportModule(),
                new TokenModule(),
                new BlackListModule(),
                new TransactionModule()
        ));
        if (!mockedProtocolPluggableService) {
            modules.add(new IssuesModule());
            modules.add(new BasicPropertiesModule());
            modules.add(new MdcDynamicModule());
            modules.add(new ProtocolPluggableModule());
            modules.add(new CustomPropertySetsModule());
        }
        return modules.toArray(new Module[modules.size()]);
    }

    private DataModel createNewDeviceConfigurationService() {
        this.deviceConfigurationService = injector.getInstance(DeviceConfigurationServiceImpl.class);
        return this.deviceConfigurationService.getDataModel();
    }

    public void run(DataModelInitializer... dataModelInitializers) {
        try (TransactionContext ctx = this.transactionService.getContext()) {
            for (DataModelInitializer initializer : dataModelInitializers) {
                initializer.initializeDataModel(this.dataModel);
            }
            ctx.commit();
        }
    }

    private void initializeMocks(String testName, boolean mockedProtocolPluggableService) {
        this.mockProtocolPluggableService = mockedProtocolPluggableService;
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(User.class);
        GrantPrivilege superGrant = mock(GrantPrivilege.class);
        when(superGrant.canGrant(any())).thenReturn(true);
        Group superUser = mock(Group.class);
        when(superUser.getPrivileges()).thenReturn(ImmutableMap.of("", asList(superGrant)));
        when(this.principal.getGroups()).thenReturn(asList(superUser));
        when(this.principal.getName()).thenReturn(testName);
        if (this.mockProtocolPluggableService) {
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
            when(this.protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.empty());

            when(protocolPluggableService.adapt(any(AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                return UPLAuthenticationLevelAdapter.adaptTo((AuthenticationDeviceAccessLevel) args[0], null);
            });
            when(protocolPluggableService.adapt(any(EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                return UPLEncryptionLevelAdapter.adaptTo((EncryptionDeviceAccessLevel) args[0], null);
            });
        }
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        this.licensedProtocolService = mock(LicensedProtocolService.class);
        when(this.licensedProtocolService.isValidJavaClassName(anyString(), any(License.class))).thenReturn(true);
        this.connectionTypeService = mock(ConnectionTypeService.class);
        when(this.connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public EventService getEventService() {
        return eventService;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    public ValidationService getValidationService() {
        return validationService;
    }

    public EstimationService getEstimationService() {
        return estimationService;
    }

    public CalendarService getCalendarService() {
        return calendarService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return customPropertySetService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public DeviceConfigurationServiceImpl getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public PluggableService getPluggableService() {
        return pluggableService;
    }

    public LicensedProtocolService getLicensedProtocolService() {
        return licensedProtocolService;
    }

    public ProtocolPluggableService getProtocolPluggableService() {
        return protocolPluggableService;
    }

    public MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    public Injector getInjector() {
        return injector;
    }

    public SchedulingService getSchedulingService() {
        return injector.getInstance(SchedulingService.class);
    }

    public void registerEventHandlers() {
        this.comTaskDeletionEventHandler = this.registerTopicHandler(new ComTaskDeletionEventHandler(this.deviceConfigurationService));
        this.logBookTypeDeletionEventHandler = this.registerTopicHandler(new LogBookTypeDeletionEventHandler(this.deviceConfigurationService));
        this.logBookTypeUpdateEventHandler = this.registerTopicHandler(new LogBookTypeUpdateEventHandler(this.deviceConfigurationService));
        this.loadProfileTypeDeletionEventHandler = this.registerTopicHandler(new LoadProfileTypeDeletionEventHandler(this.deviceConfigurationService));
        this.loadProfileTypeUpdateEventHandler = this.registerTopicHandler(new LoadProfileTypeUpdateEventHandler(this.deviceConfigurationService));
        this.measurementTypeDeletionEventHandler = this.registerTopicHandler(new MeasurementTypeDeletionEventHandler(this.deviceConfigurationService));
        this.measurementTypeUpdateEventHandler = this.registerTopicHandler(new MeasurementTypeUpdateEventHandler(this.deviceConfigurationService, masterDataService));
        this.channelTypeDeleteFromLoadProfileTypeEventHandler = this.registerTopicHandler(new ChannelTypeDeleteFromLoadProfileTypeEventHandler(this.deviceConfigurationService));
    }

    <T extends TopicHandler> T registerTopicHandler(T topicHandler) {
        this.eventService.addTopicHandler(topicHandler);
        return topicHandler;
    }

    public void unregisterEventHandlers() {
        this.unregisterSubscriber(this.comTaskDeletionEventHandler);
        this.unregisterSubscriber(this.logBookTypeDeletionEventHandler);
        this.unregisterSubscriber(this.logBookTypeUpdateEventHandler);
        this.unregisterSubscriber(this.loadProfileTypeDeletionEventHandler);
        this.unregisterSubscriber(this.loadProfileTypeUpdateEventHandler);
        this.unregisterSubscriber(this.measurementTypeDeletionEventHandler);
        this.unregisterSubscriber(this.measurementTypeUpdateEventHandler);
        this.unregisterSubscriber(this.channelTypeDeleteFromLoadProfileTypeEventHandler);
    }

    void unregisterSubscriber(TopicHandler topicHandler) {
        if (topicHandler != null) {
            this.eventService.removeTopicHandler(topicHandler);
        }
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    public User getMockedUser() {
        return this.principal;
    }

    public DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return deviceLifeCycleConfigurationService;
    }

    public SecurityManagementService getSecurityManagementService() {
        return securityManagementService;
    }

    private class MockModule extends AbstractModule {

        private final DeviceMessageSpecificationService deviceMessageSpecificationService;

        public MockModule() {
            this.deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);

            when(deviceMessageSpecificationService.findCategoryById(anyInt())).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                return Optional.of(DeviceMessageTestCategories.values()[((int) args[0])]);
            });
        }

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(IdentificationService.class).toInstance(mock(IdentificationService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
            if (mockProtocolPluggableService) {
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            }
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
        }
    }
}
