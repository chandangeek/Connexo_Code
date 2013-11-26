package com.energyict.dlms.protocolimplv2;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.protocol.*;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This V2 version of the ApplicationServiceObject does not throw IOExceptions. Error handling is already done in the connection layer.
 * If there is a problem, a proper ComServer runtime exception is thrown.
 * <p/>
 * The applicationServiceObject has three main objectives.
 * - Establish/maintain/release application associations
 * - Data transfer
 * - Layer management...
 *
 * @author gna, khe
 */
public class ApplicationServiceObjectV2 extends ApplicationServiceObject {

    public ApplicationServiceObjectV2(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId) {
        super(xDlmsAse, protocolLink, securityContext, contextId);
    }

    public ApplicationServiceObjectV2(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId, byte[] calledAPTitle, byte[] calledAEQualifier) {
        super(xDlmsAse, protocolLink, securityContext, contextId, calledAPTitle, calledAEQualifier);
    }

    /**
     * Create an ApplicationAssociation.
     * Depending on the securityLevel encrypted challenges will be used to authenticate the client and server
     * If aarqTimeout is not 0, use it to receive the AARE. Else, use the 'normal' timeout.
     */
    public void createAssociation(int aarqTimeout) {
        try {
            byte[] request = this.acse.createAssociationRequest();
            long normalTimeout = getDlmsV2Connection().getTimeout();
            if (isConfirmedAssociation()) {
                if (aarqTimeout != 0) {      //Use this timeout only for receiving the AARE
                    getDlmsV2Connection().setTimeout(aarqTimeout);
                }
                byte[] response = getDlmsV2Connection().sendRequest(request);
                if (aarqTimeout != 0) {      //Use the normal timeout for further communication
                    getDlmsV2Connection().setTimeout(normalTimeout);
                }
                analyzeAARE(response);
                getSecurityContext().setResponseSystemTitle(this.acse.getRespondingAPTtitle());
                if (this.acse.hlsChallengeMatch()) {
                    releaseAssociation();
                    ConnectionException connectionException = new ConnectionException("Invalid responding authenticationValue.");
                    throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(connectionException);
                }
                if (!DLMSMeterConfig.OLD2.equalsIgnoreCase(this.protocolLink.getMeterConfig().getExtra())) {
                    this.associationStatus = ASSOCIATION_CONNECTED;
                }

                if (getSecurityContext().isDedicatedCiphering()) {
                    // if dedicated ciphering is used, then a new FrameCounter is used for each session
                    getSecurityContext().setFrameCounterInitialized(false);
                }
                handleHighLevelSecurityAuthentication();
            } else {
                getDlmsV2Connection().sendUnconfirmedRequest(request);
                this.associationStatus = ASSOCIATION_CONNECTED;
            }
        } catch (UnsupportedException e) {
            throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("AuthenticationAccessLevel", "Manufacturer specific");
        }
    }

    private void analyzeAARE(byte[] response) {
        try {
            this.acse.analyzeAARE(response);
        } catch (ConnectionException e) {                        //Decryption failed
            throw MdcManager.getComServerExceptionFactory().createDataEncryptionException();
        } catch (IOException e) {                                //Association failed
            throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(e);
        } catch (DLMSConnectionException e) {                    //Invalid frame counter
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(new NestedIOException(e));
        }
    }

    private DlmsV2Connection getDlmsV2Connection() {
        return (DlmsV2Connection) this.protocolLink.getDLMSConnection();
    }

    public void createAssociation() {
        createAssociation(0);
    }

    /**
     * If HighLevelSecurity/Authentication is enabled, then there are two more steps to take.
     * According to the level a different algorithm must be used to encrypt the challenges.
     */
    protected void handleHighLevelSecurityAuthentication() throws UnsupportedException {
        byte[] decryptedResponse;
        byte[] plainText;

        if (DLMSMeterConfig.OLD2.equalsIgnoreCase(this.protocolLink.getMeterConfig().getExtra())) {
            this.associationStatus = ASSOCIATION_PENDING;
        }

        switch (this.securityContext.getAuthenticationType()) {
            case LOWEST_LEVEL:
            case LOW_LEVEL:
                this.associationStatus = ASSOCIATION_CONNECTED;
                break;
            case MAN_SPECIFIC_LEVEL:
                try {
                    replyToHLSAuthentication(this.securityContext.getSecurityProvider().associationEncryptionByManufacturer(this.acse.getRespondingAuthenticationValue()));
                } catch (UnsupportedException e) {
                    throw e;        //Security level not supported
                } catch (IOException e) {
                    throw MdcManager.getComServerExceptionFactory().createCipheringException(e);
                }
                // the association object will fail if we don't get a proper response.
                this.associationStatus = ASSOCIATION_CONNECTED;
                break;
            case HLS3_MD5: {
                if (this.acse.getRespondingAuthenticationValue() != null) {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    try {
                        decryptedResponse = replyToHLSAuthentication(this.securityContext.associationEncryption(plainText));
                    } catch (NoSuchAlgorithmException e) {
                        throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
                    }
                    analyzeDecryptedResponse(decryptedResponse);
                } else {
                    ConnectionException connectionException = new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                            ") requires the server to respond with a challenge.");
                    throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(connectionException);
                }
            }

            break;
            case HLS4_SHA1: {
                if (this.acse.getRespondingAuthenticationValue() != null) {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    try {
                        decryptedResponse = replyToHLSAuthentication(this.securityContext.associationEncryption(plainText));
                    } catch (NoSuchAlgorithmException e) {
                        throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
                    }
                    analyzeDecryptedResponse(decryptedResponse);
                } else {
                    ConnectionException connectionException = new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                            ") requires the server to respond with a challenge.");
                    throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(connectionException);
                }
            }

