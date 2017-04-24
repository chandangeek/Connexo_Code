/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;

import java.io.IOException;

/**
 * @author gna
 *
 */
public class ProgrammablesIndexReader extends AbstractTrimaranObject{

	private Programmables programmables;
	private int variablName;

	/**
	 *
	 */
	public ProgrammablesIndexReader(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("*** ProgrammableIndexReader: ***\n");
        strBuff.append("	- Programmable " + getVariableName()+ " : " + getProgrammables()+"\n");
        return strBuff.toString();
    }

	public Programmables getProgrammables() {
		return programmables;
	}

	public void setProgrammables(Programmables programmables){
		this.programmables = programmables;
	}

	public void setVariableName(int variableName){
		this.variablName = variableName;
	}

	protected int getVariableName() {
		return variablName;
	}

	protected void parse(byte[] data) throws IOException {
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        setProgrammables(new Programmables(dc, getTrimaranObjectFactory().getTrimaran().getTimeZone(), getVariableName()));
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

}
