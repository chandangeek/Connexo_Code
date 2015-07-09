package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
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
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.Publisher;
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
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
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
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
    private PluggableService pluggableService;

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

    public InMemoryPersistence() {
    }

    public void initializeDatabaseWithMockedProtocolPluggableService(String testName, boolean showSqlLogging) {
        this.initializeDatabase(testName, showSqlLogging, true);
    }

    public void initializeDatabaseWithRealProtocolPluggableService(String testName, boolean showSqlLogging) {
        this.initializeDatabase(testName, showSqlLogging, false);
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
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            injector.getInstance(EngineConfigurationService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.taskService = injector.getInstance(TaskService.class);
            this.validationService = injector.getInstance(ValidationService.class);
            this.estimationService = injector.getInstance(EstimationService.class);
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
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new DataVaultModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new ValidationModule(),
                new EstimationModule(),
                new DeviceLifeCycleConfigurationModule(),
                new MeteringGroupsModule(),
                new TaskModule(),
                new DeviceConfigurationModule(),
                new MdcIOModule(),
                new EngineModelModule(),
                new PluggableModule(),
                new SchedulingModule(),
                new TimeModule()));
        if (!mockedProtocolPluggableService) {
            modules.add(new IssuesModule());
            modules.add(new BasicPropertiesModule());
            modules.add(new MdcDynamicModule());
            modules.add(new ProtocolPluggableModule());
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
        this.principal = mock(Principal.class, withSettings().extraInterfaces(User.class));
        when(this.principal.getName()).thenReturn(testName);
        if (this.mockProtocolPluggableService) {
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
            when(this.protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.empty());
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

    public MasterDataService getMasterDataService() {
        return masterDataService;
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

    public PluggableService getPluggableService(){
        return pluggableService;
    }

    public LicensedProtocolService getLicensedProtocolService(){
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

    public User getMockedUser(){
        return (User) this.principal;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            if (mockProtocolPluggableService) {
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            }
            bind(DataModel.class).toProvider(() -> dataModel);
        }

    }

}