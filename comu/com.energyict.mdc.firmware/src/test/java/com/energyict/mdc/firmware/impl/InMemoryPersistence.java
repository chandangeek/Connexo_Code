package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.QueryService;
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
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 3/12/15
 * Time: 4:47 PM
 */
public class InMemoryPersistence {

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private DataModel dataModel;
    private Injector injector;
    private InMemoryBootstrapModule bootstrapModule;
    private FirmwareServiceImpl firmwareService;
    private LicenseService licenseService;

    public void initializeDatabase(String testName, boolean showSqlLogging, boolean createDefaults) {
        this.initializeMocks(testName);
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new IdsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new OrmModule(),
                new DataVaultModule(),
                new InMemoryMessagingModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(false),
                new DeviceLifeCycleConfigurationModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new MeteringGroupsModule(),
                new TaskModule(),
                new DeviceConfigurationModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new EngineModelModule(),
                new MdcDynamicModule(),
                new IssuesModule(),
                new ProtocolApiModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new SchedulingModule(),
                new TasksModule(),
                new MasterDataModule(),
                new DeviceDataModule(),
                new FirmwareModule(),
                new KpiModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(UserService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(QueryService.class);
            injector.getInstance(DeviceConfigurationService.class);
            injector.getInstance(DeviceMessageSpecificationService.class);
            injector.getInstance(DeviceDataModelServiceImpl.class);
            injector.getInstance(DeviceService.class);
            injector.getInstance(EventService.class);
            injector.getInstance(ProtocolPluggableService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.firmwareService = spy((FirmwareServiceImpl) injector.getInstance(FirmwareService.class));
            this.dataModel = firmwareService.getDataModel();
            ctx.commit();
        }
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class);
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        when(this.principal.getName()).thenReturn(testName);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public FirmwareServiceImpl getFirmwareService() {
        return firmwareService;
    }

    public Injector getInjector() {
        return injector;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(BpmService.class).toInstance(mock(BpmService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(DataModel.class).toProvider(() -> dataModel);
        }
    }

}