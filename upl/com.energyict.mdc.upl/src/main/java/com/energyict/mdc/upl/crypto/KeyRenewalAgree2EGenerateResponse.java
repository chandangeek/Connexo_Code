package com.energyict.mdc.upl.crypto;

public interface KeyRenewalAgree2EGenerateResponse {

    byte[] getAgreementData();

    byte[] getSignature();

    byte[] getPrivateEccKey();

}
