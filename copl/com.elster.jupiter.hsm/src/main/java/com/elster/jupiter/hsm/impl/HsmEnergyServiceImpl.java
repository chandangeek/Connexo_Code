package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.*;
import com.atos.worldline.jss.api.custom.energy.*;
import com.atos.worldline.jss.api.key.*;
import com.atos.worldline.jss.api.key.derivation.CertificateChainX509KeyDerivation;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.HsmProtocolService;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.HsmRenewKey;
import com.elster.jupiter.hsm.model.keys.IrreversibleKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.hsm.model.response.protocols.*;
import com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalAgree2EGenerateResponse;
import org.apache.commons.lang3.SerializationUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.net.ssl.X509KeyManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.impl.HsmEnergyServiceImpl", service = {HsmEnergyService.class, HsmProtocolService.class}, immediate = true, property = "name=" + HsmEnergyServiceImpl.COMPONENTNAME)
public class HsmEnergyServiceImpl implements HsmEnergyService, HsmProtocolService {

    private static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;
    /** Size of the frame counter when encoded. */
    private static final int FRAMECOUNTER_SIZE = 4;

    static final String COMPONENTNAME = "HsmEnergyServiceImpl";

    static final Map<Integer , SecuritySuite> SECURITY_SUITE_MAP = new HashMap<Integer , SecuritySuite>() {{
        put(0,    SecuritySuite.SUITE0);
        put(1, SecuritySuite.SUITE1);
        put(2, SecuritySuite.SUITE2);
    }};

    static final Map<Integer , KeyIDForAgree> KEY_ID_FOR_AGREE_MAP = new HashMap<Integer , KeyIDForAgree>() {{
        put(0,    KeyIDForAgree.GLOBAL_UNICAST_ENC_KEY);
        put(1, KeyIDForAgree.GLOBAL_UNICAST_ENC_KEY);
        put(2, KeyIDForAgree.AUTHENTICATION_KEY);
        put(3, KeyIDForAgree.MASTER_KEY);
    }};



    private volatile HsmConfigurationService hsmConfigurationService;

