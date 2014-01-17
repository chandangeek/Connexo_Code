package com.energyict.dlms.aso;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.protocols.util.ProtocolUtils;

public class XdlmsAse {

	/** Indicates that set with datablock is disabled. */
	public static final int SET_WITH_DATABLOCK_DISABLED = -1;

	private static final String	CRLF	= "\r\n";
    /** Contains the dedicatedKey (sessionkey) to use in this association */
	private byte[] dedicatedKey;
	private boolean responseAllowed = true; // default
	private int proposedQOS = -1;
	private int proposedDLMSversion = 6;	// default
    /** The used {@link com.energyict.dlms.aso.ConformanceBlock}*/
	private ConformanceBlock cb;
	private int maxRecPDUClientSize = -1;

	private int negotiatedQOS;
	private int negotiatedDLMSVersion;
	private ConformanceBlock negotiatedConformanceBlock;
	private int maxRecPDUServerSize = SET_WITH_DATABLOCK_DISABLED;
	private short vaaName;

	public XdlmsAse() {
	}

	/**
	 * Create a new instance of an xDLMS_ASE
	 *
	 * @param dedicatedKey - this may contain a cipherKey that can be used in subsequent transmissions to cipher xDLMS APDU's.
	 * It's use is only allowed when the xDLMS-Initiate.Request APDU has been ciphered using a GLOBALKEY. If not used, set it to NULL
	 * @param responseAllowed - indicate if a response is allowed, default true
	 * @param proposedQOS - the proposed QualityOfService, set to default -1
	 * @param proposedDLMSVersion - the proposed DLMSVersion (usually 6)
	 * @param conformanceBlock - the proposed Conformance of the client
	 * @param maxRecPDUSize - the proposed maximum PDU size of the client
	 */
	public XdlmsAse(byte[] dedicatedKey, boolean responseAllowed, int proposedQOS, int proposedDLMSVersion, ConformanceBlock conformanceBlock, int maxRecPDUSize){
		this.cb = conformanceBlock;
		this.dedicatedKey = dedicatedKey != null ? dedicatedKey.clone() : null;
		this.proposedDLMSversion = proposedDLMSVersion;
		this.proposedQOS = proposedQOS;
		this.responseAllowed = responseAllowed;
		this.maxRecPDUClientSize = maxRecPDUSize;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("XdlmsAse:[").append(CRLF);
		sb.append(" > dedicatedKey = ").append(dedicatedKey != null ? ProtocolUtils.getResponseData(dedicatedKey) : "null").append(CRLF);
		sb.append(" > responseAllowed = ").append(getResponseAllowed()).append(CRLF);
		sb.append(" > proposedQOS = ").append(getProposedQOS() != null ? getProposedQOS().getValue() : "null").append(CRLF);
		sb.append(" > proposedDLMSVersion = ").append(getProposedDLMSVersion() != null ? getProposedDLMSVersion().getValue() : "null").append(CRLF);
		sb.append(" > conformanceBlock = ").append(getConformanceBlock()).append(CRLF);
		sb.append(" > maxRecPDUClientSize = ").append(getMaxRecPDUClientSize() != null ? getMaxRecPDUClientSize().getValue() : "null").append(CRLF);
        sb.append(" > negotiatedConformanceBlock = ").append(getNegotiatedConformanceBlock()).append(CRLF);
        sb.append(" > negotiatedQOS = ").append(getNegotiatedQOS()).append(CRLF);
        sb.append(" > negotiatedDLMSVersion = ").append(getNegotiatedDLMSVersion()).append(CRLF);
        sb.append(" > maxRecPDUServerSize = ").append(getMaxRecPDUServerSize()).append(CRLF);
		sb.append(']').append(CRLF);
		return sb.toString();
	}

	/**
	 * Construct a byteArray containing an InitiateRequest using the desired parameters
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

		if(getResponseAllowed()){ // true is the default value
            xDlmsASEReq[t++] = (byte) 0x00;	// value is not present, default TRUE will be used
            //TODO the original was the one above

//			xDlmsASEReq[t++] = (byte) 0x01;	// value is not present, default TRUE will be used
//            xDlmsASEReq[t++] = (byte) 0x01;	// value is not present, default TRUE will be used
		} else {
			xDlmsASEReq[t++] = (byte)0x01; // indicating the presence of the value
			xDlmsASEReq[t++] = (byte)0x00; // value is zero
		}

		if(getProposedQOS() != null){
			xDlmsASEReq[t++] = (byte)0x01; // indicating the presence of the QOS parameter
			System.arraycopy(getProposedQOS().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 1);
			t += 1;
		} else {
			xDlmsASEReq[t++] = 0; // QOS is not present
		}

		System.arraycopy(getProposedDLMSVersion().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 1);
		t += 1;

		System.arraycopy(getConformanceBlock().getAXDREncodedConformanceBlock(), 0, xDlmsASEReq, t, getConformanceBlock().getAXDREncodedConformanceBlock().length);
		t += getConformanceBlock().getAXDREncodedConformanceBlock().length;

		if(getMaxRecPDUClientSize() != null){
			System.arraycopy(getMaxRecPDUClientSize().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 2);
			t += 2;
		} else {
			xDlmsASEReq[t++] = 0;
		}

		return ProtocolUtils.getSubArray(xDlmsASEReq, 0, t-1);
	}

	/**
	 * @return the value of the responseAllowed
	 */
	protected boolean getResponseAllowed() {
		return this.responseAllowed;
	}

