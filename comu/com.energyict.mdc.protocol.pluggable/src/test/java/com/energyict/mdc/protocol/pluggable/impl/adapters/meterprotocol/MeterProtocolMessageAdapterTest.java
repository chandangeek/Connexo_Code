package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
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
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.MessageProtocol;
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
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link MeterProtocolMessageAdapter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:05
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolMessageAdapterTest {

    @Mock
    private MessageAdapterMappingFactory messageAdapterMappingFactory;
    @Mock
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private LicensedProtocolService licensedProtocolService;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;

    private ProtocolPluggableService protocolPluggableService;
    private DataModel dataModel;
    private IssueService issueService;

    @Before
    public void before () {
        when(this.principal.getName()).thenReturn("MeterProtocolMessageAdapterTest.mdc.protocol.pluggable");
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
                new MdcDynamicModule(),
                new ProtocolPluggableModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(OrmService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.dataModel = ((ProtocolPluggableServiceImpl) protocolPluggableService).getDataModel();
            this.issueService = injector.getInstance(IssueService.class);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        environment.setApplicationContext(applicationContext);
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        new InMemoryPersistence().cleanUpDataBase();
    }

    @Before
    public void initializeMocks () {
        when(this.deviceProtocolMessageService.createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter")).
                thenReturn(new SimpleLegacyMessageConverter());
        doThrow(DeviceProtocolAdapterCodingExceptions.class).when(this.deviceProtocolMessageService).createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
    }

    @Before
    public void initializeMessageAdapterMappingFactory() {
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.meterprotocol.SimpleTestMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.common.SimpleLegacyMessageConverter");
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.meterprotocol.SecondSimpleTestMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
        when(messageAdapterMappingFactory.getMessageMappingJavaClassNameForDeviceProtocol(
                "com.energyict.comserver.adapters.meterprotocol.ThirdSimpleTestMeterProtocol"))
                .thenReturn("com.energyict.comserver.adapters.meterprotocol.ThirdSimpleTestMeterProtocol");
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        new MeterProtocolMessageAdapter(simpleTestMeterProtocol, this.dataModel, this.protocolPluggableService, this.issueService);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        MeterProtocol meterProtocol = mock(MeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new MeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService, this.issueService);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!e.getMessageId().equals("CSC-DEV-124")) {
                fail("Exception should have indicated that the given MeterProtocol is not known in the adapter mapping, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotAMessageSupportClass() {
        MeterProtocol meterProtocol = new ThirdSimpleTestMeterProtocol();
        final MeterProtocolMessageAdapter protocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol, this.dataModel, this.protocolPluggableService, this.issueService);

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.format(null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
            bind(MessageAdapterMappingFactory.class).toInstance(messageAdapterMappingFactory);
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