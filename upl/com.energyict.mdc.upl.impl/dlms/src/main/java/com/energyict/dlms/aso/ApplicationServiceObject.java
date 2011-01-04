package com.energyict.dlms.aso;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
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

    protected XdlmsAse xDlmsAse;
    protected AssociationControlServiceElement acse;
    protected SecurityContext securityContext;
    protected ProtocolLink protocolLink;

    private int associationStatus;
    public static final int ASSOCIATION_DISCONNECTED = 0;
    public static final int ASSOCIATION_PENDING = 1;
    public static final int ASSOCIATION_CONNECTED = 2;
    public static final int ASSOCIATION_READY_FOR_DISCONNECTION = 3;

    /**
     * Default constructor
     *
     * @param xDlmsAse        the used {@link com.energyict.dlms.aso.XdlmsAse}
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param securityContext the used {@link com.energyict.dlms.aso.SecurityContext}
     * @param contextId       the contextId which indicates longName or shortName communication
     */
    public ApplicationServiceObject(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId){
        this(xDlmsAse, protocolLink, securityContext, contextId, null, null);
    }

    /**
     * Constructor with additional parameters
     * @param xDlmsAse        the used {@link com.energyict.dlms.aso.XdlmsAse}
     * @param protocolLink    the used {@link com.energyict.dlms.ProtocolLink}
     * @param securityContext the used {@link com.energyict.dlms.aso.SecurityContext}
     * @param contextId       the contextId which indicates longName or shortName communication
     * @param calledAPTitle   the calledApplicationProcessTitle
     * @param calledAEQualifier the calledApplicationEntityQualifier
     */
    public ApplicationServiceObject(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId,
                                    byte[] calledAPTitle, byte[] calledAEQualifier){
        this.xDlmsAse = xDlmsAse;
        this.protocolLink = protocolLink;
        this.securityContext = securityContext;
        this.acse = new AssociationControlServiceElement(this.xDlmsAse, contextId, securityContext);
        this.acse.setCallingApplicationProcessTitle(securityContext.getSystemTitle());
        this.acse.setCalledApplicationProcessTitle(calledAPTitle);
        this.acse.setCalledApplicationEntityQualifier(calledAEQualifier);
        this.associationStatus = ASSOCIATION_DISCONNECTED;
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
     */
    public void createAssociation() throws IOException {
        byte[] request = this.acse.createAssociationRequest();
        byte[] response = this.protocolLink.getDLMSConnection().sendRequest(request);
        this.acse.analyzeAARE(response);
        getSecurityContext().setResponseSystemTitle(this.acse.getRespondingAPTtitle());

        if (!DLMSMeterConfig.OLD2.equalsIgnoreCase(this.protocolLink.getMeterConfig().getExtra())) {
            this.associationStatus = ASSOCIATION_CONNECTED;
        }

        handleHighLevelSecurityAuthentication();
    }

    /**
     * If HighLevelSecurity/Authentication is enabled, then there are two more steps to take.
     * According to the level a different algorithm must be used to encrypt the challenges.
     *
     * @throws IOException
     */
    protected void handleHighLevelSecurityAuthentication() throws IOException {
        byte[] encryptedResponse;
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
                throw new IOException("High level security 2 is not supported.");
            case HLS3_MD5: {
                if (this.acse.getRespondingAuthenticationValue() != null) {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    encryptedResponse = replyToHLSAuthentication(this.securityContext.associationEncryption(plainText));
                    analyzeEncryptedResponse(encryptedResponse);
                } else {
                    throw new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                            ") requires the server to respond with a challenge.");
                }
            }

            break;
            case HLS4_SHA1: {
                if (this.acse.getRespondingAuthenticationValue() != null) {
                    plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
                    encryptedResponse = replyToHLSAuthentication(this.securityContext.associationEncryption(plainText));
                    analyzeEncryptedResponse(encryptedResponse);
                } else {
                    throw new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                            ") requires the server to respond with a challenge.");
                }
            }

            break;
            case HLS5_GMAC: {

                if (this.acse.getRespondingAuthenticationValue() != null) {
                    encryptedResponse = replyToHLSAuthentication(this.securityContext.highLevelAuthenticationGMAC(this.acse.getRespondingAuthenticationValue()));
                    analyzeEncryptedResponse(encryptedResponse);
                } else {
                    throw new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                            ") requires the server to respond with a challenge.");
                }
            }break;
            default: {
                // should never get here
                throw new ConnectionException("Unknown authenticationLevel: " + this.securityContext.getAuthenticationLevel());
            }
        }
    }

    /**
     * Encrypt the clientToServer challenge and compare it with the encrypted response from the server
     *
     * @param encryptedResponse is the response from the server to the reply_to_HLS_authentication
     * @throws IOException if the two challenges don't match, or if the HLSSecret could be supplied, if it's not a valid algorithm or when there is no callingAuthenticationvalue
     */
    protected void analyzeEncryptedResponse(byte[] encryptedResponse) throws IOException {
        byte[] plainText = ProtocolUtils.concatByteArrays(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
        
        byte[] cToSEncrypted;
        // We have to make a distinction between the response from HLS5_GMAC or one of the below ones.
        if(this.securityContext.getAuthenticationType() != AuthenticationTypes.HLS5_GMAC){
            cToSEncrypted = this.securityContext.associationEncryption(plainText);
        } else {
            cToSEncrypted = this.securityContext.createHighLevelAuthenticationGMACResponse(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), encryptedResponse);
        }
        if (!Arrays.equals(cToSEncrypted, encryptedResponse)) {
            throw new IOException("HighLevelAuthentication failed, client and server challenges do not match.");
        } else {
            this.associationStatus = ASSOCIATION_CONNECTED;
        }
    }

    /**
     * Send a reply to the server using the AssociationLN/SN method 'reply_to_HLS_authentication'
     *
     * @param digest - the 'encrypted' ServerToClient challenge
     * @return the encrypted response, which should contain the cToS authenticationValue
     * @throws IOException
     */
    private byte[] replyToHLSAuthentication(byte[] digest) throws IOException {
        OctetString encryptedResponse = null;

        if ((this.acse.getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING)) {            // reply with AssociationLN
            AssociationLN aln = new AssociationLN(this.protocolLink);
            encryptedResponse = new OctetString(aln.replyToHLSAuthentication(digest));
        } else if ((this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING)) {    // reply with AssociationSN
            AssociationSN asn = new AssociationSN(this.protocolLink);
            encryptedResponse = new OctetString(asn.replyToHLSAuthentication(digest));
        } else {
            throw new IllegalArgumentException("Invalid ContextId: " + this.acse.getContextId());
        }


        return encryptedResponse.getContentBytes();
    }

    /**
     * Getter for the AssociationControlServiceElement object
     * @return
     */
    public AssociationControlServiceElement getAssociationControlServiceElement() {
        return acse;
    }

    /**
     * Release the current association
     *
     * @throws IOException
     */
    public void releaseAssociation() throws IOException {
        this.associationStatus = ASSOCIATION_READY_FOR_DISCONNECTION;
        byte[] request = this.acse.releaseAssociationRequest();
        byte[] response = this.protocolLink.getDLMSConnection().sendRequest(request);
        this.acse.analyzeRLRE(response);
        this.associationStatus = ASSOCIATION_DISCONNECTED;
    }

    @Override
    public String toString() {
        final String crlf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationServiceObject:").append(crlf);
        return sb.toString();
    }

}
