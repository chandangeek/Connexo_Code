package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;
import com.elster.jupiter.hsm.model.keys.HsmKey;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.*;
import com.energyict.protocol.exceptions.HsmException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;

@Component(name = "com.energyict.mdc.protocol.pluggable.upl.hsmservice", service = {HsmProtocolService.class}, immediate = true)
public class UPLHsmProtocolServiceImpl implements HsmProtocolService{

    private volatile com.elster.jupiter.hsm.HsmProtocolService actual;

    @Reference
    public void setActualHsmProtocolService(com.elster.jupiter.hsm.HsmProtocolService actual) {
        this.actual = actual;
    }

    @Activate
    public void activate() {
        Services.setHsmService(this);
    }

    @Deactivate
    public void deactivate() {
        Services.setHsmService(null);
    }

    @Override
    public byte[] generateDigestMD5(byte[] challenge, IrreversibleKey hlsSecret) throws HsmException {
        try {
            return actual.generateDigestMD5(challenge, adaptUplKeyToHsmKey(hlsSecret));
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] generateDigestSHA1(byte[] challenge, IrreversibleKey hlsSecret) throws HsmException {
        try {
            return actual.generateDigestSHA1(challenge, adaptUplKeyToHsmKey(hlsSecret));
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.generateDigestGMAC(challenge, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] generateDigestMechanism6(boolean isServerToClient, IrreversibleKey hlsSecret, byte[] systemTitleClient, byte[] systemTitleServer, byte[] challengeServerToClient, byte[] challengeClientToServer) throws HsmException {
        try {
            return actual.generateDigestMechanism6(isServerToClient, adaptUplKeyToHsmKey(hlsSecret), systemTitleClient, systemTitleServer, challengeServerToClient, challengeClientToServer);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.authenticateApdu(apdu, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] authenticateApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.authenticateApduWithAAD(apdu, additionalAuthenticationData, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] encryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.encryptApdu(apdu, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag hsmDataAndAuthenticationTag;
        try {
            hsmDataAndAuthenticationTag = actual.authenticateEncryptApdu(apdu, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
            return adaptHsmDataAndAuthenticationTagToUplValue(hsmDataAndAuthenticationTag);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public DataAndAuthenticationTag authenticateEncryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag hsmDataAndAuthenticationTag;
        try {
            hsmDataAndAuthenticationTag = actual.authenticateEncryptApduWithAAD(apdu, additionalAuthenticationData, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
            return adaptHsmDataAndAuthenticationTagToUplValue(hsmDataAndAuthenticationTag);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            actual.verifyApduAuthentication(apdu, authenticationTag, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public void verifyApduAuthenticationWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            actual.verifyApduAuthenticationWithAAD(apdu, additionalAuthenticationData, authenticationTag, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] decryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.decryptApdu(apdu, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.verifyAuthenticationDecryptApdu(apdu, authenticationTag, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] verifyAuthenticationDecryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmException {
        try {
            return actual.verifyAuthenticationDecryptApduWithAAD(apdu, additionalAuthenticationData, authenticationTag, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek), securitySuite);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] wrapMeterKeyForConcentrator(IrreversibleKey meterKey, IrreversibleKey concentratorKey) throws HsmException {
        try {
            return actual.wrapMeterKeyForConcentrator(adaptUplKeyToHsmKey(meterKey), adaptUplKeyToHsmKey(concentratorKey));
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] generateRandom(int length) throws HsmException {
        try {
            return actual.generateRandom(length);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public byte[] cosemGenerateSignature(int securitySuite, String keyLabel, byte[] dataToSign) throws HsmException {
        try {
            return actual.cosemGenerateSignature(securitySuite, keyLabel, dataToSign);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public boolean verifyFramecounterHMAC(byte[] serverSysT, byte[] clientSysT, byte[] challenge, long framecounter, IrreversibleKey gak, byte[] challengeResponse) throws HsmException {
        try {
            return actual.verifyFramecounterHMAC(serverSysT, clientSysT, challenge, framecounter, adaptUplKeyToHsmKey(gak), challengeResponse);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public EEKAgreeResponse eekAgreeSender1e1s(int securitySuite, String hesSignatureKeyLabel, Certificate[] deviceKeyAgreementKeyCertChain, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmException {
        try {
            com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse eekAgreeResponse = actual.eekAgreeSender1e1s(securitySuite, hesSignatureKeyLabel, deviceKeyAgreementKeyCertChain, deviceCaCertificateLabel, kdfOtherInfo, storageKeyLabel);
            return adaptHsmEEKAgreeResponseToUplValue(eekAgreeResponse);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public IrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmException{
        try {
            HsmKey hsmKey = actual.eekAgreeReceiver1e1s(securitySuite, deviceSignatureKeyCertChain, ephemeralKaKey, signature, hesKaKeyLabel, deviceCaCertificateLabel, kdfOtherInfo, storageKeyLabel);
            return adaptHsmKeyToUplKey(hsmKey);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerate(int securitySuite, int keyIDForAgreement, String privateEccSigningKeyLabel, String mdmStorageKeyLabel) throws HsmException {
        try {
            com.elster.jupiter.hsm.model.response.protocols.KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerateResponse = actual.keyRenewalAgree2EGenerate(securitySuite, keyIDForAgreement, privateEccSigningKeyLabel, mdmStorageKeyLabel);
            return adaptKeyRenewalAgree2EGenerateResponseToUplValue(keyRenewalAgree2EGenerateResponse);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    @Override
    public IrreversibleKey keyRenewalAgree2EFinalise(int securitySuite, int keyIDForAgree, byte[] serializedPrivateEccKey, byte[] ephemeralEccPubKeyForSmAgreementData, byte[] signature, String caCertificateLabel, Certificate[] certificateChain, byte[] otherInfo, String storageKeyLabel) throws HsmException {
        try {
            HsmKey hsmKey = actual.keyRenewalAgree2EFinalise(securitySuite, keyIDForAgree, serializedPrivateEccKey, ephemeralEccPubKeyForSmAgreementData, signature, caCertificateLabel, certificateChain, otherInfo, storageKeyLabel);
            return adaptHsmKeyToUplKey(hsmKey);
        } catch (HsmBaseException e) {
            throw new HsmException(e);
        }
    }

    private HsmIrreversibleKey adaptUplKeyToHsmKey(IrreversibleKey irreversibleKey) {
        return new HsmIrreversibleKey(irreversibleKey.getEncryptedKey(), irreversibleKey.getKeyLabel());
    }

    private IrreversibleKey adaptHsmKeyToUplKey(HsmKey hsmKey) {
        return new IrreversibleKey() {
            @Override
            public byte[] getEncryptedKey() {
                return hsmKey.getKey();
            }

            @Override
            public String getKeyLabel() {
                return hsmKey.getLabel();
            }

            @Override
            public byte[] toBase64ByteArray() {
                String labelAndKeyValue = new StringBuilder()
                        .append(hsmKey.getLabel())
                        .append(":")
                        .append(Hex.encodeHexString(hsmKey.getKey()))
                        .toString();

                return Base64.encodeBase64String(labelAndKeyValue.getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    private EEKAgreeResponse adaptHsmEEKAgreeResponseToUplValue(com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse eekAgreeResponse) {
        return new EEKAgreeResponse() {
            @Override
            public byte[] getEphemeralPublicKey() {
                return eekAgreeResponse.getEphemeralPublicKey();
            }

            @Override
            public byte[] getSignature() {
                return eekAgreeResponse.getSignature();
            }

            @Override
            public IrreversibleKey getEek() {
                return adaptHsmKeyToUplKey(eekAgreeResponse.getEek());
            }
        };
    }

    private DataAndAuthenticationTag adaptHsmDataAndAuthenticationTagToUplValue(com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag hsmDataAndAuthenticationTag) {
        return new DataAndAuthenticationTag() {
            @Override
            public byte[] getData() {
                return hsmDataAndAuthenticationTag.getData();
            }

            @Override
            public byte[] getAuthenticationTag() {
                return hsmDataAndAuthenticationTag.getAuthenticationTag();
            }
        };
    }

    private KeyRenewalAgree2EGenerateResponse adaptKeyRenewalAgree2EGenerateResponseToUplValue(com.elster.jupiter.hsm.model.response.protocols.KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerateResponse) {
        return new KeyRenewalAgree2EGenerateResponse() {

            @Override
            public byte[] getAgreementData() {
                return keyRenewalAgree2EGenerateResponse.getAgreementData();
            }

            @Override
            public byte[] getSignature() {
                return keyRenewalAgree2EGenerateResponse.getSignature();
            }

            @Override
            public byte[] getPrivateEccKey() {
                return keyRenewalAgree2EGenerateResponse.getPrivateEccKey();
            }
        };
    }

}