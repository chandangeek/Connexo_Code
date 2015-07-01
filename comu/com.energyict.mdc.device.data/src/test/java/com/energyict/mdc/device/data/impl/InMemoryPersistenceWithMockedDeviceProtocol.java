package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
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
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
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
import com.google.inject.Provider;
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

import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:52
 */
public class InMemoryPersistenceWithMockedDeviceProtocol {

    private final Clock clock;

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private MasterDataService masterDataService;
    private DeviceConfigurationService deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private DeviceDataModelServiceImpl deviceDataModelService;
    private TaskService taskService;
    private SchedulingService schedulingService;
    private ValidationService validationService;
    private InMemoryBootstrapModule bootstrapModule;
    private IssueService issueService;

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
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(),
                new MeteringGroupsModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolApiModule(),
                new KpiModule(),
                new TaskModule(),
                new TasksModule(),
                new PluggableModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new ValidationModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new MdcIOModule(),
                new SchedulingModule(),
                new DeviceDataModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(PluggableService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            injector.getInstance(MeteringGroupsService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.taskService = injector.getInstance(TaskService.class);
            this.validationService = injector.getInstance(ValidationService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.schedulingService = injector.getInstance(SchedulingService.class);
            this.issueService = injector.getInstance(IssueService.class);
            this.dataModel = this.createNewDeviceDataService(injector);
            ctx.commit();
        }
    }

    private DataModel createNewDeviceDataService(Injector injector) {
        deviceDataModelService = injector.getInstance(DeviceDataModelServiceImpl.class);
        return deviceDataModelService.dataModel();
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class);
        when(this.principal.getName()).thenReturn(testName);
        this.protocolPluggableService = mock(ProtocolPluggableService.class);
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

    public EventService getEventService() {
        return eventService;
    }

    public SchedulingService getSchedulingService() {
        return schedulingService;
    }

    public DataModel getDataModel() {
        return this.deviceDataModelService.dataModel();
    }

    public IssueService getIssueService() {
        return this.issueService;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BpmService.class).toInstance(mock(BpmService.class));
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).to(MockProtocolPluggableService.class).in(Scopes.SINGLETON);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(DataModel.class).toProvider(() -> dataModel);
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

        @Inject
        private MockProtocolPluggableService() {
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
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
        public String createOriginalAndConformRelationNameBasedOnJavaClassname(Class clazz) {
            return protocolPluggableService.createOriginalAndConformRelationNameBasedOnJavaClassname(clazz);
        }

        @Override
        public String createConformRelationTypeName(String name) {
            return protocolPluggableService.createConformRelationTypeName(name);
        }

        @Override
        public String createConformRelationAttributeName(String name) {
            return protocolPluggableService.createConformRelationAttributeName(name);
        }

        @Override
        public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName) {
            return protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(pluggableClass, dialectName);
        }

        @Override
        public boolean isDefaultAttribute(RelationAttributeType attributeType) {
            return protocolPluggableService.isDefaultAttribute(attributeType);
        }

        @Override
        public RelationType findSecurityPropertyRelationType(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            return protocolPluggableService.findSecurityPropertyRelationType(deviceProtocolPluggableClass);
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

        @Override
        public boolean hasSecurityRelations(RelationParticipant securityPropertySet, DeviceProtocol deviceProtocol) {
            return false;
        }
    }

}