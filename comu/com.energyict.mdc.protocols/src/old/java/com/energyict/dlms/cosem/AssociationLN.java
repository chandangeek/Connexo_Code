/*
 * AssociationLN.java
 *
 * Created on 7 oktober 2004, 10:41
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.AssociationLNAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

import static com.energyict.dlms.DLMSCOSEMGlobals.ASSOC_SN_ATTR_OBJ_LST;

/**
 *
 * @author  Koen
 * @changes
 * GNA |15062009| Implemented more attributes and methods
 * TODO Test all implemented methods
 */
public class AssociationLN extends AbstractCosemObject {

    /** Attributes */
    private UniversalObject[] buffer;	// the objectList
    private Structure associatedPartnersId; //
    private AbstractDataType applicationContextName;
    // The applicationContextName is implementation independent
    // old implementations will encode this as n OctetString in BER
    // newer implementations will encode this as a structure with 7 elements
    private Structure xDlmsContextInfo; // contains all the necessary info on the xDLMS context for the given association
    private AbstractDataType authenticationMechanismName;
    // The authenticationMechanismName is implementation independent
    // old implementations will encode this as n OctetString in BER
    // newer implementations will encode this as a structure with 7 elements
    private OctetString secret;	 // Contains the secret for the LLS or HLS authentication process
		// NOTE: in case of HLS with GMAC, the secret is held by the SecuritySetup object attr. 4
    private TypeEnum associationStatus; // indicates the current status of the object
    private OctetString securitySetupReference; //References the SecuritySetup object by its logical name

    /** Attribute numbers */
    private static final int ATTRB_OBJECT_LIST = 2;
    private static final int ATTRB_ASSOCIATION_PRTN_ID = 3;
    private static final int ATTRB_APPLICATION_CONTEXT_NAME = 4;
    private static final int ATTRB_XDLMS_CONTEXT_INFO = 5;
    private static final int ATTRB_AUTHENTICATION_MECH_NAME = 6;
    private static final int ATTRB_SECRET = 7;
    private static final int ATTRB_ASSOCIATON_STATUS = 8;
    private static final int ATTRB_SECURITY_SETUP_REFERENCE = 9;

    /** Method invoke */
    private static final int METHOD_REPLY_TO_HLS_AUTHENTICATION = 1;
    private static final int METHOD_CHANGE_HLS_SECRET = 2;
    private static final int METHOD_ADD_OBJECT = 3;
    private static final int METHOD_REMOVE_OBJECT = 4;

    static final byte[] LN=new byte[]{0,0,(byte)40,0,0,(byte)255};

    private static int API_CLIENT_SAP = 0;
    private static int API_SERVER_SAP = 1;

    /** Return the Class id */
    protected int getClassId() {
        return DLMSClassId.ASSOCIATION_LN.getClassId();
    }

    /** Creates a new instance of AssociationLN */
    public AssociationLN(ProtocolLink protocolLink){
    	super(protocolLink, new ObjectReference(LN));
    }

