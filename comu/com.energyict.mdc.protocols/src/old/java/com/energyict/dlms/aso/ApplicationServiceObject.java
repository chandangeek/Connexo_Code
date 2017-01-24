package com.energyict.dlms.aso;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.CosemObjectFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * <pre>
 * The applicationServiceObject has three main objectives.
 * - Establish/maintain/release application associations
 * - Data transfer
 * - Layer management...
 * </pre>
 *
 * @author gna
 */
public class ApplicationServiceObject {

    public static final int ASSOCIATION_DISCONNECTED = 0;
    public static final int ASSOCIATION_PENDING = 1;
    public static final int ASSOCIATION_CONNECTED = 2;
    public static final int ASSOCIATION_READY_FOR_DISCONNECTION = 3;
    protected XdlmsAse xDlmsAse;
    protected AssociationControlServiceElement acse;
    protected SecurityContext securityContext;
    protected ProtocolLink protocolLink;
    protected int associationStatus;

    /**
     * Default constructor
     *
     * @param xDlmsAse        the used {@link com.energyict.dlms.aso.XdlmsAse}
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param securityContext the used {@link com.energyict.dlms.aso.SecurityContext}
     * @param contextId       the contextId which indicates longName or shortName communication
     */
    public ApplicationServiceObject(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId) {
        this(xDlmsAse, protocolLink, securityContext, contextId, null, null, null);
    }

    /**
     * Constructor with additional parameters
     *
     * @param xDlmsAse          the used {@link com.energyict.dlms.aso.XdlmsAse}
     * @param protocolLink      the used {@link com.energyict.dlms.ProtocolLink}
     * @param securityContext   the used {@link com.energyict.dlms.aso.SecurityContext}
     * @param contextId         the contextId which indicates longName or shortName communication
     * @param calledAPTitle     the calledApplicationProcessTitle
     * @param calledAEQualifier the calledApplicationEntityQualifier
     * @param callingAEQualifier the callingApplicationEntityQualifier. In security suite 1 and 2, this is the signature certificate of the client (ComServer)
     */
    public ApplicationServiceObject(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId,
                                    byte[] calledAPTitle, byte[] calledAEQualifier, byte[] callingAEQualifier) {
        this.xDlmsAse = xDlmsAse;
        this.protocolLink = protocolLink;
        this.securityContext = securityContext;
        this.acse = new AssociationControlServiceElement(this.xDlmsAse, contextId, securityContext);
        this.acse.setCallingApplicationProcessTitle(securityContext.getSystemTitle());
        this.acse.setCalledApplicationProcessTitle(calledAPTitle);
        this.acse.setCalledApplicationEntityQualifier(calledAEQualifier);
        this.acse.setCallingApplicationEntityQualifier(callingAEQualifier);
        this.associationStatus = ASSOCIATION_DISCONNECTED;
    }

