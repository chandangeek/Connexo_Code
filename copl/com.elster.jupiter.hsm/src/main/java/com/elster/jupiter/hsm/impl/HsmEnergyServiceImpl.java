/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.BlockMode;
import com.atos.worldline.jss.api.basecrypto.ChainingValue;
import com.atos.worldline.jss.api.basecrypto.HashAlgorithm;
import com.atos.worldline.jss.api.basecrypto.MAC;
import com.atos.worldline.jss.api.basecrypto.RandomMode;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.basecrypto.SymmetricMACVerifyResponse;
import com.atos.worldline.jss.api.custom.energy.AuthenticationMechanism;
import com.atos.worldline.jss.api.custom.energy.CosemAuthDataDecryptionResponse;
import com.atos.worldline.jss.api.custom.energy.CosemAuthDataEncryptionResponse;
import com.atos.worldline.jss.api.custom.energy.CosemHLSAuthenticationResponse;
import com.atos.worldline.jss.api.custom.energy.Energy;
import com.atos.worldline.jss.api.custom.energy.KeyIDForAgree;
import com.atos.worldline.jss.api.custom.energy.KeyRenewalResponse;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKey;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyType;
import com.atos.worldline.jss.api.custom.energy.SecurityControl;
import com.atos.worldline.jss.api.custom.energy.SecurityControlExtended;
import com.atos.worldline.jss.api.custom.energy.SecuritySuite;
import com.atos.worldline.jss.api.custom.energy.ServiceKeyInjectionResponse;
import com.atos.worldline.jss.api.key.AESKeyToken;
import com.atos.worldline.jss.api.key.KEKEncryptionMethod;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.PrivateKeyToken;
import com.atos.worldline.jss.api.key.SecretKey;
import com.atos.worldline.jss.api.key.derivation.CertificateChainX509KeyDerivation;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.HsmProtocolService;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.FUAKPassiveGenerationNotSupportedException;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.HsmKey;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.HsmRenewKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag;
import com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTagImpl;
import com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse;
import com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponseImpl;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalAgree2EGenerateResponse;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalAgree2EGenerateResponseImpl;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalMBusResponse;
import com.elster.jupiter.hsm.model.response.protocols.KeyRenewalMBusResponseImpl;
import com.elster.jupiter.hsm.model.response.protocols.MacResponse;
import com.elster.jupiter.hsm.model.response.protocols.MacResponseImpl;
import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.util.encoders.Hex;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509KeyManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.impl.HsmEnergyServiceImpl",
        service = {HsmEnergyService.class, HsmProtocolService.class},
        immediate = true, property = "name=" + HsmEnergyServiceImpl.COMPONENTNAME)
public class HsmEnergyServiceImpl implements HsmEnergyService, HsmProtocolService {

    private static final Logger logger = LoggerFactory.getLogger(HsmEnergyServiceImpl.class);

    private static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;

    /**
     * Size of the frame counter when encoded.
     */
    private static final int FRAMECOUNTER_SIZE = 4;

    static final String COMPONENTNAME = "HsmEnergyServiceImpl";

    static final Map<Integer, SecuritySuite> SECURITY_SUITE_MAP = new HashMap<Integer, SecuritySuite>() {{
        put(0, SecuritySuite.SUITE0);
        put(1, SecuritySuite.SUITE1);
        put(2, SecuritySuite.SUITE2);
    }};

    static final Map<Integer, KeyIDForAgree> KEY_ID_FOR_AGREE_MAP = new HashMap<Integer, KeyIDForAgree>() {{
        put(0, KeyIDForAgree.GLOBAL_UNICAST_ENC_KEY);
        put(1, KeyIDForAgree.GLOBAL_UNICAST_ENC_KEY);
        put(2, KeyIDForAgree.AUTHENTICATION_KEY);
        put(3, KeyIDForAgree.MASTER_KEY);
    }};

    private volatile HsmConfigurationService hsmConfigurationService;
    private volatile HsmEncryptionService hsmEncryptService;
    private volatile Clock clock;

