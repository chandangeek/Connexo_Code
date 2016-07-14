package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cbo.CertificateAlias;
import com.energyict.cbo.PrivateKeyAlias;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.mdw.core.ECCCurve;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 21/01/2016 - 14:16
 */
public class Beacon3100SecurityProvider extends NTASecurityProvider implements GeneralCipheringSecurityProvider {

    private int securitySuite;
    private byte[] sessionKey;
    private byte[] serverSessionKey;
    private X509Certificate serverSigningCertificate;
    private X509Certificate serverKeyAgreementCertificate;
    private X509Certificate clientSigningCertificate;
    private PrivateKey clientPrivateKeyAgreementKey;
    private PrivateKey clientPrivateSigningKey;

    public Beacon3100SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel, int securitySuite) {
        super(properties, authenticationDeviceAccessLevel);
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER));
        this.securitySuite = securitySuite;
    }

    public void setSecuritySuite(int securitySuite) {
        this.securitySuite = securitySuite;
    }

    /**
     * Override, the KEK of the Beacon is stored in property DlmsWanKEK
     */
    @Override
    public byte[] getMasterKey() {
        if (this.masterKey == null) {
            String hex = properties.getTypedProperty(Beacon3100ConfigurationSupport.DLMS_WAN_KEK);
            if (hex == null || hex.isEmpty()) {
                throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.DLMS_WAN_KEK);
            }
            this.masterKey = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.masterKey;
    }

    @Override
    public byte[] getHLSSecret() {
        if (this.hlsSecret == null) {
            String hex = properties.getTypedProperty(SecurityPropertySpecName.PASSWORD.toString());
            if (hex == null || hex.isEmpty()) {
                throw DeviceConfigurationException.missingProperty(SecurityPropertySpecName.PASSWORD.toString());
            }
            this.hlsSecret = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.hlsSecret;
    }

    @Override
    public byte[] getSessionKey() {
        if (sessionKey == null) {
            sessionKey = new byte[getKeyLength()];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(sessionKey);
        }
        return sessionKey;
    }

    @Override
    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    private int getKeyLength() {
        return securitySuite == 2 ? 32 : 16;
    }

    @Override
    public byte[] getServerSessionKey() {
        return serverSessionKey;
    }

    @Override
    public void setServerSessionKey(byte[] serverSessionKey) {
        this.serverSessionKey = serverSessionKey;
    }

    @Override
    public X509Certificate getServerKeyAgreementCertificate() {
        if (serverKeyAgreementCertificate == null) {
            serverKeyAgreementCertificate = parseX509Certificate(SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
        }
        return serverKeyAgreementCertificate;
    }

    @Override
    public X509Certificate getServerSignatureCertificate() {
        if (serverSigningCertificate == null) {
            serverSigningCertificate = parseX509Certificate(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString());
        }
        return serverSigningCertificate;
    }

    @Override
    public void setServerSignatureCertificate(X509Certificate serverSignatureCertificate) {
        this.serverSigningCertificate = serverSignatureCertificate;
    }

    @Override
    public X509Certificate getClientSigningCertificate() {
        if (clientSigningCertificate == null) {
            clientSigningCertificate = parseX509Certificate(DlmsSessionProperties.CLIENT_SIGNING_CERTIFICATE);
        }
        return clientSigningCertificate;
    }

    @Override
    public PrivateKey getClientPrivateSigningKey() {
        if (clientPrivateSigningKey == null) {
            clientPrivateSigningKey = parsePrivateKey(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
        }
        return clientPrivateSigningKey;
    }

    @Override
    public PrivateKey getClientPrivateKeyAgreementKey() {
        if (clientPrivateKeyAgreementKey == null) {
            clientPrivateKeyAgreementKey = parsePrivateKey(DlmsSessionProperties.CLIENT_PRIVATE_KEY_AGREEMENT_KEY);
        }
        return clientPrivateKeyAgreementKey;
    }

    /**
     * Returns a valid X509 v3 certificate, or null if the property has no value
     */
    private X509Certificate parseX509Certificate(String propertyName) {
        CertificateAlias certificateAlias = properties.getTypedProperty(propertyName);
        if (certificateAlias == null) {
            return null;
        } else {
            try {
                Certificate serverKeyAgreementCertificate = certificateAlias.getCertificate();
                if (serverKeyAgreementCertificate == null) {
                    return null;
                }
                if (!(serverKeyAgreementCertificate instanceof X509Certificate)) {
                    throw DeviceConfigurationException.invalidPropertyFormat(
                            propertyName,
                            "Certificate with alias '" + certificateAlias.getAlias() + "'",
                            "The certificate must be of type X509 v3");
                }

                X509Certificate x509Certificate = (X509Certificate) serverKeyAgreementCertificate;

                if (x509Certificate.getPublicKey() instanceof ECPublicKey) {
                    ECPoint ecPoint = ((ECPublicKey) x509Certificate.getPublicKey()).getW();

                    int componentSize = KeyUtils.getKeySize(getECCCurve()) / 2;
                    byte[] xBytes = trim(ecPoint.getAffineX().toByteArray(), componentSize);
                    byte[] yBytes = trim(ecPoint.getAffineY().toByteArray(), componentSize);

                    if ((xBytes.length + yBytes.length) != KeyUtils.getKeySize(getECCCurve())) {
                        throw DeviceConfigurationException.invalidPropertyFormat(
                                propertyName,
                                "Certificate with alias '" + certificateAlias.getAlias() + "'",
                                "The public key of the certificate should be for the " + getECCCurve().getCurveName() + " elliptic curve (DLMS security suite " + securitySuite + ")");
                    }
                } else {
                    throw DeviceConfigurationException.invalidPropertyFormat(
                            propertyName,
                            "Certificate with alias '" + certificateAlias.getAlias() + "'",
                            "The public key of the certificate should be for elliptic curve cryptography");
                }

                x509Certificate.checkValidity();

                return x509Certificate;
            } catch (CertificateException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        "Certificate with alias '" + certificateAlias.getAlias() + "'",
                        "The certificate must be a valid X509 v3 certificate: " + e.getMessage());
            }
        }
    }

    /**
     * Remove the leading zero byte from the component bytes, if it's present.
     */
    private byte[] trim(byte[] componentBytes, int componentSize) {
        if (componentBytes.length > componentSize) {
            if (componentBytes[0] == 0x00) {
                return ProtocolTools.getSubArray(componentBytes, 1);
            }
        }
        return componentBytes;
    }

    private ECCCurve getECCCurve() {
        switch (securitySuite) {
            case 1:
                return ECCCurve.P256_SHA256;
            case 2:
                return ECCCurve.P384_SHA384;
            default:
                throw DeviceConfigurationException.unsupportedPropertyValue("SecuritySuite", String.valueOf(securitySuite));
        }
    }

    private PrivateKey parsePrivateKey(String propertyName) {
        PrivateKeyAlias alias = properties.getTypedProperty(propertyName);
        if (alias == null) {
            return null;
        } else {
            try {
                PrivateKey privateKey = alias.getPrivateKey();
                if (privateKey instanceof ECPrivateKey) {

                    int keySize = KeyUtils.getKeySize(getECCCurve()) / 2;
                    byte[] privateKeyBytes = trim(((ECPrivateKey) privateKey).getS().toByteArray(), keySize);

                    if (privateKeyBytes.length != keySize) {
                        throw DeviceConfigurationException.invalidPropertyFormat(
                                propertyName,
                                "Private key with alias '" + alias.getAlias() + "'",
                                "The private key should be for the " + getECCCurve().getCurveName() + " elliptic curve (DLMS security suite " + securitySuite + ")");
                    }
                } else {
                    throw DeviceConfigurationException.invalidPropertyFormat(
                            propertyName,
                            "Private key with alias '" + alias.getAlias() + "'",
                            "The private key should be for elliptic curve cryptography");
                }


                return privateKey;
            } catch (InvalidKeySpecException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        "Private key with alias '" + alias.getAlias() + "'",
                        "The private key must be a valid, PKCS8 encoded key");
            }
        }
    }

    @Override
    public byte[] getDedicatedKey() {
        if (dedicatedKey == null) {
            dedicatedKey = new byte[getKeyLength()];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(dedicatedKey);
        }
        return dedicatedKey;
    }
}