package com.energyict.mdc.device.config.impl;

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
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private Injector injector;

    private ApplicationContext applicationContext;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private EngineModelService engineModelService;

    public void initializeDatabase(String testName, boolean showSqlLogging, boolean createMasterData) {
        this.initializeMocks(testName);
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(this.principal),
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
                new PluggableModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.ormService = injector.getInstance(OrmService.class);
            this.eventService = injector.getInstance(EventService.class);
            this.nlsService = injector.getInstance(NlsService.class);
            this.meteringService = injector.getInstance(MeteringService.class);
            this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            this.engineModelService = injector.getInstance(EngineModelService.class);
            injector.getInstance(PluggableService.class);
            this.dataModel = this.createNewDeviceConfigurationService(createMasterData);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(this.applicationContext);
    }

    private DataModel createNewDeviceConfigurationService(boolean createMasterData) {
        this.deviceConfigurationService = new DeviceConfigurationServiceImpl(this.ormService, this.eventService, this.nlsService, this.meteringService, this.protocolPluggableService, this.readingTypeUtilService, engineModelService, createMasterData);
        return this.deviceConfigurationService.getDataModel();
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

    public DeviceConfigurationServiceImpl getDeviceConfigurationService() {
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

    public Injector getInjector() {
        return injector;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

}