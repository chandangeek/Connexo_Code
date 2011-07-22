package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.methods.AssociationSNMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;


/**
 * @author Koen
 */
public class AssociationSN extends AbstractCosemObject {

	private static final ObisCode	OBISCODE					= ObisCode.fromString("0.0.40.0.0.255");

	/** Attribute numbers (short name notation ...) */
	private static final int		ATTRB_OBJECT_LIST			= 0x08;

	/** Method invoke */
	private static final int		METHOD_CHANGE_SECRET		= 5;
	private static final int		METHOD_REPLY_TO_HLS_AUTH	= 8;

	private UniversalObject[]		buffer						= null;

	/**
	 * Creates a new instance of AssociationSN
	 *
	 * @param protocolLink
	 */
	public AssociationSN(ProtocolLink protocolLink) {
		super(protocolLink, new ObjectReference(getObisCode().getLN()));
	}

	/**
	 * Creates a new instance of AssociationSN
	 *
	 * @param protocolLink
	 * @param objectReference
	 */
	public AssociationSN(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return DLMSClassId.ASSOCIATION_SN.getClassId();
	}

	/**
	 * Getter for the default {@link ObisCode} of this object
	 *
	 * @return the logicalName ({@link ObisCode}) of this object
	 */
	public static ObisCode getObisCode() {
		return OBISCODE;
	}

	/**
	 * Read the objectList from the current association
	 *
	 * @return an array of UO containing the information of the objects in the device
	 * @throws IOException
	 */
	public UniversalObject[] getBuffer() throws IOException {
		buffer = data2UOL(getResponseData(ATTRB_OBJECT_LIST));
		return buffer;
	}

	/**
	 * Reply to the server with his encrypted challenge
	 *
	 * @param encryptedChallenge is the response from the associationRequest,
	 * encrypted with the HLSKey
	 * @return a byteArray contain the clientToServer challenge encrypted with
	 * the HLSKey
	 */
	public byte[] replyToHLSAuthentication(byte[] encryptedChallenge) throws IOException {
        return methodInvoke(AssociationSNMethods.REPLY_TO_HLS_AUTHENTICATION, new OctetString(encryptedChallenge));
	}

	/**
	 * Change the HLS_Secret, depending on the securityMechanism implementation,
	 * the new secret may contain additional check bits and it may be encrypted
	 *
	 * @param secret the new secret
	 * @return the irrelevant response (parsing has already been done)
	 * @throws IOException if the type of the secret doensn't match or when you
	 * don't have the proper permissions
	 */
	public byte[] changeSecret(byte[] secret) throws IOException {
		return methodInvoke(AssociationSNMethods.CHANGE_SECRET, new OctetString(secret));
	}
}
