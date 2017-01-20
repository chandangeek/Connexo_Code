package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.nls.LocalizedException;

/**
 * Adds behavior to {@link DataVault} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (10:30)
 */
public interface ServerDataVault extends DataVault {
    void createVault() throws LocalizedException;
}