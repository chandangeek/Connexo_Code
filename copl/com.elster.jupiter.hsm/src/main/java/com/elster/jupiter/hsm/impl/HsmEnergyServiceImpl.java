package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.RandomMode;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.custom.energy.*;
import com.atos.worldline.jss.api.key.KeyLabel;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.impl.HsmEnergyServiceImpl", service = {HsmEnergyService.class, HsmProtocolService.class}, immediate = true, property = "name=" + HsmEnergyServiceImpl.COMPONENTNAME)
public class HsmEnergyServiceImpl implements HsmEnergyService, HsmProtocolService {

    private static final int SECURITY_SUITE0 = 0;
    private static final int SECURITY_SUITE1 = 1;
    private static final int SECURITY_SUITE2 = 2;
    private static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;

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

    @Override
    public byte[] generateDigestMD5(byte[] challenge, IrreversibleKey hlsSecret) throws HsmBaseException {
        CosemHLSAuthenticationResponse response;
        try {
            response = Energy.cosemHlsAuthentication(toProtectedSessionKey(hlsSecret), AuthenticationMechanism.MECHANISM3,
                    challenge);
        } catch (FunctionFailedException ffe) {
            //TODO: add also the HSM related messageSeed to the exception. This should be applied to all "HsmException" exceptions thrown in this class
            throw new HsmBaseException(ffe);
        }
        return response.getAuthenticationTag();
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
    public byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
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
    public byte[] authenticateApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE, SecuritySuite.SUITE0), toProtectedSessionKey(gak), apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getAuthTag();
    }

    @Override
    public byte[] encryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.ENCRYPT, SecuritySuite.SUITE0), toProtectedSessionKey(gak), apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getData();
    }

    @Override
    public DataAndAuthenticationTag authenticateEncryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
        CosemAuthDataEncryptionResponse response;
        try {
            response = Energy.cosemAuthDataEncrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE_AND_ENCRYPT, SecuritySuite.SUITE0), toProtectedSessionKey(gak), apdu,
                    initializationVector);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return new DataAndAuthenticationTagImpl(response.getData(), response.getAuthTag());
    }

    @Override
    public void verifyApduAuthentication(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE, SecuritySuite.SUITE0), toProtectedSessionKey(gak), apdu,
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
    public byte[] decryptApdu(byte[] apdu, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.ENCRYPT, SecuritySuite.SUITE0), toProtectedSessionKey(gak), apdu,
                    initializationVector, null);
        } catch (FunctionFailedException ffe) {
            throw new HsmBaseException(ffe);
        }
        return response.getData();
    }

    @Override
    public byte[] verifyAuthenticationDecryptApdu(byte[] apdu, byte[] authenticationTag, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmBaseException {
        CosemAuthDataDecryptionResponse response;
        try {
            response = Energy.cosemAuthDataDecrypt(toProtectedSessionKey(guek),
                    new SecurityControlExtended(SecurityControl.AUTHENTICATE_AND_ENCRYPT, SecuritySuite.SUITE0), toProtectedSessionKey(gak), apdu,
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
        return false;
    }

    private ProtectedSessionKey toProtectedSessionKey(IrreversibleKey hlsSecret) {
        return new ProtectedSessionKey(new KeyLabel(hlsSecret.getKeyLabel()),hlsSecret.getEncryptedKey());
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

}
