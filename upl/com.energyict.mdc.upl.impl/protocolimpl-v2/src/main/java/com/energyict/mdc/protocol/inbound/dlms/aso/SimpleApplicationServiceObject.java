package com.energyict.mdc.protocol.inbound.dlms.aso;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

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
public class SimpleApplicationServiceObject {

    private static final int PROPOSED_QOS = -1;
    private static final int PROPOSED_DLMS_VERSION = 6;
    private static final long CONFORMANCE = ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK;
    private static final int MAX_REC_PDU_SIZE = 4096;
    private static final int ACSE_PROTOCOL_VERSION = 0;

    private final int securityPolicy = 0;
    public static final int SECURITYPOLICY_NONE = 0;
    public static final int SECURITYPOLICY_AUTHENTICATION = 1;
    public static final int SECURITYPOLICY_ENCRYPTION = 2;
    public static final int SECURITYPOLICY_BOTH = 3;
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

    private static final int LOGICAL_NAME_REFERENCING_NO_CIPHERING = 1;
    private static final byte[] CALLED_APPLICATION_PROCESS_TITLE = null;
    private static final byte[] CALLED_APPLICATION_ENTITY_QUALIFIER = null;
    private static final byte[] SYSTEM_IDENTIFIER = "EICTCOMM".getBytes();
    private static final int MECHANISM_ID = 0;  // Authentication level
    private static final byte[] CALLING_AUTHENTICATION_VALUE = new byte[0]; // Taking into account security level is 0


    private final DLMSConnection dlmsConnection;
    private byte[] userInformationData;

    private int associationStatus;
    public static final int ASSOCIATION_DISCONNECTED = 0;
    public static final int ASSOCIATION_PENDING = 1;
    public static final int ASSOCIATION_CONNECTED = 2;
    public static final int ASSOCIATION_READY_FOR_DISCONNECTION = 3;

