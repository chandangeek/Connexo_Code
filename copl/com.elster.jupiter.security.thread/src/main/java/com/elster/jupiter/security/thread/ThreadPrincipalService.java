package com.elster.jupiter.security.thread;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

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
     * Sets the given principal module and action as the current ones.
     *
     * @param principal
     * @param module
     * @param action
     */
    void set(Principal principal, String module, String action);

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
     */
    void runAs(Principal principal, Runnable runnable);


    void setEndToEndMetrics(Connection connection) throws SQLException;
}
