/*
 * AssociationSN.java
 *
 * Created on 20 augustus 2004, 16:47
 */

package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;
/**
 *
 * @author  Koen
 */
public class AssociationSN extends AbstractCosemObject {
    public final int DEBUG=0;
    public static final int CLASSID=12;
    
    
    /** Attributes */
    private UniversalObject[] buffer; // the objectList
    private Array accessRightsList; // contains the access rights to attributes and methods
    private OctetString securitySetupReference; //References the SecuritySetup object by its logical name
    
    /** Attribute numbers (shortname notation ...) */
    private static final int ATTRB_OBJECT_LIST = 0x08;
    private static final int ATTRB_ACCESS_RIGHTS = 0x10;
    private static final int ATTRB_SECURITY_SETUP_REF = 0x18;
    
    /** Method invoke */
    private static int METHOD_READ_BY_LOGICAL_NAME = 3;
    private static int METHOD_CHANGE_SECRET = 5;
    private static int METHOD_REPLY_TO_HLS_AUTHENTICATION = 8;
    
    static final byte[] LN=new byte[]{0,0,(byte)40,0,0,(byte)255};
    
    public AssociationSN(ProtocolLink protocolLink){
    	super(protocolLink, new ObjectReference(LN));
    }
    
    /** Creates a new instance of AssociationSN */
    public AssociationSN(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference );
    }
    
    protected int getClassId() {
        return CLASSID;
    }
    
    /** Return the logicalName (obiscode) of this object */
    public static ObisCode getObisCode(){
    	return ObisCode.fromByteArray(LN);
    }
    
    /**
     * Read the objectList from the current association
     * @return an array of UO containing the information of the objects in the device
     * @throws IOException
     */
    public UniversalObject[] getBuffer() throws IOException {
    	buffer = data2UOL(getResponseData(ASSOC_SN_ATTR_OBJ_LST));
    	return buffer;
    }
   
    /**
     * Reply to the server with his encrypted challenge
     * @param encryptedChallenge is the response from the associationRequest, encrypted with the HLSKey
     * @return a byteArray contain the clientToServer challenge encrypted with the HLSKey
     */
    public byte[] replyToHLSAuthentication(byte[] encryptedChallenge) throws IOException {
    	return invoke(METHOD_REPLY_TO_HLS_AUTHENTICATION, new OctetString(encryptedChallenge).getBEREncodedByteArray());
    }
    
    /**
     * Change the HLS_Secret, depending on the securityMechanism implementation, the new secret may contain
     * additional check bits and it may be encrypted
     * @param secret the new secret
     * @return the irrelevant response (parsing has already been done)
     * @throws IOException if the type of the secret doensn't match or when you don't have the proper permissions
     */
    public byte[] changeSecret(byte[] secret) throws IOException {
    	return invoke(METHOD_CHANGE_SECRET, new OctetString(secret).getBEREncodedByteArray());
    }
}