    /** Creates a new instance of AssociationLN */
    public AssociationLN(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    /** Return the logicalName (obiscode) of this object */
    public static ObisCode getDefaultObisCode(){
    	return ObisCode.fromByteArray(LN);
    }

    /**
     * Read the objectList from the current association
     * @return an array of UO containing the information of the objects in the device
     * @throws java.io.IOException
     */
    public UniversalObject[] getBuffer() throws IOException {
        byte[] responseData = getResponseData(ASSOC_SN_ATTR_OBJ_LST);

        buffer = data2UOL(responseData);
        return buffer;
    }

    public Array readObjectList() throws IOException {
        return new Array(getResponseData(AssociationLNAttributes.OBJECT_LIST), 0, 0);
    }

    /**
     * Read the associatedPartnersIds from the device
     * @return a structure containing the two ID's (client_SAP and server_SAP)
     * @throws java.io.IOException if read fails, or if parsing the response isn't correct
     */
    public Structure readAssociatedPartnersId() throws IOException {
    	this.associatedPartnersId = new Structure(getLNResponseData(ATTRB_ASSOCIATION_PRTN_ID), 0, 0);
    	return this.associatedPartnersId;
    }

    /**
     * @return the current associatedPartnersIds
     * @throws java.io.IOException if read fails, or if parsing the response isn't correct
     */
    public Structure getAssociatedPartnersId() throws IOException {
    	if(this.associatedPartnersId == null){
    		return readAssociatedPartnersId();
    	}
    	else {
    		return this.associatedPartnersId;
    	}
    }

    /**
     * @return the client_SAP of the associatedPartnersObject
     * @throws java.io.IOException if read fails, or if parsing the response isn't correct
     */
    public Integer8 getClientSAP() throws IOException {
    	return (Integer8)getAssociatedPartnersId().getDataType(API_CLIENT_SAP);
    }
    /**
     * @return the server_SAP of the associatedPartnersObject
     * @throws java.io.IOException if read fails, or if parsing the response isn't correct
     */
    public Unsigned16 getServerSAP() throws IOException {
    	return (Unsigned16)getAssociatedPartnersId().getDataType(API_SERVER_SAP);
    }

    /**
     * Write the given associatedParntersID, containing the client- and serverSAP, to the device
     * @param associatedParntersId is a structure of an Integer8 and Unsigned16
     * @throws java.io.IOException when writing fails or when decoding the object failed
     */
    public void writeAssociatedPartnersId(Structure associatedParntersId) throws IOException {
    	write(ATTRB_ASSOCIATION_PRTN_ID, associatedParntersId.getBEREncodedByteArray());
    	this.associatedPartnersId = associatedParntersId;
    }

    /**
     * Write a new clientSAP to the device
     * @param clientSAP
     * @throws java.io.IOException when writing fails or when decoding the object failed
     */
    public void setClientSAP(int clientSAP) throws IOException {
    	Structure api = new Structure();
    	api.addDataType(new Integer8(clientSAP));
    	api.addDataType(getServerSAP());
    	writeAssociatedPartnersId(api);
    }

    /**
     * Write a new serverSAP to the device
     * @param serverSAP
     * @throws java.io.IOException when writing fails or when decoding the object failed
     */
    public void setServerSAP(int serverSAP) throws IOException {
    	Structure api = new Structure();
    	api.addDataType(getClientSAP());
    	api.addDataType(new Unsigned16(serverSAP));
    	writeAssociatedPartnersId(api);
    }

    /**
     * Read the xDlmsContextInfo from the device
     * <pre>
     * @return a structure containing the:
     * 				- conformance block
     * 				- maxReceivePDUSize
     * 				- maxSendPDUSize
     * 				- dlmsVersionNumber
     * 				- QOS
     * 				- ciphering INFO
     * </pre>
     * @throws java.io.IOException if read fails, or if parsing the response isn't correct
     */
    public Structure readXdlmsContextInfo() throws IOException {
    	this.xDlmsContextInfo = new Structure(getLNResponseData(ATTRB_XDLMS_CONTEXT_INFO),0,0);
    	return this.xDlmsContextInfo;
    }

    /**
     * Get the current xDlmsContextInfo
     * @return the structure with 6 elements
     * @throws java.io.IOException if read fails, or if parsing the response isn't correct
     */
    public Structure getXdlmsContextInfo() throws IOException {
    	if(this.xDlmsContextInfo == null){
    		return readXdlmsContextInfo();
    	} else {
    		return this.xDlmsContextInfo;
    	}
    }

    /**
     * Write the given xDlmsContextInfo, containing all 6 elements, to the device
     * @param xDlmsContextInfo
     * @throws java.io.IOException when writing fails or when decoding the object failed
     */
    public void writeXdlmsContextInfo(Structure xDlmsContextInfo) throws IOException {
    	write(ATTRB_XDLMS_CONTEXT_INFO, xDlmsContextInfo.getBEREncodedByteArray());
    	this.xDlmsContextInfo = xDlmsContextInfo;
    }

    /**
     * @return a Structure or an OctetString containing the authentication mechanism name
     * @throws java.io.IOException when the response type isn't correct or when the reading failed
     */
    public AbstractDataType readAuthenticationMechanismName() throws IOException {
    	byte[] response = getLNResponseData(ATTRB_AUTHENTICATION_MECH_NAME);
    	if(response[0] == AxdrType.STRUCTURE.getTag()){ // Structure
    		this.authenticationMechanismName = new Structure(response, 0, 0);
    	} else if(response[0] == AxdrType.OCTET_STRING.getTag()){
    		this.authenticationMechanismName = OctetString.fromByteArray(response);
    	} else {
    		throw new ProtocolException("Response is doesn't contain a valid type");
    	}
    	return this.authenticationMechanismName;
    }

    /**
     * Write the given authenticationMechanismName to the device
     * @param amn, the abstractDataType(octetString or structure) containing the authMechName
     * @throws java.io.IOException the type isn't correct or when the writing fails
     */
    public void writeAuthenticationMechanismName(AbstractDataType amn) throws IOException {
    	write(ATTRB_AUTHENTICATION_MECH_NAME, amn.getBEREncodedByteArray());
    	this.authenticationMechanismName = amn;
    }

    /**
     * Write the given secret to the attribute
     * @param secret - the octetString containing the secret
     * @throws java.io.IOException when the type isn't correct or when the writing fails
     */
    public void writeSecret(OctetString secret) throws IOException {
    	write(ATTRB_SECRET, secret.getBEREncodedByteArray());
    	this.secret = secret;
    }

    /**
     * @return a Structure or an OctetString containing the applicationContextName
     * @throws java.io.IOException when the response type isn't correct or when the reading failed
     */
    public AbstractDataType readApplicationContextName() throws IOException {
    	byte[] response = getLNResponseData(ATTRB_APPLICATION_CONTEXT_NAME);
    	if(response[0] == AxdrType.STRUCTURE.getTag()){ // Structure
    		this.applicationContextName = new Structure(response, 0, 0);
    	} else if(response[0] == AxdrType.OCTET_STRING.getTag()){
    		this.applicationContextName = OctetString.fromByteArray(response);
    	} else {
    		throw new ProtocolException("Response is doesn't contain a valid type");
    	}
    	return this.applicationContextName;
    }

    /**
     * Write the given ApplicationContextName to the device
     * @param acn, the abstractDataType(octetString or structure) containing the applicationContextName
     * @throws java.io.IOException the type isn't correct or when the writing fails
     */
    public void writeApplicationContextName(AbstractDataType acn) throws IOException {
    	write(ATTRB_APPLICATION_CONTEXT_NAME, acn.getBEREncodedByteArray());
    	this.applicationContextName = acn;
    }

    /**
     * Read the associationStatus from the device
     * <pre>
     * @return the associationStatus
     *  - 0: non-associated
     *  - 1: association-pending
     *  - 2: associated
     * </pre>
     * @throws java.io.IOException when the response type isn't correct or when the reading failed
     */
    public TypeEnum readAssociationStatus() throws IOException {
    	this.associationStatus = new TypeEnum(getLNResponseData(ATTRB_ASSOCIATON_STATUS), 0);
    	return this.associationStatus;
    }

    /**
     * Read the securitySetupReference logicalName from the device
     * @return the securitySetupReference
     * @throws java.io.IOException when the response type isn't correct or when the reading failed
     */
    public OctetString readSecuritySetupReference() throws IOException {
    	this.securitySetupReference = new OctetString(getLNResponseData(ATTRB_SECURITY_SETUP_REFERENCE), 0);
    	return this.securitySetupReference;
    }

    /**
     * Write the given securitySetupReference to the device
     * @param securitySetupReference
     * @throws java.io.IOException when writing fails or when decoding the object failed
     */
    public void writeSecuritySetupReference(OctetString securitySetupReference) throws IOException {
    	write(ATTRB_SECURITY_SETUP_REFERENCE, securitySetupReference.getBEREncodedByteArray());
    	this.securitySetupReference = securitySetupReference;
    }

    /**
     * Reply to the server with his encrypted challenge
     * @param encryptedChallenge is the response from the associationRequest, encrypted with the HLSKey
     * @return a byteArray contain the clientToServer challenge encrypted with the HLSKey
     * @throws java.io.IOException when invoking the method fails or when decoding the object failed
     */
    public byte[] replyToHLSAuthentication(byte[] encryptedChallenge) throws IOException {
    	return invoke(METHOD_REPLY_TO_HLS_AUTHENTICATION, OctetString.fromByteArray(encryptedChallenge).getBEREncodedByteArray());
    }

    /**
     * Change the HLS_Secret, depending on the securityMechanism implementation, the new secret may contain
     * additional check bits and it may be encrypted
     * @param hlsSecret
     * @return
     * @throws java.io.IOException
     */
    public byte[] changeHLSSecret(byte[] hlsSecret)throws IOException {
    	return invoke(METHOD_CHANGE_HLS_SECRET, OctetString.fromByteArray(hlsSecret).getBEREncodedByteArray());
    }

    /**
     * Add the given object to the ObjectList of the device.
     * The definition of the object is object-independent,
     * see BlueBook 9th, AssociationLN(class_id:15), attribute 2 for a complete description of an object.
     * @param object to add to the list
     * @return
     * @throws java.io.IOException
     */
    public byte[] addObject(Structure object)throws IOException {
    	return invoke(METHOD_ADD_OBJECT, object.getBEREncodedByteArray());
    }

    /**
     * Remove the given object from the ObjectList of the device.
     * The definition of the object is object-independent,
     * see BlueBook 9th, AssociationLN(class_id:15), attribute 2 for a complete description of an object.
     * @param object to be removed from the list
     * @return
     * @throws java.io.IOException
     */
    public byte[] removeObject(Structure object)throws IOException {
    	return invoke(METHOD_REMOVE_OBJECT, object.getBEREncodedByteArray());
    }
}
