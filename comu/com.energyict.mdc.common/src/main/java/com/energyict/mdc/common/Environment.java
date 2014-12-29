package com.energyict.mdc.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-18 (16:03)
 */
public interface Environment {

    public static final AtomicReference<Environment> DEFAULT = new AtomicReference<>();

    public Object get (String name, boolean global);

    public Object get (String name);

    public void put (String name, Object value, boolean global);

    public String getProperty (String key, String defaultValue);

    public String getProperty (String key);

}