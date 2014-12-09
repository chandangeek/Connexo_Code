package com.energyict.dlms.protocolimplv2;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.AuthenticationTypes;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.exception.ProtocolAuthenticationException;
import com.energyict.protocols.exception.ProtocolEncryptionException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolUtils;

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
                    throw new ProtocolAuthenticationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, "Invalid responding authenticationValue.");
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
            throw new ProtocolAuthenticationException(MessageSeeds.UNSUPPORTED_AUTHENTICATION_TYPE, "Manufacturer specific");
        }
    }

    private void analyzeAARE(byte[] response) {
        try {
            this.acse.analyzeAARE(response);
        } catch (ConnectionException e) {                        //Decryption failed
            throw new ProtocolEncryptionException(MessageSeeds.ENCRYPTION_ERROR);
        } catch (IOException e) {                                //Association failed
            throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
        } catch (DLMSConnectionException e) {                    //Invalid frame counter
            throw new CommunicationException(MessageSeeds.INCORRECT_FRAMECOUNTER_RECEIVED);
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
                    throw new ProtocolAuthenticationException(e, MessageSeeds.UNSUPPORTED_AUTHENTICATION_TYPE);
                } catch (IOException e) {
                    e.printStackTrace();
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
                        throw new ProtocolAuthenticationException(e, MessageSeeds.UNKNOWN_ENCRYPTION_ALGORITHM);
                    }
                    analyzeDecryptedResponse(decryptedResponse);
                } else {
                    throw new ProtocolAuthenticationException(MessageSeeds.INCORRECT_AUTHENTICATION_RESPONSE);
                }
            }

            break;
            case HLS4_SHA1: {
                if (this.acse.getRespondingAuthenticationValue() != null) {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    try {
                        decryptedResponse = replyToHLSAuthentication(this.securityContext.associationEncryption(plainText));
                    } catch (NoSuchAlgorithmException e) {
                        throw new ProtocolAuthenticationException(e, MessageSeeds.UNKNOWN_ENCRYPTION_ALGORITHM);
                    }
                    analyzeDecryptedResponse(decryptedResponse);
                } else {
                    throw new ProtocolAuthenticationException(MessageSeeds.INCORRECT_AUTHENTICATION_RESPONSE);
                }
            }

            break;
            case HLS5_GMAC: {

                if (this.acse.getRespondingAuthenticationValue() != null) {
                    decryptedResponse = replyToHLSAuthentication(this.securityContext.highLevelAuthenticationGMAC(this.acse.getRespondingAuthenticationValue()));
                    analyzeDecryptedResponse(decryptedResponse);
                } else {
                    throw new ProtocolAuthenticationException(MessageSeeds.INCORRECT_AUTHENTICATION_RESPONSE);
                }
            }
            break;
            default: {
                // should never get here
                throw new ProtocolAuthenticationException(MessageSeeds.UNSUPPORTED_AUTHENTICATION_TYPE, this.securityContext.getAuthenticationLevel());
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
                throw new ProtocolAuthenticationException(e, MessageSeeds.UNKNOWN_ENCRYPTION_ALGORITHM);
            }
        } else {
            cToSEncrypted = this.securityContext.createHighLevelAuthenticationGMACResponse(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), encryptedResponse);
        }
        if (!Arrays.equals(cToSEncrypted, encryptedResponse)) {
            throw new ProtocolAuthenticationException(MessageSeeds.AUTHENTICATION_FAILED);
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
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            } catch (IOException e) {
                throw new CommunicationException(MessageSeeds.NUMBER_OF_RETRIES_REACHED, getDlmsV2Connection().getMaxTries());
            }
            try {
                decryptedResponse = new OctetString(berEncodedData, 0);
            } catch (IOException e) {
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            }
        } else if ((this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING)) {    // reply with AssociationSN
            AssociationSN asn = new CosemObjectFactory(this.protocolLink).getAssociationSN();
            byte[] response;
            try {
                response = asn.replyToHLSAuthentication(digest);
            } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            } catch (IOException e) {
                throw new CommunicationException(MessageSeeds.NUMBER_OF_RETRIES_REACHED, getDlmsV2Connection().getMaxTries());
            }
            if (response.length == 0) {
                return new byte[0];
            }
            try {
                decryptedResponse = new OctetString(response, 0);
            } catch (IOException e) {
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
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
            throw new CommunicationException(MessageSeeds.PROTOCOL_DISCONNECT_FAILED, e);
        }
    }
}
