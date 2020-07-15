/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
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
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
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
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.device.config.DeviceMessageFile;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignServiceImpl;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    private HttpService httpService;
    private Thesaurus thesaurus;
    private MeteringZoneService meteringZoneService;
    private FirmwareCampaignServiceImpl firmwareCampaignService;

    public void initializeDatabase(String testName, boolean showSqlLogging) {
        this.initializeMocks(testName);
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new EventsModule(),
                new IdsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new BpmModule(),
                new H2OrmModule(),
                new DataVaultModule(),
                new InMemoryMessagingModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new DeviceLifeCycleConfigurationModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new TaskModule(),
                new DeviceConfigurationModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new EngineModelModule(),
                new MdcDynamicModule(),
                new IssuesModule(),
                new ProtocolApiModule(),
                new PluggableModule(),
                new SchedulingModule(),
                new TasksModule(),
                new MasterDataModule(),
                new DeviceDataModule(),
                new FirmwareModule(),
                new CalendarModule(),
                new PkiModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new FileImportModule(),
                new MeteringZoneModule(),
                new TopologyModule()
        );
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            initializeCustomPropertySets();
            injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(UserService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(QueryService.class);
            injector.getInstance(DeviceConfigurationService.class);
            injector.getInstance(DeviceMessageSpecificationService.class);
            injector.getInstance(DeviceDataModelService.class);
            injector.getInstance(DeviceService.class);
            injector.getInstance(EventService.class);
            injector.getInstance(ProtocolPluggableService.class);
            injector.getInstance(AuditService.class);
            this.injector.getInstance(EndPointConfigurationService.class);
            this.injector.getInstance(WebServicesService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            this.firmwareService = spy((FirmwareServiceImpl) injector.getInstance(FirmwareService.class));
            this.dataModel = firmwareService.getDataModel();
            this.meteringZoneService = injector.getInstance(MeteringZoneService.class);
            ctx.commit();
        }
    }

    private void initializeCustomPropertySets() {
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
        injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class);
        this.licenseService = mock(LicenseService.class);
        this.thesaurus = NlsModule.SimpleThesaurus.from(new FirmwareServiceImpl().getKeys());
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        when(this.principal.getName()).thenReturn(testName);
        this.httpService = mock(HttpService.class);
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

    public TransactionService getTransactionService() {
        return transactionService;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(httpService);
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(ProtocolPluggableService.class).toInstance(mock(ProtocolPluggableService.class));
            bind(StateTransitionPropertiesProvider.class).toInstance(mock(StateTransitionPropertiesProvider.class));
            bind(AppService.class).toInstance(mock(AppService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            DeviceMessageSpecificationService deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_BROADCAST_FIRMWARE_UPGRADE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_DATA_CONCENTRATOR_MULTICAST_FIRMWARE_UPGRADE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE);
            doReturn(Optional.empty()).when(deviceMessageSpecificationService).getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE);
            doReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE)).when(deviceMessageSpecificationService)
                    .getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);

            when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                long id = (long) args[0];

                DeviceMessageSpec deviceMessageSpec = new DeviceMessageSpec() {

                    @Override
                    public DeviceMessageCategory getCategory() {
                        return mock(DeviceMessageCategory.class);
                    }

                    @Override
                    public String getName() {
                        return String.valueOf(id);
                    }

                    @Override
                    public DeviceMessageId getId() {
                        return DeviceMessageId.from(id);
                    }

                    @Override
                    public List<PropertySpec> getPropertySpecs() {
                        BasicPropertySpec propertySpec = spy(new BasicPropertySpec(new MockDeviceMessageFileValueFactory()));
                        propertySpec.setRequired(true);
                        propertySpec.setDescription("FirmwareDeviceMessage.upgrade.userfile");
                        propertySpec.setDisplayName("FirmwareDeviceMessage.upgrade.userfile");
                        propertySpec.setName("FirmwareDeviceMessage.upgrade.userfile");
                        when(propertySpec.isReference()).thenReturn(true);

                        return Collections.singletonList(propertySpec);
                    }
                };
                return Optional.of(deviceMessageSpec);

            });

            when(deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(any(DeviceMessageId.class))).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                DeviceMessageId deviceMessageId = (DeviceMessageId) args[0];
                return deviceMessageId.dbValue() == 5016 || deviceMessageId.dbValue() == 5017;
            });

            when(deviceMessageSpecificationService.canResumeFirmwareUpload(any(DeviceMessageId.class))).thenAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                DeviceMessageId deviceMessageId = (DeviceMessageId) args[0];
                return deviceMessageId.dbValue() == 5002 || deviceMessageId.dbValue() == 5003 || deviceMessageId.dbValue() == 5030;
            });

            bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }

    private class MockDeviceMessageFileValueFactory implements ValueFactory {
        @Override
        public Object fromStringValue(String stringValue) {
            if (Integer.parseInt(stringValue) < 10) {       //Oly DeviceMessageFiles with ID < 10 exist in the database
                return mock(DeviceMessageFile.class);
            } else {
                return null;
            }
        }

        @Override
        public String toStringValue(Object object) {
            return null;
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public Class getValueType() {
            return DeviceMessageFile.class;
        }

        @Override
        public Object valueFromDatabase(Object object) {
            return null;
        }

        @Override
        public Object valueToDatabase(Object object) {
            return null;
        }

        @Override
        public void bind(PreparedStatement statement, int offset, Object value) throws SQLException {
        }

        @Override
        public void bind(SqlBuilder builder, Object value) {
        }
    }

    public <T> T get(Class<T> clazz) {
        return this.injector.getInstance(clazz);
    }
}
