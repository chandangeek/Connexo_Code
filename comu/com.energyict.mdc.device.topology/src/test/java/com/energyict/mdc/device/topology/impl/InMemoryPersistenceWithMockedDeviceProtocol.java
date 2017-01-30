package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListenerRegistration;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (17:19)
 */
public class InMemoryPersistenceWithMockedDeviceProtocol {

    private final Clock clock;
    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private EventService eventService;
    private MasterDataService masterDataService;
    private DeviceConfigurationService deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private DeviceDataModelServiceImpl deviceDataModelService;
    private TopologyService topologyService;
    private SchedulingService schedulingService;
    private DataVaultService dataVaultService;
    private InMemoryBootstrapModule bootstrapModule;
    private Thesaurus thesaurus;

    public InMemoryPersistenceWithMockedDeviceProtocol() {
        this(Clock.systemDefaultZone());
    }

    public InMemoryPersistenceWithMockedDeviceProtocol(Clock clock) {
        super();
        this.clock = clock;
    }

    public void initializeDatabase(String testName, boolean showSqlLogging) {
        this.initializeMocks(testName);
        this.bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new UtilModule(clock),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(
                         "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"
                        ,"0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.167.0"
                        ,"0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0"
                        ,"0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"
                        ,"0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0"
                        ,"0.0.0.12.0.3.109.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.41.109.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.41.118.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.41.139.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.23.0"
                        ,"0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.279.0"
                        ,"0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.6.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.107.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.125.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.126.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.39.0"
                        ,"0.2.0.6.0.9.58.0.0.0.0.0.0.0.0.0.125.0"
                ),
                new MeteringGroupsModule(),
                new SearchModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolApiModule(),
                new KpiModule(),
                new TaskModule(),
                new TasksModule(),
                new PluggableModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new SchedulingModule(),
                new DeviceDataModule(),
                new TopologyModule(),
                new CalendarModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(PluggableService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
            injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            injector.getInstance(MeteringGroupsService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.schedulingService = injector.getInstance(SchedulingService.class);
            this.topologyService = injector.getInstance(TopologyService.class);
            this.dataModel = this.createNewDeviceDataService(injector);
            ctx.commit();
        }
    }

    private DataModel createNewDeviceDataService(Injector injector) {
        deviceDataModelService = (DeviceDataModelServiceImpl) injector.getInstance(DeviceDataModelService.class);
        return deviceDataModelService.dataModel();
    }

    private void initializeMocks(String testName) {
        this.dataVaultService = mock(DataVaultService.class);
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class);
        when(this.principal.getName()).thenReturn(testName);
        this.protocolPluggableService = mock(ProtocolPluggableService.class);
        this.thesaurus = mock(Thesaurus.class);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    public DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public ProtocolPluggableService getProtocolPluggableService() {
        return protocolPluggableService;
    }

    public MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    public ServerDeviceService getDeviceService() {
        return deviceDataModelService.deviceService();
    }

    public TopologyService getTopologyService() {
        return this.topologyService;
    }

    public EventService getEventService() {
        return eventService;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).to(MockProtocolPluggableService.class).in(Scopes.SINGLETON);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }

    }

    public MockProtocolPluggableService getMockProtocolPluggableService() {
        return (MockProtocolPluggableService) protocolPluggableService;
    }

    public static class MockProtocolPluggableService implements ProtocolPluggableService {

        private final ProtocolPluggableService protocolPluggableService;

        public ProtocolPluggableService getMockedProtocolPluggableService() {
            return protocolPluggableService;
        }

        @Inject
        private MockProtocolPluggableService() {
            super();
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
        }

        @Override
        public void addLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        }

        @Override
        public void addDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        }

        @Override
        public void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        }

        @Override
        public void addConnectionTypeService(ConnectionTypeService connectionTypeService) {
        }

        @Override
        public ProtocolDeploymentListenerRegistration register(ProtocolDeploymentListener listener) {
            return mock(ProtocolDeploymentListenerRegistration.class);
        }

        @Override
        public List<LicensedProtocol> getAllLicensedProtocols() {
            return Collections.emptyList();
        }

        @Override
        public boolean isLicensedProtocolClassName(String javaClassName) {
            return false;
        }

        @Override
        public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            return null;
        }

        @Override
        public Object createProtocol(String className) {
            return protocolPluggableService.createProtocol(className);
        }

        @Override
        public Object createDeviceProtocolMessagesFor(String javaClassName) {
            return protocolPluggableService.createDeviceProtocolMessagesFor(javaClassName);
        }

        @Override
        public Object createDeviceProtocolSecurityFor(String javaClassName) {
            return protocolPluggableService.createDeviceProtocolSecurityFor(javaClassName);
        }

        @Override
        public Finder<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses() {
            return protocolPluggableService.findAllDeviceProtocolPluggableClasses();
        }

        @Override
        public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long id) {
            return protocolPluggableService.findDeviceProtocolPluggableClass(id);
        }

        @Override
        public Optional<DeviceProtocolPluggableClass> findAndLockDeviceProtocolPluggableClassByIdAndVersion(long id, long version) {
            return protocolPluggableService.findAndLockDeviceProtocolPluggableClassByIdAndVersion(id, version);
        }

        @Override
        public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name) {
            return protocolPluggableService.findDeviceProtocolPluggableClassByName(name);
        }

        @Override
        public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassesByClassName(String className) {
            return protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(className);
        }

        @Override
        public void deleteDeviceProtocolPluggableClass(long id) {
            protocolPluggableService.deleteDeviceProtocolPluggableClass(id);
        }

        @Override
        public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) {
            return protocolPluggableService.newDeviceProtocolPluggableClass(name, className);
        }

        @Override
        public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties typedProperties) {
            return protocolPluggableService.newDeviceProtocolPluggableClass(name, className, typedProperties);
        }

        @Override
        public List<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClassByClassName(String javaClassName) {
            return protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(javaClassName);
        }

        @Override
        public Optional<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClass(long id) {
            return protocolPluggableService.findInboundDeviceProtocolPluggableClass(id);
        }

        @Override
        public Optional<InboundDeviceProtocolPluggableClass> findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(long id, long version) {
            return protocolPluggableService.findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(id, version);
        }

        @Override
        public List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass() {
            return protocolPluggableService.findAllInboundDeviceProtocolPluggableClass();
        }

        @Override
        public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName) {
            return protocolPluggableService.newInboundDeviceProtocolPluggableClass(name, javaClassName);
        }

        @Override
        public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName, TypedProperties properties) {
            return protocolPluggableService.newInboundDeviceProtocolPluggableClass(name, javaClassName, properties);
        }

        @Override
        public void deleteInboundDeviceProtocolPluggableClass(long id) {
            protocolPluggableService.deleteInboundDeviceProtocolPluggableClass(id);
        }

        @Override
        public List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName) {
            return protocolPluggableService.findConnectionTypePluggableClassByClassName(javaClassName);
        }

        @Override
        public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClassByName(String name) {
            return protocolPluggableService.findConnectionTypePluggableClassByName(name);
        }

        @Override
        public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long id) {
            return protocolPluggableService.findConnectionTypePluggableClass(id);
        }

        @Override
        public List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses() {
            return protocolPluggableService.findAllConnectionTypePluggableClasses();
        }

        @Override
        public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName) {
            return protocolPluggableService.newConnectionTypePluggableClass(name, javaClassName);
        }

        @Override
        public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName, TypedProperties properties) {
            return protocolPluggableService.newConnectionTypePluggableClass(name, javaClassName, properties);
        }

        @Override
        public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName) {
            return protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(pluggableClass, dialectName);
        }

        @Override
        public Optional<Object> unMarshallDeviceProtocolCache(String jsonCache) {
            return this.protocolPluggableService.unMarshallDeviceProtocolCache(jsonCache);
        }

        @Override
        public String marshallDeviceProtocolCache(Object legacyCache) {
            return this.protocolPluggableService.marshallDeviceProtocolCache(legacyCache);
        }

        @Override
        public ConnectionType createConnectionType(String javaClassName) {
            return null;
        }

        @Override
        public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
            return null;
        }

    }

}