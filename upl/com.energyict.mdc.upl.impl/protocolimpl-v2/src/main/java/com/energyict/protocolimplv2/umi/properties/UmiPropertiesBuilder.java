package com.energyict.protocolimplv2.umi.properties;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;
import com.energyict.protocolimplv2.umi.types.UmiId;

import java.security.PrivateKey;

public class UmiPropertiesBuilder {
    private SecurityScheme    cmdSignatureScheme = SecurityScheme.NO_SECURITY;
    private SecurityScheme    encryptionScheme = SecurityScheme.NO_SECURITY;
    private SecurityScheme    respSignatureSchemeRequest = SecurityScheme.NO_SECURITY;
    private short             sourceOptions;
    private short             destinationOptions;
    private UmiId             sourceUmiId;
    private UmiId             destinationUmiId = new UmiId(UmiId.MAX_UMI_ID);

    private PrivateKey        ownPrivateKey;
    private UmiCVCCertificate ownCertificate;
    private boolean           performCertificatesExchange = false;

    public UmiPropertiesBuilder cmdSignatureScheme(SecurityScheme cmdSignatureScheme) {
        this.cmdSignatureScheme = cmdSignatureScheme;
        return this;
    }

    public UmiPropertiesBuilder encryptionScheme(SecurityScheme encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        return this;
    }

    public UmiPropertiesBuilder respSignatureSchemeRequest(SecurityScheme respSignatureSchemeRequest) {
        this.respSignatureSchemeRequest = respSignatureSchemeRequest;
        return this;
    }

    public UmiPropertiesBuilder sourceOptions(short sourceOptions) {
        this.sourceOptions = sourceOptions;
        return this;
    }

    public UmiPropertiesBuilder destinationOptions(short destinationOptions) {
        this.destinationOptions = destinationOptions;
        return this;
    }

    public UmiPropertiesBuilder sourceUmiId(UmiId sourceUmiId) {
        this.sourceUmiId = sourceUmiId;
        return this;
    }

    public UmiPropertiesBuilder destinationUmiId(UmiId destinationUmiId) {
        this.destinationUmiId = destinationUmiId;
        return this;
    }

    public UmiPropertiesBuilder ownPrivateKey(PrivateKey key) {
        this.ownPrivateKey = key;
        return this;
    }

    public UmiPropertiesBuilder ownCertificate(UmiCVCCertificate certificate) {
        this.ownCertificate = certificate;
        return this;
    }

    public UmiPropertiesBuilder performCertificatesExchange(boolean performCertificatesExchange) {
        this.performCertificatesExchange = performCertificatesExchange;
        return this;
    }

    public UmiSessionProperties build() {
        return new UmiSessionPropertiesS0(this);
    }

    public SecurityScheme getCmdSignatureScheme() {
        return cmdSignatureScheme;
    }

    public SecurityScheme getEncryptionScheme() {
        return encryptionScheme;
    }

    public SecurityScheme getRespSignatureSchemeRequest() {
        return respSignatureSchemeRequest;
    }

    public short getSourceOptions() {
        return sourceOptions;
    }

    public short getDestinationOptions() {
        return destinationOptions;
    }

    public UmiId getSourceUmiId() {
        return sourceUmiId;
    }

    public UmiId getDestinationUmiId() {
        return destinationUmiId;
    }

    public PrivateKey getOwnPrivateKey() {
        return ownPrivateKey;
    }

    public UmiCVCCertificate getOwnCertificate() {
        return ownCertificate;
    }

    public boolean getPerformCertificatesExchange() {
        return performCertificatesExchange;
    }
}