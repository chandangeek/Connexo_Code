/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.impl.BpmModule;
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
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.impl.MeteringZoneModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
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
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.events.DeviceLifeCycleChangeEventHandler;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCycleModule;
import com.energyict.mdc.device.lifecycle.impl.StateTransitionChangeEventTopicHandler;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.drools.compiler.builder.impl.KnowledgeBuilderFactoryServiceImpl;
import org.drools.core.impl.KnowledgeBaseFactoryServiceImpl;
import org.drools.core.io.impl.ResourceFactoryServiceImpl;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryIntegrationPersistence {
    public static final String READING_TYPE_MRID = "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final Clock clock = mock(Clock.class);

    private TransactionService transactionService;
    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    private DeviceConfigurationService deviceConfigurationService;
    private DeviceLifeCycleService deviceLifeCycleService;
    private FiniteStateMachineService finiteStateMachineService;
    private ThreadPrincipalService threadPrincipalService;
    private LicenseService licenseService;
    private DeviceLifeCycleChangeEventHandler deviceLifeCycleChangeEventHandler;

    public InMemoryIntegrationPersistence() {
        super();
    }

    public InMemoryIntegrationPersistence(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public void initializeDatabase(boolean showSqlLogging) throws SQLException {
        this.bootstrapModule = new InMemoryBootstrapModule();
        LicensedProtocolService licensedProtocolService = mock(LicensedProtocolService.class);
        License license = mock(License.class);
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(license));
        when(licensedProtocolService.isValidJavaClassName(anyString(), eq(license))).thenReturn(true);
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(license.getLicensedValues()).thenReturn(properties);
        this.injector = Guice.createInjector(this.getModules(showSqlLogging));
        DataModel dataModel = injector.getInstance(DataModel.class);
        when(dataModel.getInstance(any())).thenAnswer(invocationOnMock -> injector.getInstance(invocationOnMock.getArgumentAt(0, Class.class)));
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            ProtocolPluggableServiceImpl protocolPluggableService = (ProtocolPluggableServiceImpl) this.injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            injector.getInstance(CustomPropertySetService.class);
            this.transactionService = this.injector.getInstance(TransactionService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(AuditService.class);
            injector.getInstance(MeteringZoneService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
            if(deviceConfigurationService == null) {
                StateTransitionTriggerEventTopicHandler stateTransitionTriggerEventTopicHandler = new StateTransitionTriggerEventTopicHandler(
                        this.injector.getInstance(EventService.class),
                        this.injector.getInstance(BpmService.class),
                        this.injector.getInstance(StateTransitionPropertiesProvider.class),
                        this.injector.getInstance(FiniteStateMachineService.class));
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(stateTransitionTriggerEventTopicHandler);
                com.elster.jupiter.metering.impl.StateTransitionChangeEventTopicHandler meteringTopicHandler =
                        new com.elster.jupiter.metering.impl.StateTransitionChangeEventTopicHandler(Clock.systemDefaultZone(),
                                this.injector.getInstance(FiniteStateMachineService.class),
                                this.injector.getInstance(MeteringService.class),
                                this.injector.getInstance(ThreadPrincipalService.class),
                                this.injector.getInstance(NlsService.class),
                                this.injector.getInstance(UserService.class));
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(meteringTopicHandler);
                StateTransitionChangeEventTopicHandler stateTransitionChangeEventTopicHandler =
                        new StateTransitionChangeEventTopicHandler(
                                this.injector.getInstance(FiniteStateMachineService.class),
                                this.injector.getInstance(MeteringService.class),
                                clock);
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(stateTransitionChangeEventTopicHandler);
                DeviceLifeCycleChangeEventHandler deviceLifeCycleChangeEventHandler = getDeviceLifeCycleChangeEventHandler();
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(deviceLifeCycleChangeEventHandler);

                this.deviceLifeCycleService = this.injector.getInstance(DeviceLifeCycleService.class);
                this.finiteStateMachineService = this.injector.getInstance(FiniteStateMachineService.class);
                this.threadPrincipalService = this.injector.getInstance(ThreadPrincipalService.class);
            }

            ctx.commit();
        }
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

    private Module[] getModules(boolean showSqlLogging) {
        List<Module> modules = new ArrayList<>();
        Collections.addAll(modules,
                new MockModule(),
                bootstrapModule,
                new H2OrmModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new DataVaultModule(),
                new InMemoryMessagingModule(),
                new IdsModule(),
                new PkiModule(),
                new TopologyModule(),
                new DeviceLifeCycleModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(READING_TYPE_MRID),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new DataVaultModule(),
                new TaskModule(),
                new UtilModule(clock),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new UserModule(),
                new BpmModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new IssuesModule(),
                new MdcReadingTypeUtilServiceModule(),
                new FiniteStateMachineModule(),
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
                new ProtocolApiModule(),
                new TasksModule(),
                new DeviceDataModule(),
                new IssueModule(),
                new IssueDataValidationModule(),
                new CalendarModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new FileImportModule(),
                new MeteringZoneModule(),
                new TokenModule(),
                new BlackListModule(),
                new AppServiceModule()
        );
        if (this.deviceConfigurationService == null) {
            modules.add(new DeviceConfigurationModule());
        }
        return modules.toArray(new Module[modules.size()]);
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

    public FiniteStateMachineService getFiniteStateMachineService() {
        return finiteStateMachineService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public DeviceLifeCycleService getDeviceLifeCycleService() {
        return deviceLifeCycleService;
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Clock.class).toInstance(clock);
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            bind(KieResources.class).to(ResourceFactoryServiceImpl.class);
            bind(KnowledgeBaseFactoryService.class).to(KnowledgeBaseFactoryServiceImpl.class);
            bind(KnowledgeBuilderFactoryService.class).to(KnowledgeBuilderFactoryServiceImpl.class);

            Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

            bind(StateTransitionPropertiesProvider.class).toInstance(mock(StateTransitionPropertiesProvider.class));
            bind(HttpAuthenticationService.class).toInstance(mock(HttpAuthenticationService.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(LicenseService.class).toInstance(licenseService);
            if (deviceConfigurationService != null) {
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(LockService.class).toInstance(mock(LockService.class));
            }
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());

            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(PropertyValueInfoService.class).toInstance(mock(PropertyValueInfoService.class));

            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(DataModel.class).toInstance(mock(DataModel.class));
        }
    }
}
