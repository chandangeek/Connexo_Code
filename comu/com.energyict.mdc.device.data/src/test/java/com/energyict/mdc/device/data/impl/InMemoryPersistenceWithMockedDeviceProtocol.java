package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bootstrap.h2.impl.ResultSetPrinter;
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
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:52
 */
public class InMemoryPersistenceWithMockedDeviceProtocol {

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    private final Clock clock;

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private MasterDataService masterDataService;
    private DeviceConfigurationService deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private ApplicationContext applicationContext;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private DeviceDataServiceImpl deviceService;
    private TaskService taskService;
    private SchedulingService schedulingService;


    public InMemoryPersistenceWithMockedDeviceProtocol() {
        this.clock = new DefaultClock();
    }

    public InMemoryPersistenceWithMockedDeviceProtocol(Clock clock) {
        super();
        this.clock = clock;
    }

    public void initializeDatabase(String testName, boolean showSqlLogging, boolean createMasterData) {
        this.initializeMocks(testName);
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MdcDynamicModule(),
                new TasksModule(),
                new PluggableModule(),
//                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MasterDataModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new SchedulingModule(),
                new DeviceDataModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistenceWithMockedDeviceProtocol.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(this.applicationContext);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.masterDataService = injector.getInstance(MasterDataService.class);
            this.taskService = injector.getInstance(TaskService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.schedulingService = injector.getInstance(SchedulingService.class);
            this.dataModel = this.createNewDeviceDataService(injector);
            ctx.commit();
        }
    }

    private DataModel createNewDeviceDataService(Injector injector) {
        deviceService = injector.getInstance(DeviceDataServiceImpl.class);
        return deviceService.getDataModel();
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
        this.protocolPluggableService = mock(ProtocolPluggableService.class);
        this.applicationContext = mock(ApplicationContext.class);
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(this.applicationContext.getTranslator()).thenReturn(translator);
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

    public MeteringService getMeteringService() {
        return meteringService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    public DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public ProtocolPluggableService getProtocolPluggableService() {
        return protocolPluggableService;
    }

    public MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public DeviceDataServiceImpl getDeviceService() {
        return deviceService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public SchedulingService getSchedulingService() {
        return schedulingService;
    }

    public static String query(String sql) {
        Connection connection = Environment.DEFAULT.get().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ResultSetPrinter(new PrintStream(out)).print(resultSet);
            return new String(out.toByteArray());
        } catch (SQLException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }

    public static String update(String sql) {
        Connection connection = Environment.DEFAULT.get().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int numberOfRows = statement.executeUpdate();
            return "Updated " + numberOfRows + " row(s).";
        } catch (SQLException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(JsonService.class).toInstance(new JsonServiceImpl());
            bind(BeanService.class).toInstance(new BeanServiceImpl());
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).to(MockProtocolPluggableService.class).in(Scopes.SINGLETON);;
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

    public MockProtocolPluggableService getMockProtocolPluggableService(){
        return (MockProtocolPluggableService) protocolPluggableService;
    }


    public static class MockProtocolPluggableService implements ProtocolPluggableService {

        private final ProtocolPluggableService protocolPluggableService;

        public ProtocolPluggableService getMockedProtocolPluggableService() {
            return protocolPluggableService;
        }

        @Inject
        private MockProtocolPluggableService(OrmService ormService, EventService eventService, NlsService nlsService, RelationService relationService, ConnectionTypeService connectionTypeService, InboundDeviceProtocolService inboundDeviceProtocolService, DeviceProtocolSecurityService deviceProtocolSecurityService, DeviceProtocolMessageService deviceProtocolMessageService, DeviceProtocolService deviceProtocolService, PluggableService pluggableService, PropertySpecService propertySpecService, IssueService issueService) {
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
        }

        @Override
        public List<LicensedProtocol> getAllLicensedProtocols() {
            return Collections.emptyList();
        }

        @Override
        public boolean isLicensedProtocolClassName(String javaClassName) {
            return false;
        }

        @Override
        public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            return null;
        }

        @Override
        public Class loadProtocolClass(String javaClassName) {
            return protocolPluggableService.loadProtocolClass(javaClassName);
        }

        @Override
        public Object createDeviceProtocolMessagesFor(String javaClassName) {
            return protocolPluggableService.createDeviceProtocolMessagesFor(javaClassName);
        }

        @Override
        public Object createDeviceProtocolSecurityFor(String javaClassName) {
            return protocolPluggableService.createDeviceProtocolSecurityFor(javaClassName);
        }

        @Override
        public Finder<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses() {
            return protocolPluggableService.findAllDeviceProtocolPluggableClasses();
        }

        @Override
        public DeviceProtocolPluggableClass findDeviceProtocolPluggableClass(long id) {
            return protocolPluggableService.findDeviceProtocolPluggableClass(id);
        }

        @Override
        public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name) {
            return protocolPluggableService.findDeviceProtocolPluggableClassByName(name);
        }

        @Override
        public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassesByClassName(String className) {
            return protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(className);
        }

        @Override
        public void deleteDeviceProtocolPluggableClass(long id) {
            protocolPluggableService.deleteDeviceProtocolPluggableClass(id);
        }

        @Override
        public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) {
            return protocolPluggableService.newDeviceProtocolPluggableClass(name, className);
        }

        @Override
        public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties typedProperties) {
            return protocolPluggableService.newDeviceProtocolPluggableClass(name, className, typedProperties);
        }

