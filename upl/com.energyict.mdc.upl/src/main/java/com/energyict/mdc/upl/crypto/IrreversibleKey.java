package com.energyict.mdc.upl.crypto;

public interface IrreversibleKey {

    byte[] getEncryptedKey();

    String getKeyLabel();

    byte[] toBase64ByteArray();
}
