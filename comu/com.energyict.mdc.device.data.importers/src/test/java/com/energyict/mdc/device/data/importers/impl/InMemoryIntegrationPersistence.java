/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
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
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.impl.MeteringZoneModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
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
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCycleModule;
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
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.sql.SQLException;
import java.time.Clock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryIntegrationPersistence {

    private static final Clock clock = mock(Clock.class);

    private TransactionService transactionService;
    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    private DataModel dataModel = mock(DataModel.class);

    public InMemoryIntegrationPersistence() {
        super();
    }

    public void initializeDatabase(boolean showSqlLogging) throws SQLException {
        this.bootstrapModule = new InMemoryBootstrapModule();

        this.injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new InMemoryMessagingModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new IdsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0", "11.0.0.9.1.2.12.0.0.0.0.1.0.0.0.0.72.0"),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new H2OrmModule(),
                new DataVaultModule(),
                new TaskModule(),
                new UtilModule(clock),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new UserModule(),
                new BpmModule(),
                new FileImportModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new AppServiceModule(),
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
                new DeviceLifeCycleModule(),
                new DeviceConfigurationModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new DeviceDataModule(),
                new TopologyModule(),
                new CalendarModule(),
                new PkiModule(),
                new MeteringZoneModule(),
                new TokenModule(),
                new BlackListModule()
        );
        when(dataModel.getInstance(any())).thenAnswer(invocationOnMock -> injector.getInstance(invocationOnMock.getArgumentAt(0, Class.class)));
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(AuditService.class);
            injector.getInstance(CustomPropertySetService.class);
            initializeCustomPropertySets();
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(DeviceDataImporterContext.class);
            injector.getInstance(MeteringZoneService.class);
            ctx.commit();
        }
    }

    public Clock getClock() {
        return clock;
    }

    private void initializeCustomPropertySets() {
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return transactionService;
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
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));

            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));

            Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(DataModel.class).toInstance(dataModel);
        }
    }
}