            break;
            case HLS5_GMAC: {

                if (this.acse.getRespondingAuthenticationValue() != null) {
                    decryptedResponse = replyToHLSAuthentication(this.securityContext.highLevelAuthenticationGMAC(this.acse.getRespondingAuthenticationValue()));
                    analyzeDecryptedResponse(decryptedResponse);
                } else {
                    ConnectionException connectionException = new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                            ") requires the server to respond with a challenge.");
                    throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(connectionException);
                }
            }
            break;
            default: {
                // should never get here
                throw MdcManager.getComServerExceptionFactory().createUnsupportedPropertyValueException("AuthenticationAccessLevel", String.valueOf(this.securityContext.getAuthenticationLevel()));
            }
        }
    }

    /**
     * Calculate the digest from the meter and compare it with the response you got from the meter.
     *
     * @param encryptedResponse is the response from the server to the reply_to_HLS_authentication
     */
    protected void analyzeDecryptedResponse(byte[] encryptedResponse) throws UnsupportedException {

        byte[] cToSEncrypted;
        // We have to make a distinction between the response from HLS5_GMAC or one of the below ones.
        if (this.securityContext.getAuthenticationType() != AuthenticationTypes.HLS5_GMAC) {
            byte[] plainText = ProtocolUtils.concatByteArrays(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
            try {
                cToSEncrypted = this.securityContext.associationEncryption(plainText);
            } catch (NoSuchAlgorithmException e) {
                throw MdcManager.getComServerExceptionFactory().createDataEncryptionException(e);
            }
        } else {
            cToSEncrypted = this.securityContext.createHighLevelAuthenticationGMACResponse(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), encryptedResponse);
        }
        if (!Arrays.equals(cToSEncrypted, encryptedResponse)) {
            IOException ioException = new IOException("HighLevelAuthentication failed, client and server challenges do not match.");
            throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(ioException);
        } else {
            this.associationStatus = ASSOCIATION_CONNECTED;
        }
    }

    /**
     * Send a reply to the server using the AssociationLN/SN method 'reply_to_HLS_authentication'
     *
     * @param digest - the 'encrypted' ServerToClient challenge
     * @return the encrypted response, which should contain the cToS authenticationValue
     */
    protected byte[] replyToHLSAuthentication(byte[] digest) {
        OctetString decryptedResponse;

        if ((this.acse.getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING)) {            // reply with AssociationLN
            AssociationLN aln = new AssociationLN(this.protocolLink);
            byte[] berEncodedData;
            try {
                berEncodedData = aln.replyToHLSAuthentication(digest);
            } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(e);
            } catch (IOException e) {
                throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, getDlmsV2Connection().getMaxRetries() + 1);
            }
            try {
                decryptedResponse = new OctetString(berEncodedData, 0);
            } catch (IOException e) {
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(e);
            }
        } else if ((this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING)) {    // reply with AssociationSN
            AssociationSN asn = new CosemObjectFactory(this.protocolLink).getAssociationSN();
            byte[] response;
            try {
                response = asn.replyToHLSAuthentication(digest);
            } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(e);
            } catch (IOException e) {
                throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, getDlmsV2Connection().getMaxRetries() + 1);
            }
            if (response.length == 0) {
                return new byte[0];
            }
            try {
                decryptedResponse = new OctetString(response, 0);
            } catch (IOException e) {
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(e);
            }
        } else {
            throw new IllegalArgumentException("Invalid ContextId: " + this.acse.getContextId());
        }
        if (decryptedResponse.getOctetStr().length == 0) {
            return new byte[0];
        }
        return decryptedResponse.getOctetStr();
    }

    /**
     * Release the current association
     */
    public void releaseAssociation() {
        this.associationStatus = ASSOCIATION_READY_FOR_DISCONNECTION;
        byte[] request = this.acse.releaseAssociationRequest();
        if (isConfirmedAssociation()) {
            byte[] response = getDlmsV2Connection().sendRequest(request);
            analyzeRLRE(response);
            this.associationStatus = ASSOCIATION_DISCONNECTED;
        } else {
            getDlmsV2Connection().sendUnconfirmedRequest(request);
            this.associationStatus = ASSOCIATION_DISCONNECTED;
        }
    }

    private void analyzeRLRE(byte[] response) {
        try {
            this.acse.analyzeRLRE(response);
        } catch (DLMSConnectionException | AssociationControlServiceElement.ACSEParsingException e) {
            throw MdcManager.getComServerExceptionFactory().createProtocolDisconnectFailed(e);    //Association release failed
        }
    }
}
