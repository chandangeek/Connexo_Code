package com.energyict.mdc.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-18 (16:03)
 */
public interface Environment extends Translator, FormatProvider {

    public static final AtomicReference<Environment> DEFAULT = new AtomicReference<>();

    public void reloadEventManager ();

    public Object get (String name, boolean global);

    public Object get (String name);

    public void put (String name, Object value, boolean global);

    public void closeConnection ();

    public void close ();

    public Connection getConnection ();

    public Connection getUnwrappedConnection ();

    public String getProperty (String key, String defaultValue);

    public String getProperty (String key);

    public <T> T execute (Transaction<T> transaction) throws BusinessException, SQLException;

    public boolean isInTransaction ();

    public void clearCache ();

    public Translator getTranslator ();

    public FactoryFinder findFactoryFinder ();

    public BusinessObjectFactory findFactory (String name);

    public BusinessObjectFactory findFactory (int factoryId);

    public FormatPreferences getFormatPreferences ();

    public DecimalFormatSymbols getDecimalFormatSymbols ();

    public char getDecimalSeparator ();

    public char getGroupingSeparator ();

    public boolean useOraLobs ();

    public String getTranslation (String key);

    public String getTranslation (String key, boolean flagError);

    public String getErrorMsg (String key);

    public String getCustomTranslation (String key);

    @Override
    public String getErrorCode (String messageId);

    public String getTranslation (String key, String defaultValue);

    public boolean hasTranslation (String key);

    public BusinessEventManager getEventManager ();

    public void setEventManager (BusinessEventManager eventManager);

    public void signalEvent (BusinessEvent event) throws BusinessException, SQLException;

    public ApplicationContext getApplicationContext ();

    public void setApplicationContext (ApplicationContext context);

    public void clearApplicationContext ();

    public Locale getLocale ();

    public ApplicationComponent getComponent (String name);

    public void preferencesChange ();

    public JmsSessionContext getJmsSessionContext ();

    public Date getDatabaseTime ();

    public int getBatchSize ();

    public void registerFinder (CanFindByLongPrimaryKey finder);

    public CanFindByLongPrimaryKey finderFor(Class valueDomain);

    public CanFindByLongPrimaryKey finderFor(FactoryIds registrationKey);

}