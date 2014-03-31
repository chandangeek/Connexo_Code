package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.LicenseServer;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialInboundConnectiontaskCrudIT {

    private InboundComPortPool inboundComPortPool, inboundComPortPool2;
    private ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;

    @Rule
    public final TestRule rule1 = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private Injector injector;
    @Mock
    private ApplicationContext applicationContext;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private EngineModelService engineModelService;
    private InMemoryBootstrapModule bootstrapModule;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private DeviceProtocolPluggableClass discoveryPluggable;
    @Mock
    private IdBusinessObjectFactory businessObjectFactory;
    @Mock
    private License license;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

    public void initializeDatabase(boolean showSqlLogging, boolean createMasterData) {
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new MdcReadingTypeUtilServiceModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new MdcDynamicModule(),
                new PluggableModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            ormService = injector.getInstance(OrmService.class);
            eventService = injector.getInstance(EventService.class);
            nlsService = injector.getInstance(NlsService.class);
            meteringService = injector.getInstance(MeteringService.class);
            readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            engineModelService = injector.getInstance(EngineModelService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            inboundDeviceProtocolService = injector.getInstance(InboundDeviceProtocolService.class);
            injector.getInstance(PluggableService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(applicationContext);
    }

    @Before
    public void setUp() {
        LicenseServer.licenseHolder.set(license);
        when(license.hasAllProtocols()).thenReturn(true);
        when(principal.getName()).thenReturn("test");
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        when(applicationContext.findFactory(5011)).thenReturn(businessObjectFactory);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());

        initializeDatabase(false, false);
        protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
        engineModelService = injector.getInstance(EngineModelService.class);

        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", NoParamsConnectionType.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", NoParamsConnectionType.class.getName());
            connectionTypePluggableClass2.save();
            discoveryPluggable = protocolPluggableService.newDeviceProtocolPluggableClass("MyDiscoveryName", MyDeviceProtocolPluggableClass.class.getName());
            discoveryPluggable.save();
            inboundComPortPool = engineModelService.newInboundComPortPool();
            inboundComPortPool.setActive(true);
            inboundComPortPool.setComPortType(ComPortType.TCP);
            inboundComPortPool.setName("inboundComPortPool");
            inboundComPortPool.setDiscoveryProtocolPluggableClass(discoveryPluggable);
            inboundComPortPool.save();
            inboundComPortPool2 = engineModelService.newInboundComPortPool();
            inboundComPortPool2.setActive(true);
            inboundComPortPool2.setComPortType(ComPortType.TCP);
            inboundComPortPool2.setName("inboundComPortPool2");
            inboundComPortPool2.setDiscoveryProtocolPluggableClass(discoveryPluggable);
            inboundComPortPool2.save();
            context.commit();
        }


    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
        LicenseServer.licenseHolder.set(null);
    }

    @Test
    public void testCreate() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.createPartialInboundConnectionTask()
                    .name("MyInbound")
                    .comPortPool(inboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        Optional<ServerPartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found).isPresent();

        ServerPartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl partialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(partialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool.getId());
        assertThat(partialInboundConnectionTask.isDefault()).isTrue();
        assertThat(partialInboundConnectionTask.getConfiguration().getId()).isEqualTo(communicationConfiguration.getId());
        assertThat(partialInboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("MyInbound");

    }

    @Test
    public void testUpdate() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.createPartialInboundConnectionTask()
                    .name("MyInbound")
                    .comPortPool(inboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            DeviceCommunicationConfiguration configuration = deviceConfigurationService.findDeviceConfigurationService(communicationConfiguration.getId());
            PartialInboundConnectionTask partialInboundConnectionTask = configuration.getPartialInboundConnectionTasks().get(0);
            partialInboundConnectionTask.setDefault(false);
            partialInboundConnectionTask.setComportPool(inboundComPortPool2);
            partialInboundConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
            partialInboundConnectionTask.setName("Changed");
            partialInboundConnectionTask.save();

            context.commit();
        }

        Optional<ServerPartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found).isPresent();

        ServerPartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl partialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(partialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool2.getId());
        assertThat(partialInboundConnectionTask.isDefault()).isFalse();
        assertThat(partialInboundConnectionTask.getConfiguration().getId()).isEqualTo(communicationConfiguration.getId());
        assertThat(partialInboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("Changed");

    }

    @Test
    public void testDelete() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.createPartialInboundConnectionTask()
                    .name("MyInbound")
                    .comPortPool(inboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            DeviceCommunicationConfiguration configuration = deviceConfigurationService.findDeviceConfigurationService(communicationConfiguration.getId());
            PartialInboundConnectionTask partialInboundConnectionTask = configuration.getPartialInboundConnectionTasks().get(0);
            configuration.remove(partialInboundConnectionTask);
            configuration.save();

            context.commit();
        }

        Optional<ServerPartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found).isAbsent();

    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Constants.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC_KEY + '}')
    public void testCreateWithUnspeccedProperty() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.createPartialInboundConnectionTask()
                    .name("MyInbound")
                    .comPortPool(inboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .asDefault(true)
                    .addProperty("unspecced", true)
                    .build();
            communicationConfiguration.save();

            context.commit();
        }
    }

    @Test(expected = DuplicateNameException.class)
