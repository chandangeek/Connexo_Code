package com.elster.jupiter.hsm.model.keys;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IrreversibleKey {

    byte[] getEncryptedKey();

    String getKeyLabel();

}
