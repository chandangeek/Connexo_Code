/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.ReadRequest;

import java.io.IOException;

/**
 * @author gna
 *
 */
public abstract class AbstractTrimaranObject {

	private TrimaranObjectFactory trimaranObjectFactory;

    protected abstract byte[] prepareBuild() throws IOException;
    protected abstract void parse(byte[] data) throws IOException;
    protected abstract int getVariableName();

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
