/*
 * AdditionalInformation.java
 *
 * Created on 22 maart 2004, 16:37
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 * @author  Koen
 */
public class AdditionalInformation extends MeterReadingsBlockImpl {
    
	private int mdDivisor;
	private int cmdDivisor; 
	private int tariffFlags;
	private int oldTariffFlags;
    
	private int mask;
    
    /** Creates a new instance of AdditionalInformation */
    public AdditionalInformation(byte[] data) {
        super(data);
    }
    
    protected void parse() throws java.io.IOException {
     
        int subtyp = ProtocolUtils.byte2int(getData()[1]);
        int protcl = ProtocolUtils.byte2int(getData()[2]);
        
        if (subtyp == 0) {
            if (protcl == 0) {
                setTariffFlags(ProtocolUtils.getIntLE(getData(),4,2));
                setOldTariffFlags(ProtocolUtils.getIntLE(getData(),6,2));
            }
            else if (protcl == 2) {
                setMdDivisor(ProtocolUtils.getIntLE(getData(),3,2));
                setCmdDivisor(ProtocolUtils.getIntLE(getData(),5,2));
            } // if (protcl == 2)
            mask |= (0x01<<protcl);
        } // if (subtyp == 0) 
    } // protected void parse()    
    
    protected String print() {
        StringBuffer strBuff = new StringBuffer();
        boolean pre=false;
        if ((mask & 0x0004) == 0x0004) {
           strBuff.append("MDDIVISOR="+getMdDivisor()+", CMDDIVISOR="+getCmdDivisor());
           pre=true;
        }
        if ((mask & 0x0001) == 0x0001) {
           if (pre) {
			strBuff.append(", ");
		}
           strBuff.append("TARIFFFLAGS="+getTariffFlags()+", OLDTARFLAGS="+getOldTariffFlags()); 
        }
        return strBuff.toString();
    }    
    
    /** Getter for property mdDivisor.
     * @return Value of property mdDivisor.
     *
     */
    public int getMdDivisor() {
        return mdDivisor;
    }
    
    /** Setter for property mdDivisor.
     * @param mdDivisor New value of property mdDivisor.
     *
     */
    public void setMdDivisor(int mdDivisor) {
        this.mdDivisor = mdDivisor;
    }
    
    /** Getter for property cmdDivisor.
     * @return Value of property cmdDivisor.
     *
     */
    public int getCmdDivisor() {
        return cmdDivisor;
    }
    
    /** Setter for property cmdDivisor.
     * @param cmdDivisor New value of property cmdDivisor.
     *
     */
    public void setCmdDivisor(int cmdDivisor) {
        this.cmdDivisor = cmdDivisor;
    }
    
    /** Getter for property tariffFlags.
     * @return Value of property tariffFlags.
     *
     */
    public int getTariffFlags() {
        return tariffFlags;
    }
    
    /** Setter for property tariffFlags.
     * @param tariffFlags New value of property tariffFlags.
     *
     */
    public void setTariffFlags(int tariffFlags) {
        this.tariffFlags = tariffFlags;
    }
    
    /** Getter for property oldTariffFlags.
     * @return Value of property oldTariffFlags.
     *
     */
    public int getOldTariffFlags() {
        return oldTariffFlags;
    }
    
    /** Setter for property oldTariffFlags.
     * @param oldTariffFlags New value of property oldTariffFlags.
     *
     */
    public void setOldTariffFlags(int oldTariffFlags) {
        this.oldTariffFlags = oldTariffFlags;
    }
    
}
