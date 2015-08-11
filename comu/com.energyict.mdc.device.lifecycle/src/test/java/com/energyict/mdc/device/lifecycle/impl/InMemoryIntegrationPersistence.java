package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
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
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import org.mockito.Matchers;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

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
        this.injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new FileImportModule(),
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
                new InMemoryMessagingModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(),
                new MeteringGroupsModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new ValidationModule(),
                new EstimationModule(),
                new SchedulingModule(),
                new TimeModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new ProtocolApiModule(),
                new TaskModule(),
                new KpiModule(),
                new TasksModule(),
                new TopologyModule(),
                new DeviceDataModule(),
                new DeviceLifeCycleModule());
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
            StateTransitionTriggerEventTopicHandler stateTransitionTriggerEventTopicHandler = new StateTransitionTriggerEventTopicHandler(this.injector.getInstance(EventService.class));
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
            DeviceLifeCycleChangeEventHandler deviceLifeCycleChangeEventHandler = new DeviceLifeCycleChangeEventHandler(
                    this.injector.getInstance(DeviceConfigurationService.class),
                    this.injector.getInstance(DeviceDataModelService.class),
                    this.injector.getInstance(MeteringService.class));
            ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(deviceLifeCycleChangeEventHandler);
            ctx.commit();
        }
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        when(this.bundleContext.getProperty(anyString())).thenReturn(null);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(User.class);
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

    public DeviceConfigurationService getDeviceConfigurationService(){
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
            bind(CronExpressionParser.class).toInstance(mock(CronExpressionParser.class, RETURNS_DEEP_STUBS));
            bind(IssueService.class).toInstance(issueService);
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(Thesaurus.class).toInstance(mock(Thesaurus.class));
        }
    }

}