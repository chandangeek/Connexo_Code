/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.calendar.impl.importers.CalendarImporterFactory;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.datavault.impl.DataVaultServiceImpl;
import com.elster.jupiter.demo.impl.ConsoleUser;
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
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.estimation.impl.EstimationServiceImpl;
import com.elster.jupiter.estimators.impl.DefaultEstimatorFactory;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.impl.DataExportServiceImpl;
import com.elster.jupiter.export.impl.ExportModule;
import com.elster.jupiter.export.processor.impl.CsvMeterDataFormatterFactory;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mail.impl.MailModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.properties.rest.PropertyValueInfoServiceModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.elster.jupiter.validation.impl.ValidationServiceImpl;
import com.elster.jupiter.validators.impl.DefaultValidatorFactory;
import com.energyict.mdc.app.impl.MdcAppInstaller;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmModule;
import com.energyict.mdc.device.alarms.impl.templates.AbstractDeviceAlarmTemplate;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.cps.ChannelSAPInfoCustomPropertySet;
import com.energyict.mdc.device.config.cps.DeviceEMeterInfoCustomPropertySet;
import com.energyict.mdc.device.config.cps.DeviceSAPInfoCustomPropertySet;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationServiceImpl;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.MultiSenseHeadEndInterfaceImpl;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.search.DeviceSearchDomain;
import com.energyict.mdc.device.data.importers.impl.attributes.connection.ConnectionAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.attributes.security.SecurityAttributesImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.activation.DeviceActivationDeactivationImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.commission.DeviceCommissioningImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.decommission.DeviceDecommissioningImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.installation.DeviceInstallationImporterFactory;
import com.energyict.mdc.device.data.importers.impl.devices.remove.DeviceRemoveImportFactory;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.DeviceShipmentImporterFactory;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImporterFactory;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCycleModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.favorites.impl.FavoritesModule;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.impl.FirmwareModule;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.io.impl.SerialIONoModemComponentServiceImpl;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionModule;
import com.energyict.mdc.issue.datacollection.impl.templates.AbstractDataCollectionTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.DataValidationIssueCreationRuleTemplate;
import com.energyict.mdc.issue.datavalidation.impl.IssueDataValidationModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
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
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import com.energyict.protocolimpl.elster.a3.AlphaA3;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DemoTest {
    private static final Logger LOG = Logger.getLogger(DemoTest.class.getName());

    protected static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private User currentUser;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(FtpClientService.class).toInstance(mock(FtpClientService.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DeviceMessageFileService.class).toInstance(mock(DeviceMessageFileService.class));
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
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(LoadProfileFactory.class).toInstance(mock(LoadProfileFactory.class));
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
                new UsagePointLifeCycleConfigurationModule(),
                new UsagePointLifeCycleModule(),
                new MeteringModule(
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.2.1.12.0.0.0.0.0.0.0.0.3.73.0",
                        "0.0.0.1.3.1.12.0.0.0.0.0.0.0.0.3.73.0",
                        "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0",
                        "0.0.0.4.3.1.12.0.0.0.0.0.0.0.0.3.73.0",
                        "0.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0",
                        "0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0",
                        "0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0",
                        "0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0",
                        "11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0",
                        "11.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0",
                        "11.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0",
                        "11.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0",
                        "13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0",
                        "13.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0",
                        "13.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0",
                        "13.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0",
                        "0.0.0.1.0.0.142.0.0.1.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.2.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.3.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.4.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.5.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.6.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.7.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.8.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.9.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.10.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.11.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.12.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.13.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.14.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.15.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.16.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.17.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.18.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.19.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.20.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.21.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.22.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.23.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.24.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.25.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.26.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.27.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.28.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.29.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.30.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.31.1.0.0.0.0.0.111.0",
                        "0.0.0.1.0.0.142.0.0.32.1.0.0.0.0.0.111.0"
                ),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new DataVaultModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new PkiModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new KpiModule(),
                new TaskModule(),
                new com.elster.jupiter.issue.impl.module.IssueModule(),
                new WebServicesModule(),
                new AppServiceModule(),
                new TimeModule(),
                new ExportModule(),
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
                new DeviceLifeCycleModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new TasksModule(),
                new IssuesModule(),
                new SchedulingModule(),
                new ProtocolApiModule(),
                new IssueDataCollectionModule(),
                new IssueDataValidationModule(),
                new TopologyModule(),
                new FavoritesModule(),
                new FirmwareModule(),
                new FileImportModule(),
                new MailModule(),
                new DemoModule(),
                new CalendarModule(),
                new PropertyValueInfoServiceModule(),
                new DeviceAlarmModule()
        );
        doPreparations();
    }

    @After
    public void deactivateEnvironment() {
        inMemoryBootstrapModule.deactivate();
    }

    @After
    public void clearPrincipal() {
        injector.getInstance(ThreadPrincipalService.class).clear();
    }

    @Test
    public void testDemoSetup() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);

        // Business method
        demoService.createDemoData("DemoServ", "host", "2014-12-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
    }

    @Test
    public void testNtaSimulationToolPropertyOnDeviceTest() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);

        // Business method
        demoService.createDemoData("DemoServ", "host", "2014-12-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> spe010000010156 = deviceService.findDeviceByName("SPE010000010001");
        assertThat(spe010000010156.get().getDeviceProtocolProperties().getProperty("NTASimulationTool")).isEqualTo(true);
    }

    @Test
    public void testTimeZonePropertyOnDeviceTest() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);

        // Business method
        demoService.createDemoData("DemoServ", "host", "2014-12-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB

        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> spe010000010156 = deviceService.findDeviceByName("SPE010000010001");
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
        String GATEWAY_NAME = "123-4567-89";
        String SLAVE1_NAME = "Demo board AS3000";
        String SLAVE2_NAME = "Demo board AS220";
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createG3Gateway(GATEWAY_NAME);
        demoService.createG3SlaveAS3000(SLAVE1_NAME);
        demoService.createG3SlaveAS220(SLAVE2_NAME);

        this.setPrincipal();
        checkCreatedG3Gateway(GATEWAY_NAME);
        checkCreatedG3SlaveDevice(SLAVE1_NAME);
        checkCreatedG3SlaveDevice(SLAVE2_NAME);
    }

    private void setPrincipal() {
        injector.getInstance(ThreadPrincipalService.class).set(getPrincipal());
    }

    private Principal getPrincipal() {
        return new ConsoleUser();
    }

    private void checkCreatedG3Gateway(String gatewayName) {
        String DEVICE_CONFIG_NAME = "Default";
        String SECURITY_PROPERTY_SET_NAME = "High level authentication - No encryption";
        String CONNECTION_METHOD_NAME = "Outbound TCP";

        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> gatewayOptional = deviceService.findDeviceByName(gatewayName);
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
        assertThat(configuration.getGatewayType()).isEqualTo(GatewayType.LOCAL_AREA_NETWORK);
        assertThat(configuration.getSecurityPropertySets()).hasSize(1);
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().get(0);
        assertThat(securityPropertySet.getName()).isEqualTo(SECURITY_PROPERTY_SET_NAME);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel().getId()).isEqualTo(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_GMAC.getValue());
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel().getId()).isEqualTo(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue());
//        assertThat(securityPropertySet.getUserActions()).containsExactly(
//                DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2,
//                DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        assertThat(configuration.getPartialOutboundConnectionTasks()).hasSize(1);
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        assertThat(connectionTask.getName()).isEqualTo(CONNECTION_METHOD_NAME);
        assertThat(connectionTask.isDefault()).isTrue();
        assertThat(connectionTask.getComPortPool().getName()).isEqualTo(OutboundTCPComPortPoolTpl.ORANGE.getName());
        assertThat(connectionTask.getRescheduleDelay().getCount()).isEqualTo(5);
        assertThat(connectionTask.getRescheduleDelay().getTimeUnit()).isEqualTo(TimeDuration.TimeUnit.MINUTES);
        assertThat(configuration.getComTaskEnablements()).hasSize(1);
        ComTaskEnablement enablement = configuration.getComTaskEnablements().get(0);
        assertThat(enablement.getComTask().getName()).isEqualTo(ComTaskTpl.TOPOLOGY_UPDATE.getName());
        assertThat(enablement.getSecurityPropertySet().getId()).isEqualTo(securityPropertySet.getId());
        assertThat(enablement.usesDefaultConnectionTask()).isTrue();
        assertThat(enablement.isIgnoreNextExecutionSpecsForInbound()).isFalse();
        assertThat(enablement.getPriority()).isEqualTo(100);
        assertThat(gateway.getConnectionTasks()).hasSize(1);
        ConnectionTask connTask = gateway.getConnectionTasks().get(0);
        assertThat(connTask).isInstanceOf(ScheduledConnectionTask.class);
        ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connTask;
        assertThat(scheduledConnectionTask.getComPortPool().getName()).isEqualTo(OutboundTCPComPortPoolTpl.ORANGE.getName());
        assertThat(scheduledConnectionTask.isDefault()).isTrue();
        assertThat(scheduledConnectionTask.getNumberOfSimultaneousConnections()).isEqualTo(1);
        assertThat(scheduledConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            assertThat(scheduledConnectionTask.getProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName()).getValue()).isEqualTo("10.0.0.135");
            assertThat(scheduledConnectionTask.getProperty(ConnectionTypePropertySpecName.OUTBOUND_IP_PORT_NUMBER.propertySpecName()).getValue()).isEqualTo(new BigDecimal(4059));
            assertThat(gateway.getSecurityProperties(securityPropertySet)).hasSize(3);
            for (SecurityProperty securityProperty : gateway.getSecurityProperties(securityPropertySet)) {
                if (SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey().equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue()).isEqualTo(BigDecimal.ONE);
                } else if (SecurityPropertySpecName.AUTHENTICATION_KEY.getKey().equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue().toString()).isEqualTo("00112233445566778899AABBCCDDEEFF");
                } else if (SecurityPropertySpecName.ENCRYPTION_KEY.getKey().equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue().toString()).isEqualTo("11223344556677889900AABBCCDDEEFF");
                }
            }
            ctx.commit();
        }
        assertThat(gateway.getDeviceProtocolProperties().getProperty("Short_MAC_address")).isEqualTo(BigDecimal.ZERO);
        assertThat(gateway.getComTaskExecutions()).hasSize(1);
    }

    private void checkCreatedG3SlaveDevice(String deviceName) {
        String SERIAL_NUMBER = "Demo board AS3000".equals(deviceName) ? "E0023000520685414" : "123457S";
        String MAC_ADDRESS = "Demo board AS3000".equals(deviceName) ? "02237EFFFEFD835B" : "02237EFFFEFD82F4";
        String SECURITY_SET_NAME = "High level MD5 authentication - No encryption";

        DeviceService deviceService = injector.getInstance(DeviceService.class);
        Optional<Device> deviceOptional = deviceService.findDeviceByName(deviceName);
        assertThat(deviceOptional.isPresent()).isTrue();
        Device device = deviceOptional.get();
        assertThat(device.getSerialNumber()).isEqualTo(SERIAL_NUMBER);

        DeviceType deviceType = device.getDeviceType();
        assertThat(deviceType.getName()).isEqualTo("Demo board AS3000".equals(deviceName) ? DeviceTypeTpl.AS3000.getName() : DeviceTypeTpl.AS220.getName());
        List<LoadProfileType> loadProfileTypes = deviceType.getLoadProfileTypes();
        assertThat(loadProfileTypes).hasSize(3);
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            if (LoadProfileTypeTpl._15_MIN_ELECTRICITY.getName().equals(loadProfileType.getName())) {
                assertThat(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getObisCode()).isEqualTo(loadProfileType.getObisCode().toString());
                assertThat(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getInterval()).isEqualTo(TimeDuration.minutes(15));
                assertThat(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getRegisterTypes()).containsExactly(
                        RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS);
            } else if (LoadProfileTypeTpl.DAILY_ELECTRICITY.getName().equals(loadProfileType.getName())) {
                assertThat(LoadProfileTypeTpl.DAILY_ELECTRICITY.getObisCode()).isEqualTo(loadProfileType.getObisCode().toString());
                assertThat(LoadProfileTypeTpl.DAILY_ELECTRICITY.getInterval()).isEqualTo(TimeDuration.days(1));
                assertThat(LoadProfileTypeTpl.DAILY_ELECTRICITY.getRegisterTypes()).containsExactly(
                        RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2);
            } else if (LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getName().equals(loadProfileType.getName())) {
                assertThat(LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getObisCode()).isEqualTo(loadProfileType.getObisCode().toString());
                assertThat(LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getInterval()).isEqualTo(TimeDuration.months(1));
                assertThat(LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getRegisterTypes()).containsExactly(
                        RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2);
            } else {
                fail("The device type of device with name = " + deviceName + " contains an unwanted loadprofile: " + loadProfileType.getName());
            }
        }
        List<RegisterType> registerTypes = deviceType.getRegisterTypes();
        assertThat(registerTypes).hasSize(6);
        for (RegisterType registerType : registerTypes) {
            if (!RegisterTypeTpl.SECONDARY_BULK_A_PLUS.getObisCode().equals(registerType.getObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_BULK_A_MINUS.getObisCode().equals(registerType.getObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1.getObisCode().equals(registerType.getObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2.getObisCode().equals(registerType.getObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1.getObisCode().equals(registerType.getObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2.getObisCode().equals(registerType.getObisCode().toString())) {
                fail("The device type of device with name = " + deviceName + " contains an unwanted register type: " + registerType.getObisCode());
            }
        }
        assertThat(deviceType.getLogBookTypes()).hasSize(1);
        LogBookType logBookType = deviceType.getLogBookTypes().get(0);
        assertThat(logBookType.getName()).isEqualTo(LogBookTypeTpl.GENERIC.getName());
        assertThat(logBookType.getObisCode().toString()).isEqualTo(LogBookTypeTpl.GENERIC.getObisCode());
        assertThat(deviceType.getConfigurations()).hasSize(1);

        DeviceConfiguration configuration = deviceType.getConfigurations().get(0);
        assertThat(configuration.getName()).isEqualTo(DeviceConfigurationTpl.AM540.getName());
        assertThat(configuration.isDirectlyAddressable()).isFalse();
        assertThat(configuration.canActAsGateway()).isTrue();
        assertThat(configuration.getGatewayType()).isEqualTo(GatewayType.HOME_AREA_NETWORK);
        assertThat(configuration.getSecurityPropertySets()).hasSize(1);
        SecurityPropertySet securityPropertySet = configuration.getSecurityPropertySets().get(0);
        assertThat(securityPropertySet.getName()).isEqualTo(SECURITY_SET_NAME);
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel().getId()).isEqualTo(DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_MD5.getValue());
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel().getId()).isEqualTo(DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue());
//        assertThat(securityPropertySet.getUserActions()).containsExactly(
//                DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2,
//                DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        assertThat(configuration.getPartialOutboundConnectionTasks().isEmpty()).isTrue();
        assertThat(configuration.getComTaskEnablements()).hasSize(3);
        for (ComTaskEnablement enablement : configuration.getComTaskEnablements()) {
            if (ComTaskTpl.TOPOLOGY_UPDATE.getName().equals(enablement.getComTask().getName()) ||
                    ComTaskTpl.READ_LOAD_PROFILE_DATA.getName().equals(enablement.getComTask().getName()) ||
                    ComTaskTpl.READ_LOG_BOOK_DATA.getName().equals(enablement.getComTask().getName()) ||
                    ComTaskTpl.READ_REGISTER_DATA.getName().equals(enablement.getComTask().getName())) {
                assertThat(enablement.getSecurityPropertySet().getId()).isEqualTo(securityPropertySet.getId());
                assertThat(enablement.usesDefaultConnectionTask()).isTrue();
                assertThat(enablement.isIgnoreNextExecutionSpecsForInbound()).isFalse();
                assertThat(enablement.getPriority() == 100).isTrue();
            } else {
                fail("The device type of device with name = " + deviceName + " contains an unwanted com task configuration : " + enablement.getComTask().getName());
            }
        }

        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        assertThat(loadProfiles).hasSize(3);
        for (LoadProfile loadProfile : loadProfiles) {
            if (LoadProfileTypeTpl._15_MIN_ELECTRICITY.getName().equals(loadProfile.getLoadProfileSpec().getLoadProfileType().getName())) {
                assertThat(loadProfile.getChannels()).hasSize(2);
            } else if (LoadProfileTypeTpl.DAILY_ELECTRICITY.getName().equals(loadProfile.getLoadProfileSpec().getLoadProfileType().getName())) {
                assertThat(loadProfile.getChannels()).hasSize(4);
            } else if (LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getName().equals(loadProfile.getLoadProfileSpec().getLoadProfileType().getName())) {
                assertThat(loadProfile.getChannels()).hasSize(4);
            } else {
                fail("The device with name = " + deviceName + " contains an unwanted loadprofile: " + loadProfile.getLoadProfileSpec().getLoadProfileType().getName());
            }
        }
        assertThat(device.getLogBooks()).hasSize(1);
        LogBook logBook = device.getLogBooks().get(0);
        assertThat(LogBookTypeTpl.GENERIC.getName()).isEqualTo(logBook.getLogBookType().getName());
        assertThat(LogBookTypeTpl.GENERIC.getObisCode()).isEqualTo(logBook.getLogBookType().getObisCode().toString());
        List<Register> registers = device.getRegisters();
        assertThat(registers).hasSize(6);
        for (Register register : registers) {
            if (!RegisterTypeTpl.SECONDARY_BULK_A_PLUS.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_BULK_A_MINUS.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1.getObisCode().equals(register.getRegisterSpecObisCode().toString()) &&
                    !RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2.getObisCode().equals(register.getRegisterSpecObisCode().toString())) {
                fail("The device with name = " + deviceName + " contains an unwanted register : " + register.getRegisterSpecObisCode());
            }
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            for (SecurityProperty securityProperty : device.getSecurityProperties(securityPropertySet)) {
                if ("ClientMacAddress".equals(securityProperty.getName())) {
                    assertThat(securityProperty.getValue()).isEqualTo(BigDecimal.ONE);
                } else if ("Password".equals(securityProperty.getName())) {
                    assertThat(((Password) securityProperty.getValue()).getValue()).isEqualTo("1234567890123456");
                }
            }
            ctx.commit();
        }
        assertThat(device.getDeviceProtocolProperties().getProperty("MAC_address")).isEqualTo(MAC_ADDRESS);
    }

    @Test
    public void testExecuteCreateG3DevicesTwice() {
        String MRID_GATEWAY = "123-4567-89";
        String MRID_SLAVE1 = "Demo board AS3000";
        String MRID_SLAVE2 = "Demo board AS220";

        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createG3Gateway(MRID_GATEWAY);
        demoService.createG3SlaveAS3000(MRID_SLAVE1);
        demoService.createG3SlaveAS220(MRID_SLAVE2);

        demoService.createG3Gateway(MRID_GATEWAY);
        demoService.createG3SlaveAS3000(MRID_SLAVE1);
        demoService.createG3SlaveAS220(MRID_SLAVE2);

        this.setPrincipal();
        checkCreatedG3Gateway(MRID_GATEWAY);
        checkCreatedG3SlaveDevice(MRID_SLAVE1);
        checkCreatedG3SlaveDevice(MRID_SLAVE2);
    }

    @Test
    public void testExecuteCreateDemoDataTwice() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createDemoData("DemoServ", "host", "2014-12-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
        demoService.createDemoData("DemoServ", "host", "2014-12-01", "2", true);
        // Calling the command 'createDemoData' twice shouldn't produce errors
    }

    @Test
    public void testStartDate() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        try {
            demoService.createDemoData("DemoServ", "host", "2020-12-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
        } catch (UnableToCreate e) {
            assertThat(e.getMessage()).contains("Incorrect start date parameter");
        }
    }

    @Test
    public void testCreateDefaultLifeCycleCommand() {
        DeviceConfigurationService deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
        DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService = injector.getInstance(DeviceLifeCycleConfigurationService.class);
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);

        demoService.createDemoData("DemoServ", "host", "2015-01-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
        demoService.createDefaultDeviceLifeCycle("2015-01-01");

        Optional<DeviceLifeCycle> defaultDeviceLifeCycle = deviceLifeCycleConfigurationService.findDefaultDeviceLifeCycle();
        assertThat(defaultDeviceLifeCycle.isPresent()).isTrue();

        assertThat(deviceConfigurationService.findAllDeviceTypes().stream().noneMatch(x -> x.getDeviceLifeCycle().getId() != defaultDeviceLifeCycle.get().getId()));
    }

    @Test
    public void testRuleCreation() {
        IssueService issueService = injector.getInstance(IssueService.class);
        IssueCreationService issueCreationService = issueService.getIssueCreationService();

        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createDemoData("DemoServ", "host", "2015-01-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB

        assertThat(issueCreationService.getCreationRuleQuery().select(Condition.TRUE)).hasSize(5);
    }

    @Test
    public void testCreateImportersCommand() {
        FileImportService fileImportService = injector.getInstance(FileImportService.class);

        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);

        demoService.createDemoData("DemoServ", "host", "2015-01-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
        demoService.createImporters();

        assertThat(fileImportService.getImportSchedules()).hasSize(10);
    }

    @Test
    public void testCreateDemoUserCommand() {
        UserService userService = injector.getInstance(UserService.class);

        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);

        demoService.createDemoData("DemoServ", "host", "2015-01-01", "2", true); // Skip firmware management data, as H2 doesn't support update of LOB
        demoService.createDemoUser("MyDemoUser");

        Optional<Group> group = userService.getGroup("Demo Users");
        assertThat(group.isPresent()).isTrue();
        assertThat(group.get().getPrivileges().isEmpty()).isFalse();

        Optional<User> user = userService.findUser("MyDemoUser");
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getGroups()).hasSize(1);
        assertThat(user.get().isMemberOf("Demo Users")).isTrue();
    }

    @Test
    public void testCreateSPEDevice() {
        DemoServiceImpl demoService = injector.getInstance(DemoServiceImpl.class);
        demoService.createDemoData("DemoServ", "host", "2015-01-01", "1", true);
        demoService.createSPEDevice("123");
    }

    protected void doPreparations() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(DataVaultServiceImpl.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(UsagePointLifeCycleService.class); // install default usage point life cycle
            initializeCustomPropertySets();
            createRequiredProtocols();
            createDefaultStuff();
            injector.getInstance(DemoServiceImpl.class);
            prepareSearchDomain();
            ctx.commit();
        }
        tuneDeviceCountForSpeedTest();
    }

    private void initializeCustomPropertySets() {
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(injector.getInstance(DeviceEMeterInfoCustomPropertySet.class));
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(injector.getInstance(DeviceSAPInfoCustomPropertySet.class));
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(injector.getInstance(ChannelSAPInfoCustomPropertySet.class));
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

        DefaultValidatorFactory defaultValidatorFactory = new DefaultValidatorFactory();
        defaultValidatorFactory.setPropertySpecService(propertySpecService);
        defaultValidatorFactory.setNlsService(injector.getInstance(NlsService.class));
        ((ValidationServiceImpl) injector.getInstance(ValidationService.class)).addResource(defaultValidatorFactory);

        FileImportService fileImportService = injector.getInstance(FileImportService.class);
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceDecommissioningImportFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceShipmentImporterFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceReadingsImporterFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(ConnectionAttributesImportFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceCommissioningImportFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceActivationDeactivationImportFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(SecurityAttributesImportFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceInstallationImporterFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(DeviceRemoveImportFactory.class));
        ((FileImportServiceImpl) fileImportService).addFileImporter(injector.getInstance(CalendarImporterFactory.class));

        ((DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class)).setQueryService(injector.getInstance(QueryService.class));
        ((DataExportServiceImpl) injector.getInstance(DataExportService.class)).addFormatter(injector.getInstance(CsvMeterDataFormatterFactory.class), ImmutableMap.of(DataExportService.DATA_TYPE_PROPERTY, DataExportService.STANDARD_READING_DATA_TYPE));

        injector.getInstance(MeteringDataModelService.class).addHeadEndInterface(injector.getInstance(MultiSenseHeadEndInterfaceImpl.class));
        injector.getInstance(IssueDataCollectionService.class);
        injector.getInstance(IssueDataValidationService.class);
        injector.getInstance(DeviceAlarmService.class);
        fixIssueTemplates();
        fixEstimators(propertySpecService, injector.getInstance(TimeService.class));
    }

    private void fixIssueTemplates() {
        AbstractDataCollectionTemplate template = injector.getInstance(BasicDataCollectionRuleTemplate.class);
        DataValidationIssueCreationRuleTemplate dataValidationIssueCreationRuleTemplate = injector.getInstance(DataValidationIssueCreationRuleTemplate.class);
        AbstractDeviceAlarmTemplate alarmTemplate = injector.getInstance(BasicDeviceAlarmRuleTemplate.class);

        IssueServiceImpl issueService = (IssueServiceImpl) injector.getInstance(IssueService.class);
        issueService.addCreationRuleTemplate(template);
        issueService.addCreationRuleTemplate(dataValidationIssueCreationRuleTemplate);
        issueService.addCreationRuleTemplate(alarmTemplate);
    }

    private void fixEstimators(PropertySpecService propertySpecService, TimeService timeService) {
        EstimationServiceImpl estimationService = (EstimationServiceImpl) injector.getInstance(EstimationService.class);
        DefaultEstimatorFactory estimatorFactory =
                new DefaultEstimatorFactory(
                        injector.getInstance(NlsService.class),
                        propertySpecService,
                        injector.getInstance(ValidationService.class),
                        injector.getInstance(MeteringService.class),
                        timeService);
        estimationService.addEstimatorFactory(estimatorFactory);

    }

    private void createDefaultStuff() {
        injector.getInstance(FirmwareService.class);

        MdcAppInstaller mdcAppInstaller = new MdcAppInstaller();
        mdcAppInstaller.setUserService(injector.getInstance(UserService.class));
        new MdcAppInstaller.Installer(injector.getInstance(UserService.class)).createDefaultRoles();
        new MdcAppInstaller.Installer(injector.getInstance(UserService.class)).assignPrivilegesToDefaultRoles();
    }

    private void tuneDeviceCountForSpeedTest() {
        for (DeviceTypeTpl template : DeviceTypeTpl.values()) {
            template.tuneDeviceCountForSpeedTest();
        }
    }

    private void prepareSearchDomain() {
        SearchService searchService = injector.getInstance(SearchService.class);
        DeviceSearchDomain deviceSearchDomain = injector.getInstance(DeviceSearchDomain.class);
        searchService.register(deviceSearchDomain);
    }
}
