package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;

/**
 * Adds behavior to {@link Formula} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:00)
 */
public interface ServerFormula extends Formula {

    /**
     * Delete this {@link Formula}
     */
    void delete();
}