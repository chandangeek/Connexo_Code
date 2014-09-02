package com.energyict.mdc.common;

import java.sql.SQLException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-18 (16:03)
 */
public interface Environment extends FormatProvider {

    public static final AtomicReference<Environment> DEFAULT = new AtomicReference<>();

    public Object get (String name, boolean global);

    public Object get (String name);

    public void put (String name, Object value, boolean global);

    public void close ();

    public String getProperty (String key, String defaultValue);

    public String getProperty (String key);

    public FormatPreferences getFormatPreferences ();

    public DecimalFormatSymbols getDecimalFormatSymbols ();

    public char getDecimalSeparator ();

    public char getGroupingSeparator ();

    public void signalEvent (BusinessEvent event) throws BusinessException, SQLException;

    public ApplicationContext getApplicationContext ();

    public void setApplicationContext (ApplicationContext context);

    public Locale getLocale ();

}