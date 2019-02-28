package com.elster.jupiter.hsm.model.keys;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmKey {
    /**
     *
     * @return plain key bytes (for reversible keys) or already encrypted key for Ireversible ones
     */
    byte[] getKey();

    /**
     *
     * @return label used to symmetricEncrypt key
     */
    String getLabel();

    boolean isReversible();

}
