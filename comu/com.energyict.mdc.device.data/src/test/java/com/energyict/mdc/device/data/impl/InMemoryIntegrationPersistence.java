package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
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
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleModule;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.ExecutionTimerService;
import com.elster.jupiter.util.time.impl.ExecutionTimerServiceImpl;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingHandler;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.ami.MultiSenseHeadEndInterfaceImpl;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.events.TestProtocolWithRequiredStringAndOptionalNumericDialectProperties;
import com.energyict.mdc.device.data.impl.search.DeviceSearchDomain;
import com.energyict.mdc.device.data.impl.tasks.InboundIpConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.ModemConnectionType;
import com.energyict.mdc.device.data.impl.tasks.ModemNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.OutboundIpConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.OutboundNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.impl.tasks.SimpleDiscoveryProtocol;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
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
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
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
import org.osgi.service.log.LogService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:52
 */
public class InMemoryIntegrationPersistence {

    private BundleContext bundleContext;
    private User principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private EventService eventService;
    private NlsService nlsService;
    private static final Clock clock = mock(Clock.class);
    private JsonService jsonService;
    private CustomPropertySetService customPropertySetService;
    private EngineConfigurationService engineConfigurationService;
    private MasterDataService masterDataService;
    private DeviceConfigurationService deviceConfigurationService;
    private MeteringService meteringService;
    private MetrologyConfigurationService metrologyConfigurationService;
    private DataModel dataModel;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private TaskService taskService;
    private DeviceDataModelService deviceDataModelService;
    private SchedulingService schedulingService;
    private InMemoryBootstrapModule bootstrapModule;
    private PropertySpecService propertySpecService;
    private LicenseService licenseService;
    private LicensedProtocolService licensedProtocolService;
    private ConnectionTypeService connectionTypeService;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private DeviceProtocolService deviceProtocolService;
    private ValidationService validationService;
    private EstimationService estimationService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;
    private MeteringGroupsService meteringGroupsService;
    private IssueService issueService;
    private Thesaurus thesaurus;
    private BatchService batchService;
    private DeviceSearchDomain deviceSearchDomain;
    private DataCollectionKpiService dataCollectionKpiService;
    private DataValidationKpiService dataValidationKpiService;
    private FiniteStateMachineService finiteStateMachineService;
    private CalendarService calendarService;
    private ServiceCallService serviceCallService;
    private DeviceMessageService deviceMessageService;
    private Injector injector;

