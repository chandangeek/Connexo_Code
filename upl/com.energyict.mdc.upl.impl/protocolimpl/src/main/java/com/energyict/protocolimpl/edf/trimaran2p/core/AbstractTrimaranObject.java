/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.ReadRequest;

/**
 * @author gna
 *
 */
abstract public class AbstractTrimaranObject {
	
	private TrimaranObjectFactory trimaranObjectFactory;
	
    abstract protected byte[] prepareBuild() throws IOException;
    abstract protected void parse(byte[] data) throws IOException;
    abstract protected int getVariableName();

	/**
	 * Creates a new instance of AbstractTrimaranObject
	 */
	public AbstractTrimaranObject(TrimaranObjectFactory trimaranObjectFactory) {
		this.trimaranObjectFactory = trimaranObjectFactory;
	}
	
	public void write() throws IOException{
		getTrimaranObjectFactory().getTrimaran().getDLMSPDUFactory().getWriteRequest(getVariableName(), prepareBuild());
	}
	
	public void read() throws IOException{
		ReadRequest rr = getTrimaranObjectFactory().getTrimaran().getDLMSPDUFactory().getReadRequest(getVariableName());
		parse(rr.getReadResponse().getReadResponseData());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	/**
	 * @return the trimaranObjectFactory
	 */
	protected TrimaranObjectFactory getTrimaranObjectFactory() {
		return trimaranObjectFactory;
	}
	/**
	 * @param trimaranObjectFactory the trimaranObjectFactory to set
	 */
	protected void setTrimaranObjectFactory(
			TrimaranObjectFactory trimaranObjectFactory) {
		this.trimaranObjectFactory = trimaranObjectFactory;
	}

}
