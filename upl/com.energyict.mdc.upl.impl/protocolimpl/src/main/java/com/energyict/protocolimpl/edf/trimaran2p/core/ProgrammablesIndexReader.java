/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	protected int getVariableName() {
		return variablName;
	}

	protected void parse(byte[] data) throws IOException {
        DataContainer dc = new DataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        setProgrammables(new Programmables(dc, getTrimaranObjectFactory().getTrimaran().getTimeZone(), getVariableName()));
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

}
