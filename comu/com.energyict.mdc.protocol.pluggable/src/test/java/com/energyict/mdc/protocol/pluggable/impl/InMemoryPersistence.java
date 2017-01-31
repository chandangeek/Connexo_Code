/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides initialization services that is typically used by classes that focus
 * on testing the correct implementation of the persistence aspects of entities in this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (09:57)
 */
public class InMemoryPersistence {

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private ThreadPrincipalService threadPrincipalService;
    private EventService eventService;
    private NlsService nlsService;
    private DataModel dataModel;
    private UserService userService;
    private MeteringService meteringService;

    private ConnectionTypeService connectionTypeService;
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    private DeviceProtocolMessageService deviceProtocolMessageService;
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    private DeviceProtocolService deviceProtocolService;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private LicensedProtocolService licensedProtocolService;
    private IssueService issueService;
    private PropertySpecService propertySpecService;
    private PluggableService pluggableService;
    private CustomPropertySetService customPropertySetService;
    private DeviceCacheMarshallingService deviceCacheMarshallingService;
    private DataVaultService dataVaultService;
    private LicenseService licenseService;

    private ProtocolPluggableServiceImpl protocolPluggableService;
    private InMemoryBootstrapModule bootstrapModule;

    public void initializeDatabase (String testName, DataModelInitializer... dataModelInitializers) {
        this.initializeMocks(testName);
        this.bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new PluggableModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            this.dataVaultService = injector.getInstance(DataVaultService.class);
            this.issueService = injector.getInstance(IssueService.class);
            this.dataModel = this.createNewProtocolPluggableService();
            for (DataModelInitializer initializer : dataModelInitializers) {
                initializer.initializeDataModel(this.dataModel);
            }
            ctx.commit();
        }
    }

    public void run(DataModelInitializer... dataModelInitializers) {
        try (TransactionContext ctx = this.transactionService.getContext()) {
            for (DataModelInitializer initializer : dataModelInitializers) {
                initializer.initializeDataModel(this.dataModel);
            }
            ctx.commit();
        }
    }

    private void initializeMocks(String testName) {
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.threadPrincipalService = mock(ThreadPrincipalService.class);
        this.principal = mock(Principal.class);
        when(this.principal.getName()).thenReturn(testName);
        this.propertySpecService = mock(PropertySpecService.class);
        this.pluggableService = mock(PluggableService.class);
        this.customPropertySetService = mock(CustomPropertySetService.class);
        this.deviceProtocolService = mock(DeviceProtocolService.class);
        this.deviceProtocolMessageService = mock(DeviceProtocolMessageService.class);
        this.deviceProtocolSecurityService = mock(DeviceProtocolSecurityService.class);
        this.inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        this.connectionTypeService = mock(ConnectionTypeService.class);
        this.licensedProtocolService = mock(LicensedProtocolService.class);
        this.legacySecurityPropertyConverter = mock(LegacySecurityPropertyConverter.class);
        this.deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        this.licenseService = mock(LicenseService.class);
        this.userService = mock(UserService.class);
        this.meteringService = mock(MeteringService.class);

        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
    }

    private DataModel createNewProtocolPluggableService() {
        this.protocolPluggableService =
                new ProtocolPluggableServiceImpl(
                        this.ormService,
                        this.threadPrincipalService,
                        this.eventService,
                        this.nlsService,
                        this.issueService,
                        this.userService,
                        this.meteringService,
                        this.propertySpecService,
                        this.pluggableService,
                        this.customPropertySetService,
                        this.licenseService,
                        this.dataVaultService,
                        this.transactionService,
                        UpgradeModule.FakeUpgradeService.getInstance()
                        );
        this.protocolPluggableService.addInboundDeviceProtocolService(this.inboundDeviceProtocolService);
        this.protocolPluggableService.addConnectionTypeService(this.connectionTypeService);
        this.protocolPluggableService.addDeviceCacheMarshallingService(this.deviceCacheMarshallingService);
        this.protocolPluggableService.addDeviceProtocolSecurityService(this.deviceProtocolSecurityService);
        this.protocolPluggableService.addDeviceProtocolMessageService(this.deviceProtocolMessageService);
        this.protocolPluggableService.addDeviceProtocolService(this.deviceProtocolService);
        this.protocolPluggableService.addLicensedProtocolService(this.licensedProtocolService);
        return this.protocolPluggableService.getDataModel();
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public ProtocolPluggableServiceImpl getProtocolPluggableService() {
        return protocolPluggableService;
    }

    public DeviceProtocolMessageService getDeviceProtocolMessageService() {
        return deviceProtocolMessageService;
    }

    public IssueService getIssueService() {
        return issueService;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    public DeviceCacheMarshallingService getDeviceCacheMarshallingService() {
        return deviceCacheMarshallingService;
    }

    public DeviceProtocolSecurityService getDeviceProtocolSecurityService() {
        return deviceProtocolSecurityService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TimeService.class).toInstance(mock(TimeService.class));
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(CustomPropertySetService.class).toInstance(customPropertySetService);
            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(DeviceCacheMarshallingService.class).toInstance(deviceCacheMarshallingService);
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }

    }

}