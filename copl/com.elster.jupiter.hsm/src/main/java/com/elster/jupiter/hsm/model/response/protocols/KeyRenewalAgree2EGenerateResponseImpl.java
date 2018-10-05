package com.elster.jupiter.hsm.model.response.protocols;

public class KeyRenewalAgree2EGenerateResponseImpl implements KeyRenewalAgree2EGenerateResponse {

    private final byte[] agreementData;
    private final byte[] signature;
    private final byte[] privateEccKey;

    public KeyRenewalAgree2EGenerateResponseImpl(byte[] mdmAgreementData, byte[] mdmSignature, byte[] privateEccKey) {
        this.agreementData = mdmAgreementData;
        this.signature = mdmSignature;
        this.privateEccKey = privateEccKey;
    }

    @Override
    public byte[] getAgreementData() {
        return this.agreementData;
    }

    @Override
    public byte[] getSignature() {
        return this.signature;
    }

    @Override
    public byte[] getPrivateEccKey() {
        return this.privateEccKey;
    }
}
