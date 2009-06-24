package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;

public class SecuritySetup extends AbstractCosemObject{
	
	static public final int CLASSID = 64;
	static final byte[] LN=new byte[]{0,0,43,0,0,(byte)255};
	
	/** Attributes */
	private TypeEnum securityPolicy = null; 		//Enforces authentication and/or encryption algorithm provided with security_suite.
	private TypeEnum securitySuite = null;			//Specifies authentication, encryption and key wrapping algorithm. 
	private OctetString clientSystemTitle = null;	//Carries the current client system title
	private OctetString serverSystemTitle = null;	//Carries the server system title
	
	/** Attribute numbers */
	static private final int ATTRB_SECURITY_POLICY = 2;
	static private final int ATTRB_SECURITY_SUITE = 3;
	static private final int ATTRB_CLIENT_SYSTEM_TITLE = 4;
	static private final int ATTRB_SERVER_SYSTEM_TITLE = 5;
	
	/** Methods */
	static private final int METHOD_SECURITY_ACTIVATE = 1;		// Activates and strengthens the security policy
	static private final int METHOD_GLOBAL_KEY_TRANSFER = 2;	// Update one or more global keys
	
	public SecuritySetup(ProtocolLink protocolLink,	ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}
	
	public SecuritySetup(ProtocolLink protocolLink){
		super(protocolLink, new ObjectReference(LN));
	}

	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	} 
	
	protected int getClassId() {
		return this.CLASSID;
	}

	/**
	 * Read the current securityPolicy from the device
	 * @return 
	 * @throws IOException
	 */
	public TypeEnum readSecurityPolicy() throws IOException{
		try {
			this.securityPolicy = new TypeEnum(getLNResponseData(ATTRB_SECURITY_POLICY), 0);
			return this.securityPolicy;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Couldn't read the securityPolicy." + e.getMessage());
		}
	}
	/**
	 * @return the current securityPolicy
	 * @throws IOException
	 */
	public TypeEnum getSecurityPolicy() throws IOException {
		if(this.securityPolicy == null){
			return readSecurityPolicy();
		} else {
			return this.securityPolicy;
		}
	}
	
	/**
	 * Read the current securitySuite from the device
	 * @return
	 * @throws IOException
	 */
	public TypeEnum readSecuritySuite() throws IOException{
		try{
			this.securitySuite = new TypeEnum(getLNResponseData(ATTRB_SECURITY_SUITE),0);
			return this.securitySuite;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Couldn't read the securitySuite." + e.getMessage()); 
		}
	}
	/**
	 * @return the current securitySuite
	 * @throws IOException
	 */
	public TypeEnum getSecuritySuite() throws IOException{
		if(this.securitySuite == null){
			return readSecuritySuite();
		} else {
			return this.securitySuite;
		}
	}
	/**
	 * Write the given securitySuite to the device
	 * @param securitySuite
	 * @throws IOException
	 */
	public void writeSecuritySuite(TypeEnum securitySuite) throws IOException{
		try {
			write(ATTRB_SECURITY_SUITE, securitySuite.getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Couldn't write the securitySuite to the device." + e.getMessage());
		}
	}
	
	/**
	 * @return the current clientSystem title
	 * @throws IOException
	 */
	public OctetString readClientSystemTitle() throws IOException {
		try{
			this.clientSystemTitle = new OctetString(getLNResponseData(ATTRB_CLIENT_SYSTEM_TITLE));
			return this.clientSystemTitle;
		} catch(IOException e) {
			e.printStackTrace();
			throw new IOException("Couldn't read the current client system title from the device." + e.getMessage());
		}
	}
	
	/**
	 * @return the serverSystem title
	 * @throws IOException
	 */
	public OctetString readServerSystemTitle() throws IOException {
		try{
			this.serverSystemTitle = new OctetString(getLNResponseData(ATTRB_SERVER_SYSTEM_TITLE));
			return this.serverSystemTitle;
		} catch(IOException e){
			e.printStackTrace();
			throw new IOException("Couldn't read the server system title." + e.getMessage()); 
		}
	}
	
	/**
	 * Activate the given securityPolicy for this device.
	 * NOTE: THE SECURITY POLICY CAN ONLY BE STRENGTHENED
	 * @param securityPolicy
	 * @return
	 * @throws IOException
	 */
	public byte[] activateSecurity(TypeEnum securityPolicy) throws IOException {
		try {
			return invoke(METHOD_SECURITY_ACTIVATE, securityPolicy.getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not activate the securityPolicy." + e.getMessage());
		}
	}
	
	/**
	 * Transfer one or more global keys to the device.
	 * The global keys must be wrapped with the MasterKey
	 * @param keyData
	 * @return
	 * @throws IOException
	 */
	public byte[] transferGlobalKey(Array keyData) throws IOException {
		try{
			return invoke(METHOD_GLOBAL_KEY_TRANSFER, keyData.getBEREncodedByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not transfer the globalKey(s)" + e.getMessage());
		}
	}
}
