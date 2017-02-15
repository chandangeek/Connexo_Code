/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.aso;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.encryption.XDlmsDecryption;
import com.energyict.encryption.XDlmsEncryption;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Arrays;

/**
 * The AssociationControlServiceElement is an application layer protocol to establish and release an association between two entities
 * and to determine the application context of that association.
 *
 * @author gna
 */
public class AssociationControlServiceElement {

    public static final int LOGICAL_NAME_REFERENCING_NO_CIPHERING = 1;
    public static final int LOGICAL_NAME_REFERENCING_WITH_CIPHERING = 3;
    public static final int SHORT_NAME_REFERENCING_NO_CIPHERING = 2;
    public static final int SHORT_NAME_REFERENCING_WITH_CIPHERING = 4;
    public static final String REFUSED_BY_THE_VDE_HANDLER = ", refused by the VDE handler";
    public static final String ACSE_SERVICE_USER_NO_REASON_GIVEN = ", ACSE_SERVICE_USER, no reason given";
    public static final String ACSE_SERVICE_PROVIDER_NO_REASON_GIVEN = ", ACSE_SERVICE_PROVIDER, No Reason Given!";
    /**
     * <pre>
     * This default object identifier means:
     *  - two ASE's are present (ACSE and xDLMS_ASE)
     *  - xDLMS_ASE is as it is specified in 61334-4-41
     *  - the transfer syntax is A-XDR
     * </pre>
     */
    private static final byte[] DEFAULT_OBJECT_IDENTIFIER = new byte[]{(byte) 0x60,
            (byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08};
    protected byte[] respondingAPTitle;
    protected byte[] respondingApplicationEntityQualifier;
    protected XdlmsAse xdlmsAse;
    private int ACSE_protocolVersion = 0; // default version1
    private int contextId = 1;
    private int mechanismId = 0;
    private byte[] userInformationData;
    private byte[] respondingAuthenticationValue;
    private byte[] callingApplicationProcessTitle;
    private byte[] calledApplicationProcessTitle;
    private byte[] calledApplicationEntityQualifier;
    private byte[] callingApplicationEntityQualifier;
    private SecurityContext sc;

    /**
     * Create a new instance of the AssociationControlServiceElement
     *
     * @param xDlmsAse        - the xDLMS_ASE
     * @param contextId       - the applicationContextId which indicates which type of reference(LN/SN) and the use of ciphering
     * @param securityContext - the used {@link com.energyict.dlms.aso.SecurityContext}
     */
    public AssociationControlServiceElement(XdlmsAse xDlmsAse, int contextId, SecurityContext securityContext) {
        this.xdlmsAse = xDlmsAse;
        this.contextId = contextId;
        this.mechanismId = securityContext.getAuthenticationLevel();
        this.sc = securityContext;
    }

    /**
     * @return the CallingAuthenticationValue from the {@link com.energyict.dlms.aso.SecurityProvider}
     */
    private byte[] getCallingAuthenticationValue() throws UnsupportedException {
        return sc.getSecurityProvider().getCallingAuthenticationValue();
    }

    /**
     * Getter for the {@link com.energyict.dlms.aso.SecurityContext}
     *
     * @return the used SecurityContext
     */
    public SecurityContext getSecurityContext() {
        return sc;
    }

    @Override
    public String toString() {
        final Object crlf = "\r\n";
        StringBuffer sb = new StringBuffer();
        sb.append("AssociationControlServiceElement").append(crlf);
        sb.append(" > xdlmsAse = ").append(xdlmsAse != null ? xdlmsAse.toString().replace("\r\n", "") : "null").append(crlf);
        sb.append(" > contextId = ").append(contextId).append(crlf);
        sb.append(" > mechanismId = ").append(mechanismId).append(crlf);
        byte[] callingAuthenticationValue = new byte[0];
        try {
            callingAuthenticationValue = getCallingAuthenticationValue();
        } catch (UnsupportedException e) {
            //Absorb
        }
        sb.append(" > callingAuthenticationValue = ").append(callingAuthenticationValue != null ? ProtocolUtils.getResponseData(callingAuthenticationValue) : "null").append(crlf);
        sb.append(crlf);
        return sb.toString();
    }

    /**
     * Create an Association, based on the available variables
     *
     * @throws UnsupportedException in case of unsupported security level
     *                              e.g. the manufacturer specific authentication level is usually not supported.
     */
    public byte[] createAssociationRequest() throws UnsupportedException {
        updateUserInformation();
        return addUnusedPrefixBytesForCompliancyWithOldCode(buildAARQApdu());
    }

    private void updateUserInformation() {
        byte[] userInformation = this.xdlmsAse.getInitiatRequestByteArray();

        if (!getSecurityContext().getSecurityPolicy().isRequestPlain()) {
            userInformation = encryptAndAuthenticateUserInformation(userInformation);
            getSecurityContext().incFrameCounter();
        }

        setUserInformation(userInformation);
    }

    protected byte[] encryptAndAuthenticateUserInformation(byte[] userInformation) {
        XDlmsEncryption xdlmsEncryption = new XDlmsEncryption(getSecurityContext().getSecuritySuite());
            xdlmsEncryption.setPlainText(userInformation);
        byte[] paddedSystemTitle = Arrays.copyOf(getSecurityContext().getSystemTitle(), SecurityContext.SYSTEM_TITLE_LENGTH);
        xdlmsEncryption.setSystemTitle(paddedSystemTitle);
            xdlmsEncryption.setFrameCounter(getSecurityContext().getFrameCounterInBytes());
            xdlmsEncryption.setAuthenticationKey(getSecurityContext().getSecurityProvider().getAuthenticationKey());
            xdlmsEncryption.setGlobalKey(getSecurityContext().getSecurityProvider().getGlobalKey());
        byte securityControlByte = (byte) 0x30;
        securityControlByte |= (this.sc.getSecuritySuite() & 0x0F); // add the securitySuite to bits 0 to 3
        xdlmsEncryption.setSecurityControlByte(securityControlByte);
            userInformation = xdlmsEncryption.generateCipheredAPDU();
        return userInformation;
    }

    /**
     * Getter for the CallingApplicationTitle
     *
     * @return the title
     */
    public byte[] getCallingApplicationProcessTitle() {
        return callingApplicationProcessTitle;
    }

    /**
     * Setter for the CallingApplicationTitle
     *
     * @param callingApplicationProcessTitle the title to set
     */
    public void setCallingApplicationProcessTitle(byte[] callingApplicationProcessTitle) {
        if (callingApplicationProcessTitle != null) {
            this.callingApplicationProcessTitle = callingApplicationProcessTitle.clone();
        }
    }

    /**
     * Getter for the {@link #calledApplicationProcessTitle}
     *
     * @return the calledApplicationProcessTitle
     */
    public byte[] getCalledApplicationProcessTitle() {
        return this.calledApplicationProcessTitle;
    }

    /**
     * Setter for the {@link #calledApplicationProcessTitle}
     *
     * @param calledApplicationProcessTitle the APTitle to set
     */
    public void setCalledApplicationProcessTitle(byte[] calledApplicationProcessTitle) {
        if (calledApplicationProcessTitle != null) {
            this.calledApplicationProcessTitle = calledApplicationProcessTitle.clone();
        }
    }

    /**
     * Getter for the {@link #calledApplicationEntityQualifier}
     *
     * @return the calledApplicationEntityQualifier
     */
    public byte[] getCalledApplicationEntityQualifier() {
        return this.calledApplicationEntityQualifier;
    }

    /**
     * Setter for the {@link #calledApplicationEntityQualifier}
     *
     * @param calledApplicationEntityQualifier the called AEQualifier to set
     */
    public void setCalledApplicationEntityQualifier(byte[] calledApplicationEntityQualifier) {
        if (calledApplicationEntityQualifier != null) {
            this.calledApplicationEntityQualifier = calledApplicationEntityQualifier.clone();
        }
    }

    /**
     * Getter for the {@link #calledApplicationEntityQualifier}
     * In DLMS suite 1/2 this field contains our (client) certificate for digital signature.
     */
    public byte[] getCallingApplicationEntityQualifier() {
        return this.callingApplicationEntityQualifier;
    }

    /**
     * Setter for the {@link #calledApplicationEntityQualifier}
     *
     * @param callingApplicationEntityQualifier the calling AEQualifier to set
     */
    public void setCallingApplicationEntityQualifier(byte[] callingApplicationEntityQualifier) {
        if (callingApplicationEntityQualifier != null) {
            this.callingApplicationEntityQualifier = callingApplicationEntityQualifier.clone();
        }
    }

    /**
     * Getter for the responding-AE-qualifier. We received this (optionally) in the AARE of the meter.
     * In DLMS suite 1/2 this field contains the certificate of the server for digital signature.
     */
    public byte[] getRespondingApplicationEntityQualifier() {
        return respondingApplicationEntityQualifier;
    }

    /**
     * Create a request to release the current association
     *
     * @return a byteArray containing an ApplicationAssociationReleaseRequest
     */
    public byte[] releaseAssociationRequest() {
        updateUserInformation();
        return addUnusedPrefixBytesForCompliancyWithOldCode(buildRLRQApdu());
    }

    /**
     * FIXME TCPIPConnection strips the first three bytes of the byteArray.
     * This method add three redundant bytes in front of your array to be compliant with old implementation.
     */
    private byte[] addUnusedPrefixBytesForCompliancyWithOldCode(byte[] request) {
        byte[] r = new byte[request.length + 3];
        System.arraycopy(new byte[]{(byte) 0xE6, (byte) 0xE6, (byte) 0x00}, 0, r, 0, 3);
        System.arraycopy(request, 0, r, 3, request.length);
        return r;
    }

    /**
     * Build up the ApplicationAssociationRequest
     *
     * @return the generated AARQ to establish an ApplicationAssociation
     * @throws IOException
     */
    protected byte[] buildAARQApdu() throws UnsupportedException {
        int t = 0;

        //Take the size of the calling-AE-qualifier into account, since it can be big (contains a fully signed certificate)
        byte[] aarq = new byte[1024 + (getCallingApplicationEntityQualifier() == null ? 0 : getCallingApplicationEntityQualifier().length)];

        if (getACSEProtocolVersion() != null) { // Optional parameter
            System.arraycopy(getACSEProtocolVersion(), 0, aarq, t,
                    getACSEProtocolVersion().length);
            t += getACSEProtocolVersion().length;
        }

        System.arraycopy(getApplicationContextName(), 0, aarq, t,
                getApplicationContextName().length);
        t += getApplicationContextName().length;

        if (getCalledApplicationProcessTitle() != null) {
            byte[] calledAPTitle = generateCalledApplicationProcessTitleField();
            System.arraycopy(calledAPTitle, 0, aarq, t, calledAPTitle.length);
            t += calledAPTitle.length;
        }

        if (getCalledApplicationEntityQualifier() != null) {
            byte[] calledAEQualifier = generateCalledApplicationEntityQualifier();
            System.arraycopy(calledAEQualifier, 0, aarq, t, calledAEQualifier.length);
            t += calledAEQualifier.length;
        }

        /**
         * AE-qualifier OPTIONAL, called-AP-invocation-id [4]
         * AP-invocation-identifier OPTIONAL, called-AE-invocation-id [5]
         * AE-invocation-identifier OPTIONAL,
         *
         * calling-AP-invocation-id [8] AP-invocation-identifier OPTIONAL,
         * calling-AE-invocation-id [9] AE-invocation-identifier OPTIONAL,
         *
         * All above mentioned attributes are optional in the request. If they
         * are not used then they are not coded. They are encoded as
         * printableStrings. TODO encode the above attributes
         */

        if (getCallingApplicationProcessTitle() != null) {
            byte[] callingAPTitle = generateCallingApplicationProcessTitleField();
            System.arraycopy(callingAPTitle, 0, aarq, t, callingAPTitle.length);
            t += callingAPTitle.length;
        }

        if (getCallingApplicationEntityQualifier() != null) {
            byte[] callingAEQualifier = generateCallingApplicationEntityQualifier();
            System.arraycopy(callingAEQualifier, 0, aarq, t, callingAEQualifier.length);
            t += callingAEQualifier.length;
        }

        if (this.mechanismId != 0) {
            System.arraycopy(getSenderACSERequirements(), 0, aarq, t,
                    getSenderACSERequirements().length);
            t += getSenderACSERequirements().length;

            System.arraycopy(getMechanismName(), 0, aarq, t,
                    getMechanismName().length);
            t += getMechanismName().length;

            System.arraycopy(assembleCallingAuthenticationValue(), 0, aarq, t,
                    assembleCallingAuthenticationValue().length);
            t += assembleCallingAuthenticationValue().length;

        }

        // TODO implementation-information

        if (getUserInformation() != null) {
            System.arraycopy(getUserInformation(), 0, aarq, t,
                    getUserInformation().length);
            t += getUserInformation().length;
        }

        byte[] size = DLMSUtils.getAXDRLengthEncoding(t);
        byte[] completeAarq = new byte[1 + size.length + t];
        completeAarq[0] = DLMSCOSEMGlobals.AARQ_TAG;
        System.arraycopy(size, 0, completeAarq, 1, size.length);
        System.arraycopy(aarq, 0, completeAarq, 1 + size.length, t);
        return completeAarq;
    }

    /**
     * Analyze the responseData
     *
     * @param responseData from the device
     * @throws IOException
     */

    public void analyzeAARE(byte[] responseData) throws IOException, DLMSConnectionException {
        int i = 0;
        boolean resultOk = true;
        String strResultSourceDiagnostics = "";
        try {
            while (true) {
                if (responseData[i] == DLMSCOSEMGlobals.AARE_TAG) {
                    i++; // skip tag
                    i += DLMSUtils.getAXDRLengthOffset(responseData, i);
                    while (true) {
                        if (responseData[i] == DLMSCOSEMGlobals.AARE_APPLICATION_CONTEXT_NAME) {
                            i++; // skip tag
                            i += responseData[i]; // skip length + data
                        } // if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)

                        else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESULT) {
                            i++; // skip tag
                            if ((responseData[i] == 3)
                                    && (responseData[i + 1] == 2)
                                    && (responseData[i + 2] == 1)
                                    && (responseData[i + 3] == 0)) {
                                // Result OK
//							return;	 //Don't return otherwise you don't get all info
                                i += responseData[i]; // skip length + data
                                resultOk = true;
                            } else {
                                // the result wasn't OK, but we keep going so we get the proper info
                                i += responseData[i]; // skip length + data
                                resultOk = false;
                            }
                        } // else if (responseData[i] == AARE_RESULT)

                        else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESPONING_AP_TITLE) {
                            i++; // skip tag
                            if (responseData[i] > 0) { // length of octet string
                                if ((responseData[i] - responseData[i + 2]) != 2) {
                                    this.respondingAPTitle = ProtocolUtils.getSubArray2(responseData, i + 1, responseData[i]);
                                } else {
                                    this.respondingAPTitle = ProtocolUtils.getSubArray2(responseData, i + 3, responseData[i + 2]);
                                }
                            }
                            i += responseData[i];
                        } else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESPONDING_AE_QUALIFIER) {
                            i++; // skip tag
                            int respondingAEQualifierLength = DLMSUtils.getAXDRLength(responseData, i);
                            i += DLMSUtils.getAXDRLengthOffset(respondingAEQualifierLength);
                            if (respondingAEQualifierLength > 0) { // length of octet string
                                i += 1;  //Skip octet string tag
                                respondingAEQualifierLength = DLMSUtils.getAXDRLength(responseData, i);
                                i += DLMSUtils.getAXDRLengthOffset(respondingAEQualifierLength);
                                if (respondingAEQualifierLength > 0) {
                                    this.respondingApplicationEntityQualifier = ProtocolTools.getSubArray(responseData, i, i + respondingAEQualifierLength);
                                }
                            }
                            i += respondingAEQualifierLength - 1;
                        } else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESULT_SOURCE_DIAGNOSTIC) {
                            i++; // skip tag
                            if (responseData[i] == 5) // check length
                            {
                                if (responseData[i + 1] == DLMSCOSEMGlobals.ACSE_SERVICE_USER) {
                                    if ((responseData[i + 2] == 3)
                                            && (responseData[i + 3] == 2)
                                            && (responseData[i + 4] == 1)) {
                                        if (responseData[i + 5] == 0x00) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
                                        } else if (responseData[i + 5] == 0x01) {
                                            strResultSourceDiagnostics += ACSE_SERVICE_USER_NO_REASON_GIVEN;
                                            throw new ProtocolException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x02) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
                                            throw new ProtocolException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0B) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
                                            throw new ProtocolException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0C) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
                                            throw new ProtocolException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0D) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
                                            throw new ProtocolException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0E) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
                                            if (!resultOk) {  //0x0E only represents an error code if the association result was not ok.
                                                throw new ProtocolException("Application Association Establishment Failed"
                                                        + strResultSourceDiagnostics);
                                            }
                                        } else {
                                            throw new ProtocolException(
                                                    "Application Association Establishment failed, ACSE_SERVICE_USER, unknown result code: " + responseData[i + 5]);
                                        }
                                    } else {
                                        throw new ProtocolException(
                                                "Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
                                    }
                                } // if (responseData[i+1] == ACSE_SERVICE_USER)
                                else if (responseData[i + 1] == DLMSCOSEMGlobals.ACSE_SERVICE_PROVIDER) {
                                    if ((responseData[i + 2] == 3)
                                            && (responseData[i + 3] == 2)
                                            && (responseData[i + 4] == 1)) {
                                        if (responseData[i + 5] == 0x00) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER!";
                                        } else if (responseData[i + 5] == 0x01) {
                                            strResultSourceDiagnostics += ACSE_SERVICE_PROVIDER_NO_REASON_GIVEN;
                                        } else if (responseData[i + 5] == 0x02) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
                                        } else {
                                            throw new ProtocolException(
                                                    "Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
                                        }
                                    } else {
                                        throw new ProtocolException(
                                                "Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
                                    }
                                } // else if (responseData[i+1] ==
                                // ACSE_SERVICE_PROVIDER)
                                else {
                                    throw new ProtocolException(
                                            "Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
                                }
                            } else {
                                throw new ProtocolException(
                                        "Application Association Establishment Failed, result_source_diagnostic, wrong length");
                            }

                            i += responseData[i]; // skip length + data
                        } // else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)

                        else if (responseData[i] == DLMSCOSEMGlobals.AARE_MECHANISM_NAME) {
                            i++; //skip tag
                            if (responseData[i + 7] != this.mechanismId) {
                                throw new ProtocolException("Application Association Establishment Failed, mechanim_id(" + responseData[i + 7] + "),  different then proposed(" + this.mechanismId + ")");
                            }
                            i += responseData[i]; // skip length + data
                        } else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESPONDING_AUTHENTICATION_VALUE) {
                            i++; //skip tag

                            if (responseData[i + 1] == (byte) 0x80) { // encoding choice for GraphicString
                                setRespondingAuthenticationValue(ProtocolUtils.getSubArray2(responseData, i + 3, responseData[i + 2]));
                            }

                            i += responseData[i]; // skip length + data
                        } else if (responseData[i] == DLMSCOSEMGlobals.AARE_USER_INFORMATION) {
                            i++; // skip tag

                            if (responseData[i + 2] > 0) { // length of octet string

                                /*
                                 * Check if the userinformation field is encrypted,
                                 * and if so, replace the encrypted part with the
                                 * plain text for further parsing
                                 */
                                if (DLMSCOSEMGlobals.AARE_GLOBAL_INITIATE_RESPONSE_TAG == responseData[i + 3]) {
                                    byte[] encryptedUserInformation = new byte[responseData.length - (i + 4)];
                                    System.arraycopy(responseData, i + 4, encryptedUserInformation, 0, encryptedUserInformation.length);
                                    byte[] ui = decryptUserInformation(encryptedUserInformation);
                                    System.arraycopy(ui, 0, responseData, i + 3, ui.length);
                                    responseData = ProtocolUtils.getSubArray(responseData, 0, i + 2 + ui.length);
                                }

                                if (DLMSCOSEMGlobals.DLMS_PDU_INITIATE_RESPONSE == responseData[i + 3]) {
                                    int baseAddress = i + 4;
                                    if (responseData[baseAddress] != 0x00) {
                                        getXdlmsAse().setNegotiatedQOS(responseData[++baseAddress]);
                                    } else {
                                        getXdlmsAse().setNegotiatedQOS((byte) 0x00);
                                    }
                                    baseAddress++;
                                    getXdlmsAse().setNegotiatedDlmsVersion(responseData[baseAddress++]);
                                    baseAddress++; // Jump over 0x5F 0x1F tag
                                    // For compliance with existing implementations, encoding of the [Application 31] tag on one byte (5F)
                                    // instead of two bytes (5F 1F) is accepted when the 3-layer, connection-oriented, HDLC-based profile is used.
                                    if (responseData[baseAddress] == 0x1F) {
                                        baseAddress++;
                                    }
                                    baseAddress += (responseData[baseAddress] & 0x0FF) - 3; // Conformance block is parsed as int (4 bytes) but normally only 3 are used.
                                    getXdlmsAse().setNegotiatedConformance((ProtocolUtils.getInt(responseData, baseAddress) & 0x00FFFFFF)); // conformance has only 3 bytes, 24 bit
                                    baseAddress += 4; // Jump over conformance block (just pased this)
                                    this.getXdlmsAse().setMaxRecPDUServerSize(DLMSUtils.getIntFromBytes(responseData, baseAddress, 2));
                                    baseAddress += 2; // Jump over maxPDU size (two bytes)
                                    getXdlmsAse().setVAAName(ProtocolUtils.getShort(responseData, baseAddress));
                                    return;

                                } else if (DLMSCOSEMGlobals.DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i + 3]) {
                                    if (0x01 == responseData[i + 4]) {
                                        strResultSourceDiagnostics += ", InitiateError";
                                    } else if (0x02 == responseData[i + 4]) {
                                        strResultSourceDiagnostics += ", getStatus";
                                    } else if (0x03 == responseData[i + 4]) {
                                        strResultSourceDiagnostics += ", getNameList";
                                    } else if (0x13 == responseData[i + 4]) {
                                        strResultSourceDiagnostics += ", terminateUpload";
                                    } else {
                                        throw new ProtocolException(
                                                "Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");
                                    }

                                    if (0x06 != responseData[i + 5]) {
                                        strResultSourceDiagnostics += ", No ServiceError tag";
                                    }

                                    if (0x00 == responseData[i + 6]) {
                                        strResultSourceDiagnostics += "";
                                    } else if (0x01 == responseData[i + 6]) {
                                        strResultSourceDiagnostics += ", DLMS version too low";
                                    } else if (0x02 == responseData[i + 6]) {
                                        strResultSourceDiagnostics += ", Incompatible conformance";
                                    } else if (0x03 == responseData[i + 6]) {
                                        strResultSourceDiagnostics += ", pdu size too short";
                                    } else if (0x04 == responseData[i + 6]) {
                                        strResultSourceDiagnostics += REFUSED_BY_THE_VDE_HANDLER;
                                    } else {
                                        throw new ProtocolException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
                                    }
                                } else {
                                    throw new ProtocolException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
                                }

                            } // if (responseData[i+2] > 0) --> length of the octet
                            // string

                            i += responseData[i]; // skip length + data
                        } // else if (responseData[i] == AARE_USER_INFORMATION)
                        else {
                            i++; // skip tag
                            // Very tricky, suppose we receive a length > 128
                            // because of corrupted data,
                            // then if we keep byte, it is signed and we can enter a
                            // LOOP because length will
                            // be subtracted from i!!!
                            i += (((int) responseData[i]) & 0x000000FF); // skip
                            // length
                            // +
                            // data
                        }

                        if (i++ >= (responseData.length - 1)) {
                            i = (responseData.length - 1);
                            break;
                        }
                    } // while(true)

                } // if (responseData[i] == AARE_TAG)

                if (i++ >= (responseData.length - 1)) {
                    i = (responseData.length - 1);
                    break;
                }
            } // while(true)
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ACSEParsingException("Unexpected end of AARE response", e, responseData);
        }
        throw new ProtocolException("Application Association Establishment Failed" + strResultSourceDiagnostics);
    }

    /**
     * Decrypt the UserInformation field
     *
     * @param encryptedUserInformation the encrypted userInformationField from the Device
     * @throws IOException
     */
    private byte[] decryptUserInformation(byte[] encryptedUserInformation) throws IOException, DLMSConnectionException {
        int ptr = 0;
        byte length = encryptedUserInformation[ptr++];
        byte scb = encryptedUserInformation[ptr++];
        byte[] fc = new byte[4];
        byte[] at = new byte[12];
        byte[] ct = new byte[0];
        try {
            for (int j = 0; j < fc.length; j++) {
                fc[j] = encryptedUserInformation[ptr++];
            }

            int ctLen = length - fc.length - at.length - 1;
            ct = new byte[ctLen];
            for (int j = 0; j < ct.length; j++) {
                ct[j] = encryptedUserInformation[ptr++];
            }

            for (int j = 0; j < at.length; j++) {
                at[j] = encryptedUserInformation[ptr++];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ACSEParsingException("Unexpected end of encryptedUserInformation", e, encryptedUserInformation);
        }

        getSecurityContext().setResponseFrameCounter(ProtocolUtils.getInt(fc));
        return decrypt(at, ct, fc, scb);
    }

    /**
     * Subclasses can override the decryption implementation
     */
    protected byte[] decrypt(byte[] authenticationTag, byte[] cipheredText, byte[] frameCounter, byte securityControl) throws ConnectionException {
        XDlmsDecryption decryption = new XDlmsDecryption(getSecurityContext().getSecuritySuite());
        decryption.setAuthenticationKey(getSecurityContext().getSecurityProvider().getAuthenticationKey());
        decryption.setGlobalKey(getSecurityContext().getSecurityProvider().getGlobalKey());
        decryption.setAuthenticationTag(authenticationTag);
        decryption.setCipheredText(cipheredText);
        decryption.setFrameCounter(frameCounter);
        decryption.setSecurityControlByte(securityControl);
        decryption.setSystemTitle(respondingAPTitle);
        return decryption.generatePlainText();
    }

    /**
     * @return the generated RLRQ to release the association
     */
    protected byte[] buildRLRQApdu() {
        int t = 0;
        byte[] rlrq = new byte[1024];
        // byte
        rlrq[t++] = DLMSCOSEMGlobals.RLRQ_TAG;

        if (userInformationData != null) {
            if (getSecurityContext().getSecurityPolicy().isRequestEncryptedOnly() || getSecurityContext().getSecurityPolicy().isRequestAuthenticatedAndEncrypted()) {
                    rlrq[t++] = (byte) (userInformationData.length + 4); // total length
                    rlrq[t++] = DLMSCOSEMGlobals.RLRQ_USER_INFORMATION;
                    rlrq[t++] = (byte) (userInformationData.length + 2); // Total length of the userInformation (including the following 2 bytes)
                    rlrq[t++] = 0x04; // OctetString
                    rlrq[t++] = (byte) userInformationData.length; // Length of the userInformation
                for (byte aByte : userInformationData) {
                    rlrq[t++] = aByte;
                    }
            } else {
                    rlrq[t++] = 0x00;
            }
        } else {
            rlrq[t++] = 0x00;
        }

        //TODO a Release-Request-Reason and UserInformationField can be added, but they are optional ...

        rlrq[1] = (byte) (t - 2);
        return ProtocolUtils.getSubArray(rlrq, 0, t - 1);
    }

    /**
     * Analyze the ReleaseResponse
     *
     * @param responseData the response from the device
     * @throws DLMSConnectionException the disconnect failed
     * @throws ACSEParsingException    unexpected end of the AARE
     */
    public void analyzeRLRE(byte[] responseData) throws DLMSConnectionException, ACSEParsingException {
        int i = 0;
        try {
            while (true) {
                if (responseData[i] == DLMSCOSEMGlobals.RLRE_TAG) {
                    if (responseData[i + 1] != 0) {
                        i += 2; // skip tag & length
                        while (true) {
                            if (responseData[i] == DLMSCOSEMGlobals.RLRE_RELEASE_RESPONSE_REASON) {
                                i++; // skip tag

                                /* Contains the length of the tagged component. Normally this is '1', but some code this
                                as an Integer (0x02) with length 1 (0x01) which causes the tagged length to be 3(0x03)
                                 */
                                int lenghtOfTaggedComponent = responseData[i];

                                i += lenghtOfTaggedComponent;
                                if (lenghtOfTaggedComponent != 0) {
                                    switch (responseData[i++]) {
                                        case 0x00:
                                            return; // normal release
                                        case 0x01:
                                            throw new DLMSConnectionException("Release was not finished.");
                                        case 0x30:
                                            throw new DLMSConnectionException("Response from the release is userDefined: " + 30);
                                        default:
                                            throw new DLMSConnectionException("Unknown release response");
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (i++ >= (responseData.length - 1)) {
                                i = (responseData.length - 1);
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }

                if (i++ >= (responseData.length - 1)) {
                    i = (responseData.length - 1);
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ACSEParsingException("Unexpected end of RLRE response", e, responseData);
        }
    }

    /**
     * Getter for the ApplicationControlServiceElement Requirements
     *
     * @return the ACSE req.
     */
    private byte[] getSenderACSERequirements() {
        byte[] senderACSEReq = new byte[4];
        senderACSEReq[0] = DLMSCOSEMGlobals.AARQ_SENDER_ACSE_REQUIREMENTS;
        senderACSEReq[1] = (byte) 0x02; // length of the following bitString
        senderACSEReq[2] = (byte) 0x07; // coding number of unused bits in the last byte of the BIT STRING
        senderACSEReq[3] = (byte) 0x80; // coding of the authentication functional unit
        return senderACSEReq;
    }

    /**
     * Getter for the UserInformationField <i>(encryption must already be applied)</i>
     *
     * @return the UserInformation byteArray
     */
    protected byte[] getUserInformation() {
        if (this.userInformationData != null) {
            byte[] uiData = new byte[this.userInformationData.length + 4];
            uiData[0] = DLMSCOSEMGlobals.AARQ_USER_INFORMATION;
            uiData[1] = (byte) (this.userInformationData.length + 2);
            uiData[2] = (byte) 0x04;// choice for user information: [4], Universal, Octetstring type
            uiData[3] = (byte) this.userInformationData.length;
            System.arraycopy(this.userInformationData, 0, uiData, 4, this.userInformationData.length);
            return uiData;
        }
        return null;
    }

    /**
     * Setter for the userInformation field. If you plan on initiating an
     * association, then normally this is a xDLMS.initiateRequest()
     *
     * @param userInformation
     */
    public void setUserInformation(byte[] userInformation) {
        this.userInformationData = userInformation.clone();
    }

    /**
     * The applicationContextName is generated from a default objectIdentifier
     * and two specific bytes
     *
     * @return the byteArray encoded applicationContextName
     */
    protected byte[] getApplicationContextName() {
        byte[] appContextName = new byte[DEFAULT_OBJECT_IDENTIFIER.length + 6];
        appContextName[0] = DLMSCOSEMGlobals.AARQ_APPLICATION_CONTEXT_NAME;
        appContextName[1] = (byte) 0x09; // length
        appContextName[2] = (byte) 0x06; // choice for application context name: OBJECT IDENTIFIER, Universal
        appContextName[3] = (byte) 0x07; // length
        System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, appContextName, 4, DEFAULT_OBJECT_IDENTIFIER.length);
        appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 4] = 1; // 1 meaning application context
        appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 5] = (byte) this.contextId;
        return appContextName;
    }

    /**
     * Generate the Calling ApplicationTitle byteArray
     *
     * @return the byteArray containing the our ApplicationProcess title
     */
    private byte[] generateCallingApplicationProcessTitleField() {
        if (getCallingApplicationProcessTitle() != null) {
            byte[] callingAppTitleField = new byte[getCallingApplicationProcessTitle().length + 4];
            callingAppTitleField[0] = DLMSCOSEMGlobals.AARQ_CALLING_AP_TITLE;
            callingAppTitleField[1] = (byte) (callingAppTitleField.length - 2); // length
            callingAppTitleField[2] = (byte) 0x04; // choice for calling app title: [4], Universal, Octetstring type
            callingAppTitleField[3] = (byte) (callingAppTitleField.length - 4); // length
            System.arraycopy(getCallingApplicationProcessTitle(), 0, callingAppTitleField, 4, getCallingApplicationProcessTitle().length);
            return callingAppTitleField;
        } else {
            return null;
        }
    }

    /**
     * Generate the Called AppplicationProcess Title byteArray
     *
     * @return the byteArray containing the called ApplicationProcess title
     */
    private byte[] generateCalledApplicationProcessTitleField() {
        if (getCalledApplicationProcessTitle() != null) {
            byte[] callingAppTitleField = new byte[getCalledApplicationProcessTitle().length + 4];
            callingAppTitleField[0] = DLMSCOSEMGlobals.AARQ_CALLED_AP_TITLE;
            callingAppTitleField[1] = (byte) (callingAppTitleField.length - 2); // length
            callingAppTitleField[2] = (byte) 0x04; // choice for calling app title
            callingAppTitleField[3] = (byte) (callingAppTitleField.length - 4); // length
            System.arraycopy(getCalledApplicationProcessTitle(), 0, callingAppTitleField, 4, getCalledApplicationProcessTitle().length);
            return callingAppTitleField;
        } else {
            return null;
        }
    }

    /**
     * Generate the called ApplicationEntity qualifier byteArray
     *
     * @return the byteArray containing the called ApplicationEntity qualifier
     */
    private byte[] generateCalledApplicationEntityQualifier() {
        if (getCalledApplicationEntityQualifier() != null) {
            byte[] result = new byte[getCalledApplicationEntityQualifier().length + 4];
            //TODO change
            result[0] = DLMSCOSEMGlobals.AARQ_CALLED_AE_QUALIFIER;
            result[1] = (byte) (result.length - 2); // length
            result[2] = (byte) 0x04;                // [4], Universal, Octetstring type
            result[3] = (byte) (result.length - 4); // length
            System.arraycopy(getCalledApplicationEntityQualifier(), 0, result, 4, getCalledApplicationEntityQualifier().length);
            return result;
        } else {
            return null;
        }
    }

    /**
     * Generate the calling ApplicationEntity qualifier byteArray
     *
     * @return the byteArray containing the calling ApplicationEntity qualifier
     */
    private byte[] generateCallingApplicationEntityQualifier() {
        if (getCallingApplicationEntityQualifier() != null) {
            byte[] callingAEQualifierLength = DLMSUtils.getAXDRLengthEncoding(getCallingApplicationEntityQualifier().length);

            return ProtocolTools.concatByteArrays(
                    new byte[]{DLMSCOSEMGlobals.AARQ_CALLING_AE_QUALIFIER},
                    DLMSUtils.getAXDRLengthEncoding(getCallingApplicationEntityQualifier().length + 1 + callingAEQualifierLength.length),
                    new byte[]{0x04},   // [4], Universal, Octetstring type
                    callingAEQualifierLength,
                    getCallingApplicationEntityQualifier()
            );
        } else {
            return null;
        }
    }

    /**
     * The mechanism name is generated from a default objectIdentifier and to
     * specific bytes
     *
     * @return the byteArray encoded mechanism name
     */
    protected byte[] getMechanismName() {
        byte[] mechanismName = new byte[DEFAULT_OBJECT_IDENTIFIER.length + 4];
        mechanismName[0] = DLMSCOSEMGlobals.AARQ_MECHANISM_NAME;
        mechanismName[1] = (byte) 0x07; // length
        System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, mechanismName, 2, DEFAULT_OBJECT_IDENTIFIER.length);
        mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 2] = 2; // 2 meaning mechanism name
        mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 3] = (byte) this.mechanismId;
        return mechanismName;
    }

    /**
     * @return a byteArray containing the callingAuthenticationValue(in other
     * words, the password) coded as a graphical string...
     * @throws ConnectionException when the callingauthenticationvalue is not filled in
     */
    protected byte[] assembleCallingAuthenticationValue() throws UnsupportedException {
        byte[] authValue = new byte[getCallingAuthenticationValue().length + 4];
        authValue[0] = DLMSCOSEMGlobals.AARQ_CALLING_AUTHENTICATION_VALUE;
        authValue[1] = (byte) (getCallingAuthenticationValue().length + 2);
        authValue[2] = (byte) 0x80; // choice for authentication-information ...
        authValue[3] = (byte) getCallingAuthenticationValue().length;
        System.arraycopy(getCallingAuthenticationValue(), 0, authValue, 4, getCallingAuthenticationValue().length);
        return authValue;
    }

    /**
     * <pre>
     * Create the byteArray for the protocolVersion.
     * If it is the default value(which will mostly be the case) then it's not required to put it in the request
     * </pre>
     *
     * @return the BER encoded BitString protocolVersion
     */
    private byte[] getACSEProtocolVersion() {
        if (this.ACSE_protocolVersion != 0) {
            return new BitString(this.ACSE_protocolVersion).getBEREncodedByteArray();
        }
        return null;
    }

    /**
     * <p>
     * Setter for the authentication mechanism - mechanism id
     * </p>
     *
     * @param mechanismId
     */
    public void setAuthMechanismId(int mechanismId) {
        this.mechanismId = mechanismId;
    }

    /**
     * @return the current xDLMS_ASE
     */
    public XdlmsAse getXdlmsAse() {
        if (this.xdlmsAse == null) {
            this.xdlmsAse = new XdlmsAse();
        }
        return this.xdlmsAse;
    }

    /**
     * @return the authenticaionValue(challenge) from the server
     */
    public byte[] getRespondingAuthenticationValue() {
        return this.respondingAuthenticationValue;
    }

    /**
     * Set the authenticationValue(challenge) from the server
     *
     * @param respondingAuthenticationValue - the challenge from the server
     */
    protected void setRespondingAuthenticationValue(byte[] respondingAuthenticationValue) {
        if (respondingAuthenticationValue != null) {
            this.respondingAuthenticationValue = respondingAuthenticationValue.clone();
        }
    }

    /**
     * @return the applicationContextId
     */
    public int getContextId() {
        return this.contextId;
    }

    /**
     * <p/>
     * Setter for the application context name - context id
     * <p/>
     *
     * @param contextId
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    protected void setRespondingAPTitle(byte[] respondingAPTitle) {
        if (respondingAPTitle != null) {
            this.respondingAPTitle = respondingAPTitle.clone();
        }
    }

    /**
     * Getter for the Responding ApplicatonTitle
     *
     * @return the Responding AP title
     */
    public byte[] getRespondingAPTtitle() {
        return this.respondingAPTitle;
    }

    /**
     * Checks if the calling- and responding authenticationValue are identical, if so then it is possible a fake meter is on the other side ...
     *
     * @return true if both challenges are the same, false otherwise.
     */
    public boolean hlsChallengeMatch() throws UnsupportedException {
        if (sc.getAuthenticationLevel() > 1) {          // SVA|17042012|For Authentication level 1, this check is not valid - ActarisSL7000 failed on this.
            return Arrays.equals(getCallingAuthenticationValue(), getRespondingAuthenticationValue());
        } else {
            return false;
        }
    }

    public class ACSEParsingException extends IOException {

        public ACSEParsingException(String s, Exception e) {
            super(s);
            initCause(e);
        }

        public ACSEParsingException(String s, Exception e, byte[] array) {
            super(new StringBuffer().append(s).append(" [").append(ProtocolUtils.getResponseData(array)).append("]").toString());
            initCause(e);
        }

    }

}