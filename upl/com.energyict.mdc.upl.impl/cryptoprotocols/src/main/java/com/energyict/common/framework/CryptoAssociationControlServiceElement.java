package com.energyict.common.framework;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.DataAndAuthenticationTag;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.HsmException;
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
        IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(getSecurityContext().getSecurityProvider().getGlobalKey());
        IrreversibleKey ak = IrreversibleKeyImpl.fromByteArray(getSecurityContext().getSecurityProvider().getAuthenticationKey());
        DataAndAuthenticationTag dataAndAuthenticationTag;
        try {
            dataAndAuthenticationTag = Services.hsmService().authenticateEncryptApduWithAAD(userInformation, new byte[0], getInitialVector(), ak, ek, getSecurityContext().getSecuritySuite());
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unexpectedHsmProtocolError(new NestedIOException(e));
        }

        byte[] part = ProtocolTools.concatByteArrays(generateSecurityHeader(), dataAndAuthenticationTag.getData(), dataAndAuthenticationTag.getAuthenticationTag());
        userInformation = new byte[part.length + 2];
        userInformation[0] = DLMSCOSEMGlobals.AARE_GLOBAL_INITIATE_REQUEST_TAG;     //Add tag and length
        userInformation[1] = (byte) part.length;
        System.arraycopy(part, 0, userInformation, 2, part.length);
        return userInformation;
    }

    @Override
    protected byte[] decrypt(byte[] authenticationTag, byte[] cipheredText, byte[] frameCounter, byte securityControl) throws ConnectionException {
        if (!isSecurityAplied(securityControl)) {
            return cipheredText;  //No encryption, no authentication
        }

        IrreversibleKey ek = IrreversibleKeyImpl.fromByteArray(getSecurityContext().getSecurityProvider().getGlobalKey());
        IrreversibleKey ak = IrreversibleKeyImpl.fromByteArray(getSecurityContext().getSecurityProvider().getAuthenticationKey());
        try {
            return Services.hsmService().verifyAuthenticationDecryptApduWithAAD(cipheredText, new byte[0], authenticationTag, getInitialVector(getRespondingAPTtitle(), frameCounter), ak, ek, getSecurityContext().getSecuritySuite());
        } catch (HsmException e) {
            throw ConnectionCommunicationException.unexpectedHsmProtocolError(new NestedIOException(e));
        }
    }

    /**
     * Bit 4 indicates authentication, bit 5 indicates encryption is applied
     */
    private boolean isSecurityAplied(byte securityControl) {
        boolean auth = ((securityControl & 0x10) == 0x10);
        boolean encr = ((securityControl & 0x20) == 0x20);
        return auth || encr;
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