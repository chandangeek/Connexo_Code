/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
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
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.impl.MeteringZoneModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
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
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.multielement.MultiElementDeviceModule;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:07)
 */
public class InMemoryIntegrationPersistence {
    private BundleContext bundleContext;
    private User principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private Publisher publisher;
    private NlsService nlsService;
    private Clock clock;
    private JsonService jsonService;
    private EngineConfigurationService engineConfigurationService;
    private MasterDataService masterDataService;
    private DeviceConfigurationService deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private TaskService taskService;
    private DeviceDataModelService deviceDataModelService;
    private ServerTopologyService topologyService;
    private MultiElementDeviceService multiElementDeviceService;
    private SchedulingService schedulingService;
    private InMemoryBootstrapModule bootstrapModule;
    private PropertySpecService propertySpecService;
    private LicenseService licenseService;
    private LicensedProtocolService licensedProtocolService;
    private ValidationService validationService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;
    private ConnectionTypeService connectionTypeService;
    private DataVaultService dataVaultService;
    private IssueService issueService;
    private Thesaurus thesaurus;
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private RegisteredDevicesKpiService registeredDevicesKpiService;
    private MeteringGroupsService meteringGroupsService;
    private SearchService searchService;
    private MessageService messageService;
    private MeteringZoneService meteringZoneService;

    public InMemoryIntegrationPersistence(Clock clock) {
        super();
        this.clock = clock;
    }

    public void initializeDatabase(String testName, boolean showSqlLogging) throws SQLException {
        this.initializeMocks(testName);
        bootstrapModule = new InMemoryBootstrapModule();
        licensedProtocolService = mock(LicensedProtocolService.class);
        License license = mock(License.class);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(license));
        when(licensedProtocolService.isValidJavaClassName(anyString(), eq(license))).thenReturn(true);
        connectionTypeService = mock(ConnectionTypeService.class);
        when(connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(license.getLicensedValues()).thenReturn(properties);
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new UtilModule(clock),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new PkiModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new BpmModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(
                         "0.0.0.0.0.41.92.0.0.0.0.0.0.0.0.0.114.0"
                        ,"0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"
                        ,"0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0"
                        ,"0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"
                        ,"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.2.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.2.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.0.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"
                        ,"0.0.2.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0"
                        ,"0.0.2.1.19.2.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.2.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0"
                        ,"0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.111.0"
                        ,"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.111.0"
                        ,"0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.167.0"
                        ,"0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0"
                        ,"0.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0"
                        ,"0.0.0.12.0.3.109.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.41.109.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.41.118.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.41.139.0.0.0.0.0.0.0.0.0.109.0"
                        ,"0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.23.0"
                        ,"0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.279.0"
                        ,"0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.6.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.107.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.125.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.126.0"
                        ,"0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.39.0"
                        ,"0.2.0.6.0.9.58.0.0.0.0.0.0.0.0.0.125.0"
                ),
                new MeteringGroupsModule(),
                new SearchModule(),
                new InMemoryMessagingModule(),
                new KpiModule(),
                new H2OrmModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new TaskModule(),
                new TasksModule(),
                new DeviceDataModule(),
                new SchedulingModule(),
                new TopologyModule(),
                new CalendarModule(),
                new MultiElementDeviceModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new FileImportModule(),
                new MeteringZoneModule(),
                new TokenModule(),
                new BlackListModule()
        );
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.jsonService = injector.getInstance(JsonService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(AuditService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
            this.ormService = injector.getInstance(OrmService.class);
            this.transactionService = injector.getInstance(TransactionService.class);
            this.publisher = injector.getInstance(Publisher.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            injector.getInstance(MeteringGroupsService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.messageService = injector.getInstance(MessageService.class);
            injector.getInstance(KpiService.class);
            this.taskService = injector.getInstance(TaskService.class);
            this.validationService = injector.getInstance(ValidationService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            this.protocolPluggableService = (ProtocolPluggableServiceImpl) injector.getInstance(ProtocolPluggableService.class);
            this.protocolPluggableService.addLicensedProtocolService(this.licensedProtocolService);
            this.protocolPluggableService.addConnectionTypeService(this.connectionTypeService);
            this.schedulingService = injector.getInstance(SchedulingService.class);
            this.deviceDataModelService = injector.getInstance(DeviceDataModelService.class);
            this.topologyService = injector.getInstance(ServerTopologyService.class);
            this.multiElementDeviceService = injector.getInstance(MultiElementDeviceService.class);
            this.deviceMessageSpecificationService = injector.getInstance(DeviceMessageSpecificationService.class);
            this.propertySpecService = injector.getInstance(PropertySpecService.class);
            this.userService = injector.getInstance(UserService.class);
            this.threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            this.issueService = injector.getInstance(IssueService.class);
            this.deviceLifeCycleConfigurationService = injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.registeredDevicesKpiService = injector.getInstance(RegisteredDevicesKpiService.class);
            this.meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            this.meteringZoneService = injector.getInstance(MeteringZoneService.class);
            this.searchService = injector.getInstance(SearchService.class);
            this.dataModel = this.deviceDataModelService.dataModel();
            ctx.commit();
        }
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
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>empty());
        this.dataVaultService = mock(DataVaultService.class);
        this.thesaurus = mock(Thesaurus.class);
    }

    public void cleanUpDataBase() throws SQLException {
        bootstrapModule.deactivate();
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    public EngineConfigurationService getEngineConfigurationService() {
        return engineConfigurationService;
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

    public ServerConnectionTaskService getConnectionTaskService() {
        return this.deviceDataModelService.connectionTaskService();
    }

    public ServerCommunicationTaskService getCommunicationTaskService() {
        return this.deviceDataModelService.communicationTaskService();
    }

    public ServerDeviceService getDeviceService() {
        return this.deviceDataModelService.deviceService();
    }

    public ServerTopologyService getTopologyService() {
        return this.topologyService;
    }

    public MultiElementDeviceService getMultiElementDeviceService(){
        return this.multiElementDeviceService;
    }

    public SchedulingService getSchedulingService() {
        return schedulingService;
    }

    public DataModel getDeviceDataModel() {
        return this.deviceDataModelService.dataModel();
    }

    public DataModel getTopologyDataModel() {
        return this.topologyService.dataModel();
    }

    public DataModel getMultiEditServiceDataModel() {
        return topologyService.dataModel();
    }

    public Thesaurus getThesaurus() {
        return this.deviceDataModelService.thesaurus();
    }

    public Clock getClock() {
        return clock;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public EventService getEventService() {
        return eventService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return deviceLifeCycleConfigurationService;
    }

    public RegisteredDevicesKpiService getRegisteredDevicesKpiService() {
        return registeredDevicesKpiService;
    }

    public int update(SqlBuilder sqlBuilder) throws SQLException {
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.getStatement(connection)) {
            return statement.executeUpdate();
        }
    }

    public String update(String sql) {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int numberOfRows = statement.executeUpdate();
                return "Updated " + numberOfRows + " row(s).";
            } catch (SQLException e) {
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                return stringWriter.toString();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    public IssueService getIssueService() {
        return this.issueService;
    }

    public MeteringGroupsService getMeteringGroupsService() {
        return meteringGroupsService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(licenseService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(AppService.class).toInstance(mock(AppService.class));
        }
    }

    public User getMockedUser(){
        return this.principal;
    }

    public UserService getUserService() {
        return userService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public DeviceMessageSpecificationService getDeviceMessageSpecificationService() {
        return deviceMessageSpecificationService;
    }

}
