package com.elster.jupiter.demo;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.datavault.impl.DataVaultServiceImpl;
import com.elster.jupiter.demo.impl.DemoServiceImpl;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.ComTaskTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.elster.jupiter.demo.impl.templates.LogBookTypeTpl;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.demo.impl.templates.RegisterTypeTpl;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.impl.DataExportServiceImpl;
import com.elster.jupiter.export.impl.ExportModule;
import com.elster.jupiter.export.processor.impl.StandardCsvDataProcessorFactory;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
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
import com.elster.jupiter.time.TimeDuration;
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
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationServiceImpl;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.DeviceServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.favorites.impl.FavoritesModule;
import com.energyict.mdc.firmware.impl.FirmwareModule;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.io.impl.SerialIONoModemComponentServiceImpl;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionModule;
import com.energyict.mdc.issue.datacollection.impl.templates.AbstractDataCollectionTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
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
    public void setEnvironment() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new BpmModule(),
                new FiniteStateMachineModule(),
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
                new EstimationModule(),
                new DeviceLifeCycleConfigurationModule(),
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
                new FirmwareModule(),
                new FileImportModule(),
                new DemoModule()
        );
        doPreparations();
    }

    @After
    public void deactivateEnvironment() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testDemoSetup() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try {
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
        } catch (Exception e) {
            fail("The demo command shouldn't produce errors");
        }
    }

    @Test
    public void testNtaSimulationToolPropertyOnDeviceTest() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try {
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
        } catch (Exception e) {
            fail("The demo command shouldn't produce errors");
        }
        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> spe010000010156 = deviceService.findByUniqueMrid("SPE010000010001");
        assertThat(spe010000010156.get().getDeviceProtocolProperties().getProperty("NTASimulationTool")).isEqualTo(true);
    }

    @Test
    public void testTimeZonePropertyOnDeviceTest() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try {
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
        } catch (Exception e) {
            fail("The demo command shouldn't produce errors");
        }
        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> spe010000010156 = deviceService.findByUniqueMrid("SPE010000010001");
        assertThat(spe010000010156.get().getDeviceProtocolProperties().getProperty("TimeZone")).isEqualTo(TimeZone.getTimeZone("Europe/Brussels"));
    }

    @Test
    public void testCreateA3Device() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createA3Device();
        // The demo command shouldn't produce errors
    }

    @Test
    public void testExecuteCreateA3DeviceTwice() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createA3Device();
        demoService.createA3Device();
        // Calling the command 'createA3Device' twice shouldn't produce errors
    }

    @Test
    public void testCreateG3Devices() {
        String MRID_GATEWAY = "123-4567-89";
        String MRID_SLAVE1 = "E0023000520685414";
        String MRID_SLAVE2 = "123457S";
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createG3Gateway(MRID_GATEWAY);
        demoService.createG3SlaveDevice();

        checkCreatedG3Gateway(MRID_GATEWAY);
        checkCreatedG3SlaveDevice(MRID_SLAVE1);
        checkCreatedG3SlaveDevice(MRID_SLAVE2);
    }

    private void checkCreatedG3Gateway(String mridGateway) {
        String DEVICE_CONFIG_NAME = "Default";
        String SECURITY_PROPERTY_SET_NAME = "High level authentication - No encryption";
        String CONNECTION_METHOD_NAME = "Outbound TCP";

        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> gatewayOptional = deviceService.findByUniqueMrid(mridGateway);
        assertThat(gatewayOptional.isPresent()).isTrue();
        Device gateway = gatewayOptional.get();
        DeviceType deviceType = gateway.getDeviceType();
        assertThat(deviceType.getName()).isEqualTo(DeviceTypeTpl.RTU_Plus_G3.getName());
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
        assertThat(deviceType.getRegisterTypes()).isEmpty();
        assertThat(deviceType.getLogBookTypes()).isEmpty();
        DeviceConfiguration configuration = gateway.getDeviceConfiguration();
        assertThat(configuration.getName()).isEqualTo(DEVICE_CONFIG_NAME);
        assertThat(configuration.isDirectlyAddressable()).isTrue();
        assertThat(configuration.canActAsGateway()).isTrue();
        assertThat(configuration.getGetwayType()).isEqualTo(GatewayType.LOCAL_AREA_NETWORK);
        assertThat(configuration.getSecurityPropertySets().size()).isEqualTo(1);
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().get(0);
        assertThat(securityPropertySet.getName()).isEqualTo(SECURITY_PROPERTY_SET_NAME);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel().getId()).isEqualTo(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_GMAC.getValue());
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel().getId()).isEqualTo(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue());
        assertThat(securityPropertySet.getUserActions()).containsExactly(
            DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2,
            DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        assertThat(configuration.getPartialOutboundConnectionTasks().size()).isEqualTo(1);
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        assertThat(connectionTask.getName()).isEqualTo(CONNECTION_METHOD_NAME);
        assertThat(connectionTask.isDefault()).isTrue();
        assertThat(connectionTask.getComPortPool().getName()).isEqualTo(OutboundTCPComPortPoolTpl.ORANGE.getName());
        assertThat(connectionTask.getRescheduleDelay().getCount()).isEqualTo(5);
        assertThat(connectionTask.getRescheduleDelay().getTimeUnit()).isEqualTo(TimeDuration.TimeUnit.MINUTES);
        assertThat(configuration.getComTaskEnablements().size()).isEqualTo(1);
        ComTaskEnablement enablement = configuration.getComTaskEnablements().get(0);
        assertThat(enablement.getComTask().getName()).isEqualTo(ComTaskTpl.TOPOLOGY_UPDATE.getName());
        assertThat(enablement.getSecurityPropertySet().getId()).isEqualTo(securityPropertySet.getId());
        assertThat(enablement.usesDefaultConnectionTask()).isTrue();
        assertThat(enablement.isIgnoreNextExecutionSpecsForInbound()).isTrue();
        assertThat(enablement.getPriority()==100).isTrue();
        assertThat(gateway.getConnectionTasks().size()).isEqualTo(1);
        ConnectionTask connTask = gateway.getConnectionTasks().get(0);
        assertThat(connTask instanceof ScheduledConnectionTask).isTrue();
        ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask)connTask;
        assertThat(scheduledConnectionTask.getComPortPool().getName()).isEqualTo(OutboundTCPComPortPoolTpl.ORANGE.getName());
        assertThat(scheduledConnectionTask.isDefault()).isTrue();
        assertThat(scheduledConnectionTask.isSimultaneousConnectionsAllowed()).isFalse();
        assertThat(scheduledConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            assertThat(scheduledConnectionTask.getProperty("host").getValue()).isEqualTo("10.0.0.135");
            assertThat(scheduledConnectionTask.getProperty("portNumber").getValue()).isEqualTo(new BigDecimal(4059));
            assertThat(gateway.getAllSecurityProperties(securityPropertySet).size()).isEqualTo(3);
            for (SecurityProperty securityProperty : gateway.getAllSecurityProperties(securityPropertySet)) {
                if ("ClientMacAddress".equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue()).isEqualTo(new BigDecimal(1));
                } else if ("AuthenticationKey".equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue().toString()).isEqualTo("00112233445566778899AABBCCDDEEFF");
                } else if ("EncryptionKey".equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue().toString()).isEqualTo("11223344556677889900AABBCCDDEEFF");
                }
            }
            ctx.commit();
        }
        assertThat(gateway.getDeviceProtocolProperties().getProperty("Short_MAC_address")).isEqualTo(new BigDecimal(0));
        assertThat(gateway.getComTaskExecutions().size()).isEqualTo(1);
    }

    private void checkCreatedG3SlaveDevice(String mridDevice) {
        String SERIAL_NUMBER = "E0023000520685414".equals(mridDevice) ? "05206854" : "35075302";
        String MAC_ADDRESS = "E0023000520685414".equals(mridDevice) ? "02237EFFFEFD835B" : "02237EFFFEFD82F4";
        String SECURITY_SET_NAME = "High level MD5 authentication - No encryption";

        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> deviceOptional = deviceService.findByUniqueMrid(mridDevice);
        assertThat(deviceOptional.isPresent()).isTrue();
        Device device = deviceOptional.get();
        assertThat(device.getSerialNumber()).isEqualTo(SERIAL_NUMBER);

        DeviceType deviceType = device.getDeviceType();
        assertThat(deviceType.getName()).isEqualTo(DeviceTypeTpl.AM540.getName());
        List<LoadProfileType> loadProfileTypes = deviceType.getLoadProfileTypes();
        assertThat(loadProfileTypes.size()).isEqualTo(3);
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            if (LoadProfileTypeTpl._15_MIN_ELECTRICITY.getName().equals(loadProfileType.getName())) {
                assertThat(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getObisCode()).isEqualTo(loadProfileType.getObisCode().toString());
                assertThat(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getInterval()).isEqualTo(TimeDuration.minutes(15));
                assertThat(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getRegisterTypes()).containsExactly(
                    RegisterTypeTpl.B_F_E_S_M_E, RegisterTypeTpl.B_R_E_S_M_E);
            } else if (LoadProfileTypeTpl.DAILY_ELECTRICITY.getName().equals(loadProfileType.getName())) {
                assertThat(LoadProfileTypeTpl.DAILY_ELECTRICITY.getObisCode()).isEqualTo(loadProfileType.getObisCode().toString());
                assertThat(LoadProfileTypeTpl.DAILY_ELECTRICITY.getInterval()).isEqualTo(TimeDuration.days(1));
                assertThat(LoadProfileTypeTpl.DAILY_ELECTRICITY.getRegisterTypes()).containsExactly(
                    RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2);
            } else if (LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getName().equals(loadProfileType.getName())) {
                assertThat(LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getObisCode()).isEqualTo(loadProfileType.getObisCode().toString());
                assertThat(LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getInterval()).isEqualTo(TimeDuration.months(1));
                assertThat(LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getRegisterTypes()).containsExactly(
                    RegisterTypeTpl.S_F_E_S_M_E_T1, RegisterTypeTpl.S_F_E_S_M_E_T2, RegisterTypeTpl.S_R_E_S_M_E_T1, RegisterTypeTpl.S_R_E_S_M_E_T2);
            } else {
                fail("The device type of device with MRID = "+ mridDevice +" contains an unwanted loadprofile: " + loadProfileType.getName());
            }
        }
        List<RegisterType> registerTypes = deviceType.getRegisterTypes();
        assertThat(registerTypes.size()).isEqualTo(6);
        for (RegisterType registerType : registerTypes) {
            if ( !RegisterTypeTpl.B_F_E_S_M_E.getObisCode().equals(registerType.getObisCode().toString()) &&
                 !RegisterTypeTpl.B_R_E_S_M_E.getObisCode().equals(registerType.getObisCode().toString()) &&
                 !RegisterTypeTpl.S_F_E_S_M_E_T1.getObisCode().equals(registerType.getObisCode().toString()) &&
                 !RegisterTypeTpl.S_F_E_S_M_E_T2.getObisCode().equals(registerType.getObisCode().toString()) &&
                 !RegisterTypeTpl.S_R_E_S_M_E_T1.getObisCode().equals(registerType.getObisCode().toString()) &&
                 !RegisterTypeTpl.S_R_E_S_M_E_T2.getObisCode().equals(registerType.getObisCode().toString()) ) {
                fail("The device type of device with MRID = "+ mridDevice +" contains an unwanted register type: " + registerType.getObisCode());
            }
        }
        assertThat(deviceType.getLogBookTypes().size()).isEqualTo(1);
        LogBookType logBookType = deviceType.getLogBookTypes().get(0);
        assertThat(logBookType.getName()).isEqualTo(LogBookTypeTpl.GENERIC.getName());
        assertThat(logBookType.getObisCode().toString()).isEqualTo(LogBookTypeTpl.GENERIC.getObisCode());
        assertThat(deviceType.getConfigurations().size()).isEqualTo(1);

        DeviceConfiguration configuration = deviceType.getConfigurations().get(0);
        assertThat(configuration.getName()).isEqualTo(DeviceConfigurationTpl.AM540.getName());
        assertThat(configuration.isDirectlyAddressable()).isFalse();
        assertThat(configuration.canActAsGateway()).isTrue();
        assertThat(configuration.getGetwayType()).isEqualTo(GatewayType.HOME_AREA_NETWORK);
        assertThat(configuration.getSecurityPropertySets().size()).isEqualTo(1);
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().get(0);
        assertThat(securityPropertySet.getName()).isEqualTo(SECURITY_SET_NAME);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel().getId()).isEqualTo(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_GMAC.getValue());
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel().getId()).isEqualTo(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue());
        assertThat(securityPropertySet.getUserActions()).containsExactly(
            DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2,
            DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        assertThat(configuration.getPartialOutboundConnectionTasks().isEmpty()).isTrue();
        assertThat(configuration.getComTaskEnablements().size()).isEqualTo(4);
        for (ComTaskEnablement enablement : configuration.getComTaskEnablements()) {
            if ( ComTaskTpl.TOPOLOGY_UPDATE.getName().equals(enablement.getComTask().getName()) ||
                 ComTaskTpl.READ_LOAD_PROFILE_DATA.getName().equals(enablement.getComTask().getName()) ||
                 ComTaskTpl.READ_LOG_BOOK_DATA.getName().equals(enablement.getComTask().getName()) ||
                 ComTaskTpl.READ_REGISTER_DATA.getName().equals(enablement.getComTask().getName()) ) {
                assertThat(enablement.getSecurityPropertySet().getId()).isEqualTo(securityPropertySet.getId());
                assertThat(enablement.usesDefaultConnectionTask()).isTrue();
                assertThat(enablement.isIgnoreNextExecutionSpecsForInbound()).isTrue();
                assertThat(enablement.getPriority()==100).isTrue();
            } else {
                fail("The device type of device with MRID = "+ mridDevice +" contains an unwanted com task configuration : " + enablement.getComTask().getName());
            }
        }

        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        assertThat(loadProfiles.size()).isEqualTo(3);
        for (LoadProfile loadProfile : loadProfiles) {
            if (LoadProfileTypeTpl._15_MIN_ELECTRICITY.getName().equals(loadProfile.getLoadProfileSpec().getLoadProfileType().getName())) {
                assertThat(loadProfile.getChannels().size()).isEqualTo(2);
            } else if (LoadProfileTypeTpl.DAILY_ELECTRICITY.getName().equals(loadProfile.getLoadProfileSpec().getLoadProfileType().getName())) {
                assertThat(loadProfile.getChannels().size()).isEqualTo(4);
            } else if (LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getName().equals(loadProfile.getLoadProfileSpec().getLoadProfileType().getName())) {
                assertThat(loadProfile.getChannels().size()).isEqualTo(4);
            } else {
                fail("The device with MRID = "+ mridDevice +" contains an unwanted loadprofile: " + loadProfile.getLoadProfileSpec().getLoadProfileType().getName());
            }
        }
        assertThat(device.getLogBooks().size()).isEqualTo(1);
        LogBook logBook = device.getLogBooks().get(0);
        assertThat(LogBookTypeTpl.GENERIC.getName()).isEqualTo(logBook.getLogBookType().getName());
        assertThat(LogBookTypeTpl.GENERIC.getObisCode()).isEqualTo(logBook.getLogBookType().getObisCode().toString());
        List<Register> registers = device.getRegisters();
        assertThat(registers.size()).isEqualTo(6);
        for (Register register : registers) {
            if ( !RegisterTypeTpl.B_F_E_S_M_E.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                 !RegisterTypeTpl.B_R_E_S_M_E.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                 !RegisterTypeTpl.S_F_E_S_M_E_T1.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                 !RegisterTypeTpl.S_F_E_S_M_E_T2.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                 !RegisterTypeTpl.S_R_E_S_M_E_T1.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                 !RegisterTypeTpl.S_R_E_S_M_E_T2.getObisCode().equals(register.getRegisterSpecObisCode().toString()) ) {
                fail("The device with MRID = "+ mridDevice +" contains an unwanted register : " + register.getRegisterSpecObisCode());
            }
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            for (SecurityProperty securityProperty : device.getAllSecurityProperties(securityPropertySet)) {
                if ("ClientMacAddress".equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue()).isEqualTo(new BigDecimal(1));
                } else if ("Password".equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue().toString()).isEqualTo("1234567890123456");
                }
            }
            ctx.commit();
        }
        assertThat(device.getDeviceProtocolProperties().getProperty("MAC_address")).isEqualTo(MAC_ADDRESS);

    }

    @Test
    public void testExecuteCreateG3DevicesTwice() {
        String MRID_GATEWAY = "123-4567-89";
        String MRID_SLAVE1 = "E0023000520685414";
        String MRID_SLAVE2 = "123457S";

        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createG3Gateway(MRID_GATEWAY);
        demoService.createG3SlaveDevice();
        demoService.createG3Gateway(MRID_GATEWAY);
        demoService.createG3SlaveDevice();
        // Calling the commands 'createG3Gateway' and 'createG3SlaveDevice' twice shouldn't produce errors
        checkCreatedG3Gateway(MRID_GATEWAY);
        checkCreatedG3SlaveDevice(MRID_SLAVE1);
        checkCreatedG3SlaveDevice(MRID_SLAVE2);
    }

    @Test
    public void testExecuteCreateDemoDataTwice() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
            demoService.createDemoData("DemoServ", "host", "2014-12-01");
        // Calling the command 'createDemoData' twice shouldn't produce errors
    }

    @Test
    public void testStartDate() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try {
            demoService.createDemoData("DemoServ", "host", "2020-12-01");
        } catch (UnableToCreate e) {
            assertThat(e.getMessage()).contains("Incorrect start date parameter");
        }
    }

    protected void doPreparations() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            createOracleTablesSubstitutes();
            injector.getInstance(DataVaultServiceImpl.class).install();
            createRequiredProtocols();
            createDefaultStuff();
            injector.getInstance(DemoServiceImpl.class);
            ctx.commit();
        }
        tuneDeviceCountForSpeedTest();
    }

    private void createOracleTablesSubstitutes() {
        OrmServiceImpl ormService = (OrmServiceImpl) injector.getInstance(OrmService.class);
        try (Connection connection = ormService.getConnection(true)) {
            SqlBuilder sqlBuilder = new SqlBuilder("CREATE VIEW USER_TABLES AS (select * from INFORMATION_SCHEMA.TABLES)");
            sqlBuilder.prepare(connection).execute();
            sqlBuilder = new SqlBuilder("CREATE VIEW USER_SEQUENCES AS (select * from INFORMATION_SCHEMA.SEQUENCES)");
            sqlBuilder.prepare(connection).execute();
            sqlBuilder = new SqlBuilder("CREATE VIEW USER_IND_COLUMNS AS (select INDEX_NAME, TABLE_NAME, COLUMN_NAME, '1' COLUMN_POSITION from INFORMATION_SCHEMA.INDEXES AS ind)");
            sqlBuilder.prepare(connection).execute();
        } catch (SQLException e) {
            LOG.severe("Errors during creating substitutes for ORACLE tables. It may cause unpredictable work.");
        }
    }

    private void createRequiredProtocols() {
        fixMissedDynamicReference();
        ProtocolPluggableService protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
        protocolPluggableService.newInboundDeviceProtocolPluggableClass("DlmsSerialNumberDiscover", DlmsSerialNumberDiscover.class.getName()).save();
        protocolPluggableService.newDeviceProtocolPluggableClass("WebRTUKP", WebRTUKP.class.getName()).save();
        protocolPluggableService.newDeviceProtocolPluggableClass("ALPHA_A3", AlphaA3.class.getName()).save();
        protocolPluggableService.newDeviceProtocolPluggableClass("RTU_PLUS_G3", com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer.class.getName()).save();
        protocolPluggableService.newDeviceProtocolPluggableClass("AM540", com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540.class.getName()).save();
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
        propertySpecService.addFactoryProvider((DeviceServiceImpl) injector.getInstance(DeviceService.class));
        propertySpecService.addFactoryProvider((ConnectionTaskServiceImpl) injector.getInstance(ConnectionTaskService.class));

        DefaultValidatorFactory defaultValidatorFactory = new DefaultValidatorFactory();
        defaultValidatorFactory.setPropertySpecService(propertySpecService);
        defaultValidatorFactory.setNlsService(injector.getInstance(NlsService.class));
        ((ValidationServiceImpl) injector.getInstance(ValidationService.class)).addResource(defaultValidatorFactory);

        ((DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class)).setQueryService(injector.getInstance(QueryService.class));
        ((DataExportServiceImpl) injector.getInstance(DataExportService.class)).addProcessor(injector.getInstance(StandardCsvDataProcessorFactory.class));

        injector.getInstance(IssueDataCollectionService.class);
        fixIssueTemplates();
    }

    private void fixIssueTemplates() {
        AbstractDataCollectionTemplate template = injector.getInstance(BasicDataCollectionRuleTemplate.class);
        IssueServiceImpl issueService = (IssueServiceImpl) injector.getInstance(IssueService.class);
        issueService.addCreationRuleTemplate(template);
    }

    private void createDefaultStuff() {
        injector.getInstance(FirmwareService.class);
        MdcAppInstaller mdcAppInstaller = new MdcAppInstaller();
        mdcAppInstaller.setUserService(injector.getInstance(UserService.class));
        mdcAppInstaller.install();
    }

    private void tuneDeviceCountForSpeedTest() {
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
