package com.energyict.mdc.upl.crypto;

public interface EEKAgreeResponse {

    byte[] getEphemeralPublicKey();

    byte[] getSignature();

    IrreversibleKey getEek();

}