    public InMemoryIntegrationPersistence() {
        super();
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
        when(connectionTypeService.createConnectionType(InboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new InboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(ModemNoParamsConnectionTypeImpl.class.getName())).thenReturn(new ModemNoParamsConnectionTypeImpl());
        inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        when(inboundDeviceProtocolService.createInboundDeviceProtocolFor(SimpleDiscoveryProtocol.class.getName())).thenReturn(new SimpleDiscoveryProtocol());
        when(inboundDeviceProtocolService.createInboundDeviceProtocolFor(any(PluggableClass.class))).thenReturn(new SimpleDiscoveryProtocol());
        deviceProtocolService = mock(DeviceProtocolService.class);
        when(deviceProtocolService.createProtocol(DeviceMessageImplTest.MessageTestDeviceProtocol.class.getName())).thenReturn(new DeviceMessageImplTest.MessageTestDeviceProtocol());
        when(deviceProtocolService.createProtocol(TestProtocol.class.getName())).thenReturn(new TestProtocol(propertySpecService));
        TestProtocolWithRequiredStringAndOptionalNumericDialectProperties testProtocolThatUseMocking = new TestProtocolWithRequiredStringAndOptionalNumericDialectProperties();
        when(deviceProtocolService.createProtocol(TestProtocolWithRequiredStringAndOptionalNumericDialectProperties.class.getName())).thenReturn(testProtocolThatUseMocking);
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(license.getLicensedValues()).thenReturn(properties);
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new UsagePointLifeCycleModule(),
                new MeteringModule(
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new MeteringGroupsModule(),
                new SearchModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new CustomPropertySetsModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new TaskModule(),
                new TasksModule(),
                new DeviceDataModule(),
                new SchedulingModule(),
                new CalendarModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.jsonService = injector.getInstance(JsonService.class);
            injector.getInstance(OrmService.class);
            this.transactionService = injector.getInstance(TransactionService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.metrologyConfigurationService = injector.getInstance(MetrologyConfigurationService.class);
            this.meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.taskService = injector.getInstance(TaskService.class);
            this.validationService = injector.getInstance(ValidationService.class);
            this.estimationService = injector.getInstance(EstimationService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            this.serviceCallService = injector.getInstance(ServiceCallService.class);
            this.customPropertySetService = injector.getInstance(CustomPropertySetService.class);
            initializeCustomPropertySets();
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.protocolPluggableService.addLicensedProtocolService(this.licensedProtocolService);
            this.protocolPluggableService.addConnectionTypeService(this.connectionTypeService);
            this.protocolPluggableService.addInboundDeviceProtocolService(this.inboundDeviceProtocolService);
            this.protocolPluggableService.addDeviceProtocolService(this.deviceProtocolService);
            this.schedulingService = injector.getInstance(SchedulingService.class);
            this.deviceDataModelService = injector.getInstance(DeviceDataModelService.class);
            this.deviceMessageSpecificationService = injector.getInstance(DeviceMessageSpecificationService.class);
            this.propertySpecService = injector.getInstance(PropertySpecService.class);
            when(connectionTypeService.createConnectionType(OutboundIpConnectionTypeImpl.class.getName())).thenReturn(new OutboundIpConnectionTypeImpl(propertySpecService));
            when(connectionTypeService.createConnectionType(InboundIpConnectionTypeImpl.class.getName())).thenReturn(new InboundIpConnectionTypeImpl(propertySpecService));
            when(connectionTypeService.createConnectionType(ModemConnectionType.class.getName())).thenReturn(new ModemConnectionType(propertySpecService));
            this.userService = injector.getInstance(UserService.class);
            this.threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            this.issueService = injector.getInstance(IssueService.class);
            this.dataModel = this.deviceDataModelService.dataModel();
            this.batchService = injector.getInstance(BatchService.class);
            this.deviceSearchDomain = injector.getInstance(DeviceSearchDomain.class);
            injector.getInstance(SearchService.class).register(deviceSearchDomain);
            this.meteringGroupsService.addQueryProvider(injector.getInstance(DeviceEndDeviceQueryProvider.class));
            this.dataCollectionKpiService = injector.getInstance(DataCollectionKpiService.class);
            this.dataValidationKpiService = injector.getInstance(DataValidationKpiService.class);
            this.finiteStateMachineService = injector.getInstance(FiniteStateMachineService.class);
            this.calendarService = injector.getInstance(CalendarService.class);
            this.deviceMessageService = injector.getInstance(DeviceMessageService.class);
            injector.getInstance(UsagePointLifeCycleService.class);
            initHeadEndInterface();
            initializePrivileges();
            ctx.commit();
        }
    }

    private void initHeadEndInterface() {
        injector.getInstance(MeteringDataModelService.class).addHeadEndInterface(injector.getInstance(MultiSenseHeadEndInterfaceImpl.class));
    }

    private void initializeCustomPropertySets() {
        customPropertySetService.addCustomPropertySet(new CommandCustomPropertySet());
        customPropertySetService.addCustomPropertySet(new CompletionOptionsCustomPropertySet());
        customPropertySetService.addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
    }

    private void initializePrivileges() {
        new com.energyict.mdc.device.config.impl.Installer(dataModel, eventService, userService).getModuleResources()
                .forEach(definition -> this.userService.saveResourceWithPrivileges(definition.getComponentName(), definition.getName(), definition.getDescription(), definition.getPrivilegeNames()
                        .stream().toArray(String[]::new)));
        new InstallerV10_2Impl(userService, meteringService, serviceCallService, customPropertySetService).getModuleResources()
                .forEach(definition -> this.userService.saveResourceWithPrivileges(definition.getComponentName(), definition.getName(), definition.getDescription(), definition.getPrivilegeNames()
                        .stream().toArray(String[]::new)));
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(User.class);
        when(this.principal.getName()).thenReturn(testName);
        when(this.principal.hasPrivilege(any(), anyString())).thenReturn(true);
        GrantPrivilege superGrant = mock(GrantPrivilege.class);
        when(superGrant.canGrant(any())).thenReturn(true);
        Group superUser = mock(Group.class);
        when(superUser.getPrivileges()).thenReturn(ImmutableMap.of("", asList(superGrant)));
        when(this.principal.getGroups()).thenReturn(asList(superUser));
        when(this.principal.getLocale()).thenReturn(Optional.empty());
        Privilege ePrivilege1 = mockPrivilege(EditPrivilege.LEVEL_1);
        Privilege vPrivilege1 = mockPrivilege(ViewPrivilege.LEVEL_1);
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(ePrivilege1);
        privileges.add(vPrivilege1);
        when(this.principal.getPrivileges()).thenReturn(privileges);
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>empty());
        this.thesaurus = mock(Thesaurus.class);
        this.issueService = mock(IssueService.class, RETURNS_DEEP_STUBS);
        when(this.issueService.findStatus(any())).thenReturn(Optional.<IssueStatus>empty());
    }

    private Privilege mockPrivilege(EditPrivilege privilege1) {
        Privilege privilege = mock(Privilege.class);
        when(privilege.getName()).thenReturn(privilege1.getPrivilege());
        return privilege;
    }

    private Privilege mockPrivilege(ViewPrivilege privilege1) {
        Privilege privilege = mock(Privilege.class);
        when(privilege.getName()).thenReturn(privilege1.getPrivilege());
        return privilege;
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

    public MetrologyConfigurationService getMetrologyConfigurationService() {
        return metrologyConfigurationService;
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

    public CustomPropertySetService getCustomPropertySetService() {
        return this.customPropertySetService;
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

    public ConnectionTaskReportService getConnectionTaskReportService() {
        return this.deviceDataModelService.connectionTaskReportService();
    }

    public ServerCommunicationTaskService getCommunicationTaskService() {
        return this.deviceDataModelService.communicationTaskService();
    }

    public CommunicationTaskReportService getCommunicationTaskReportService() {
        return this.deviceDataModelService.communicationTaskReportService();
    }

    public CalendarService getCalendarService() {
        return this.calendarService;
    }

    public DeviceDataModelService getDeviceDataModelService() {
        return deviceDataModelService;
    }

    public ServerDeviceService getDeviceService() {
        return this.deviceDataModelService.deviceService();
    }

    public SchedulingService getSchedulingService() {
        return schedulingService;
    }

    public DataModel getDataModel() {
        return this.deviceDataModelService.dataModel();
    }

    public Thesaurus getThesaurusFromDeviceDataModel() {
        return this.deviceDataModelService.thesaurus();
    }

    public Thesaurus getMockedThesaurus() {
        return thesaurus;
    }

    public NlsService getNlsService() {
        return nlsService;
    }

    public LicenseService getLicenseService() {
        return licenseService;
    }

    public ConnectionTypeService getConnectionTypeService() {
        return connectionTypeService;
    }

    public InboundDeviceProtocolService getInboundDeviceProtocolService() {
        return inboundDeviceProtocolService;
    }

    public DeviceProtocolService getDeviceProtocolService() {
        return deviceProtocolService;
    }

    public ValidationService getValidationService() {
        return validationService;
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return finiteStateMachineService;
    }

    public Clock getClock() {
        return clock;
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

    public MeteringGroupsService getMeteringGroupsService() {
        return meteringGroupsService;
    }

    public EstimationService getEstimationService() {
        return estimationService;
    }

    public CalendarService calendarService() {
        return calendarService;
    }

    public ServiceCallService getServiceCallService() {
        return serviceCallService;
    }

    public DeviceMessageService getDeviceMessageService() {
        return deviceMessageService;
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

    public TopicHandler getRegisterSpecUpdateHandler() {
        return injector.getInstance(RegisterSpecUpdateEventHandler.class);
    }

    public TopicHandler getChannelSpecUpdateHandler() {
        return injector.getInstance(ChannelSpecUpdateEventHandler.class);
    }

    public DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return injector.getInstance(DeviceLifeCycleConfigurationService.class);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ExecutionTimerService.class).to(ExecutionTimerServiceImpl.class);
            bind(JsonService.class).toInstance(new JsonServiceImpl());
            bind(BeanService.class).toInstance(new BeanServiceImpl());
            bind(Clock.class).toInstance(clock);
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(licenseService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(CronExpressionParser.class).toInstance(mock(CronExpressionParser.class, RETURNS_DEEP_STUBS));
            bind(IssueService.class).toInstance(issueService);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    public User getMockedUser() {
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

    public IssueService getIssueService() {
        return issueService;
    }

    public BatchService getBatchService() {
        return batchService;
    }

    public DeviceConfigConflictMappingHandler getDeviceConfigConflictMappingHandler() {
        return injector.getInstance(DeviceConfigConflictMappingHandler.class);
    }

    public SearchDomain getDeviceSearchDomain() {
        return deviceSearchDomain;
    }

    public DataCollectionKpiService getDataCollectionKpiService() {
        return dataCollectionKpiService;
    }

    public DataValidationKpiService getDataValidationKpiService() {
        return dataValidationKpiService;
    }

}