//    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Constants.PARTIAL_CONNECTION_TASK_DUPLICATE_KEY + '}')
    public void testCreateWithDuplicateName() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.createPartialInboundConnectionTask()
                    .name("MyInbound")
                    .comPortPool(inboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .asDefault(true).build();
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.createPartialInboundConnectionTask()
                    .name("MyInbound")
                    .comPortPool(inboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }


    }


    public static class MyDeviceProtocolPluggableClass implements DeviceProtocolPluggableClass, DeviceProtocol {

        @Override
        public void setPropertySpecService(PropertySpecService propertySpecService) {
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public String getSecurityRelationTypeName() {
            return null;
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getSecurityPropertySpec(String name) {
            return null;
        }

        @Override
        public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

        }

        @Override
        public void terminate() {

        }

        @Override
        public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
            return Collections.emptyList();
        }

        @Override
        public String getProtocolDescription() {
            return null;
        }

        @Override
        public DeviceFunction getDeviceFunction() {
            return null;
        }

        @Override
        public ManufacturerInformation getManufacturerInformation() {
            return null;
        }

        @Override
        public List<ConnectionType> getSupportedConnectionTypes() {
            return Collections.emptyList();
        }

        @Override
        public void logOn() {

        }

        @Override
        public void daisyChainedLogOn() {
        }

        @Override
        public void logOff() {

        }

        @Override
        public void daisyChainedLogOff() {
        }

        @Override
        public String getSerialNumber() {
            return null;
        }

        @Override
        public void setTime(Date timeToSet) {

        }

        @Override
        public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
            return Collections.emptyList();
        }

        @Override
        public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
            return null;
        }

        @Override
        public Date getTime() {
            return null;
        }

        @Override
        public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

        }

        @Override
        public DeviceProtocolCache getDeviceCache() {
            return null;
        }

        @Override
        public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
            return null;
        }

        @Override
        public List<DeviceMessageSpec> getSupportedMessages() {
            return Collections.emptyList();
        }

        @Override
        public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
            return null;
        }

        @Override
        public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
            return null;
        }

        @Override
        public String format(PropertySpec propertySpec, Object messageAttribute) {
            return null;
        }

        @Override
        public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
            return Collections.emptyList();
        }

        @Override
        public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public void copyProperties(TypedProperties properties) {
        }

        @Override
        public DeviceProtocol getDeviceProtocol() {
            return this;
        }

        @Override
        public TypedProperties getProperties() {
            return TypedProperties.empty();
        }

        @Override
        public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
            return Collections.emptyList();
        }

        @Override
        public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

        }

        @Override
        public CollectedTopology getDeviceTopology() {
            return null;
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setName(String name) throws BusinessException {

        }

        @Override
        public PluggableClassType getPluggableClassType() {
            return null;
        }

        @Override
        public String getJavaClassName() {
            return null;
        }

        @Override
        public Date getModificationDate() {
            return null;
        }

        @Override
        public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
            return null;
        }

        @Override
        public void setProperty(PropertySpec propertySpec, Object value) {

        }

        @Override
        public void removeProperty(PropertySpec propertySpec) {

        }

        @Override
        public void save() {
        }

        @Override
        public void delete() {
        }
    }

}
