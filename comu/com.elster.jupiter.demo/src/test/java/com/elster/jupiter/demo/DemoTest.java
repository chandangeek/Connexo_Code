package com.elster.jupiter.demo;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.datavault.impl.DataVaultProvider;
import com.elster.jupiter.demo.impl.DemoServiceImpl;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.impl.DataExportServiceImpl;
import com.elster.jupiter.export.impl.ExportModule;
import com.elster.jupiter.export.processor.impl.StandardCsvDataProcessorFactory;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
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
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.elster.jupiter.validation.impl.ValidationServiceImpl;
import com.elster.jupiter.validators.impl.DefaultValidatorFactory;
import com.energyict.mdc.app.impl.MdcAppInstaller;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationServiceImpl;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.DeviceServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.favorites.impl.FavoritesModule;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.io.impl.SerialIONoModemComponentServiceImpl;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionModule;
import com.energyict.mdc.issue.datacollection.impl.templates.AbstractTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDatacollectionRuleTemplate;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.protocolimpl.elster.a3.AlphaA3;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DemoTest {
    private static final Logger LOG = Logger.getLogger(DemoTest.class.getName());

    protected static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UserFileFactory.class).toInstance(mock(UserFileFactory.class));
            bind(CodeFactory.class).toInstance(mock(CodeFactory.class));
            bind(CollectedDataFactory.class).toInstance(mock(CollectedDataFactory.class));

            Thesaurus thesaurus = mock(Thesaurus.class);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

            LicenseService licenseService = mock(LicenseService.class);
            License license = mockLicense();
            when(licenseService.getLicenseForApplication("MDC")).thenReturn(Optional.of(license));
            bind(LicenseService.class).toInstance(licenseService);
            bind(SerialComponentService.class).to(SerialIONoModemComponentServiceImpl.class).in(Scopes.SINGLETON);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(KieResources.class).toInstance(mock(KieResources.class));
            bind(KnowledgeBaseFactoryService.class).toInstance(mock(KnowledgeBaseFactoryService.class, RETURNS_DEEP_STUBS));
            bind(KnowledgeBuilderFactoryService.class).toInstance(mock(KnowledgeBuilderFactoryService.class, RETURNS_DEEP_STUBS));
            bind(FileImportService.class).toInstance(mock(FileImportService.class));
        }

        private License mockLicense() {
            License license = mock(License.class);
            Properties properties = new Properties();
            properties.setProperty("protocols", "all");
            when(license.getApplicationKey()).thenReturn("MDC");
            when(license.getDescription()).thenReturn("MDC application license example");
            when(license.getStatus()).thenReturn(License.Status.ACTIVE);
            when(license.getType()).thenReturn(License.Type.EVALUATION);
            when(license.getGracePeriodInDays()).thenReturn(5);
            when(license.getExpiration()).thenReturn(Instant.parse("9999-12-31T24:00:00Z"));
            when(license.getLicensedValues()).thenReturn(properties);
            return license;
        }
    }

    @Before
    public void setEnvironment(){
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(),
                new DataVaultModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new MeteringGroupsModule(),
                new KpiModule(),
                new TaskModule(),
                new com.elster.jupiter.issue.impl.module.IssueModule(),
                new AppServiceModule(),
                new TimeModule(),
                new ExportModule(),
                new MeteringModule(true),

                new MdcIOModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new EngineModelModule(),
                new EngineModule(),
                new ProtocolsModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new ValidationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new TasksModule(),
                new IssuesModule(),
                new SchedulingModule(),
                new ProtocolApiModule(),
                new IssueDataCollectionModule(),
                new TopologyModule(),
                new FavoritesModule(),

                new DemoModule()
        );
        doPreparations();
    }

    @After
    public void deactivateEnvironment(){
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testDemoSetup() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try{
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
        } catch (Exception e) {
            fail("The demo command shouldn't produce errors");
        }
    }

    @Test
    public void testA3Device() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try{
            demoService.createA3Device();
        } catch (Exception e) {
            fail("The demo command shouldn't produce errors");
        }
    }

    @Test
    public void testReExecute() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try{
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
        } catch (Exception e) {
            fail("The demo command shouldn't produce errors");
        }
    }

    @Test
    public void testStartDate() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try{
            demoService.createDemoData("DemoServ", "host", "2020-12-01");
        } catch (UnableToCreate e) {
            assertThat(e.getMessage()).contains("Incorrect start date parameter");
        }
    }

    protected void doPreparations() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            createOracleTablesSubstitutes();
            createRequiredProtocols();
            createDefaultStuff();
            injector.getInstance(DemoServiceImpl.class);
            ctx.commit();
        }
        tuneDeviceCountForSpeedTest();
    }

    private void createOracleTablesSubstitutes() {
        OrmServiceImpl ormService = (OrmServiceImpl) injector.getInstance(OrmService.class);
        try (Connection connection = ormService.getConnection(true)){
            SqlBuilder sqlBuilder = new SqlBuilder("CREATE VIEW USER_TABLES AS (select * from INFORMATION_SCHEMA.TABLES)");
            sqlBuilder.prepare(connection).execute();
            sqlBuilder = new SqlBuilder("CREATE VIEW USER_SEQUENCES AS (select * from INFORMATION_SCHEMA.SEQUENCES)");
            sqlBuilder.prepare(connection).execute();
            sqlBuilder = new SqlBuilder("CREATE VIEW USER_IND_COLUMNS AS (select INDEX_NAME, TABLE_NAME, COLUMN_NAME, '1' COLUMN_POSITION from INFORMATION_SCHEMA.INDEXES AS ind)");
            sqlBuilder.prepare(connection).execute();
        } catch (SQLException e) {
            LOG.severe("Erros during creating substitutes for ORACLE tables. It may cause unpredictable work.");
        }
    }

    private void createRequiredProtocols() {
        fixMissedDynamicReference();
        ProtocolPluggableService protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
        protocolPluggableService.newInboundDeviceProtocolPluggableClass("DlmsSerialNumberDiscover", DlmsSerialNumberDiscover.class.getName()).save();
        protocolPluggableService.newDeviceProtocolPluggableClass("WebRTUKP", WebRTUKP.class.getName()).save();
        protocolPluggableService.newDeviceProtocolPluggableClass("ALPHA_A3", AlphaA3.class.getName()).save();
        protocolPluggableService.newConnectionTypePluggableClass("OutboundTcpIp", OutboundTcpIpConnectionType.class.getName());
    }

    private void fixMissedDynamicReference() {
        // Register device factory provider
        injector.getInstance(MeteringGroupsService.class);
        injector.getInstance(MasterDataService.class);

        ProtocolPluggableServiceImpl protocolPluggableService = (ProtocolPluggableServiceImpl) injector.getInstance(ProtocolPluggableService.class);
        protocolPluggableService.addInboundDeviceProtocolService(injector.getInstance(InboundDeviceProtocolService.class));
        protocolPluggableService.addDeviceProtocolService(injector.getInstance(DeviceProtocolService.class));
        protocolPluggableService.addConnectionTypeService(injector.getInstance(ConnectionTypeService.class));
        protocolPluggableService.addDeviceProtocolMessageService(injector.getInstance(DeviceProtocolMessageService.class));
        protocolPluggableService.addDeviceProtocolSecurityService(injector.getInstance(DeviceProtocolSecurityService.class));
        protocolPluggableService.addLicensedProtocolService(injector.getInstance(LicensedProtocolService.class));

        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        propertySpecService.addFactoryProvider((DeviceServiceImpl)injector.getInstance(DeviceService.class));
        propertySpecService.addFactoryProvider((ConnectionTaskServiceImpl)injector.getInstance(ConnectionTaskService.class));

        DefaultValidatorFactory defaultValidatorFactory = new DefaultValidatorFactory();
        defaultValidatorFactory.setPropertySpecService(propertySpecService);
        defaultValidatorFactory.setNlsService(injector.getInstance(NlsService.class));
        ((ValidationServiceImpl)injector.getInstance(ValidationService.class)).addResource(defaultValidatorFactory);

        ((DeviceConfigurationServiceImpl)injector.getInstance(DeviceConfigurationService.class)).setQueryService(injector.getInstance(QueryService.class));
        ((DataExportServiceImpl)injector.getInstance(DataExportService.class)).addResource(injector.getInstance(StandardCsvDataProcessorFactory.class));

        injector.getInstance(IssueDataCollectionService.class);
        fixIssueTemplates();
    }

    private void fixIssueTemplates() {
        AbstractTemplate template = injector.getInstance(BasicDatacollectionRuleTemplate.class);
        IssueServiceImpl issueService = (IssueServiceImpl) injector.getInstance(IssueService.class);
        issueService.addCreationRuleTemplate(template);
    }

    private void createDefaultStuff(){
        MdcAppInstaller mdcAppInstaller = new MdcAppInstaller();
        mdcAppInstaller.setUserService(injector.getInstance(UserService.class));
        mdcAppInstaller.install();
    }

    private void tuneDeviceCountForSpeedTest(){
        try {
            Field deviceCount = DeviceTypeTpl.class.getDeclaredField("deviceCount");
            deviceCount.setAccessible(true);
            for (DeviceTypeTpl template : DeviceTypeTpl.values()) {
                deviceCount.setInt(template, 1);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