    public ApplicationServiceObject(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext) {
        this.xDlmsAse = xDlmsAse;
        this.protocolLink = protocolLink;
        this.securityContext = securityContext;
        this.associationStatus = ASSOCIATION_DISCONNECTED;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public SecurityContext getSecurityContext() {
        return this.securityContext;
    }

    /**
     * @return the status of the current association(connected/disconnected/pending)
     */
    public int getAssociationStatus() {
        return this.associationStatus;
    }

    public void setAssociationState(int state) {
        this.associationStatus = state;
    }

    /*******************************************************************************************************
     * Application association management
     *******************************************************************************************************/

    /**
     * Create an ApplicationAssociation.
     * Depending on the securityLevel encrypted challenges will be used to authenticate the client and server
     * If aarqTimeout is not 0, use it to receive the AARE. Else, use the 'normal' timeout.
     *
     * @throws UnsupportedException    in case of unsupported security level
     * @throws IOException             timeout or other communication related error
     * @throws DLMSConnectionException association to the meter failed
     */
    public void createAssociation(int aarqTimeout) throws IOException, DLMSConnectionException {
        byte[] request = this.acse.createAssociationRequest();
        long normalTimeout = getDlmsConnection().getTimeout();
        if (isConfirmedAssociation()) {
            if (aarqTimeout != 0) {      //Use this timeout only for receiving the AARE
                getDlmsConnection().setTimeout(aarqTimeout);
            }
            byte[] response = getDlmsConnection().sendRequest(request);
            if (aarqTimeout != 0) {      //Use the normal timeout for further communication
                getDlmsConnection().setTimeout(normalTimeout);
            }
            this.acse.analyzeAARE(response);
            getSecurityContext().setResponseSystemTitle(this.acse.getRespondingAPTtitle());
            if (this.acse.hlsChallengeMatch()) {
                releaseAssociation();
                throw new ConnectionException("Invalid responding authenticationValue.");
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
            getDlmsConnection().sendUnconfirmedRequest(request);
            this.associationStatus = ASSOCIATION_CONNECTED;
        }
    }

    private DLMSConnection getDlmsConnection() {
        return this.protocolLink.getDLMSConnection();
    }

    public void createAssociation() throws IOException, DLMSConnectionException {
        createAssociation(0);
    }

    protected boolean isConfirmedAssociation() {
        return getDlmsConnection().getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriorityObject().needsResponse();
    }

    /**
     * If HighLevelSecurity/Authentication is enabled, then there are two more steps to take.
     * According to the level a different algorithm must be used to encrypt the challenges.
     *
     * @throws IOException
     */
    protected void handleHighLevelSecurityAuthentication() throws IOException {
        byte[] decryptedResponse;
        byte[] plainText;

        if (DLMSMeterConfig.OLD2.equalsIgnoreCase(this.protocolLink.getMeterConfig().getExtra())) {
            this.associationStatus = ASSOCIATION_PENDING;
        }

        //HLS3, 4, 5, 6 and 7 require a StoC (Server to Client challengte)
        if (this.acse.getRespondingAuthenticationValue() == null && (this.securityContext.getAuthenticationType().getLevel() > 2)) {
            silentDisconnect();
            throw new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                    ") requires the server to respond with a challenge.");
        }

        switch (this.securityContext.getAuthenticationType()) {
            case LOWEST_LEVEL:
            case LOW_LEVEL:
                this.associationStatus = ASSOCIATION_CONNECTED;
                break;
            case MAN_SPECIFIC_LEVEL:
                replyToHLSAuthentication(this.securityContext.getSecurityProvider().associationEncryptionByManufacturer(this.acse.getRespondingAuthenticationValue()));
                // the association object will fail if we don't get a proper response.
                this.associationStatus = ASSOCIATION_CONNECTED;
                break;
            case HLS3_MD5: {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    decryptedResponse = replyToHLSAuthentication(associationEncryption(plainText));
                    analyzeDecryptedResponse(decryptedResponse);
            }

            break;
            case HLS4_SHA1: {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    decryptedResponse = replyToHLSAuthentication(associationEncryption(plainText));
                    analyzeDecryptedResponse(decryptedResponse);
            }

            break;
            case HLS5_GMAC: {
                    decryptedResponse = replyToHLSAuthentication(this.securityContext.highLevelAuthenticationGMAC(this.acse.getRespondingAuthenticationValue()));
                    analyzeDecryptedResponse(decryptedResponse);
            }
            break;
            case HLS6_SHA256: {
                throw new UnsupportedException("HLS6 is currently not supported for V1 protocols");
            }
            case HLS7_ECDSA: {
                throw new UnsupportedException("HLS7 is currently not supported for V1 protocols");
            }
            default: {
                // should never get here
                silentDisconnect();
                throw new ConnectionException("Unknown authenticationLevel: " + this.securityContext.getAuthenticationLevel());
            }
        }
    }

    /**
     * Manually calculate the digest from the meter and compare it with the response you got from the meter.
     *
     * @param serverDigest is the response from the server to the reply_to_HLS_authentication
     * @throws IOException if the two challenges don't match, or if the HLSSecret could not be supplied, if it's not a valid algorithm or when there is no callingAuthenticationvalue
     */
    protected void analyzeDecryptedResponse(byte[] serverDigest) throws IOException {

        byte[] calculatedServerDigest = new byte[0];
        if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS3_MD5 || this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS4_SHA1) {
            byte[] plainText = ProtocolUtils.concatByteArrays(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
            calculatedServerDigest = associationEncryption(plainText);
        } else if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS5_GMAC) {
            calculatedServerDigest = this.securityContext.createHighLevelAuthenticationGMACResponse(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), serverDigest);
        } else if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS6_SHA256) {
            throw new UnsupportedException("HLS6 is currently not supported for V1 protocols");
        } else if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS7_ECDSA) {
            throw new UnsupportedException("HLS7 is currently not supported for V1 protocols");
        }

        if (!Arrays.equals(calculatedServerDigest, serverDigest)) {
            silentDisconnect();
            throw new ProtocolException("HighLevelAuthentication failed, client and server challenges do not match.");
        } else {
            this.associationStatus = ASSOCIATION_CONNECTED;
        }
    }

    private byte[] associationEncryption(byte[] plainText) throws IOException {
        try {
            return this.securityContext.associationEncryption(plainText);
        } catch (NoSuchAlgorithmException e) {
            silentDisconnect();
            throw new ProtocolException(this.securityContext.getAuthenticationType() + " algorithm isn't a valid algorithm type." + e.getMessage());
        }
    }

    /**
     * Send a reply to the server using the AssociationLN/SN method 'reply_to_HLS_authentication'
     *
     * @param digest - the 'encrypted' ServerToClient challenge
     * @return the encrypted response, which should contain the cToS authenticationValue
     * @throws IOException
     */
    protected byte[] replyToHLSAuthentication(byte[] digest) throws IOException {
        OctetString decryptedResponse = null;

        if ((this.acse.getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING)) {            // reply with AssociationLN
            AssociationLN aln = new AssociationLN(this.protocolLink);
            decryptedResponse = new OctetString(aln.replyToHLSAuthentication(digest), 0);
        } else if ((this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING)) {    // reply with AssociationSN
            AssociationSN asn = new CosemObjectFactory(this.protocolLink).getAssociationSN();
            byte[] response = asn.replyToHLSAuthentication(digest);
            if (response.length == 0) {
                return new byte[0];
            }
            decryptedResponse = new OctetString(response, 0);
        } else {
            throw new IllegalArgumentException("Invalid ContextId: " + this.acse.getContextId());
        }
        if (decryptedResponse.getOctetStr().length == 0) {
            return new byte[0];
        }
        return decryptedResponse.getOctetStr();
    }

    /**
     * Getter for the AssociationControlServiceElement object
     *
     * @return
     */
    public AssociationControlServiceElement getAssociationControlServiceElement() {
        return acse;
    }

    /**
     * Release the current association
     *
     * @throws IOException             timeout or other communication error
     * @throws AssociationControlServiceElement.ACSEParsingException unexpected end of RLRE
     * @throws DLMSConnectionException association release failed
     */
    public void releaseAssociation() throws IOException, DLMSConnectionException {
        this.associationStatus = ASSOCIATION_READY_FOR_DISCONNECTION;
        byte[] request = this.acse.releaseAssociationRequest();
        if (isConfirmedAssociation()) {
            byte[] response = getDlmsConnection().sendRequest(request);
            this.acse.analyzeRLRE(response);
            this.associationStatus = ASSOCIATION_DISCONNECTED;
        } else {
            getDlmsConnection().sendUnconfirmedRequest(request);
            this.associationStatus = ASSOCIATION_DISCONNECTED;
        }
    }

    @Override
    public String toString() {
        final String crlf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationServiceObject:").append(crlf);
        return sb.toString();
    }

    private void silentDisconnect() {
        try {
            releaseAssociation();
            getDlmsConnection().disconnectMAC();
        } catch (Exception e) {
            //do nothing
        }
    }
}
