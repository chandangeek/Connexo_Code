package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cbo.CertificateAlias;
import com.energyict.cbo.PrivateKeyAlias;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RespondingFrameCounterHandler;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 21/01/2016 - 14:16
 */
public class Beacon3100SecurityProvider extends NTASecurityProvider implements GeneralCipheringSecurityProvider {

    private byte[] sessionKey;
    private X509Certificate serverSigningCertificate;
    private X509Certificate serverKeyAgreementCertificate;
    private X509Certificate clientSigningCertificate;
    private PrivateKey clientPrivateKeyAgreementKey;
    private PrivateKey clientPrivateSigningKey;

    public Beacon3100SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel) {
        super(properties, authenticationDeviceAccessLevel);
        setRespondingFrameCounterHandling(new DSMR40RespondingFrameCounterHandler());
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
    public byte[] getSessionKey() {
        if (sessionKey == null) {
            sessionKey = new byte[16];
            Random rnd = new Random();
            rnd.nextBytes(sessionKey);
        }
        return sessionKey;
    }

    @Override
    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
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
                if (!(serverKeyAgreementCertificate instanceof X509Certificate)) {
                    throw DeviceConfigurationException.invalidPropertyFormat(
                            propertyName,
                            "Certificate with alias '" + certificateAlias.getAlias() + "'",
                            "The certificate must be of type X509 v3");
                }

                X509Certificate x509Certificate = (X509Certificate) serverKeyAgreementCertificate;
                x509Certificate.checkValidity();
                return x509Certificate;
            } catch (CertificateException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        "Certificate with alias '" + certificateAlias.getAlias() + "'",
                        "The certificate must be a valid X509 v3 certificate");
            }
        }
    }

    private PrivateKey parsePrivateKey(String propertyName) {
        PrivateKeyAlias alias = properties.getTypedProperty(propertyName);
        if (alias == null) {
            return null;
        } else {
            try {
                return alias.getPrivateKey();
            } catch (InvalidKeySpecException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(
                        propertyName,
                        "Private key with alias '" + alias.getAlias() + "'",
                        "The private key must be a valid, PKCS8 encoded key");
            }
        }
    }
}