package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolMessageAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.SecondSimpleTestMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.SimpleTestMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.ThirdSimpleTestMeterProtocol;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.*;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Provides initialization services that is typically used by classes that focus
 * on testing the correct implementation of the persistence aspects of entities in this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (09:57)
 */
public class InMemoryPersistence {

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private DataModel dataModel;

    private ConnectionTypeService connectionTypeService;
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    private DeviceProtocolMessageService deviceProtocolMessageService;
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    private DeviceProtocolService deviceProtocolService;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private LicensedProtocolService licensedProtocolService;
    private IssueService issueService;
    private PropertySpecService propertySpecService;
    private ApplicationContext applicationContext;
    private PluggableService pluggableService;
    private RelationService relationService;

    private ProtocolPluggableServiceImpl protocolPluggableService;

    public void initializeDatabase (String testName, DataModelInitializer... dataModelInitializers) {
        this.initializeMocks(testName);
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new IssuesModule(),
                new PluggableModule(),
                new MdcCommonModule(),
                new MdcDynamicModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            this.pluggableService = injector.getInstance(PluggableService.class);
            this.relationService = injector.getInstance(RelationService.class);
            this.issueService = injector.getInstance(IssueService.class);
            this.dataModel = this.createNewProtocolPluggableService();
            for (DataModelInitializer initializer : dataModelInitializers) {
                initializer.initializeDataModel(this.dataModel);
            }
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(this.applicationContext);
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
        this.principal = mock(Principal.class);
        when(this.principal.getName()).thenReturn(testName);
        this.propertySpecService = mock(PropertySpecService.class);
        this.pluggableService = mock(PluggableService.class);
        this.relationService = mock(RelationService.class);
        this.deviceProtocolService = mock(DeviceProtocolService.class);
        this.deviceProtocolMessageService = mock(DeviceProtocolMessageService.class);
        this.deviceProtocolSecurityService = mock(DeviceProtocolSecurityService.class);
        this.inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        this.connectionTypeService = mock(ConnectionTypeService.class);
        this.licensedProtocolService = mock(LicensedProtocolService.class);
        this.legacySecurityPropertyConverter = mock(LegacySecurityPropertyConverter.class);
        this.applicationContext = mock(ApplicationContext.class);
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(this.applicationContext.getTranslator()).thenReturn(translator);
    }

    private DataModel createNewProtocolPluggableService() {
        this.protocolPluggableService =
                new ProtocolPluggableServiceImpl(
                        this.ormService,
                        this.eventService,
                        this.nlsService,
                        this.issueService,
                        this.propertySpecService,
                        this.pluggableService,
                        this.relationService,
                        this.deviceProtocolService,
                        this.deviceProtocolMessageService,
                        this.deviceProtocolSecurityService,
                        this.inboundDeviceProtocolService,
                        this.connectionTypeService);
        return this.protocolPluggableService.getDataModel();
    }

    public void cleanUpDataBase() throws SQLException {
        Environment environment = Environment.DEFAULT.get();
        if (environment != null) {
            Object bootstrapModule = environment.get(JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME);
            if (bootstrapModule != null) {
                deactivate(bootstrapModule);
            }
        }
    }

    private void deactivate(Object bootstrapModule) {
        if (bootstrapModule instanceof InMemoryBootstrapModule) {
            InMemoryBootstrapModule inMemoryBootstrapModule = (InMemoryBootstrapModule) bootstrapModule;
            inMemoryBootstrapModule.deactivate();
        }
    }

    public ProtocolPluggableServiceImpl getProtocolPluggableService() {
        return protocolPluggableService;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public DeviceProtocolMessageService getDeviceProtocolMessageService() {
        return deviceProtocolMessageService;
    }

    public IssueService getIssueService() {
        return issueService;
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
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

}