    public SimpleApplicationServiceObject(DLMSConnection dlmsConnection) {
        this.dlmsConnection = dlmsConnection;
        this.associationStatus = ASSOCIATION_DISCONNECTED;
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
    public void createAssociation() throws IOException, DLMSConnectionException {
        byte[] request = createAssociationRequest();
        if (isConfirmedAssociation()) {
            byte[] response = this.dlmsConnection.sendRequest(request);
            analyzeAARE(response);
            this.associationStatus = ASSOCIATION_CONNECTED;
            /* No extra checks required - cause we are not using any of the advanced security mechanisms */
        } else {
            this.dlmsConnection.sendUnconfirmedRequest(request);
            this.associationStatus = ASSOCIATION_CONNECTED;
        }
    }

    /**
     * Release the current association
     *
     * @throws java.io.IOException
     */
    public void releaseAssociation() throws IOException {
        this.associationStatus = ASSOCIATION_READY_FOR_DISCONNECTION;
        byte[] request = releaseAssociationRequest();
        if (isConfirmedAssociation()) {
            byte[] response = this.dlmsConnection.sendRequest(request);
            analyzeRLRE(response);
            this.associationStatus = ASSOCIATION_DISCONNECTED;
        } else {
            this.dlmsConnection.sendUnconfirmedRequest(request);
            this.associationStatus = ASSOCIATION_DISCONNECTED;
        }
    }

    private final boolean isConfirmedAssociation() {
        return this.dlmsConnection.getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriorityObject().needsResponse();
    }

    /**
     * Create an Association, based on the available variables
     *
     * @throws java.io.IOException
     */
    public byte[] createAssociationRequest() throws IOException {
        updateUserInformation();
        return addUnusedPrefixBytesForCompliancyWithOldCode(buildAARQApdu());
    }

    private void updateUserInformation() throws IOException {
        this.userInformationData = getInitiatRequestByteArray();
        // Remark: No encryption applied - as dataTransport security level is 0
    }

    /**
     * Construct a byteArray containing an InitiateRequest using the desired parameters
     *
     * @return an A-XDR encoded byteArray
     */
    public byte[] getInitiatRequestByteArray() {
        int t = 0;
        byte[] xDlmsASEReq = new byte[1024];

        xDlmsASEReq[t++] = DLMSCOSEMGlobals.COSEM_INITIATEREQUEST;

        if (getDedicatedKey() != null) {
            xDlmsASEReq[t++] = (byte) 0x01; // indicating the presence of the key
            xDlmsASEReq[t++] = (byte) getDedicatedKey().getOctetStr().length;
            System.arraycopy(getDedicatedKey().getBEREncodedByteArray(), 2,
                    xDlmsASEReq, t, getDedicatedKey().getOctetStr().length);
            t += getDedicatedKey().getOctetStr().length;
        } else {
            xDlmsASEReq[t++] = 0; // key not present
        }

        if (getResponseAllowed()) { // true is the default value
            xDlmsASEReq[t++] = (byte) 0x00;    // value is not present, default TRUE will be used

            //			xDlmsASEReq[t++] = (byte) 0x01;	// value is not present, default TRUE will be used
            //            xDlmsASEReq[t++] = (byte) 0x01;	// value is not present, default TRUE will be used
        } else {
            xDlmsASEReq[t++] = (byte) 0x01; // indicating the presence of the value
            xDlmsASEReq[t++] = (byte) 0x00; // value is zero
        }

        if (getProposedQOS() != null) {
            xDlmsASEReq[t++] = (byte) 0x01; // indicating the presence of the QOS parameter
            System.arraycopy(getProposedQOS().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 1);
            t += 1;
        } else {
            xDlmsASEReq[t++] = 0; // QOS is not present
        }

        System.arraycopy(getProposedDLMSVersion().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 1);
        t += 1;

        System.arraycopy(getConformanceBlock().getAXDREncodedConformanceBlock(), 0, xDlmsASEReq, t, getConformanceBlock().getAXDREncodedConformanceBlock().length);
        t += getConformanceBlock().getAXDREncodedConformanceBlock().length;

        if (getMaxRecPDUClientSize() != null) {
            System.arraycopy(getMaxRecPDUClientSize().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 2);
            t += 2;
        } else {
            xDlmsASEReq[t++] = 0;
        }

        return ProtocolUtils.getSubArray(xDlmsASEReq, 0, t - 1);
    }

    /**
     * Build up the ApplicationAssociationRequest
     *
     * @return the generated AARQ to establish an ApplicationAssociation
     * @throws java.io.IOException
     */
    protected byte[] buildAARQApdu() throws IOException {
        int t = 0;
        byte[] aarq = new byte[1024];

        if (getACSEProtocolVersion() != null) { // Optional parameter
            System.arraycopy(getACSEProtocolVersion(), 0, aarq, t,
                    getACSEProtocolVersion().length);
            t += getACSEProtocolVersion().length;
        }

        System.arraycopy(getApplicationContextName(), 0, aarq, t,
                getApplicationContextName().length);
        t += getApplicationContextName().length;

        if (generateCalledApplicationProcessTitleField() != null) {
            System.arraycopy(generateCalledApplicationProcessTitleField(), 0, aarq, t, generateCalledApplicationProcessTitleField().length);
            t += generateCalledApplicationProcessTitleField().length;
        }

        if (generateCalledApplicationEntityQualifier() != null) {
            System.arraycopy(generateCalledApplicationEntityQualifier(), 0, aarq, t, generateCalledApplicationEntityQualifier().length);
            t += generateCalledApplicationEntityQualifier().length;
        }
        /**
         * called-AE-qualifier [3]
         * AE-qualifier OPTIONAL, called-AP-invocation-id [4]
         * AP-invocation-identifier OPTIONAL, called-AE-invocation-id [5]
         * AE-invocation-identifier OPTIONAL,
         *
         * OPTIONAL, calling-AE-qualifier [7] AE-qualifier OPTIONAL,
         * calling-AP-invocation-id [8] AP-invocation-identifier OPTIONAL,
         * calling-AE-invocation-id [9] AE-invocation-identifier OPTIONAL,
         *
         * All above mentioned attributes are optional in the request. If they
         * are not used then they are not coded. They are encoded as
         * printableStrings.
         */

        if (generateCallingApplicationProcessTitleField() != null) {
            System.arraycopy(generateCallingApplicationProcessTitleField(), 0, aarq, t, generateCallingApplicationProcessTitleField().length);
            t += generateCallingApplicationProcessTitleField().length;
        }

        if (MECHANISM_ID != 0) {
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
     * Release the current association
     *
     * @return a byteArray containing an ApplicationAssociationReleaseRequest
     * @throws java.io.IOException
     */
    public byte[] releaseAssociationRequest() throws IOException {
        updateUserInformation();
        return addUnusedPrefixBytesForCompliancyWithOldCode(buildRLRQApdu());
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
            switch (securityPolicy) {
                case SECURITYPOLICY_BOTH:
                case SECURITYPOLICY_ENCRYPTION:
                    rlrq[t++] = (byte) (userInformationData.length + 4); // total length
                    rlrq[t++] = DLMSCOSEMGlobals.RLRQ_USER_INFORMATION;
                    rlrq[t++] = (byte) (userInformationData.length + 2); // Total length of the userInformation (including the following 2 bytes)
                    rlrq[t++] = 0x04; // OctetString
                    rlrq[t++] = (byte) userInformationData.length; // Length of the userInformation
                    for (int i = 0; i < userInformationData.length; i++) {
                        rlrq[t++] = userInformationData[i];
                    }
                    break;
                default:
                    rlrq[t++] = 0x00;
            }
        } else {
            rlrq[t++] = 0x00;
        }
        // a Release-Request-Reason and UserInformationField can be added, but they are optional ...

        rlrq[1] = (byte) (t - 2);
        return ProtocolUtils.getSubArray(rlrq, 0, t - 1);
    }

    /**
     * Analyze the responseData
     *
     * @param responseData from the device
     * @throws java.io.IOException
     */
    protected void analyzeAARE(byte[] responseData) throws IOException {
        int i = 0;
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
                        } else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESULT) {
                            i++; // skip tag
                            if ((responseData[i] == 3)
                                    && (responseData[i + 1] == 2)
                                    && (responseData[i + 2] == 1)
                                    && (responseData[i + 3] == 0)) {
                                return; // AARQ accepted
                            } else {
                                // the result wasn't OK, but we keep going so we get the proper info
                                i += responseData[i]; // skip length + data
                            }
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
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
                                            throw new IOException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x02) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
                                            throw new IOException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0B) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
                                            throw new IOException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0C) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
                                            throw new IOException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0D) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
                                            throw new IOException("Application Association Establishment Failed"
                                                    + strResultSourceDiagnostics);
                                        } else if (responseData[i + 5] == 0x0E) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
                                        } else {
                                            throw new IOException(
                                                    "Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
                                        }
                                    } else {
                                        throw new IOException(
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
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Reason Given!";
                                        } else if (responseData[i + 5] == 0x02) {
                                            strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
                                        } else {
                                            throw new IOException(
                                                    "Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
                                        }
                                    } else {
                                        throw new IOException(
                                                "Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
                                    }
                                } // else if (responseData[i+1] ==
                                // ACSE_SERVICE_PROVIDER)
                                else {
                                    throw new IOException(
                                            "Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
                                }
                            } else {
                                throw new IOException(
                                        "Application Association Establishment Failed, result_source_diagnostic, wrong length");
                            }

                            i += responseData[i]; // skip length + data
                        }

                        if (i++ >= (responseData.length - 1)) {
                            i = (responseData.length - 1);
                            break;
                        }
                    }
                } else {
                    i++; // skip tag
                }

                if (i >= (responseData.length - 1)) {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Unexpected end of AARE response");
        }
        throw new IOException("Application Association Establishment Failed" + strResultSourceDiagnostics);
    }

    /**
     * Analyze the ReleaseResponse
     *
     * @param responseData the response from the device
     * @throws java.io.IOException
     */
    protected void analyzeRLRE(byte[] responseData) throws IOException {
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
                                            throw new IOException("Release was not finished.");
                                        case 0x30:
                                            throw new IOException("Response from the release is userDefined: " + 30);
                                        default:
                                            throw new IOException("Unknown release response");
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
            throw new IOException("Unexpected end of RLRE response: " + e.getMessage());
        }
    }

    /**
     * @return the value of the dedicatedKey
     */
    protected OctetString getDedicatedKey() {
        return null;
    }

    /**
     * @return the value of the responseAllowed
     */
    protected boolean getResponseAllowed() {
        return this.dlmsConnection.getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriorityObject().needsResponse();
    }

    /**
     * @return the proposed qualityOfService
     */
    protected Integer8 getProposedQOS() {
        if (PROPOSED_QOS != -1) {
            return new Integer8(PROPOSED_QOS);
        } else {
            return null;
        }
    }

    /**
     * @return the proposed DLMSVersion
     */
    protected Unsigned8 getProposedDLMSVersion() {
        return new Unsigned8(PROPOSED_DLMS_VERSION);
    }

    /**
     * @return the ConformanceBlock
     */
    protected ConformanceBlock getConformanceBlock() {
        return new ConformanceBlock(CONFORMANCE);
    }

    /**
     * @return the clients maximum receive PDU size
     */
    protected Unsigned16 getMaxRecPDUClientSize() {
        if (MAX_REC_PDU_SIZE != -1) {
            return new Unsigned16(MAX_REC_PDU_SIZE);
        }
        return null;
    }

    /**
     * <pre>
     * Create the byteArray for the protocolVersion.
     * If it is the default value(which will mostly be the case) then it's not required to put it in the request
     * </pre>
     *
     * @return the BER encoded BitString protocolVersion
     * @throws java.io.IOException is never throw, but it's defined on the interface
     */
    private byte[] getACSEProtocolVersion() throws IOException {
        if (ACSE_PROTOCOL_VERSION != 0) {
            return new BitString(ACSE_PROTOCOL_VERSION).getBEREncodedByteArray();
        }
        return null;
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
        appContextName[2] = (byte) 0x06; // choice for application context name ...
        appContextName[3] = (byte) 0x07; // length
        System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, appContextName, 4, DEFAULT_OBJECT_IDENTIFIER.length);
        appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 4] = 1; // 1 meaning application context
        appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 5] = (byte) getContextId();
        return appContextName;
    }

    /**
     * Define the contextID of the associationServiceObject.
     *
     * @return the contextId
     */
    private int getContextId() {
        return LOGICAL_NAME_REFERENCING_NO_CIPHERING;
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

    public byte[] getCalledApplicationProcessTitle() {
        return CALLED_APPLICATION_PROCESS_TITLE;
    }

    /**
     * Generate the called ApplicationEntity qualifier byteArray
     *
     * @return the byteArray containing the called ApplicationEntity qualifier
     */
    private byte[] generateCalledApplicationEntityQualifier() {
        if (getCalledApplicationEntityQualifier() != null) {
            byte[] callingAppTitleField = new byte[getCalledApplicationEntityQualifier().length + 4];

            callingAppTitleField[0] = DLMSCOSEMGlobals.AARQ_CALLED_AE_QUALIFIER;
            callingAppTitleField[1] = (byte) (callingAppTitleField.length - 2); // length
            callingAppTitleField[2] = (byte) 0x04; // choice for calling app title
            callingAppTitleField[3] = (byte) (callingAppTitleField.length - 4); // length
            System.arraycopy(getCalledApplicationEntityQualifier(), 0, callingAppTitleField, 4, getCalledApplicationEntityQualifier().length);
            return callingAppTitleField;
        } else {
            return null;
        }
    }

    public byte[] getCalledApplicationEntityQualifier() {
        return CALLED_APPLICATION_ENTITY_QUALIFIER;
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
            callingAppTitleField[2] = (byte) 0x04; // choice for calling app title
            callingAppTitleField[3] = (byte) (callingAppTitleField.length - 4); // length
            System.arraycopy(getCallingApplicationProcessTitle(), 0, callingAppTitleField, 4, getCallingApplicationProcessTitle().length);
            return callingAppTitleField;
        } else {
            return null;
        }
    }

    /**
     * Getter for the CallingApplicationTitle
     *
     * @return the title
     */
    public byte[] getCallingApplicationProcessTitle() {
        return SYSTEM_IDENTIFIER;
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
        mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 3] = (byte) MECHANISM_ID;
        return mechanismName;
    }

    /**
     * @return a byteArray containing the callingAuthenticationValue(in other
     *         words, the password) coded as a graphical string...
     * @throws com.energyict.dialer.connection.ConnectionException
     *          when the callingauthenticationvalue is not filled in
     */
    protected byte[] assembleCallingAuthenticationValue() throws ConnectionException {
        if (getCallingAuthenticationValue() == null) {
            throw new ConnectionException("CallingAuthenticationValue is not filled in.");
        }
        byte[] authValue = new byte[getCallingAuthenticationValue().length + 4];
        authValue[0] = DLMSCOSEMGlobals.AARQ_CALLING_AUTHENTICATION_VALUE;
        authValue[1] = (byte) (getCallingAuthenticationValue().length + 2);
        authValue[2] = (byte) 0x80; // choice for authentication-information ...
        authValue[3] = (byte) getCallingAuthenticationValue().length;
        System.arraycopy(getCallingAuthenticationValue(), 0, authValue, 4, getCallingAuthenticationValue().length);
        return authValue;
    }

    /**
     * @return the CallingAuthenticationValue
     */
    private byte[] getCallingAuthenticationValue() {
        return CALLING_AUTHENTICATION_VALUE;
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
            uiData[2] = (byte) 0x04;// choice for user information
            uiData[3] = (byte) this.userInformationData.length;
            System.arraycopy(this.userInformationData, 0, uiData, 4, this.userInformationData.length);
            return uiData;
        }
        return null;
    }
}
