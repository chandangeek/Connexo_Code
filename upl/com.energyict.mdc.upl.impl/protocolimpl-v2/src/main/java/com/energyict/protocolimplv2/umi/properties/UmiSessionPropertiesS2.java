package com.energyict.protocolimplv2.umi.properties;

import com.energyict.protocolimplv2.umi.security.AppPacketSecurity;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.security.scheme2.AppPacketSecurityS2;
import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;
import com.energyict.protocolimplv2.umi.types.UmiInitialisationVector;

import java.security.PrivateKey;

public class UmiSessionPropertiesS2 extends UmiSessionPropertiesS0 {
    private byte[] sessionKey;

    private PrivateKey        ownPrivateKey;
    private UmiCVCCertificate ownCertificate;
    private UmiCVCCertificate remoteCertificate;

    private UmiInitialisationVector inboundIV;
    private UmiInitialisationVector outboundIV;

    private boolean performCertificateExchange;
    private AppPacketSecurity security;

    public UmiSessionPropertiesS2(UmiPropertiesBuilder builder) {
        super(builder);
        this.ownPrivateKey = builder.getOwnPrivateKey();
        this.ownCertificate = builder.getOwnCertificate();
        this.inboundIV = new UmiInitialisationVector(getDestinationUmiId());
        this.outboundIV = new UmiInitialisationVector(getSourceUmiId());
        this.performCertificateExchange = builder.getPerformCertificatesExchange();
        this.security = createSecurity();
    }

    public UmiSessionPropertiesS2(UmiPropertiesBuilder builder, UmiCVCCertificate remoteCertificate) {
        this(builder);
        this.remoteCertificate = remoteCertificate;
    }

    public UmiInitialisationVector getInboundIV() {
        return inboundIV;
    }

    public UmiInitialisationVector getOutboundIV() {
        return outboundIV;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public PrivateKey getOwnPrivateKey() {
        return ownPrivateKey;
    }

    public UmiCVCCertificate getOwnCertificate() {
        return ownCertificate;
    }

    public UmiCVCCertificate getRemoteCertificate() {
        return remoteCertificate;
    }

    public void setRemoteCertificate(UmiCVCCertificate remoteCertificate) {
        this.remoteCertificate = remoteCertificate;
    }

    public boolean getPerformCertificateExchange() {
        return performCertificateExchange;
    }

    @Override
    protected AppPacketSecurity createSecurity() {
        return new AppPacketSecurityS2(this);
    }

    @Override
    public AppPacketSecurity getSecurity() {
        return this.getEncryptionScheme().equals(SecurityScheme.NO_SECURITY) ? super.getSecurity() : this.security;
    }
}
