package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.CertificateWrapper;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 21/01/2016 - 14:16
 */
public class Beacon3100SecurityProvider extends NTASecurityProvider implements GeneralCipheringSecurityProvider {

    private final CertificateWrapperExtractor certificateWrapperExtractor;
    private int securitySuite;
    private byte[] sessionKey;
    private byte[] serverSessionKey;
    private X509Certificate serverSigningCertificate;
    private X509Certificate serverKeyAgreementCertificate;
    private X509Certificate clientSigningCertificate;
    private PrivateKey clientPrivateKeyAgreementKey;
    private PrivateKey clientPrivateSigningKey;

    public Beacon3100SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel, int securitySuite, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(properties, authenticationDeviceAccessLevel);
        this.certificateWrapperExtractor = certificateWrapperExtractor;
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
            String hex = properties.getTypedProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString());
            if (hex == null || hex.isEmpty()) {
                throw DeviceConfigurationException.missingProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString());
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

    public int getKeyLength() {
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
            serverKeyAgreementCertificate = parseX509Certificate(SecurityPropertySpecTranslationKeys.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
        }
        return serverKeyAgreementCertificate;
    }

    @Override
    public X509Certificate getServerSignatureCertificate() {
        if (serverSigningCertificate == null) {
            serverSigningCertificate = parseX509Certificate(SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE.toString());
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
            clientSigningCertificate = parseCertificateOfPrivateKey(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
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
        CertificateWrapper certificateWrapper = properties.getTypedProperty(propertyName);
        if (certificateWrapper == null) {
            return null;
        } else {
            Optional<X509Certificate> optionalCertificate = certificateWrapperExtractor.getCertificate(certificateWrapper);
            if (!optionalCertificate.isPresent()) {
                return null;
            }

            X509Certificate certificate = optionalCertificate.get();
            String propertyValue = "Certificate with serial number '" + certificate.getSerialNumber() + "'";
            try {
                return validateCertificate(propertyName, propertyValue, certificate);
            } catch (CertificateException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        propertyValue,
                        "The certificate must be a valid X509 v3 certificate: " + e.getMessage());
            }
        }
    }

    private X509Certificate validateCertificate(String propertyName, String propertyValue, Certificate certificate) throws CertificateExpiredException, CertificateNotYetValidException {
        if (certificate == null) {
            return null;
        }
        if (!(certificate instanceof X509Certificate)) {
            throw DeviceConfigurationException.invalidPropertyFormat(
                    propertyName,
                    propertyValue,
                    "The certificate must be of type X.509 v3");
        }

        X509Certificate x509Certificate = (X509Certificate) certificate;

        if (x509Certificate.getPublicKey() instanceof ECPublicKey) {
            ECPoint ecPoint = ((ECPublicKey) x509Certificate.getPublicKey()).getW();

            int componentSize = KeyUtils.getKeySize(getECCCurve()) / 2;
            byte[] xBytes = trim(ecPoint.getAffineX().toByteArray(), componentSize);
            byte[] yBytes = trim(ecPoint.getAffineY().toByteArray(), componentSize);

            if ((xBytes.length + yBytes.length) != KeyUtils.getKeySize(getECCCurve())) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        propertyValue,
                        "The public key of the certificate should be for the " + getECCCurve().getCurveName() + " elliptic curve (DLMS security suite " + securitySuite + ")");
            }
        } else {
            throw DeviceConfigurationException.invalidPropertyFormat(
                    propertyName,
                    propertyValue,
                    "The public key of the certificate should be for elliptic curve cryptography");
        }

        x509Certificate.checkValidity();

        return x509Certificate;
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

    /**
     * Return the private key from the EIServer key store for the configured alias.
     * Throw the proper exception if the private key could not be found based on the configured alias.
     */
    private PrivateKey parsePrivateKey(String propertyName) {
        CertificateWrapper certificateWrapper = properties.getTypedProperty(propertyName);
        if (certificateWrapper == null) {
            throw DeviceConfigurationException.missingProperty(propertyName);
        } else {
            String alias = certificateWrapperExtractor.getAlias(certificateWrapper);
            try {
                PrivateKey privateKey = certificateWrapperExtractor.getPrivateKey(certificateWrapper);
                if (privateKey == null) {
                    throw DeviceConfigurationException.invalidPropertyFormat(
                            propertyName,
                            alias,
                            "The configured alias does not refer to an existing entry in the EIServer persisted key store.");
                }

                if (privateKey instanceof ECPrivateKey) {

                    int keySize = KeyUtils.getKeySize(getECCCurve()) / 2;
                    byte[] privateKeyBytes = trim(((ECPrivateKey) privateKey).getS().toByteArray(), keySize);

                    if (privateKeyBytes.length != keySize) {
                        throw DeviceConfigurationException.invalidPropertyFormat(
                                propertyName,
                                "Private key with alias '" + alias + "'",
                                "The private key should be for the " + getECCCurve().getCurveName() + " elliptic curve (DLMS security suite " + securitySuite + ")");
                    }
                } else {
                    throw DeviceConfigurationException.invalidPropertyFormat(
                            propertyName,
                            "Private key with alias '" + alias + "'",
                            "The private key should be for elliptic curve cryptography");
                }


                return privateKey;
            } catch (InvalidKeyException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        "Private key with alias '" + alias + "'",
                        "The private key must be a valid, PKCS8 encoded key");
            }
        }
    }

    /**
     * The client CertificateWrapper contains both the private key and its matching certificate, fetched from the EIServer persisted key store.
     */
    private X509Certificate parseCertificateOfPrivateKey(String propertyName) {
        CertificateWrapper certificateWrapper = properties.getTypedProperty(propertyName);
        if (certificateWrapper == null) {
            return null;
        } else {
            String alias = certificateWrapperExtractor.getAlias(certificateWrapper);
            String propertyValue = "Certificate with alias '" + alias + "'";
            try {
                Optional<X509Certificate> certificate = certificateWrapperExtractor.getCertificate(certificateWrapper);
                if (!certificate.isPresent()) {
                    return null;
                }
                return validateCertificate(propertyName, propertyValue, certificate.get());
            } catch (CertificateException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        propertyValue,
                        "The certificate must be a valid X509 v3 certificate: " + e.getMessage());
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