    @Override
    public HsmEncryptedKey importKey(ImportKeyRequest importKeyRequest) throws HsmBaseException {
        try {
            HsmConfiguration hsmConfiguration = hsmConfigurationService.getHsmConfiguration();
            String encryptLabel = importKeyRequest.getImportLabel();

            KeyImportResponse keyImportResponse = Energy.keyImport(importKeyRequest.getTransportKey(hsmConfiguration), importKeyRequest.getWrapperKeyAlgorithm().getHsmSpecs().getPaddingAlgorithm(), importKeyRequest.getDeviceKey(), new KeyLabel(encryptLabel), importKeyRequest.getImportSessionCapability()
                    .toProtectedSessionKeyCapability());
            ProtectedSessionKey psk = keyImportResponse.getProtectedSessionKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    public HsmRenewKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException {
        try {
            KeyLabel newLabel = new KeyLabel(renewKeyRequest.getRenewLabel());
            ProtectedSessionKey protectedSessionKey = new ProtectedSessionKey(new KeyLabel(renewKeyRequest.getActualLabel()),renewKeyRequest.getActualKey());
            KeyRenewalResponse response = Energy.cosemKeyRenewal(renewKeyRequest.getRenewCapability().toProtectedSessionKeyCapability(),
                    protectedSessionKey,
                    newLabel,
                    getSessionKeyType(renewKeyRequest.getHsmKeyType()));
            ProtectedSessionKey psk = response.getMdmStorageKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmRenewKey(response.getSmartMeterKey(), psk.getValue(), kekLabel);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    private ProtectedSessionKeyType getSessionKeyType(HsmKeyType keyType) throws HsmBaseException {

        int keyLength = keyType.getKeySize();
        if (keyLength == AES_KEY_LENGTH) {
            return ProtectedSessionKeyType.AES;
        }

        if (keyLength == AES256_KEY_LENGTH) {
            return ProtectedSessionKeyType.AES_256;
        }
        throw new HsmBaseException("Could not determine session key type for key length:" + keyLength);
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) {
        this.hsmConfigurationService = hsmConfigurationService;
    }

    private static KeyDerivation[] createKeyDerivationArray(Certificate[] certChain) {
        KeyDerivation[] keyDerivations = new KeyDerivation[certChain.length];
        for (int i = 0; i < certChain.length; i++) {
            try {
                keyDerivations[i] = new CertificateChainX509KeyDerivation(certChain[i].getEncoded());
            } catch (CertificateEncodingException e) {
                throw new RuntimeException("Failed to create KeyDerivation array from provided certificate chain. " + e);
            }
        }
        return keyDerivations;
    }

    private static RandomMode getRandomGenerator() {
        return RandomMode.PseudoRandomGenerator; //Default random generator
    }

    @Override
    public byte[] generateDigestSHA1(byte[] challenge, IrreversibleKey hlsSecret) throws HsmBaseException {
        CosemHLSAuthenticationResponse response;
        try {
            response = Energy.cosemHlsAuthentication(toProtectedSessionKey(hlsSecret), AuthenticationMechanism.MECHANISM4,
                    challenge);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthenticationTag();
    }

    @Override
    public byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthTag();
    }

    @Override
    public byte[] encryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.ENCRYPT, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getData();
    }

    @Override
    public DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE_AND_ENCRYPT, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return new DataAndAuthenticationTagImpl(response.getData(), response.getAuthTag());
    }

    @Override
    public byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE_AND_ENCRYPT, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), apdu,
                    initializationVector, authenticationTag);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }

        final Boolean authResult = response.getAuthResult();
        if (authResult == null || authResult.equals(false)) {
            throw new HsmBaseException(new HsmBaseException("Authentication failed."));
        }
        return response.getData();
    }

    @Override
    public byte[] wrapMeterKeyForConcentrator(IrreversibleKey meterKey, IrreversibleKey concentratorKey) throws HsmBaseException {
        byte[] result;
        try {
            result = Energy.cosemPskExportDataConcentrator(toProtectedSessionKey(meterKey),
                    toProtectedSessionKey(concentratorKey));
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return result;
    }

    @Override
    public void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), apdu,
                    initializationVector, authenticationTag);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        final Boolean authResult = response.getAuthResult();
        if (authResult == null || authResult.equals(false)) {
            throw new HsmBaseException(new HsmBaseException("Authentication failed."));
        }
    }

    @Override
    public byte[] cosemGenerateSignature(int securitySuite, String keyLabel, byte[] dataToSign) throws HsmBaseException {
        byte[] result;
        try {
            KeyLabel hsmKeyLabel = new KeyLabel(keyLabel);
            result = Energy.cosemGenerateSignature(getAtosSecuritySuite(securitySuite), hsmKeyLabel, dataToSign);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return result;
    }

    @Override
    public boolean verifyFramecounterHMAC(byte[] serverSysT, byte[] clientSysT, byte[] challenge, long framecounter, IrreversibleKey gak, byte[] challengeResponse) throws HsmBaseException {
        final byte[] framecounterBytes = new byte[FRAMECOUNTER_SIZE];

        framecounterBytes[0] = (byte)((framecounter >> 24) & 0xff);
        framecounterBytes[1] = (byte)((framecounter >> 16) & 0xff);
        framecounterBytes[2] = (byte)((framecounter >> 8) & 0xff);
        framecounterBytes[3] = (byte)(framecounter & 0xff);

        final byte[] macInputData = new byte[serverSysT.length + clientSysT.length + challenge.length + FRAMECOUNTER_SIZE];

        /* Data is defined as : serverSysT || clientSysT || challenge || frameCounter */
        System.arraycopy(serverSysT, 0, macInputData, 0, serverSysT.length);
        System.arraycopy(clientSysT, 0, macInputData, serverSysT.length, clientSysT.length);
        System.arraycopy(challenge, 0, macInputData, serverSysT.length + clientSysT.length, challenge.length);
        System.arraycopy(framecounterBytes, 0, macInputData, serverSysT.length + clientSysT.length + challenge.length, FRAMECOUNTER_SIZE);

        final MAC mac = new MAC(challengeResponse);

        try {
            final SecretKey ak = new AESKeyToken(AES_KEY_LENGTH, gak.getEncryptedKey(), null, KEKEncryptionMethod.PROTECTED_SESSION_KEY, new KeyLabel(gak.getKeyLabel()), KeyDerivation.FIXED_KEY_ARRAY);
            final SymmetricMACVerifyResponse response = Symmetric.verifyHMAC(ak, KeyDerivation.FIXED_KEY_ARRAY, macInputData, null, HashAlgorithm.SHA_256, BlockMode.SINGLE, mac);

            if (response != null) {
                return response.getResult();
            }

            return false;
        } catch (final FunctionFailedException | UnsupportedKEKEncryptionMethodException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public byte[] generateDigestMD5(byte[] challenge, IrreversibleKey hlsSecret) throws HsmBaseException {
        CosemHLSAuthenticationResponse response;
        try {
            response = Energy.cosemHlsAuthentication(toProtectedSessionKey(hlsSecret), AuthenticationMechanism.MECHANISM3,
                    challenge);
        } catch (FunctionFailedException ffe) {
            //TODO: add also the HSM related messageSeed to the exception?. This should be applied to all "HsmBaseException" exceptions thrown in this class
            throw new HsmBaseException(ffe);
        }
        return response.getAuthenticationTag();
    }

    @Override
    public byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemHLSAuthenticationResponse response;
        try {
            response = Energy.cosemHlsGMACAuthentication(toProtectedSessionKey(guek), toProtectedSessionKey(gak), challenge,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthenticationTag();
    }

    @Override
    public byte[] decryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.ENCRYPT, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), apdu,
                    initializationVector, null);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getData();
    }

    @Override
    public IrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException {
        KeyDerivation[] signatureKeyCertChain = createKeyDerivationArray(deviceSignatureKeyCertChain);
        //TODO: remove this test hardcoded values and use the ones comming from the protocol
        String clientPrivateKeyAgreementKeyLabel = "vm-cosem-ka-s1-1";
        String caCertificateTest = "Energy CA certificate_certificate_ROOTCA_CERT";
        ProtectedSessionKey protectedSessionKey =
                null;
        try {
            protectedSessionKey = Energy.eekAgreeReceiver1e1s(getAtosSecuritySuite(securitySuite),
                    signatureKeyCertChain,
                    ephemeralKaKey,
                    signature,
                    new KeyLabel(clientPrivateKeyAgreementKeyLabel),
                    new KeyLabel(caCertificateTest),
                    kdfOtherInfo,
                    new KeyLabel(storageKeyLabel));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
        return new HsmEncryptedKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
    }

    @Override
    public byte[] generateRandom(int length) throws HsmBaseException {
        try {
            return Symmetric.rndGenerate(getRandomGenerator(), length).getData();
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public EEKAgreeResponse eekAgreeSender1e1s(int securitySuite, String hesSignatureKeyLabel, Certificate[] deviceKeyAgreementKeyCertChain, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException {

        KeyDerivation[] certChainFromReceiver = createKeyDerivationArray(deviceKeyAgreementKeyCertChain);
        //TODO: remove this test hardcoded values and use the ones comming from the protocol
        String clientPrivateSigningKeyLabel = "vm-cosem-sign-s1-1";
        String caCertificateTest = "Energy CA certificate_certificate_ROOTCA_CERT";

        com.atos.worldline.jss.api.custom.energy.EEKAgreeResponse eekAgreeResponse =
                null;
        try {
            eekAgreeResponse = Energy.eekAgreeSender1e1s(getAtosSecuritySuite(securitySuite),
                    new KeyLabel(clientPrivateSigningKeyLabel),
                    certChainFromReceiver,
                    new KeyLabel(caCertificateTest),
                    kdfOtherInfo,
                    new KeyLabel(storageKeyLabel));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
        return new EEKAgreeResponseImpl(new HsmEncryptedKey(eekAgreeResponse.getEek().getValue(), ((KeyLabel) eekAgreeResponse.getEek().getKek()).getValue()), eekAgreeResponse.getEphemeralKaKey(), eekAgreeResponse.getSignature());
    }

    @Override
    public final KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerate(int securitySuite, int keyIDForAgreement, String privateEccSigningKeyLabel, String mdmStorageKeyLabel) throws HsmBaseException {
        //TODO: implement CXO workflow/action to make use of it
        try {
            com.atos.worldline.jss.api.custom.energy.KeyRenewalAgree2EGenerateResponse keyRenewalAgree2EGenerateResponse = Energy.keyRenewalAgree2EGenerate(getAtosSecuritySuite(securitySuite), getAtosKeyIDForAgreement(keyIDForAgreement), getRandomGenerator(), new KeyLabel(privateEccSigningKeyLabel), new KeyLabel(mdmStorageKeyLabel));
            byte[] serializedPrivateEccKey = SerializationUtils.serialize(keyRenewalAgree2EGenerateResponse.getPrivateEccKey());
            return new KeyRenewalAgree2EGenerateResponseImpl(keyRenewalAgree2EGenerateResponse.getAgreementData(), keyRenewalAgree2EGenerateResponse.getSignature(), serializedPrivateEccKey);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public final IrreversibleKey keyRenewalAgree2EFinalise(int securitySuite, int keyIDForAgree, byte[] serializedPrivateEccKey, byte[] ephemeralEccPubKeyForSmAgreementData, byte[] signature, String caCertificateLabel, Certificate[] certificateChain, byte[] otherInfo, String storageKeyLabel) throws HsmBaseException {
        //TODO: implement CXO workflow/action to make use of it
        try {
            PrivateKeyToken privateKeyToken = SerializationUtils.deserialize(serializedPrivateEccKey);
            ProtectedSessionKey protectedSessionKey = Energy.keyRenewalAgree2EFinalise(getAtosSecuritySuite(securitySuite), getAtosKeyIDForAgreement(keyIDForAgree), privateKeyToken, ephemeralEccPubKeyForSmAgreementData, signature, new KeyLabel(caCertificateLabel), createKeyDerivationArray(certificateChain), otherInfo, new KeyLabel(storageKeyLabel));
            return new HsmEncryptedKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    /**
     * Used for TLS connection
     *
     * @param keyStore
     * @param password
     * @param clientTlsPrivateKeyAlias
     * @return
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    @Override
    public X509KeyManager getKeyManager(KeyStore keyStore, char[] password, String clientTlsPrivateKeyAlias) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return new HsmKeyManagerImpl(keyStore, password, clientTlsPrivateKeyAlias);
    }

    private ProtectedSessionKey toProtectedSessionKey(IrreversibleKey irreversibleKey) {
        return new ProtectedSessionKey(new KeyLabel(irreversibleKey.getKeyLabel()), irreversibleKey.getEncryptedKey());
    }

    private SecuritySuite getAtosSecuritySuite(int securitySuite) throws HsmBaseException {
        SecuritySuite atosSecuritySuite = SECURITY_SUITE_MAP.get(securitySuite);
        if(atosSecuritySuite == null) {
            throw new HsmBaseException("Security suite "+securitySuite+" is NOT SUPPORTED!");
        }
        return atosSecuritySuite;

    }

    private KeyIDForAgree getAtosKeyIDForAgreement(int keyIdForAgreement) throws HsmBaseException {
        KeyIDForAgree keyIDForAgree = KEY_ID_FOR_AGREE_MAP.get(keyIdForAgreement);
        if(keyIDForAgree == null) {
            throw new HsmBaseException("Key ID for agreement "+keyIdForAgreement+" is NOT SUPPORTED!");
        }
        return keyIDForAgree;

    }

}
