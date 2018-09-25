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
import com.elster.jupiter.hsm.model.keys.IrreversibleKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTag;
import com.elster.jupiter.hsm.model.response.protocols.DataAndAuthenticationTagImpl;
import com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponse;
import com.elster.jupiter.hsm.model.response.protocols.EEKAgreeResponseImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.net.ssl.X509KeyManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

@Component(name = "com.elster.jupiter.impl.HsmEnergyServiceImpl", service = {HsmEnergyService.class, HsmProtocolService.class}, immediate = true, property = "name=" + HsmEnergyServiceImpl.COMPONENTNAME)
public class HsmEnergyServiceImpl implements HsmEnergyService, HsmProtocolService {

    private static final int SECURITY_SUITE0 = 0;
    private static final int SECURITY_SUITE1 = 1;
    private static final int SECURITY_SUITE2 = 2;
    private static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;
    /** Size of the frame counter when encoded. */
    private static final int FRAMECOUNTER_SIZE = 4;

    static final String COMPONENTNAME = "HsmEnergyServiceImpl";


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

    public HsmEncryptedKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException {
        try {
            KeyLabel newLabel = new KeyLabel(renewKeyRequest.getRenewLabel());
            ProtectedSessionKey protectedSessionKey = new ProtectedSessionKey(new KeyLabel(renewKeyRequest.getActualLabel()),renewKeyRequest.getActualKey());
            KeyRenewalResponse response = Energy.cosemKeyRenewal(renewKeyRequest.getRenewCapability().toProtectedSessionKeyCapability(),
                    protectedSessionKey,
                    newLabel,
                    getSessionKeyType(renewKeyRequest.getHsmKeyType()));
            ProtectedSessionKey psk = response.getMdmStorageKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
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
                throw new RuntimeException("Failed to create KeyDerivation array from provided certificate chain. "+e);
            }
        }
        return keyDerivations;
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
    public byte[] generateRandom(int length) throws HsmBaseException {
        try {
            return Symmetric.rndGenerate(RandomMode.PseudoRandomGenerator, length).getData();
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
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
    public EEKAgreeResponse eekAgreeSender1e1s(int securitySuite, String hesSignatureKeyLabel, Certificate[] deviceKeyAgreementKeyCertChain, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException {

        KeyDerivation[] certChainFromReceiver = createKeyDerivationArray(deviceKeyAgreementKeyCertChain);
        //TODO: remove this test hardcoded values and use the ones comming from the protocol
        String clientPrivateSigningKeyLabel = "vm-cosem-sign-s1-1";
        String caCertificateTest = "Energy CA certificate_certificate_ROOTCA_CERT";
        String storageKeyTest = "S-DB";

        com.atos.worldline.jss.api.custom.energy.EEKAgreeResponse eekAgreeResponse =
                null;
        try {
            eekAgreeResponse = Energy.eekAgreeSender1e1s(getAtosSecuritySuite(securitySuite),
                    new KeyLabel(clientPrivateSigningKeyLabel),
                    certChainFromReceiver,
                    new KeyLabel(caCertificateTest),
                    kdfOtherInfo,
                    new KeyLabel(storageKeyTest));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
        return new EEKAgreeResponseImpl(eekAgreeResponse);
    }

    @Override
    public IrreversibleKey eekAgreeReceiver1e1s(int securitySuite, Certificate[] deviceSignatureKeyCertChain, byte[] ephemeralKaKey, byte[] signature, String hesKaKeyLabel, String deviceCaCertificateLabel, byte[] kdfOtherInfo, String storageKeyLabel) throws HsmBaseException {
        KeyDerivation[] signatureKeyCertChain = createKeyDerivationArray(deviceSignatureKeyCertChain);
        //TODO: remove this test hardcoded values and use the ones comming from the protocol
        String clientPrivateKeyAgreementKeyLabel = "vm-cosem-ka-s1-1";
        String caCertificateTest = "Energy CA certificate_certificate_ROOTCA_CERT";
        String storageKeyTest = "S-DB";
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
                    new KeyLabel(storageKeyTest));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
        return new HsmEncryptedKey(protectedSessionKey.getValue(), ((KeyLabel) protectedSessionKey.getKek()).getValue());
    }

    @Override
    public X509KeyManager getKeyManager(KeyStore keyStore, char[] password) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return new HsmKeyManagerImpl(keyStore, password);
    }

    private SecuritySuite getAtosSecuritySuite(int securitySuite) throws HsmBaseException {
        if(securitySuite == SECURITY_SUITE0) {
            return SecuritySuite.SUITE0;
        } else if(securitySuite == SECURITY_SUITE1) {
            return SecuritySuite.SUITE1;
        } else if(securitySuite == SECURITY_SUITE2) {
            return SecuritySuite.SUITE2;
        }
        throw new HsmBaseException("Security suite "+securitySuite+" NOT SUPPORTED!");
    }

    private ProtectedSessionKey toProtectedSessionKey(IrreversibleKey irreversibleKey) {
        return new ProtectedSessionKey(new KeyLabel(irreversibleKey.getKeyLabel()), irreversibleKey.getEncryptedKey());
    }

}
