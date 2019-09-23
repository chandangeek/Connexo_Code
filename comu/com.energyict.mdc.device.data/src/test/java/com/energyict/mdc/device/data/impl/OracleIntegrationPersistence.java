/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.oracle.impl.OracleBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.impl.ZoneModule;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
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
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
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
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
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
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

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
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Sets up an oracle database for integration testing purposes.
 * Requires an empty schema RVK_EMPTY which you can create as follows:
 * <code>create user RVK_EMPTY identified by zorro;</code><br>
 * <code>grant dba to RVK_EMPTY;</code>
 *
 * @since Feb 12, 2015 (10:54)
 */
public class OracleIntegrationPersistence {

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private Thesaurus thesaurus;
    private TransactionService transactionService;
    private EventService eventService;
    private static final Clock clock = mock(Clock.class);
    private JsonService jsonService;
    private EngineConfigurationService engineConfigurationService;
    private MasterDataService masterDataService;
    private DeviceConfigurationService deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private ProtocolPluggableService protocolPluggableService;
    private TaskService taskService;
    private DeviceDataModelService deviceDataModelService;
    private SchedulingService schedulingService;
    private OracleBootstrapModule bootstrapModule;
    private PropertySpecService propertySpecService;
    private LicenseService licenseService;
    private UserService userService;
    private MeteringGroupsService meteringGroupsService;
    private DeviceSearchDomain deviceSearchDomain;
    private MeteringZoneService meteringZoneService;

    public void initializeDatabase(String testName) throws SQLException {
        this.initializeMocks(testName);
        bootstrapModule = new OracleBootstrapModule();
        LicensedProtocolService licensedProtocolService = mock(LicensedProtocolService.class);
        License license = mock(License.class);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(license));
        when(licensedProtocolService.isValidJavaClassName(anyString(), eq(license))).thenReturn(true);
        ConnectionTypeService connectionTypeService = mock(ConnectionTypeService.class);
        when(connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(InboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new InboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(ModemNoParamsConnectionTypeImpl.class.getName())).thenReturn(new ModemNoParamsConnectionTypeImpl());
        InboundDeviceProtocolService inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        when(inboundDeviceProtocolService.createInboundDeviceProtocolFor(SimpleDiscoveryProtocol.class.getName())).thenReturn(new SimpleDiscoveryProtocol());
        when(inboundDeviceProtocolService.createInboundDeviceProtocolFor(any(PluggableClass.class))).thenReturn(new SimpleDiscoveryProtocol());
        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        when(deviceProtocolService.createProtocol(DeviceMessageImplTest.MessageTestDeviceProtocol.class.getName())).thenReturn(new DeviceMessageImplTest.MessageTestDeviceProtocol());
        when(deviceProtocolService.createProtocol(TestProtocol.class.getName())).thenReturn(new TestProtocol(propertySpecService));
        when(deviceProtocolService.createProtocol(CommandlyTestProtocol.class.getName())).thenReturn(new CommandlyTestProtocol());
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(license.getLicensedValues()).thenReturn(properties);
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new TimeModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(true),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new CalendarModule(),
                new MeteringModule(),
                new MeteringGroupsModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new SearchModule(),
                new InMemoryMessagingModule(),
                new H2OrmModule(),
                new DataVaultModule(),
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
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new TaskModule(),
                new TasksModule(),
                new DeviceDataModule(),
                new SchedulingModule(),
                new ZoneModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.jsonService = injector.getInstance(JsonService.class);
            injector.getInstance(OrmService.class);
            this.transactionService = injector.getInstance(TransactionService.class);
            this.eventService = injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.meteringZoneService =  injector.getInstance(MeteringZoneService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            initializeCustomPropertySets(injector);
            injector.getInstance(MdcReadingTypeUtilService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.taskService = injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            this.protocolPluggableService.addConnectionTypeService(connectionTypeService);
            this.protocolPluggableService.addInboundDeviceProtocolService(inboundDeviceProtocolService);
            this.protocolPluggableService.addDeviceProtocolService(deviceProtocolService);
            this.schedulingService = injector.getInstance(SchedulingService.class);
            this.deviceDataModelService = injector.getInstance(DeviceDataModelService.class);
            injector.getInstance(DeviceMessageSpecificationService.class);
            this.propertySpecService = injector.getInstance(PropertySpecService.class);
            when(connectionTypeService.createConnectionType(OutboundIpConnectionTypeImpl.class.getName())).thenReturn(new OutboundIpConnectionTypeImpl(propertySpecService));
            when(connectionTypeService.createConnectionType(InboundIpConnectionTypeImpl.class.getName())).thenReturn(new InboundIpConnectionTypeImpl(propertySpecService));
            when(connectionTypeService.createConnectionType(ModemConnectionType.class.getName())).thenReturn(new ModemConnectionType(propertySpecService));
            this.userService = injector.getInstance(UserService.class);
            injector.getInstance(ThreadPrincipalService.class);
            this.dataModel = this.deviceDataModelService.dataModel();
            this.deviceSearchDomain = injector.getInstance(DeviceSearchDomain.class);
            injector.getInstance(SearchService.class).register(deviceSearchDomain);
            ctx.commit();
        }
    }

    private void initializeCustomPropertySets(Injector injector) {
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcurl")).thenReturn("jdbc:oracle:thin:@localhost:1521/XE");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcuser")).thenReturn("RVK_EMPTY");
        when(bundleContext.getProperty("com.elster.jupiter.datasource.jdbcpassword")).thenReturn("zorro");
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class, withSettings().extraInterfaces(User.class));
        when(this.principal.getName()).thenReturn(testName);
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>empty());
        this.thesaurus = mock(Thesaurus.class);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in " + OracleIntegrationPersistence.class.getSimpleName());
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
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

    public ServerDeviceService getDeviceService() {
        return this.deviceDataModelService.deviceService();
    }

    public SchedulingService getSchedulingService() {
        return schedulingService;
    }

    public DataModel getDataModel() {
        return this.deviceDataModelService.dataModel();
    }

    public Thesaurus getThesaurus() {
        return this.deviceDataModelService.thesaurus();
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

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(JsonService.class).toInstance(new JsonServiceImpl());
            bind(BeanService.class).toInstance(new BeanServiceImpl());
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(licenseService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(CronExpressionParser.class).toInstance(mock(CronExpressionParser.class, RETURNS_DEEP_STUBS));
            bind(IssueService.class).toInstance(mock(IssueService.class));
            bind(DataModel.class).toProvider(() -> dataModel);

            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public DeviceSearchDomain getDeviceSearchDomain() {
        return deviceSearchDomain;
    }
}