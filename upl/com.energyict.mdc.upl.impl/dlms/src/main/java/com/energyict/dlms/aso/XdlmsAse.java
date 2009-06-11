package com.energyict.dlms.aso;

import java.io.IOException;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.protocol.ProtocolUtils;

public class XdlmsAse {

	private String dedicatedKey;
	private boolean responseAllowed = true; // default
	private int proposedQOS = -1;
	private int proposedDLMSversion = 6;	// default
	private ConformanceBlock cb;
	private int maxRecPDUClientSize = -1;
	
	private int negotiatedQOS;
	private int negotiatedDLMSVersion;
	private ConformanceBlock negotiatedConformanceBlock;
	private int maxRecPDUServerSize;
	private short vaaName;

	public XdlmsAse() {
	}
	
	/**
	 * @param dedicatedKey
	 * @param responseAllowed
	 * @param proposedQOS
	 * @param proposedDLMSVersion
	 * @param conformanceBlock
	 */
	public XdlmsAse(String dedicatedKey, boolean responseAllowed, int proposedQOS, int proposedDLMSVersion, ConformanceBlock conformanceBlock, int maxRecPDUSize){
		setConformanceBlock(conformanceBlock);
		setDedicatedKey(dedicatedKey);
		setProposedDLMSVersion(proposedDLMSVersion);
		setProposedQOS(proposedQOS);
		setResponseAllowed(responseAllowed);
		setMaxRecPDUClientSize(maxRecPDUSize);
	}

	public byte[] getInitiatRequestByteArray() throws IOException {
		int t = 0;
		byte[] xDlmsASEReq = new byte[1024];
		
		xDlmsASEReq[t++] = DLMSCOSEMGlobals.COSEM_INITIATEREQUEST;
		
		if (getDedicatedKey() != null) {
			System.arraycopy(getDedicatedKey().getBEREncodedByteArray(), 2,
					xDlmsASEReq, t, getDedicatedKey().getDecodedSize());
			t += getDedicatedKey().getDecodedSize();
		} else {
			xDlmsASEReq[t++] = 0; // key not present
		}

		xDlmsASEReq[t++] = getResponseAllowed()?(byte)0xff:(byte)0x00;
		
		if(getProposedQOS() != null){
			System.arraycopy(getProposedQOS().getBEREncodedByteArray(), 1, xDlmsASEReq, t, 1);
			t += 1;
		} else {
			xDlmsASEReq[t++] = 0; // no QOS proposed
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
	
	protected boolean getResponseAllowed() {
		return this.responseAllowed;
	}

	public void setResponseAllowed(boolean allowed) {
		this.responseAllowed = allowed;
	}

	protected OctetString getDedicatedKey() {
		if (this.dedicatedKey != null) {
			return OctetString.fromString(this.dedicatedKey);
		} else {
			return null;
		}
	}

	public void setDedicatedKey(String dedicatedKey) {
		this.dedicatedKey = dedicatedKey;
	}
	
	protected Integer8 getProposedQOS(){
		if(this.proposedQOS != -1){
			return new Integer8(this.proposedQOS);
		} else
			return null;
	}
	
	public void setProposedQOS(int proposedQOS){
		this.proposedQOS = proposedQOS;
	}
	
	protected Unsigned8 getProposedDLMSVersion(){
		return new Unsigned8(this.proposedDLMSversion);
	}
	
	public void setProposedDLMSVersion(int proposedDLMSVersion){
		this.proposedDLMSversion = proposedDLMSVersion;
	}
	
	protected ConformanceBlock getConformanceBlock(){
		if(this.cb == null){
			this.cb = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
		}
		return this.cb;
	}
	
	public void setConformanceBlock(ConformanceBlock cb){
		this.cb = cb;
	}
	
	protected Unsigned16 getMaxRecPDUClientSize(){
		if(this.maxRecPDUClientSize != -1){
			return new Unsigned16(this.maxRecPDUClientSize);
		}
		return null;
	}
	
	public void setMaxRecPDUClientSize(int maxSize){
		this.maxRecPDUClientSize = maxSize;
	}

	public void setNegotiatedQOS(byte qos) {
		this.negotiatedQOS = qos;
	}

	public void setNegotiatedDlmsVersion(byte dlmsVersion) {
		this.negotiatedDLMSVersion = dlmsVersion;
	}

	public void setNegotiatedConformance(int conformance) {
		this.negotiatedConformanceBlock = new ConformanceBlock((long)conformance);
	}

	public void setMaxRecPDUServerSize(short maxPDUServer) {
		this.maxRecPDUServerSize = maxPDUServer;
	}

	public void setVAAName(short vaaName) {
		this.vaaName = vaaName;
	}
}