        @Override
        public List<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClassByClassName(String javaClassName) {
            return protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(javaClassName);
        }

        @Override
        public InboundDeviceProtocolPluggableClass findInboundDeviceProtocolPluggableClass(long id) {
            return protocolPluggableService.findInboundDeviceProtocolPluggableClass(id);
        }

        @Override
        public List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass() {
            return protocolPluggableService.findAllInboundDeviceProtocolPluggableClass();
        }

        @Override
        public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName) {
            return protocolPluggableService.newInboundDeviceProtocolPluggableClass(name, javaClassName);
        }

        @Override
        public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName, TypedProperties properties) {
            return protocolPluggableService.newInboundDeviceProtocolPluggableClass(name, javaClassName, properties);
        }

        @Override
        public void deleteInboundDeviceProtocolPluggableClass(long id) {
            protocolPluggableService.deleteInboundDeviceProtocolPluggableClass(id);
        }

        @Override
        public List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName) {
            return protocolPluggableService.findConnectionTypePluggableClassByClassName(javaClassName);
        }

        @Override
        public ConnectionTypePluggableClass findConnectionTypePluggableClassByName(String name) {
            return protocolPluggableService.findConnectionTypePluggableClassByName(name);
        }

        @Override
        public ConnectionTypePluggableClass findConnectionTypePluggableClass(long id) {
            return protocolPluggableService.findConnectionTypePluggableClass(id);
        }

        @Override
        public List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses() {
            return protocolPluggableService.findAllConnectionTypePluggableClasses();
        }

        @Override
        public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName) {
            return protocolPluggableService.newConnectionTypePluggableClass(name, javaClassName);
        }

        @Override
        public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName, TypedProperties properties) {
            return protocolPluggableService.newConnectionTypePluggableClass(name, javaClassName, properties);
        }

        @Override
        public String createOriginalAndConformRelationNameBasedOnJavaClassname(Class clazz) {
            return protocolPluggableService.createOriginalAndConformRelationNameBasedOnJavaClassname(clazz);
        }

        @Override
        public String createConformRelationTypeName(String name) {
            return protocolPluggableService.createConformRelationTypeName(name);
        }

        @Override
        public String createConformRelationAttributeName(String name) {
            return protocolPluggableService.createConformRelationAttributeName(name);
        }

        @Override
        public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName) {
            return protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(pluggableClass, dialectName);
        }

        @Override
        public boolean isDefaultAttribute(RelationAttributeType attributeType) {
            return protocolPluggableService.isDefaultAttribute(attributeType);
        }

        @Override
        public RelationType findSecurityPropertyRelationType(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            return protocolPluggableService.findSecurityPropertyRelationType(deviceProtocolPluggableClass);
        }

        @Override
        public DeviceProtocolCache unMarshalDeviceProtocolCache(String type, String jsonCache) {
            return protocolPluggableService.unMarshalDeviceProtocolCache(type, jsonCache);
        }

        @Override
        public String marshalDeviceProtocolCache(DeviceProtocolCache deviceProtocolCache) {
            return protocolPluggableService.marshalDeviceProtocolCache(deviceProtocolCache);
        }
    }

}