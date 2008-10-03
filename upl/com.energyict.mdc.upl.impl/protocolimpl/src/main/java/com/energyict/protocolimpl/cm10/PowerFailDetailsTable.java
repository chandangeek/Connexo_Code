package com.energyict.protocolimpl.cm10;

import com.energyict.protocol.ProtocolUtils;

public class PowerFailDetailsTable {
	
	private CM10 cm10Protocol;
	
	private byte[] timPf;
	private byte[] timPr;
	private byte[] dialStructure;
	private byte[] hrOut;
	private byte[] secOut;
	private byte[] lpfCnt;
	private byte[] recentPf;
	private byte[] longestPf;
	
	
	public PowerFailDetailsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	


	public void parse(byte[] data) {
		cm10Protocol.getLogger().info("length events: " + data.length);
		timPf =ProtocolUtils.getSubArray(data, 0, 5);
		timPr =ProtocolUtils.getSubArray(data, 6, 11);
		dialStructure =ProtocolUtils.getSubArray(data, 12, 155);
		hrOut =ProtocolUtils.getSubArray(data, 156, 157);
		secOut =ProtocolUtils.getSubArray(data, 158, 159);
		lpfCnt =ProtocolUtils.getSubArray(data, 160, 161);
		recentPf =ProtocolUtils.getSubArray(data, 162, 233);
		longestPf =ProtocolUtils.getSubArray(data, 234, 251);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("");
		buf.append("timPf = " + ProtocolUtils.outputHexString(timPf)).append("\n");
		buf.append("timPr = " + ProtocolUtils.outputHexString(timPr)).append("\n");
		buf.append("dialStructure = " + ProtocolUtils.outputHexString(dialStructure)).append("\n");
		buf.append("hrOut = " + ProtocolUtils.outputHexString(hrOut)).append("\n");
		buf.append("secOut = " + ProtocolUtils.outputHexString(secOut)).append("\n");
		buf.append("lpfCnt = " + ProtocolUtils.outputHexString(lpfCnt)).append("\n");
		buf.append("recentPf = " + ProtocolUtils.outputHexString(recentPf)).append("\n");
		buf.append("longestPf = " + ProtocolUtils.outputHexString(longestPf)).append("\n");
		return buf.toString();
	}

}
