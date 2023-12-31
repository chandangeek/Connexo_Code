/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.impl.ZoneModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
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
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListenerRegistration;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLRequestSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLResponseSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLSecuritySuiteLevelAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageCategoryAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageSpecAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLOfflineDeviceAdapter;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.TypedProperties;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
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
    private Thesaurus thesaurus;
    private MeteringZoneService meteringZoneService;

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
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new UtilModule(clock),
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new BpmModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new CalendarModule(),
                new MeteringModule(
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.6.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.6.72.0"),
                new MeteringGroupsModule(),
                new SearchModule(),
                new InMemoryMessagingModule(),
                new H2OrmModule(),
                new DataVaultModule(),
                new PkiModule(),
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
                new SchedulingModule(),
                new DeviceDataModule(),
                new CalendarModule(),
                new WebServicesModule(),
                new FileImportModule(),
                new ZoneModule(),
                new AuditServiceModule(),
                new TokenModule(),
                new BlackListModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(PluggableService.class);
            injector.getInstance(AuditService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.meteringZoneService = injector.getInstance(MeteringZoneService.class);
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

    public Clock getClock() {
        return clock;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).to(MockProtocolPluggableService.class).in(Scopes.SINGLETON);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(AppService.class).toInstance(mock(AppService.class));

        }

    }

    public MockProtocolPluggableService getMockProtocolPluggableService() {
        return (MockProtocolPluggableService) protocolPluggableService;
    }

    public static class MockProtocolPluggableService implements ProtocolPluggableService {

        private final ProtocolPluggableService protocolPluggableService;
        private final Thesaurus thesaurus;

        @Inject
        public MockProtocolPluggableService(Thesaurus thesaurus) {
            super();
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
            this.thesaurus = thesaurus;
        }

        public ProtocolPluggableService getMockedProtocolPluggableService() {
            return protocolPluggableService;
        }

        @Override
        public PropertySpec adapt(com.energyict.mdc.upl.properties.PropertySpec uplPropertySpec) {
            return UPLToConnexoPropertySpecAdapter.adaptTo(uplPropertySpec);
        }

        @Override
        public com.energyict.mdc.upl.properties.PropertySpec adapt(PropertySpec propertySpec) {
            return ConnexoToUPLPropertSpecAdapter.adaptTo(propertySpec);
        }

        @Override
        public AuthenticationDeviceAccessLevel adapt(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel uplLevel) {
            return UPLAuthenticationLevelAdapter.adaptTo(uplLevel, thesaurus);
        }

        @Override
        public EncryptionDeviceAccessLevel adapt(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel uplLevel) {
            return UPLEncryptionLevelAdapter.adaptTo(uplLevel, thesaurus);
        }

        @Override
        public com.energyict.mdc.upl.messages.DeviceMessageCategory adapt(DeviceMessageCategory connexoCategory) {
            return ConnexoDeviceMessageCategoryAdapter.adaptTo(connexoCategory);
        }

        @Override
        public com.energyict.mdc.upl.messages.DeviceMessageSpec adapt(DeviceMessageSpec connexoSpec) {
            return ConnexoDeviceMessageSpecAdapter.adaptTo(connexoSpec);
        }

        @Override
        public OfflineDevice adapt(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice) {
            return new UPLOfflineDeviceAdapter(offlineDevice);
        }

        public SecuritySuite adapt(com.energyict.mdc.upl.security.SecuritySuite uplLevel) {
            return UPLSecuritySuiteLevelAdapter.adaptTo(uplLevel, thesaurus);
        }

        @Override
        public RequestSecurityLevel adapt(com.energyict.mdc.upl.security.RequestSecurityLevel uplLevel) {
            return UPLRequestSecurityLevelAdapter.adaptTo(uplLevel, thesaurus);
        }

        @Override
        public ResponseSecurityLevel adapt(com.energyict.mdc.upl.security.ResponseSecurityLevel uplLevel) {
            return UPLResponseSecurityLevelAdapter.adaptTo(uplLevel, thesaurus);
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
        public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClassByNameTranslationKey(String name) {
            return protocolPluggableService.findConnectionTypePluggableClassByNameTranslationKey(name);
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
