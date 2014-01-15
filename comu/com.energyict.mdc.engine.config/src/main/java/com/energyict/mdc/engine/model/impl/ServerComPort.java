package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.ComPort;

import java.sql.SQLException;
import java.util.Date;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.ComPort} that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-18 (16:43)
 */
public interface ServerComPort extends ComPort {

    /**
     * Makes this ComPort obsolete, i.e. it will no longer be available
     * to be used to connect to devices nor will it be returned by
     * the {@link ComPortService} finder methods.
     * This will also remove the ComPort from all {@link ComPortPool}s
     * it belongs to.
     *
     */
    public void makeObsolete ();

    /**
     * Indicates if this ComPort is obsolete
     *
     * @return A flag that indicates if this ComPort is obsolete
     */
    public boolean isObsolete ();

    /**
     * Gets the date on which this ComPort was made obsolete.
     *
     * @return The date when this ComPort was made obsolete
     *         or <code>null</code> when this ComPort is not obsolete at all.
     */
    public UtcInstant getObsoleteDate();

}