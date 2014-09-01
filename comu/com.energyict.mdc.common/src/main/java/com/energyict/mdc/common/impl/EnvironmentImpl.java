package com.energyict.mdc.common.impl;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessEvent;
import com.energyict.mdc.common.BusinessEventManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryFinder;
import com.energyict.mdc.common.FormatPreferences;
import com.energyict.mdc.common.Transaction;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

@Component(name="com.energyict.mdc.environment", service = Environment.class)
public class EnvironmentImpl implements Environment {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentImpl.class.getName());

    /**
     * The name of the key that will be used to find the {@link ApplicationContext}
     * in the map that holds the (globally) registered objects.
     */
    private static final String APPLICATIONCONTEXT = "APPLICATIONCONTEXT";

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

    @SuppressWarnings("unused")
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

    private FactoryFinder findFactoryFinder () {
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

    private BusinessEventManager getEventManager () {
        return this.eventManagerHolder.get();
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
    public Locale getLocale () {
        ApplicationContext context = this.getApplicationContext();
        if (context == null) {
            return Locale.getDefault();
        }
        else {
            return context.getLocale();
        }

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

}