package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
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
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.impl.NextExecutionSpecsImpl;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import org.assertj.core.api.Condition;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PartialOutboundConnectiontaskCrudIT {

    private static final ComWindow COM_WINDOW = new ComWindow(3600, 7200);
    private OutboundComPortPool outboundComPortPool, outboundComPortPool1;
    private ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;

    @Rule
    public final TestRule thereIsNOOOORuleNumber6 = new ExpectedConstraintViolationRule();

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
    private SpyEventService eventService;
    private NlsService nlsService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;
    private SchedulingService schedulingService;
    private DataModel dataModel;
    private Injector injector;
    @Mock
    private ApplicationContext applicationContext;
    private PropertySpecServiceImpl propertySpecService;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private EngineModelService engineModelService;
    private InMemoryBootstrapModule bootstrapModule;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private InboundDeviceProtocolPluggableClass discoveryPluggable;
    @Mock
    private IdBusinessObjectFactory businessObjectFactory;
    @Mock
    private LicenseService licenseService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
            bind(EventService.class).to(SpyEventService.class).in(Scopes.SINGLETON);
        }

    }

    public void initializeDatabase(boolean showSqlLogging, boolean createMasterData) {
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
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
                new OrmModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new TasksModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new SchedulingModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            ormService = injector.getInstance(OrmService.class);
            eventService = (SpyEventService) injector.getInstance(EventService.class);
            nlsService = injector.getInstance(NlsService.class);
            propertySpecService = (PropertySpecServiceImpl) injector.getInstance(PropertySpecService.class);
            meteringService = injector.getInstance(MeteringService.class);
            readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            engineModelService = injector.getInstance(EngineModelService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            inboundDeviceProtocolService = injector.getInstance(InboundDeviceProtocolService.class);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            schedulingService = injector.getInstance(SchedulingService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(applicationContext);
        createOracleAliases();
    }

    @Before
    public void setUp() {
        when(principal.getName()).thenReturn("test");
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        when(applicationContext.findFactory(5011)).thenReturn(businessObjectFactory);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>absent());
        initializeDatabase(false, false);

        propertySpecService.addFactoryProvider(new ReferencePropertySpecFinderProvider() {
            @Override
            public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
                List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
                finders.add(new ConnectionMethodFinder());
                return finders;
            }
        });

        protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
        engineModelService = injector.getInstance(EngineModelService.class);

        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", NoParamsConnectionType.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", NoParamsConnectionType.class.getName());
            connectionTypePluggableClass2.save();
            discoveryPluggable = protocolPluggableService.newInboundDeviceProtocolPluggableClass("MyDiscoveryName", DlmsSerialNumberDiscover.class.getName());
            discoveryPluggable.save();
            outboundComPortPool = engineModelService.newOutboundComPortPool();
            outboundComPortPool.setActive(true);
            outboundComPortPool.setComPortType(ComPortType.TCP);
            outboundComPortPool.setName("inboundComPortPool");
            outboundComPortPool.setTaskExecutionTimeout(TimeDuration.minutes(15));
            outboundComPortPool.save();
            outboundComPortPool1 = engineModelService.newOutboundComPortPool();
            outboundComPortPool1.setActive(true);
            outboundComPortPool1.setComPortType(ComPortType.TCP);
            outboundComPortPool1.setName("inboundComPortPool2");
            outboundComPortPool1.setTaskExecutionTimeout(TimeDuration.minutes(5));
            outboundComPortPool1.save();
            context.commit();
        }
    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    public void testCreate() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)));
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");
        assertThat(partialOutboundConnectionTask.getRescheduleDelay()).isEqualTo(TimeDuration.seconds(60));
        assertThat(partialOutboundConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);

        verify(eventService.getSpy()).postEvent(EventType.PARTIAL_OUTBOUND_CONNECTION_TASK_CREATED.topic(), outboundConnectionTask);
    }

    @Test
    public void createDefaultWithoutDefaultTest() {
        PartialScheduledConnectionTaskImpl notTheDefault;
        PartialScheduledConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            notTheDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(false).build();
            theDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyDefault", connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.getPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.getPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault).isPresent();
        assertThat(foundTheDefault).isPresent();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    public void createDefaultWithAlreadyDefaultTest() {
        PartialScheduledConnectionTaskImpl notTheDefault;
        PartialScheduledConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            notTheDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(true).build();
            theDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyDefault", connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.getPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.getPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault).isPresent();
        assertThat(foundTheDefault).isPresent();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    public void testUpdate() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(new TimeDuration(10, TimeDuration.HOURS),new TimeDuration(1, TimeDuration.HOURS)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        ComWindow newComWindow = new ComWindow(7200, 10800);
        PartialScheduledConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
            task.setDefault(false);
            task.setComportPool(outboundComPortPool1);
            task.setConnectionTypePluggableClass(connectionTypePluggableClass2);
            task.setTemporalExpression(new TemporalExpression(new TimeDuration(12, TimeDuration.HOURS), new TimeDuration(2, TimeDuration.HOURS)));
            task.setComWindow(newComWindow);
            task.setName("Changed");
            task.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool1.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isFalse();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(newComWindow);
        assertThat(partialOutboundConnectionTask.getTemporalExpression().getEvery().getCount()).isEqualTo(12);
        assertThat(partialOutboundConnectionTask.getTemporalExpression().getEvery().getTimeUnitCode()).isEqualTo(TimeDuration.HOURS);
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("Changed");

        verify(eventService.getSpy()).postEvent(EventType.PARTIAL_OUTBOUND_CONNECTION_TASK_UPDATED.topic(), task);

    }

    @Test
    public void updateToDefaultWithoutCurrentDefaultTest() {
        PartialScheduledConnectionTaskImpl notTheDefault;
        PartialScheduledConnectionTaskImpl theToBeDefault;
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            notTheDefault = deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName1, connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(false).build();
            theToBeDefault = deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName2, connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .asDefault(false).build();
            deviceConfiguration.save();

            context.commit();
        }

        PartialScheduledConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
            task.setDefault(true);
            task.save();

            context.commit();
        }

        DeviceConfiguration reloadedDeviceConfig = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());
        PartialScheduledConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialScheduledConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    @Test
    public void updateToDefaultWithCurrentDefaultTest() {
        PartialScheduledConnectionTaskImpl temporaryDefault;
        PartialScheduledConnectionTaskImpl theToBeDefault;
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            temporaryDefault = deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName1, connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(true).build();
            theToBeDefault = deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName2, connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .asDefault(false).build();
            deviceConfiguration.save();

            context.commit();
        }

        PartialScheduledConnectionTask initialDefault = getConnectionTaskWithName(deviceConfiguration, connectionTaskName1);
        assertThat(initialDefault.isDefault()).isTrue();

        PartialScheduledConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
            task.setDefault(true);
            task.save();

            context.commit();
        }

        DeviceConfiguration reloadedDeviceConfig = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());
        PartialScheduledConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialScheduledConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    private PartialScheduledConnectionTask getConnectionTaskWithName(DeviceConfiguration deviceConfiguration, String connectionTaskName) {
        for (PartialScheduledConnectionTask partialScheduledConnectionTask : deviceConfiguration.getPartialOutboundConnectionTasks()) {
            if(partialScheduledConnectionTask.getName().equals(connectionTaskName)){
                return partialScheduledConnectionTask;
            }
        }
        return null;
    }

    @Test
    public void testUpdateNextExecutionSpecsWithMinimizeConnections() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.minutes(120), TimeDuration.minutes(60)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());

        PartialScheduledConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
            NextExecutionSpecsImpl instance = (NextExecutionSpecsImpl) schedulingService.newNextExecutionSpecs(null);
            instance.setTemporalExpression(new TemporalExpression(TimeDuration.minutes(60), TimeDuration.minutes(60)));
            instance.save();
            task.setNextExecutionSpecs(instance);
            task.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.minutes(60), TimeDuration.minutes(60)));

    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS + "}", property = "nextExecutionSpecs")
    public void testCanNotCreateConnectionTaskWithNextExecutionSpecIfAsSoonAsPossible() {

        try (TransactionContext context = transactionService.getContext()) {
            DeviceConfiguration deviceConfiguration;
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.minutes(120), TimeDuration.minutes(60)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
    }

    @Test
    public void testUpdateNextExecutionSpecsWithAsSoonAsPossible() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());

        PartialScheduledConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
            NextExecutionSpecsImpl instance = (NextExecutionSpecsImpl) schedulingService.newNextExecutionSpecs(null);
            instance.setTemporalExpression(new TemporalExpression(TimeDuration.minutes(60), TimeDuration.minutes(60)));
            instance.save();
            task.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNull();

    }

    @Test
    public void testDelete() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        PartialScheduledConnectionTask partialOutboundConnectionTask;
        try (TransactionContext context = transactionService.getContext()) {
            partialOutboundConnectionTask = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
            deviceConfiguration.remove(partialOutboundConnectionTask);
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isAbsent();

        verify(eventService.getSpy()).postEvent(EventType.PARTIAL_OUTBOUND_CONNECTION_TASK_DELETED.topic(), partialOutboundConnectionTask);

    }

    @Test
    public void testCreateReferencingInitiation() {

        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType;
        try (TransactionContext context = transactionService.getContext()) {
            deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            connectionInitiationTask = deviceConfiguration.newPartialConnectionInitiationTask("MyInitiation", connectionTypePluggableClass, TimeDuration.seconds(60))
                    .comPortPool(outboundComPortPool)
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .build();
            deviceConfiguration.save();

            context.commit();
        }

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        try (TransactionContext context = transactionService.getContext()) {

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .initiationTask(connectionInitiationTask)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)));
        assertThat(partialOutboundConnectionTask.getInitiatorTask().getId()).isEqualTo(connectionInitiationTask.getId());
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");

    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.CONNECTION_STRATEGY_REQUIRED + '}')
    public void testCreateWithoutConnectionStrategy() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), null)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS + '}')
    public void testCreateMinimizingConnectionsWithoutNextExecutionSpecs() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW + '}')
    public void testCreateWithNextExecutionSpecsOffsetNotWithinComWindowTest() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.hours(15), TimeDuration.hours(4)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.UNDER_MINIMUM_RESCHEDULE_DELAY + '}')
    public void testCreateWithTooLowReschedulingRetryDelayTest() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(59), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC + '}')
    public void testCreateWithNonSpeccedProperty() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("IPConnectionType", IpConnectionType.class.getName());
            connectionTypePluggableClass.save();
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .addProperty("NONSPECCED", 5)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }


    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE + '}')
    public void testCreateWithWrongValueForProperty() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("IPConnectionType", IpConnectionType.class.getName());
            connectionTypePluggableClass.save();
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .addProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, BigDecimal.valueOf(140024, 2))
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }


    }

    @Test
    public void testCreateWithCorrectProperty() {

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("IPConnectionType", IpConnectionType.class.getName());
            connectionTypePluggableClass.save();
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .comPortPool(outboundComPortPool)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .addProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, "127.0.0.1")
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        assertThat(typedProperties.hasValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo("127.0.0.1");

    }


    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {

    }

    public class ConnectionMethodFinder implements CanFindByLongPrimaryKey {

        @Override
        public FactoryIds factoryId() {
            return FactoryIds.CONNECTION_METHOD;
        }

        @Override
        public Class valueDomain() {
            return null;
        }

        @Override
        public Optional findByPrimaryKey(long id) {
            return null;
        }
    }

    private static void createOracleAliases() {
        try {
            try (PreparedStatement preparedStatement = Environment.DEFAULT.get().getConnection().prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_TABLES AS select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = Environment.DEFAULT.get().getConnection().prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_IND_COLUMNS AS select index_name, table_name, column_name, ordinal_position AS column_position from INFORMATION_SCHEMA.INDEXES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = Environment.DEFAULT.get().getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS USER_SEQUENCES ( SEQUENCE_NAME VARCHAR2 (30) NOT NULL, MIN_VALUE NUMBER, MAX_VALUE NUMBER, INCREMENT_BY NUMBER NOT NULL, CYCLE_FLAG VARCHAR2 (1), ORDER_FLAG VARCHAR2 (1), CACHE_SIZE NUMBER NOT NULL, LAST_NUMBER NUMBER NOT NULL)"
            )) {
                preparedStatement.execute();
            }
            Environment.DEFAULT.get().closeConnection();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }


}