	/**
	 * Set the value of the responseAllowed
	 * @param allowed
	 */
	public void setResponseAllowed(boolean allowed) {
		this.responseAllowed = allowed;
	}

	/**
	 * @return the value of the dedicatedKey
	 */
	protected OctetString getDedicatedKey() {
		if (this.dedicatedKey != null) {
			return OctetString.fromByteArray(this.dedicatedKey, dedicatedKey.length);
		} else {
			return null;
		}
	}

	/**
	 * Set the value of the dedicatedKey
	 * @param dedicatedKey
	 */
	public void setDedicatedKey(byte[] dedicatedKey) {
		this.dedicatedKey = dedicatedKey != null ? dedicatedKey.clone() : null;
	}

	/**
	 * @return the proposed qualityOfService
	 */
	protected Integer8 getProposedQOS(){
		if(this.proposedQOS != -1){
			return new Integer8(this.proposedQOS);
		} else {
			return null;
		}
	}

	/**
	 * Set the proposed qualityOfService
	 * @param proposedQOS
	 */
	public void setProposedQOS(int proposedQOS){
		this.proposedQOS = proposedQOS;
	}

	/**
	 * @return the proposed DLMSVersion
	 */
	protected Unsigned8 getProposedDLMSVersion(){
		return new Unsigned8(this.proposedDLMSversion);
	}

	/**
	 * Set the proposed DLMSVersion
	 * @param proposedDLMSVersion
	 */
	public void setProposedDLMSVersion(int proposedDLMSVersion){
		this.proposedDLMSversion = proposedDLMSVersion;
	}

	/**
	 * @return the ConformanceBlock
	 */
	protected ConformanceBlock getConformanceBlock(){
		if(this.cb == null){
			this.cb = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
		}
		return this.cb;
	}

	/**
	 * Set a proposed ConformanceBlock
	 * @param cb
	 */
	public void setConformanceBlock(ConformanceBlock cb){
		this.cb = cb;
	}

	/**
	 * @return the clients maximum receive PDU size
	 */
	protected Unsigned16 getMaxRecPDUClientSize(){
		if(this.maxRecPDUClientSize != -1){
			return new Unsigned16(this.maxRecPDUClientSize);
		}
		return null;
	}

	/**
	 * Set the clients maximum receive PDU size
	 * @param maxSize
	 */
	public void setMaxRecPDUClientSize(int maxSize){
		this.maxRecPDUClientSize = maxSize;
	}

	/**
	 * Set the server his negotiated QualityOfService
	 * @param qos
	 */
	public void setNegotiatedQOS(byte qos) {
		this.negotiatedQOS = qos;
	}

	/**
	 * Set the server his negotiated DLMSVersion
	 * @param dlmsVersion
	 */
	public void setNegotiatedDlmsVersion(byte dlmsVersion) {
		this.negotiatedDLMSVersion = dlmsVersion;
	}

	/**
	 * Set the server his negotiated ConformanceBlock
	 * @param conformance
	 */
	public void setNegotiatedConformance(int conformance) {
		this.negotiatedConformanceBlock = new ConformanceBlock((long)conformance);
	}

    /**
     * Get the negotiated ConformanceBlock, received from the client.
     *
     * @return
     */
    public ConformanceBlock getNegotiatedConformanceBlock() {
        return negotiatedConformanceBlock;
    }

    /**
     * Get the negotiated Quality of service (QOS), received from the client.
     *
     * @return
     */
    public int getNegotiatedQOS() {
        return negotiatedQOS;
    }

    /**
     * Get the negotiated DLMS version, received from the client.
     *
     * @return
     */
    public int getNegotiatedDLMSVersion() {
        return negotiatedDLMSVersion;
    }

    /**
	 * Set the server his maximum receive PDU size
	 * @param maxPDUServer
	 */
	public final void setMaxRecPDUServerSize(final int maxPDUServer) {
		this.maxRecPDUServerSize = maxPDUServer;
	}

	/**
	 * @return the servers his proposed maximum PDU size
	 */
	public int getMaxRecPDUServerSize(){
		return this.maxRecPDUServerSize;
	}

	/**
	 * Set the server his VAA name
	 * @param vaaName
	 */
	public void setVAAName(short vaaName) {
		this.vaaName = vaaName;
	}
}