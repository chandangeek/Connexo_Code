package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * General interface for upgrade tasks.
 *
 * @author Steven Willems.
 * @since Jun 18, 2009.
 */
public interface Upgrader {

    /**
     * Check if the application should be upgraded. Implementations may evaluate once, and return the same result for subsequent calls without re-evaluating, unless a rest() was called().
     *
     * @return true if the application needs an update, false otherwise.
     */
    boolean needsUpgrade();


    /**
     * Perform the update. <em>The implementor should always check if the the upgrade
     * should actually do something. It is possible this method is called, even
     * when needsUpgrade or canUpgrade both return false.</em>
     *
     * @throws SQLException      Some SQL exception occurred.
     * @throws BusinessException Some Businnes logic failed.
     */
    void upgrade() throws SQLException, BusinessException;

    /**
     * Returns a warning message that should be displayed before executing the upgrade
     *
     * @return a warning message that should be displayed before executing the upgrade, or null in case there is none
     */
    String getWarningMessage();

    /**
     * Indicates to this Upgrader that a system change has occurred that may affect the need to upgrade. The next call to needsUpgrade() must re-evaluate the need to upgrade, and cannot return a cached result.
     */
    void reset();
}
