package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.DataAndAuthenticationTag;
import com.energyict.mdc.upl.crypto.HsmProtocolService;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.protocol.exceptions.HsmException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

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
    public byte[] generateDigestGMAC(byte[] challenge, byte[] initializationVector, IrreversibleKey gak, IrreversibleKey guek) throws HsmException {
        try {
            return actual.generateDigestGMAC(challenge, initializationVector, adaptUplKeyToHsmKey(gak), adaptUplKeyToHsmKey(guek));
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

    private com.elster.jupiter.hsm.model.keys.IrreversibleKey adaptUplKeyToHsmKey(IrreversibleKey irreversibleKey) {
        return new com.elster.jupiter.hsm.model.keys.IrreversibleKey() {
            @Override
            public byte[] getEncryptedKey() {
                return irreversibleKey.getEncryptedKey();
            }

            @Override
            public String getKeyLabel() {
                return irreversibleKey.getKeyLabel();
            }
        };
    }

}
