package com.energyict.mdc.common.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessEvent;
import com.energyict.mdc.common.BusinessEventManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.CacheHolder;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryFinder;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.FormatPreferences;
import com.energyict.mdc.common.JmsSessionContext;
import com.energyict.mdc.common.MultiBundleTranslator;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.Translator;
import oracle.jdbc.OracleConnection;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

@Component(name="com.energyict.mdc.environment", service = Environment.class)
public class EnvironmentImpl implements Environment {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentImpl.class.getName());

    /**
     * The name of the key that will be used to find the {@link ApplicationContext}
     * in the map that holds the (globally) registered objects.
     */
    private static final String APPLICATIONCONTEXT = "APPLICATIONCONTEXT";
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * The objects that are globally shared and made available by name.
     */
    private NamedObjects globalNamedObjects = new GlobalNamedObjects();

    /**
     * The objects that are shared within the currrent thread and made available by name.
     */
    private ThreadLocal<NamedObjects> localNamedObjectsHolder;

    /**
     * The {@link TransactionContext} that is forced to be unique per thread.
     */
    private ThreadLocal<TransactionContext> transactionContextHolder = new ThreadLocal<>();

    /**
     * The {@link BusinessEventManager} that is forced to be unique per thread.
     */
    private ThreadLocal<BusinessEventManager> eventManagerHolder;

    /**
     * The Jupiter DataSource from which Connections are acquired.
     */
    private volatile DataSource dataSource;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BundleContext context;
    private volatile Map<FactoryIds, CanFindByLongPrimaryKey> findersById = new HashMap<>();
    private volatile Map<Class, CanFindByLongPrimaryKey> findersByClass = new HashMap<>();

    public EnvironmentImpl () {
        this.localNamedObjectsHolder = new ThreadLocal<NamedObjects>() {
            protected NamedObjects initialValue () {
                return new LocalNamedObjects();
            }
        };
        this.startEventManager();
    }

    @Inject
    public EnvironmentImpl(DataSource dataSource, TransactionService transactionService, ThreadPrincipalService threadPrincipalService, BundleContext bundleContext) {
        this();
        this.dataSource = dataSource;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.activate(bundleContext);
    }

    private void startEventManager () {
        this.eventManagerHolder = new ThreadLocal<BusinessEventManager>() {
            protected BusinessEventManager initialValue () {
                return getApplicationContext().createEventManager();
            }
        };
    }

    public DataSource getDataSource () {
        return dataSource;
    }

    @Reference
    public void setDataSource (DataSource dataSource) {
        LOGGER.fine("DataSource is being injected into the MDC environment: " + dataSource);
        this.dataSource = dataSource;
    }

    public TransactionService getTransactionService () {
        return transactionService;
    }

    @Reference
    public void setTransactionService (TransactionService transactionService) {
        LOGGER.fine("Transaction service is being injected into the MDC environment: " + transactionService);
        this.transactionService = transactionService;
    }

    public ThreadPrincipalService getThreadPrincipalService () {
        return threadPrincipalService;
    }

    @Reference
    public void setThreadPrincipalService (ThreadPrincipalService threadPrincipalService) {
        LOGGER.fine("Thread principle service is being injected into the MDC environment: " + threadPrincipalService);
        this.threadPrincipalService = threadPrincipalService;
    }

    @Activate
    public void activate (BundleContext context) {
        try {
            this.context = context;
            Environment.DEFAULT.set(this);
            LOGGER.fine("MDC environment is actived");
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Deactivate
    public void deactivate () {
        Environment.DEFAULT.set(null);
        this.context = null;
        LOGGER.fine("MDC environment is deactived");
    }

    @Override
    public void reloadEventManager () {
        this.startEventManager();
    }

    @Override
    public Object get (String name, boolean global) {
        if (global) {
            return this.globalNamedObjects.get(name);
        }
        else {
            return this.localNamedObjectsHolder.get().get(name);
        }
    }

    @Override
    public Object get (String name) {
        Object result = get(name, false);
        if (result == null) {
            result = get(name, true);
        }
        return result;
    }

    @Override
    public void put (String name, Object value, boolean global) {
        if (global) {
            this.globalNamedObjects.put(name, value);
        }
        else {
            this.localNamedObjectsHolder.get().put(name, value);
        }
    }

    @Override
    public void closeConnection () {
        if (!this.isInTransaction()) {
            this.getTransactionContext().closeConnection();
        }
    }

    @Override
    public void close () {
        this.getTransactionContext().close(this.getEventManager());
        this.transactionContextHolder.remove();
        if (this.eventManagerHolder != null) {
            this.eventManagerHolder.remove();
        }
        if (this.localNamedObjectsHolder != null) {
            this.localNamedObjectsHolder.remove();
        }
        if (this.globalNamedObjects != null) {
            this.globalNamedObjects.clear();
        }
    }

    @Override
    public Connection getConnection () {
        return this.getTransactionContext().getConnection();
    }

    @Override
    public Connection getUnwrappedConnection () {
        return new ConnectionUnwrapper().unwrap(this.getConnection());
    }

    @Override
    public String getProperty (String key, String defaultValue) {
        String value = this.getProperty(key);
        if (this.isEmpty(value)) {
            return defaultValue;
        }
        else {
            return value;
        }
    }

    private boolean isEmpty (String value) {
        return value == null || value.isEmpty();
    }

    @Override
    public String getProperty (String key) {
        return this.context.getProperty(key);
    }

    @Override
    public <T> T execute (Transaction<T> transaction) throws BusinessException, SQLException {
        if (!this.isInTransaction()) {
            /* Close the connection that was used outside of the transaction context
             * to avoid Connection leakage.
             * In the Jupiter context, a new Connection is created
             * once a transaction context is setup. */
            getTransactionContext().closeConnection();
        }
        return getTransactionContext().execute(transaction, this.getTransactionService(), this.getEventManager());
    }

    @Override
    public boolean isInTransaction () {
        TransactionContext transactionContext = this.transactionContextHolder.get();
        return transactionContext != null && !transactionContext.isFinished();
    }

    @Override
    public void clearCache () {
        for (CacheHolder each : this.getCacheHolders()) {
            each.clearCache();
        }
    }

    private Collection<CacheHolder> getCacheHolders () {
        Collection<CacheHolder> cacheHolders = new ArrayList<>();
        this.collectCacheHolders(cacheHolders, this.localNamedObjectsHolder.get().values());
        this.collectCacheHolders(cacheHolders, this.globalNamedObjects.values());
        return cacheHolders;
    }

    private void collectCacheHolders (Collection<CacheHolder> cacheHolders, Iterable<Object> objects) {
        for (Object each : objects) {
            if (each instanceof CacheHolder) {
                cacheHolders.add((CacheHolder) each);
            }
        }
    }

    @Override
    public Translator getTranslator () {
        ApplicationContext context = getApplicationContext();
        if (context != null) {
            return context.getTranslator();
        }
        else {
            return new MultiBundleTranslator(Locale.getDefault());
        }
    }

    @Override
    public FactoryFinder findFactoryFinder () {
        return getApplicationContext();
    }

    @Override
    public BusinessObjectFactory findFactory (String name) {
        return findFactoryFinder().findFactory(name);
    }

    @Override
    public BusinessObjectFactory findFactory (int factoryId) {
        return findFactoryFinder().findFactory(factoryId);
    }

    @Override
    public FormatPreferences getFormatPreferences () {
        ApplicationContext context = getApplicationContext();
        if (context == null) {
            return new FormatPreferences();
        }
        else {
            return context.getFormatPreferences();
        }
    }

    @Override
    public DecimalFormatSymbols getDecimalFormatSymbols () {
        return getFormatPreferences().getDecimalFormatSymbols();
    }

    @Override
    public char getDecimalSeparator () {
        return getDecimalFormatSymbols().getDecimalSeparator();
    }

    @Override
    public char getGroupingSeparator () {
        return getDecimalFormatSymbols().getGroupingSeparator();
    }

    @Override
    public boolean useOraLobs () {
        // Use configuration support for OSGi services
        return false;
    }

    @Override
    public String getTranslation (String key) {
        return getTranslator().getTranslation(key);
    }

    @Override
    public String getTranslation (String key, boolean flagError) {
        return getTranslator().getTranslation(key, flagError);
    }

    @Override
    public String getErrorMsg (String key) {
        return getTranslator().getErrorMsg(key);
    }

    @Override
    public String getCustomTranslation (String key) {
        return getTranslator().getCustomTranslation(key);
    }

    @Override
    public String getErrorCode (String messageId) {
        return getTranslator().getErrorCode(messageId);
    }

    @Override
    public String getTranslation (String key, String defaultValue) {
        return getTranslator().getTranslation(key, defaultValue);
    }

    @Override
    public boolean hasTranslation (String key) {
        return getTranslator().hasTranslation(key);
    }

    public static void writeSeal (Properties properties, File file) throws IOException {
        Configuration.writeSeal(properties, file);
    }

    private TransactionContext getTransactionContext () {
        TransactionContext transactionContext = this.transactionContextHolder.get();
        if (transactionContext == null) {
            transactionContext = this.newTransactionContext();
            transactionContextHolder.set(transactionContext);
        }
        return transactionContext;
    }

    private TransactionContext newTransactionContext () {
        return new TransactionContext(this.dataSource);
    }

    @Override
    public BusinessEventManager getEventManager () {
        return this.eventManagerHolder.get();
    }

    @Override
    public void setEventManager (BusinessEventManager eventManager) {
        this.eventManagerHolder.set(eventManager);
    }

    @Override
    public void signalEvent (BusinessEvent event) throws BusinessException, SQLException {
        BusinessEventManager manager = getEventManager();
        if (manager != null) {
            manager.signalEvent(event);
        }
    }

    @Override
    public ApplicationContext getApplicationContext () {
        return (ApplicationContext) get(APPLICATIONCONTEXT);
    }

    @Override
    public void setApplicationContext (ApplicationContext context) {
        put(APPLICATIONCONTEXT, context, context.isGlobal());
    }

    @Override
    public void clearApplicationContext () {
        put(APPLICATIONCONTEXT, null, false);
        put(APPLICATIONCONTEXT, null, true);
    }

    @Override
    public Locale getLocale () {
        ApplicationContext context = this.getApplicationContext();
        if (context == null) {
            return Locale.getDefault();
        }
        else {
            return context.getLocale();
        }

    }

    @Override
    public ApplicationComponent getComponent (String name) {
        ApplicationContext context = this.getApplicationContext();
        if (context == null) {
            return null;
        }
        else {
            return context.getComponent(name);
        }
    }

    @Override
    public void preferencesChange () {
        ApplicationContext context = this.getApplicationContext();
        if (context != null) {
            context.preferencesChange();
        }
    }

    @Override
    public JmsSessionContext getJmsSessionContext () {
        return getTransactionContext().getJmsSessionContext();
    }

    @Override
    public Date getDatabaseTime () {
        try {
            ResultSet resultSet = null;
            try (PreparedStatement statement = this.getConnection().prepareStatement("select systimestamp from dual")) {
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getTimestamp(1);
                }
                else {
                    throw new ApplicationException("getDatabaseTime unexpected error");
                }
            }
            finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
        catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getBatchSize () {
        return DEFAULT_BATCH_SIZE;
    }

    private interface NamedObjects {

        public Object get (String key);

        public void put (String key, Object value);

        public Iterable<Object> values ();

        public void clear ();

    }

    private abstract class NamedObjectsImpl implements NamedObjects {
        private Map<String, Object> backingMap;

        protected NamedObjectsImpl (Map<String, Object> backingMap) {
            super();
            this.backingMap = backingMap;
        }

        @Override
        public Object get (String key) {
            return this.backingMap.get(key);
        }

        @Override
        public void put (String key, Object value) {
            this.backingMap.put(key, value);
        }

        @Override
        public Iterable<Object> values () {
            return this.backingMap.values();
        }

        @Override
        public void clear () {
            this.backingMap.clear();
        }

    }

    private class LocalNamedObjects extends NamedObjectsImpl {
        protected LocalNamedObjects () {
            super(new HashMap<String, Object>());
        }
    }

    private class GlobalNamedObjects extends NamedObjectsImpl {
        protected GlobalNamedObjects () {
            super(Collections.synchronizedMap(new HashMap<String, Object>()));
        }
    }

    @Override
    public void registerFinder(CanFindByLongPrimaryKey finder) {
        this.findersById.put(finder.registrationKey(), finder);
        this.findersByClass.put(finder.valueDomain(), finder);
    }

    @Override
    public CanFindByLongPrimaryKey finderFor(Class valueDomain) {
        return this.findersByClass.get(valueDomain);
    }

    @Override
    public CanFindByLongPrimaryKey finderFor(FactoryIds registrationKey) {
        return this.findersById.get(registrationKey);
    }

    private class ConnectionUnwrapper {

        public OracleConnection unwrap (Connection connection) {
            if (connection == null) {
                return null;
            }
            if (connection instanceof OracleConnection) {
                return (OracleConnection) connection;
            }
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                if (metaData != null) {
                    Connection metaDataConnection = metaData.getConnection();
                    OracleConnection unwrapped = this.unwrap(metaDataConnection);
                    if (unwrapped != null) {
                        return unwrapped;
                    }
                }
                else {
                /* No metadata provided, try to find a related connection
                 * via getter methods and unwrap the result. */
                    Connection related = this.findWrappedConnection(connection);
                    if (related != null) {
                        return this.unwrap(related);
                    }
                }
            }
            catch (SQLException e) {
                throw new ApplicationException("Cannot unwrap connection");
            }
            throw new ApplicationException("Cannot unwrap connection");
        }

        private Connection findWrappedConnection (Connection connection) {
            for (Method method : this.getConnectionGetterMethods(connection)) {
                try {
                    return (java.sql.Connection) (method.invoke(connection));
                }
                catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                    // Happy to ignore this and continue with the other getter methods
                }
            }
            return null;
        }

        private List<Method> getConnectionGetterMethods (Connection connection) {
            List<Method> methods = new ArrayList<>();
            for (Method method : connection.getClass().getMethods()) {
                if (this.isConnectionGetterMethod(method)) {
                    methods.add(method);
                }
            }
            return methods;
        }

        private boolean isConnectionGetterMethod (Method method) {
            return method.getReturnType().isAssignableFrom(Connection.class)
                    && method.getParameterTypes().length == 0;
        }

    }

}