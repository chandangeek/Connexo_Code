package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

/**
 * References an externally defined process.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (13:01)
 */
@ProviderType
public interface ProcessReference {

    public StateChangeBusinessProcess getStateChangeBusinessProcess();

}