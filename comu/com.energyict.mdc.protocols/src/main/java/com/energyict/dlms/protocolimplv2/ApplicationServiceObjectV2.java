/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.protocolimplv2;

import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.protocols.exception.ProtocolAuthenticationException;
import com.energyict.protocols.exception.ProtocolEncryptionException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.naming.SecurityPropertySpecName;
import com.energyict.protocols.util.ProtocolUtils;

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
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.encryption.asymetric.signature.ECDSASignatureImpl;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * This V2 version of the ApplicationServiceObject does not throw IOExceptions. Error handling is already done in the connection layer.
 * If there is a problem, a proper ComServer runtime exception is thrown.
 * <p>
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

    public ApplicationServiceObjectV2(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityContext securityContext, int contextId, byte[] calledAPTitle, byte[] calledAEQualifier, byte[] callingAEQualifier) {
        super(xDlmsAse, protocolLink, securityContext, contextId, calledAPTitle, calledAEQualifier, callingAEQualifier);
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
                    silentDisconnect();
                    ConnectionException connectionException = new ConnectionException("Invalid responding authenticationValue.");
                    throw new CommunicationException(MessageSeeds.PROTOCOL_DISCONNECT_FAILED, connectionException);
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
            handleRespondingAEQualifier();
        } catch (ConnectionException e) {                        //Decryption failed
            throw new ProtocolEncryptionException(MessageSeeds.ENCRYPTION_ERROR);
        } catch (IOException e) {                                //Association failed
            if (e.getMessage().contains(AssociationControlServiceElement.REFUSED_BY_THE_VDE_HANDLER)
                    || e.getMessage().contains(AssociationControlServiceElement.ACSE_SERVICE_PROVIDER_NO_REASON_GIVEN)
                    || e.getMessage().contains(AssociationControlServiceElement.ACSE_SERVICE_USER_NO_REASON_GIVEN)
                    ) {
                //Association already open, retry mechanism in the protocols will be used
                throw DLMSIOExceptionHandler.handle(e, 0);
            } else {
                //Association failed, abort
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            }
            // throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
        } catch (DLMSConnectionException e) {                    //Invalid frame counter
            throw new CommunicationException(MessageSeeds.INCORRECT_FRAMECOUNTER_RECEIVED);
        }
    }


    /**
     * The (optional) responding-AE-qualifier is the certificate of the server for digital signature.
     * <p>
     * If we received the server signing certificate, there's two possible scenario's:
     * - if it was already configured as a general property, compare the certificates, they should match.
     * - if it was not yet configured as a general property, start using the received certificate in this session.
     */
    private void handleRespondingAEQualifier() {
        if (acse.getRespondingApplicationEntityQualifier() != null) {
            if (getSecurityContext().getSecurityProvider() instanceof GeneralCipheringSecurityProvider) {
                GeneralCipheringSecurityProvider generalCipheringSecurityProvider = (GeneralCipheringSecurityProvider) getSecurityContext().getSecurityProvider();
                if (generalCipheringSecurityProvider.getServerSignatureCertificate() == null) {
                    generalCipheringSecurityProvider.setServerSignatureCertificate(parseEncodedCertificate());
                } else {
                    byte[] configuredCertificate;
                    try {
                        configuredCertificate = generalCipheringSecurityProvider.getServerSignatureCertificate().getEncoded();
                    } catch (CertificateEncodingException e) {
                        silentDisconnect();
                        throw DeviceConfigurationException.missingProperty(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString());
                    }

                    if (!Arrays.equals(acse.getRespondingApplicationEntityQualifier(), configuredCertificate)) {
                        silentDisconnect();
                        ProtocolException protocolException = new ProtocolException("The received server signing certificate does not match the certificate that is configured in the general properties.");
                        throw new ConnectionCommunicationException(MessageSeeds.AUTHENTICATION_FAILED, protocolException);
                    }
                }
            }
        }
    }

    private X509Certificate parseEncodedCertificate() {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(acse.getRespondingApplicationEntityQualifier());
            return (X509Certificate) certFactory.generateCertificate(in);
        } catch (CertificateException e) {
            silentDisconnect();
            ProtocolException protocolException = new ProtocolException("Received an invalid server signing certificate, should be an ASN.1 DER encoded X.509 v3 certificate.");
            throw new ConnectionCommunicationException(MessageSeeds.AUTHENTICATION_FAILED, protocolException);
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

        //HLS3, 4, 5, 6 and 7 require a StoC (Server to Client challengte)
        if (this.acse.getRespondingAuthenticationValue() == null && (this.securityContext.getAuthenticationType().getLevel() > 2)) {
            silentDisconnect();
            ConnectionException connectionException = new ConnectionException("No challenge was responded; Current authenticationLevel(" + this.securityContext.getAuthenticationLevel() +
                    ") requires the server to respond with a challenge.");
            throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, connectionException);
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
                    throw new ConnectionCommunicationException(MessageSeeds.AUTHENTICATION_FAILED, e);
                }
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
                plainText = ProtocolTools.concatByteArrays(
                        this.securityContext.getSecurityProvider().getHLSSecret(),
                        this.securityContext.getSystemTitle(),
                        this.securityContext.getResponseSystemTitle(),
                        this.acse.getRespondingAuthenticationValue(),
                        this.securityContext.getSecurityProvider().getCallingAuthenticationValue()
                );

                byte[] digest = associationEncryption(plainText);   //Hash the plaintext with SHA-256
                decryptedResponse = replyToHLSAuthentication(digest);

                analyzeDecryptedResponse(decryptedResponse);
            }
            break;
            case HLS7_ECDSA: {
                plainText = ProtocolTools.concatByteArrays(
                        this.securityContext.getSystemTitle(),
                        this.securityContext.getResponseSystemTitle(),
                        this.acse.getRespondingAuthenticationValue(),
                        this.securityContext.getSecurityProvider().getCallingAuthenticationValue()
                );

                ECDSASignatureImpl signing = new ECDSASignatureImpl(getSecurityContext().getECCCurve());
                PrivateKey clientPrivateSigningKey = getGeneralCipheringSecurityProvider().getClientPrivateSigningKey();
                if (clientPrivateSigningKey == null) {
                    throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
                }

                byte[] signature = signing.sign(plainText, clientPrivateSigningKey);
                decryptedResponse = replyToHLSAuthentication(signature);

                analyzeDecryptedResponse(decryptedResponse);
            }
            break;
            default: {
                // should never get here
                throw new ProtocolAuthenticationException(MessageSeeds.UNSUPPORTED_AUTHENTICATION_TYPE, this.securityContext.getAuthenticationLevel());
            }
        }
    }

    private GeneralCipheringSecurityProvider getGeneralCipheringSecurityProvider() {
        if (!(this.securityContext.getSecurityProvider() instanceof GeneralCipheringSecurityProvider)) {
            throw new DeviceConfigurationException(MessageSeeds.UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS,GeneralCipheringSecurityProvider.class);
        }
        return (GeneralCipheringSecurityProvider) this.securityContext.getSecurityProvider();
    }

    private byte[] associationEncryption(byte[] plainText) {
        try {
            return this.securityContext.associationEncryption(plainText);
        } catch (NoSuchAlgorithmException e) {
            throw DataEncryptionException.dataEncryptionException(e);
        }
    }

    /**
     * Manually calculate the digest from the meter and compare it with the response you got from the meter.
     *
     * @param serverDigest is the response from the server to the reply_to_HLS_authentication
     * @throws ConnectionCommunicationException if the two challenges don't match, or if the HLSSecret could not be supplied, if it's not a valid algorithm or when there is no callingAuthenticationvalue
     */
    protected void analyzeDecryptedResponse(byte[] serverDigest) throws UnsupportedException {

        byte[] calculatedServerDigest = new byte[0];
        if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS3_MD5 || this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS4_SHA1) {
            byte[] plainText = ProtocolUtils.concatByteArrays(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), this.securityContext.getSecurityProvider().getHLSSecret());
            calculatedServerDigest = associationEncryption(plainText);
        } else if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS5_GMAC) {
            calculatedServerDigest = this.securityContext.createHighLevelAuthenticationGMACResponse(this.securityContext.getSecurityProvider().getCallingAuthenticationValue(), serverDigest);
        } else if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS6_SHA256) {
            byte[] plainText = ProtocolTools.concatByteArrays(
                    this.securityContext.getSecurityProvider().getHLSSecret(),
                    this.securityContext.getResponseSystemTitle(),
                    this.securityContext.getSystemTitle(),
                    this.securityContext.getSecurityProvider().getCallingAuthenticationValue(),
                    this.acse.getRespondingAuthenticationValue()
            );

            calculatedServerDigest = associationEncryption(plainText);
        } else if (this.securityContext.getAuthenticationType() == AuthenticationTypes.HLS7_ECDSA) {
            byte[] plainText = ProtocolTools.concatByteArrays(
                    this.securityContext.getResponseSystemTitle(),
                    this.securityContext.getSystemTitle(),
                    this.securityContext.getSecurityProvider().getCallingAuthenticationValue(),
                    this.acse.getRespondingAuthenticationValue()
            );

            ECDSASignatureImpl signing = new ECDSASignatureImpl(getSecurityContext().getECCCurve());
            X509Certificate serverSignatureCertificate = getGeneralCipheringSecurityProvider().getServerSignatureCertificate();
            if (serverSignatureCertificate == null) {
                throw DeviceConfigurationException.missingProperty(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString());
            }

            if (signing.verify(plainText, serverDigest, serverSignatureCertificate.getPublicKey())) {
                this.associationStatus = ASSOCIATION_CONNECTED;
                return;
            } else {
                silentDisconnect();
                ProtocolException protocolException = new ProtocolException("Verification of the received digital signature (HLS7 using ECDSA) using the server signing certificate failed.");
                throw new ConnectionCommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, protocolException);
            }
        }

        if (!Arrays.equals(calculatedServerDigest, serverDigest)) {
            silentDisconnect();
            IOException ioException = new IOException("HighLevelAuthentication failed, client and server challenges do not match.");
            throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, ioException);
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
                silentDisconnect();
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            } catch (IOException e) {
                throw new ConnectionCommunicationException(getDlmsV2Connection().getMaxTries()+1);
            } catch (ConnectionCommunicationException e) {
                if(e.getMessageSeed().equals(MessageSeeds.UNEXPECTED_IO_EXCEPTION) || e.getMessageSeed().equals(com.energyict.mdc.protocol.api.MessageSeeds.UNEXPECTED_PROTOCOL_ERROR)){
                    silentDisconnect();
                }
                throw e;
            }

            try {
                decryptedResponse = new OctetString(berEncodedData, 0);
            } catch (IOException e) {
                silentDisconnect();
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            }
        } else if ((this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING)
                || (this.acse.getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING)) {    // reply with AssociationSN
            AssociationSN asn = new CosemObjectFactory(this.protocolLink).getAssociationSN();
            byte[] response;
            try {
                response = asn.replyToHLSAuthentication(digest);
            } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
                silentDisconnect();
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
            } catch (IOException e) {
                throw new ConnectionCommunicationException(getDlmsV2Connection().getMaxTries());
            }
            if (response.length == 0) {
                return new byte[0];
            }
            try {
                decryptedResponse = new OctetString(response, 0);
            } catch (IOException e) {
                silentDisconnect();
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

    private void silentDisconnect() {
        try {
            releaseAssociation();
            getDlmsV2Connection().disconnectMAC();
        } catch (Exception e) {
            // Absorb exception
        }
    }
}