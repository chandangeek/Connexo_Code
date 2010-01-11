package com.energyict.protocolimpl.dlms.as220;

/**
 * @author jme
 */
public class InitiateResponse {

	private byte	bNegotiatedQualityOfService	= 0;
	private byte	bNegotiatedDLMSVersionNR	= 0;
	private long	lNegotiatedConformance		= 0;
	private short	sServerMaxReceivePduSize	= 0;
	private short	sVAAName					= 0;

	public void setbNegotiatedDLMSVersionNR(byte bNegotiatedDLMSVersionNR) {
		this.bNegotiatedDLMSVersionNR = bNegotiatedDLMSVersionNR;
	}

	public void setbNegotiatedQualityOfService(byte bNegotiatedQualityOfService) {
		this.bNegotiatedQualityOfService = bNegotiatedQualityOfService;
	}

	public void setlNegotiatedConformance(long lNegotiatedConformance) {
		this.lNegotiatedConformance = lNegotiatedConformance;
	}

	public void setsServerMaxReceivePduSize(short sServerMaxReceivePduSize) {
		this.sServerMaxReceivePduSize = sServerMaxReceivePduSize;
	}

	public void setsVAAName(short sVAAName) {
		this.sVAAName = sVAAName;
	}

	public byte getbNegotiatedDLMSVersionNR() {
		return bNegotiatedDLMSVersionNR;
	}

	public byte getbNegotiatedQualityOfService() {
		return bNegotiatedQualityOfService;
	}

	public long getlNegotiatedConformance() {
		return lNegotiatedConformance;
	}

	public short getsServerMaxReceivePduSize() {
		return sServerMaxReceivePduSize;
	}

	public short getsVAAName() {
		return sVAAName;
	}

}