    public HsmEnergyServiceImpl() {
        // for OSGI purposes
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public HsmKey importKey(ImportKeyRequest importKeyRequest) throws HsmBaseException, HsmNotConfiguredException {
        logger.debug("Import request:" + importKeyRequest);
        HsmConfiguration hsmConfiguration = hsmConfigurationService.getHsmConfiguration();
        if (importKeyRequest.getHsmKeyType().isReversible()) {
            return new ReversibleKeyImporter().importKey(importKeyRequest, hsmConfiguration, hsmEncryptService);
        }
        return new IreversibleKeyImporter().importKey(importKeyRequest, hsmConfiguration);

    }

    public HsmRenewKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException, FUAKPassiveGenerationNotSupportedException {
        try {
            KeyLabel newLabel = new KeyLabel(renewKeyRequest.getRenewLabel());
            ProtectedSessionKey protectedSessionKey = new ProtectedSessionKey(new KeyLabel(renewKeyRequest.getMasterKeyLabel()), renewKeyRequest.getMasterKey());

            if (isSecretRenewal(renewKeyRequest)) {
                KeyRenewalResponse response = Energy.cosemSecretRenewal(renewKeyRequest.getRenewCapability().toProtectedSessionKeyCapability(),
                        renewKeyRequest.getHsmKeyType().getKeySize(),
                        protectedSessionKey,
                        newLabel);
                ProtectedSessionKey psk = response.getMdmStorageKey();
                String kekLabel = ((KeyLabel) psk.getKek()).getValue();
                return new HsmRenewKey(response.getSmartMeterKey(), psk.getValue(), kekLabel);
            } else {
                final ProtectedSessionKeyCapability keyCapability = renewKeyRequest.getRenewCapability().toProtectedSessionKeyCapability();

                if (keyCapability.equals(ProtectedSessionKeyCapability.SM_WK_MBUSFWAUTH_RENEWAL)) {
                    throw new FUAKPassiveGenerationNotSupportedException("FUAK key renew is currently not supported by this functionality. Key will be renewed from the protocol command.");
                }

                KeyRenewalResponse response = Energy.cosemKeyRenewal(keyCapability,
                        protectedSessionKey,
                        newLabel,
                        getSessionKeyType(renewKeyRequest.getHsmKeyType()));
                ProtectedSessionKey psk = response.getMdmStorageKey();
                String kekLabel = ((KeyLabel) psk.getKek()).getValue();
                return new HsmRenewKey(response.getSmartMeterKey(), psk.getValue(), kekLabel);
            }
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    private ProtectedSessionKeyType getSessionKeyType(HsmKeyType hsmKeyType) throws HsmBaseException {
        int keySize = hsmKeyType.getKeySize();
        /**
         * This might be wrong but with no specs this is all I could do :)
         * Somehow it works E2E for 16 and 32 bytes. What we do for other sizes!? Hell knows!
         */
        if (HsmJssKeyType.AES.equals(hsmKeyType.getHsmJssKeyType())) {
            if (keySize == AES_KEY_LENGTH) {
                return ProtectedSessionKeyType.AES;
            }
            if (keySize == AES256_KEY_LENGTH) {
                return ProtectedSessionKeyType.AES_256;
            }
            throw new HsmBaseException("Could not determine session key type for key length (expected 16 or 32):" + keySize);
        }
        throw new HsmBaseException("Only AES device key accepted:" + keySize);
    }

    @Override
    public Message prepareServiceKey(String hexServiceKey, String kek, String hexKeyValue) throws HsmBaseException {
        try {
            Message serviceKey = new Message(Hex.decode(hexServiceKey));
            Message keyValue = new Message(Hex.decode(hexKeyValue));
            ProtectedSessionKey protectedSessionKey = new ProtectedSessionKey(new KeyLabel(kek), keyValue.getBytes());
            return new Message(Energy.prepareServiceKeyInjection(serviceKey.getBytes(), protectedSessionKey,
                    Date.from(clock.instant())));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public com.elster.jupiter.hsm.model.response.ServiceKeyInjectionResponse serviceKeyInjection(String hexData,
                                                                                                 String hexSignature,
                                                                                                 String verifyKey) throws HsmBaseException {
        try {
            Message data = new Message(Hex.decode(hexData));
            Message signature = new Message(Hex.decode(hexSignature));
            ServiceKeyInjectionResponse response = Energy.serviceKeyInjection(data.getBytes(), signature.getBytes(),
                    new KeyLabel(verifyKey));
            return new com.elster.jupiter.hsm.model.response.ServiceKeyInjectionResponseImpl(response.getServiceKey(),
                    response.getWarning());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    private boolean isSecretRenewal(RenewKeyRequest renewKeyRequest) {
        return renewKeyRequest.getHsmKeyType().getHsmJssKeyType().equals(HsmJssKeyType.AUTHENTICATION)
                || renewKeyRequest.getHsmKeyType().getHsmJssKeyType().equals(HsmJssKeyType.HLSECRET);
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
    public byte[] generateDigestSHA1(byte[] challenge, HsmIrreversibleKey hlsSecret) throws HsmBaseException {
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
    public byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
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
    public byte[] authenticateApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncryptWithAAD(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), additionalAuthenticationData, apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthTag();
    }

    @Override
    public byte[] encryptApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.ENCRYPT, getAtosSecuritySuite(securitySuite)), null, apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getData();
    }

    @Override
    public DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
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
    public byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
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
    public DataAndAuthenticationTag authenticateEncryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncryptWithAAD(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE_AND_ENCRYPT, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), additionalAuthenticationData, apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return new DataAndAuthenticationTagImpl(response.getData(), response.getAuthTag());
    }

    @Override
    public byte[] verifyAuthenticationDecryptApduWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecryptWithAAD(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE_AND_ENCRYPT, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), additionalAuthenticationData, apdu,
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
    public byte[] wrapMeterKeyForConcentrator(HsmIrreversibleKey meterKey, HsmIrreversibleKey concentratorKey) throws HsmBaseException {
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
    public void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
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
    public void verifyApduAuthenticationWithAAD(byte[] apdu, byte[] additionalAuthenticationData, byte[] authenticationTag, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecryptWithAAD(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE, getAtosSecuritySuite(securitySuite)), toProtectedSessionKey(gak), additionalAuthenticationData, apdu,
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
    public boolean verifyFramecounterHMAC(byte[] serverSysT, byte[] clientSysT, byte[] challenge, long framecounter, HsmIrreversibleKey gak, byte[] challengeResponse) throws HsmBaseException {
        final byte[] framecounterBytes = new byte[FRAMECOUNTER_SIZE];

        framecounterBytes[0] = (byte) ((framecounter >> 24) & 0xff);
        framecounterBytes[1] = (byte) ((framecounter >> 16) & 0xff);
        framecounterBytes[2] = (byte) ((framecounter >> 8) & 0xff);
        framecounterBytes[3] = (byte) (framecounter & 0xff);

        final byte[] macInputData = new byte[serverSysT.length + clientSysT.length + challenge.length + FRAMECOUNTER_SIZE];

        /* Data is defined as : serverSysT || clientSysT || challenge || frameCounter */
        System.arraycopy(serverSysT, 0, macInputData, 0, serverSysT.length);
        System.arraycopy(clientSysT, 0, macInputData, serverSysT.length, clientSysT.length);
        System.arraycopy(challenge, 0, macInputData, serverSysT.length + clientSysT.length, challenge.length);
        System.arraycopy(framecounterBytes, 0, macInputData, serverSysT.length + clientSysT.length + challenge.length, FRAMECOUNTER_SIZE);

        final MAC mac = new MAC(challengeResponse);

        try {
            final SecretKey ak = new AESKeyToken(AES_KEY_LENGTH, gak.getKey(), null, KEKEncryptionMethod.PROTECTED_SESSION_KEY, new KeyLabel(gak.getLabel()), KeyDerivation.FIXED_KEY_ARRAY);
            final SymmetricMACVerifyResponse response = Symmetric.verifyHMAC(ak, KeyDerivation.FIXED_KEY_ARRAY, macInputData, null, HashAlgorithm.SHA_256, BlockMode.SINGLE, mac);

            if (response != null) {
                return response.getResult();
            }

            return false;
        } catch (final FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public byte[] generateDigestMD5(byte[] challenge, HsmIrreversibleKey hlsSecret) throws HsmBaseException {
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
    public byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemHLSAuthenticationResponse response;
        try {
            response = Energy.cosemHlsGMACAuthentication(toProtectedSessionKey(guek), toProtectedSessionKey(gak), challenge,
                    initializationVector, getAtosSecuritySuite(securitySuite));
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthenticationTag();
    }

    @Override
    public byte[] generateDigestMechanism6(boolean isServerToClient, HsmIrreversibleKey hlsSecret, byte[] systemTitleClient, byte[] systemTitleServer, byte[] challengeServerToClient, byte[] challengeClientToServer) throws HsmBaseException {
        CosemHLSAuthenticationResponse response;
        try {
            response = Energy.cosemHlsAuthenticationMechanism6(isServerToClient, toProtectedSessionKey(hlsSecret), systemTitleClient, systemTitleServer, challengeServerToClient, challengeClientToServer);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthenticationTag();
    }

    @Override
    public byte[] decryptApdu(byte[] apdu, byte[] initializationVector, HsmIrreversibleKey gak, HsmIrreversibleKey guek, int securitySuite) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.ENCRYPT, getAtosSecuritySuite(securitySuite)), null, apdu,
                    initializationVector, null);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getData();
    }

    @Override
    public HsmIrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException {
        KeyDerivation[] signatureKeyCertChain = createKeyDerivationArray(deviceSignatureKeyCertChain);
        ProtectedSessionKey protectedSessionKey = null;

        try {
            protectedSessionKey = Energy.eekAgreeReceiver1e1s(getAtosSecuritySuite(securitySuite),
                    signatureKeyCertChain,
                    ephemeralKaKey,
                    signature,
                    new KeyLabel(hesKaKeyLabel),
                    new KeyLabel(deviceCaCertificateLabel),
                    kdfOtherInfo,
                    new KeyLabel(storageKeyLabel));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
        return new HsmIrreversibleKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
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
        com.atos.worldline.jss.api.custom.energy.EEKAgreeResponse eekAgreeResponse = null;

        try {
            eekAgreeResponse = Energy.eekAgreeSender1e1s(getAtosSecuritySuite(securitySuite),
                    new KeyLabel(hesSignatureKeyLabel),
                    certChainFromReceiver,
                    new KeyLabel(deviceCaCertificateLabel),
                    kdfOtherInfo,
                    new KeyLabel(storageKeyLabel));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
        return new EEKAgreeResponseImpl(new HsmIrreversibleKey(eekAgreeResponse.getEek().getValue(), ((KeyLabel) eekAgreeResponse.getEek().getKek()).getValue()), eekAgreeResponse.getEphemeralKaKey(), eekAgreeResponse.getSignature());
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
    public final HsmIrreversibleKey keyRenewalAgree2EFinalise(int securitySuite, int keyIDForAgree, byte[] serializedPrivateEccKey, byte[] ephemeralEccPubKeyForSmAgreementData, byte[] signature, String caCertificateLabel, Certificate[] certificateChain, byte[] otherInfo, String storageKeyLabel) throws HsmBaseException {
        //TODO: implement CXO workflow/action to make use of it
        try {
            PrivateKeyToken privateKeyToken = SerializationUtils.deserialize(serializedPrivateEccKey);
            ProtectedSessionKey protectedSessionKey = Energy.keyRenewalAgree2EFinalise(getAtosSecuritySuite(securitySuite), getAtosKeyIDForAgreement(keyIDForAgree), privateKeyToken, ephemeralEccPubKeyForSmAgreementData, signature, new KeyLabel(caCertificateLabel), createKeyDerivationArray(certificateChain), otherInfo, new KeyLabel(storageKeyLabel));
            return new HsmIrreversibleKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
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
     * @param certificateChain
     * @return
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    @Override
    public X509KeyManager getKeyManager(KeyStore keyStore, char[] password, String clientTlsPrivateKeyAlias, X509Certificate[] certificateChain) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return new HsmKeyManagerImpl(keyStore, password, clientTlsPrivateKeyAlias, certificateChain);
    }

    private ProtectedSessionKey toProtectedSessionKey(HsmIrreversibleKey hsmKey) {
        return new ProtectedSessionKey(new KeyLabel(hsmKey.getLabel()), hsmKey.getKey());
    }

    public final KeyRenewalMBusResponse renewMBusUserKey(byte[] method7apdu, byte[] initializationVector, HsmIrreversibleKey authKey, HsmIrreversibleKey encrKey, HsmIrreversibleKey defaultKey, int securitySuite) throws
            HsmBaseException {

        try {
            com.atos.worldline.jss.api.custom.energy.KeyRenewalMBusResponse response = Energy.renewMBusUserKey(toProtectedSessionKey(encrKey), method7apdu, initializationVector, toProtectedSessionKey(authKey), toProtectedSessionKey(defaultKey), getAtosSecuritySuite(securitySuite));
            ProtectedSessionKey protectedSessionKey = response.getMdmSmWK();
            HsmIrreversibleKey mdmSmWK = null;
            if (protectedSessionKey != null) {
                mdmSmWK = new HsmIrreversibleKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
            }
            return new KeyRenewalMBusResponseImpl(response.getSmartMeterKey(), response.getAuthenticationTag(), response.getMbusDeviceKey(), mdmSmWK, response.getMBusAuthTag());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to send MBus encryption keys using the cryptoserver: " + e.getMessage());
        }

    }

    public final KeyRenewalMBusResponse renewMBusFuakWithGCM(String workingKeyLabel, HsmIrreversibleKey defaultKey, byte[] mBusInitialVector) throws HsmBaseException {
        try {
            KeyLabel mdmDbStorageKey = new KeyLabel(workingKeyLabel);
            com.atos.worldline.jss.api.custom.energy.KeyRenewalMBusResponse response = Energy.renewMBusFuakWithGCM(mdmDbStorageKey, toProtectedSessionKey(defaultKey), mBusInitialVector);
            ProtectedSessionKey protectedSessionKey = response.getMdmSmWK();
            HsmIrreversibleKey mdmSmWK = new HsmIrreversibleKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
            return new KeyRenewalMBusResponseImpl(response.getSmartMeterKey(), response.getAuthenticationTag(), response.getMbusDeviceKey(), mdmSmWK, response.getMBusAuthTag());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to send Mbus FUAK using the cryptoserver: " + e.getMessage());
        }
    }

    public KeyRenewalMBusResponse renewMBusUserKeyWithGCM(HsmIrreversibleKey encrKey, byte[] apduTemplate, byte[] eMeterIV, HsmIrreversibleKey authKey, HsmIrreversibleKey defaultKey, byte[] mbusIV, int securitySuite) throws
            HsmBaseException {
        try {
            com.atos.worldline.jss.api.custom.energy.KeyRenewalMBusResponse response = Energy.renewMBusUserKeyWithGCM(toProtectedSessionKey(encrKey), apduTemplate, eMeterIV, toProtectedSessionKey(authKey), toProtectedSessionKey(defaultKey), mbusIV, getAtosSecuritySuite(securitySuite));
            return new KeyRenewalMBusResponseImpl(response.getSmartMeterKey(), response.getAuthenticationTag(), response.getMbusDeviceKey(), null, response.getMBusAuthTag());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to send Mbus P2 key using the cryptoserver: " + e.getMessage());
        }
    }

    public MacResponse generateMacFirstBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] data) throws HsmBaseException {
        try {
            com.atos.worldline.jss.api.custom.energy.MacResponse macResponse = Energy.generateMacFirstBlock(toProtectedSessionKey(firmwareUpdateAuthKey), data);
            return new MacResponseImpl(macResponse.getData(), macResponse.getInitVector());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to generate first block using the cryptoserver: " + e.getMessage());
        }
    }

    public MacResponse generateMacMiddleBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] state) throws HsmBaseException {
        try {
            com.atos.worldline.jss.api.custom.energy.MacResponse macResponse = Energy.generateMacMiddleBlock(toProtectedSessionKey(firmwareUpdateAuthKey), clearData, state);
            return new MacResponseImpl(macResponse.getData(), macResponse.getInitVector());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to generate first block using the cryptoserver: " + e.getMessage());
        }
    }

    public MacResponse generateMacLastBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] icv, byte[] state) throws HsmBaseException {
        try {
            com.atos.worldline.jss.api.custom.energy.MacResponse macResponse = Energy.generateMacLastBlock(toProtectedSessionKey(firmwareUpdateAuthKey), clearData, new ChainingValue(icv), state);
            return new MacResponseImpl(macResponse.getData(), macResponse.getInitVector());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to generate first block using the cryptoserver: " + e.getMessage());
        }
    }

    public MacResponse generateMacSingleBlock(HsmIrreversibleKey firmwareUpdateAuthKey, byte[] clearData, byte[] icv) throws HsmBaseException {
        try {
            com.atos.worldline.jss.api.custom.energy.MacResponse macResponse = Energy.generateMacSingleBlock(toProtectedSessionKey(firmwareUpdateAuthKey), clearData, new ChainingValue(icv));
            return new MacResponseImpl(macResponse.getData(), macResponse.getInitVector());
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("Failed to generate first block using the cryptoserver: " + e.getMessage());
        }
    }

    @Override
    public byte[] wrapServiceKey(byte[] preparedData, byte[] signature, String verifyKey) throws HsmBaseException {
        ServiceKeyInjectionResponse serviceKeyInjectionResponse;

        try {
            serviceKeyInjectionResponse = Energy.serviceKeyInjection(preparedData, signature, new KeyLabel(verifyKey));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException("HSM Function serviceKeyInjection failed: " + e.getMessage());
        }

        if (serviceKeyInjectionResponse == null) {
            throw new HsmBaseException("Incorrect signature, cannot write the service key");
        }

        String warning = serviceKeyInjectionResponse.getWarning();
        if (warning != null) {
            throw new HsmBaseException("Warning from the Cryptoserver because of time difference between prepare and inject: " + warning);
        }

        byte[] serviceKey = serviceKeyInjectionResponse.getServiceKey();
        if (serviceKey == null) {
            throw new HsmBaseException("Incorrect signature, cannot write the service key");
        }
        return serviceKey;
    }


    private SecuritySuite getAtosSecuritySuite(int securitySuite) throws HsmBaseException {
        SecuritySuite atosSecuritySuite = SECURITY_SUITE_MAP.get(securitySuite);
        if (atosSecuritySuite == null) {
            throw new HsmBaseException("Security suite " + securitySuite + " is NOT SUPPORTED!");
        }
        return atosSecuritySuite;
    }

    private KeyIDForAgree getAtosKeyIDForAgreement(int keyIdForAgreement) throws HsmBaseException {
        KeyIDForAgree keyIDForAgree = KEY_ID_FOR_AGREE_MAP.get(keyIdForAgreement);
        if (keyIDForAgree == null) {
            throw new HsmBaseException("Key ID for agreement " + keyIdForAgreement + " is NOT SUPPORTED!");
        }
        return keyIDForAgree;
    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) {
        this.hsmConfigurationService = hsmConfigurationService;
    }

    @Reference
    public void setHsmEncryptService(HsmEncryptionService hsmEncryptService) {
        this.hsmEncryptService = hsmEncryptService;
    }

}