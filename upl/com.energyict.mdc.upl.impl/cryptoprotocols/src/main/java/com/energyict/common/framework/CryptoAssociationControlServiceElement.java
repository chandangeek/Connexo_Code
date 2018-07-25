package com.energyict.common.framework;

import com.atos.worldline.jss.api.custom.energy.SecurityControl;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;

/**
 * Extension of the ACSE. This uses the HSM to encrypt/decrypt the user information field of the AARQ/AARE/RLRQ/RLRE
 * <p/>
 * Copyrights EnergyICT
 * Author: khe
 */
public class CryptoAssociationControlServiceElement extends AssociationControlServiceElement {

    /**
     * Create a new instance of the AssociationControlServiceElement
     *
     * @param xDlmsAse        - the xDLMS_ASE
     * @param contextId       - the applicationContextId which indicates which type of reference(LN/SN) and the use of ciphering
     * @param securityContext - the used {@link com.energyict.dlms.aso.SecurityContext}
     */
    public CryptoAssociationControlServiceElement(XdlmsAse xDlmsAse, int contextId, SecurityContext securityContext) {
        super(xDlmsAse, contextId, securityContext);
    }

    @Override
    protected byte[] encryptAndAuthenticateUserInformation(byte[] userInformation) {
//        IrreversibleKey ek = IrreversibleKey.fromByteArray(getSecurityContext().getSecurityProvider().getGlobalKey());
//        IrreversibleKey ak = IrreversibleKey.fromByteArray(getSecurityContext().getSecurityProvider().getAuthenticationKey());
//        DataAndAuthenticationTag dataAndAuthenticationTag;
//        try {
//            dataAndAuthenticationTag = ProtocolService.INSTANCE.get().authenticateEncryptApdu(userInformation, getInitialVector(), ak, ek);
//        } catch (HsmException e) {
//            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
//        }
//
//        byte[] part = ProtocolTools.concatByteArrays(generateSecurityHeader(), dataAndAuthenticationTag.getData(), dataAndAuthenticationTag.getAuthenticationTag());
//        userInformation = new byte[part.length + 2];
//        userInformation[0] = DLMSCOSEMGlobals.AARE_GLOBAL_INITIATE_REQUEST_TAG;     //Add tag and length
//        userInformation[1] = (byte) part.length;
//        System.arraycopy(part, 0, userInformation, 2, part.length);
//        return userInformation;
        //TODO: use the available HSMEncryptionKey
        return null;
    }

    @Override
    protected byte[] decrypt(byte[] authenticationTag, byte[] cipheredText, byte[] frameCounter, byte securityControl) throws ConnectionException {
        if (getSecurityControl(securityControl) == null) {
            return cipheredText;  //No encryption, no authentication
        }

        byte[] irreversibleKey = new byte[1];//TODO: retreive it from security accessor
        String keyLabel = "";//TODO: retreive it from security accessor
        HsmEncryptedKey hsmEncryptedKey = new HsmEncryptedKey(irreversibleKey, keyLabel);
//        IrreversibleKey ek = IrreversibleKey.fromByteArray(getSecurityContext().getSecurityProvider().getGlobalKey());
//        IrreversibleKey ak = IrreversibleKey.fromByteArray(getSecurityContext().getSecurityProvider().getAuthenticationKey());
//        try {
//            return ProtocolService.INSTANCE.get().verifyAuthenticationDecryptApdu(cipheredText, authenticationTag, getInitialVector(getRespondingAPTtitle(), frameCounter), ak, ek);
//        } catch (HsmException e) {
//            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
//        }
        //TODO: use the available HSMEncryptionKey
        return null;
    }

    /**
     * Bit 4 indicates authentication, bit 5 indicates encryption is applied
     */
    private SecurityControl getSecurityControl(byte securityControl) {
        boolean auth = ((securityControl & 0x10) == 0x10);
        boolean encr = ((securityControl & 0x20) == 0x20);

        if (auth && encr) {
            return SecurityControl.AUTHENTICATE_AND_ENCRYPT;
        } else if (auth) {
            return SecurityControl.AUTHENTICATE;
        } else if (encr) {
            return SecurityControl.ENCRYPT;
        } else {
            return null;
        }
    }

    private byte[] generateSecurityHeader() {
        byte securityControlByte = (byte) 0x30;     //Security control byte 0x30 means authentication and encryption
        securityControlByte |= (this.getSecurityContext().getSecuritySuite() & 0x0F); // add the securitySuite to bits 0 to 3
        return ProtocolTools.concatByteArrays(new byte[]{securityControlByte}, getSecurityContext().getFrameCounterInBytes());
    }

    private byte[] getInitialVector() {
        byte[] systemTitle = Arrays.copyOf(getSecurityContext().getSystemTitle(), SecurityContext.SYSTEM_TITLE_LENGTH);
        byte[] fc = getSecurityContext().getFrameCounterInBytes();
        return getInitialVector(systemTitle, fc);
    }

    private byte[] getInitialVector(byte[] systemTitle, byte[] fc) {
        byte[] iv = new byte[systemTitle.length + fc.length];
        System.arraycopy(systemTitle, 0, iv, 0, systemTitle.length);
        System.arraycopy(fc, 0, iv, systemTitle.length, fc.length);
        return iv;
    }
}