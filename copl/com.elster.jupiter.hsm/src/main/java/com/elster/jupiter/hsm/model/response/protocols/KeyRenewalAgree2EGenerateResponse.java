package com.elster.jupiter.hsm.model.response.protocols;

public interface KeyRenewalAgree2EGenerateResponse {

    byte[] getAgreementData();

    byte[] getSignature();

    byte[] getPrivateEccKey();
}
