/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
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
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.ExecutionTimerService;
import com.elster.jupiter.util.time.impl.ExecutionTimerServiceImpl;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.events.DeviceLifeCycleChangeEventHandler;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Properties;

import org.mockito.Matchers;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-11 (13:32)
 */
public class InMemoryIntegrationPersistence {

    private final Clock clock = mock(Clock.class);
    private BundleContext bundleContext;
    private User principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private InMemoryBootstrapModule bootstrapModule;
    private PropertySpecService propertySpecService;
    private LicenseService licenseService;
    private Injector injector;
    private IssueService issueService;
    private DeviceLifeCycleChangeEventHandler deviceLifeCycleChangeEventHandler;

    public InMemoryIntegrationPersistence() {
        super();
    }

    public void initializeDatabase(String testName, boolean showSqlLogging) throws SQLException {
        this.initializeMocks(testName);
        this.bootstrapModule = new InMemoryBootstrapModule();
        LicensedProtocolService licensedProtocolService = mock(LicensedProtocolService.class);
        License license = mock(License.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(license));
        when(licensedProtocolService.isValidJavaClassName(anyString(), eq(license))).thenReturn(true);
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(license.getLicensedValues()).thenReturn(properties);
        when(this.clock.instant()).thenReturn(Instant.now());
        when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
        this.injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new FileImportModule(),
                new WebServicesModule(),
                new AppServiceModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new OrmModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new InMemoryMessagingModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
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
                new SchedulingModule(),
                new TimeModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new ProtocolApiModule(),
                new TaskModule(),
                new TasksModule(),
                new TopologyModule(),
                new DeviceDataModule(),
                new DeviceLifeCycleModule(),
                new CustomPropertySetsModule(),
                new CalendarModule());
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.transactionService = this.injector.getInstance(TransactionService.class);
            this.injector.getInstance(FiniteStateMachineService.class);
            ProtocolPluggableServiceImpl protocolPluggableService = (ProtocolPluggableServiceImpl) this.injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            this.injector.getInstance(SchedulingService.class);
            this.injector.getInstance(DeviceMessageSpecificationService.class);
            this.propertySpecService = this.injector.getInstance(PropertySpecService.class);
            this.injector.getInstance(UserService.class);
            this.injector.getInstance(ThreadPrincipalService.class);
            this.injector.getInstance(ServiceCallService.class);
            this.injector.getInstance(CustomPropertySetService.class);
            initializeCustomPropertySets();
            StateTransitionTriggerEventTopicHandler stateTransitionTriggerEventTopicHandler = new StateTransitionTriggerEventTopicHandler(this.injector
                    .getInstance(EventService.class));
            ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(stateTransitionTriggerEventTopicHandler);
            com.elster.jupiter.metering.impl.StateTransitionChangeEventTopicHandler meteringTopicHandler =
                    new com.elster.jupiter.metering.impl.StateTransitionChangeEventTopicHandler(Clock.systemDefaultZone(),
                            this.injector.getInstance(FiniteStateMachineService.class),
                            this.injector.getInstance(MeteringService.class));
            ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(meteringTopicHandler);
            StateTransitionChangeEventTopicHandler stateTransitionChangeEventTopicHandler =
                    new StateTransitionChangeEventTopicHandler(
                            this.injector.getInstance(FiniteStateMachineService.class),
                            this.injector.getInstance(MeteringService.class),
                            this.clock);
            ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(stateTransitionChangeEventTopicHandler);
            DeviceLifeCycleChangeEventHandler deviceLifeCycleChangeEventHandler = getDeviceLifeCycleChangeEventHandler();
            ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(deviceLifeCycleChangeEventHandler);
            ctx.commit();
        }
    }

    private void initializeCustomPropertySets() {
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
    }

    public DeviceLifeCycleChangeEventHandler getDeviceLifeCycleChangeEventHandler() {
        if (deviceLifeCycleChangeEventHandler == null) {
            deviceLifeCycleChangeEventHandler = new DeviceLifeCycleChangeEventHandler(
                    this.injector.getInstance(DeviceConfigurationService.class),
                    this.injector.getInstance(DeviceDataModelService.class),
                    this.injector.getInstance(MeteringService.class));
        }
        return deviceLifeCycleChangeEventHandler;
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        when(this.bundleContext.getProperty(anyString())).thenReturn(null);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(User.class);
        GrantPrivilege superGrant = mock(GrantPrivilege.class);
        when(superGrant.canGrant(any())).thenReturn(true);
        Group superUser = mock(Group.class);
        when(superUser.getPrivileges()).thenReturn(ImmutableMap.of("", asList(superGrant)));
        when(this.principal.getGroups()).thenReturn(asList(superUser));
        when(this.principal.getName()).thenReturn(testName);
        when(this.principal.hasPrivilege(Matchers.matches("MDC"), any(Privilege.class))).thenReturn(true);
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>empty());
        this.issueService = mock(IssueService.class);
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public Clock getClock() {
        return clock;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public OrmService getOrmService() {
        return this.injector.getInstance(OrmService.class);
    }

    public DeviceConfigurationService getDeviceConfigurationService() {
        return this.injector.getInstance(DeviceConfigurationService.class);
    }

    public DeviceService getDeviceService() {
        return this.injector.getInstance(DeviceService.class);
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return this.injector.getInstance(FiniteStateMachineService.class);
    }

    public DeviceLifeCycleService getDeviceLifeCycleService() {
        return this.injector.getInstance(DeviceLifeCycleService.class);
    }

    public DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return this.injector.getInstance(DeviceLifeCycleConfigurationService.class);
    }

    public ProtocolPluggableService getProtocolPluggableService() {
        return this.injector.getInstance(ProtocolPluggableService.class);
    }

    public AppService getAppService() {
        return this.injector.getInstance(AppService.class);
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    public IssueService getIssueService() {
        return issueService;
    }

    public Injector getInjector() {
        return injector;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(JsonService.class).toInstance(new JsonServiceImpl());
            bind(BeanService.class).toInstance(new BeanServiceImpl());
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LicenseService.class).toInstance(licenseService);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(ExecutionTimerService.class).to(ExecutionTimerServiceImpl.class);
            bind(CronExpressionParser.class).toInstance(new DefaultCronExpressionParser());
            bind(IssueService.class).toInstance(issueService);
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(Thesaurus.class).toInstance(mock(Thesaurus.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
        }
    }

}