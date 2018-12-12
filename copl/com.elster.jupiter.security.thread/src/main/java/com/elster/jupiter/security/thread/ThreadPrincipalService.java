/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.security.thread;

import aQute.bnd.annotation.ProviderType;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

@ProviderType
public interface ThreadPrincipalService {

    /**
     * @return the current Principal
     */
    Principal getPrincipal();

    /**
     * @return the current module
     */
    String getModule();

    /**
     * @return the current action
     */
    String getAction();

    /**
     * @return the current locale
     */
    Locale getLocale();

    /**
     * @return the application name
     */
    String getApplicationName();

    /**
     * Sets the given application name as the current one.
     *
     * @param applicationName
     */
    void setApplicationName(String applicationName);

    /**
     * Sets the given principal module and action as the current ones.
     *
     * @param principal
     * @param module
     * @param action
     * @param locale
     */
    void set(Principal principal, String module, String action, Locale locale);

    /**
     * Sets the given principal as the current one.
     *
     * @param principal
     */
    void set(Principal principal);

    /**
     * Sets the given module and action as the current ones.
     *
     * @param module
     * @param action
     */
    void set(String module, String action);

    /**
     * Clears the security context.
     */
    void clear();

    /**
     * Runs the given Runnable as the given Principal.
     * @param principal
     * @param runnable
     * @param locale
     */
    void runAs(Principal principal, Runnable runnable, Locale locale);


    void setEndToEndMetrics(Connection connection) throws SQLException;

    Runnable withContextAdded(Runnable runnable, Principal principal);

    Runnable withContextAdded(Runnable runnable, Principal principal, String module, String action, Locale locale);
}
