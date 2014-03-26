package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.BaseLogBook;

import java.io.Serializable;

/**
 * Uniquely identifies a log book that is stored in a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:51)
 */
public interface LogBookIdentifier extends Serializable {

    /**
     * Finds the {@link com.energyict.mdc.protocol.api.device.BaseLogBook} that is uniquely identified by this LogBookIdentifier.
     *
     * @return the LogBook
     */
    public BaseLogBook getLogBook();

}