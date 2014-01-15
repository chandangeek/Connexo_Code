/*
 * TotalRegister.java
 *
 * Created on 19 maart 2004, 12:51
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.EnergyTypeCode;
/**
 *
 * @author  Koen
 */
public class TotalRegister extends MeterReadingsBlockImpl {

	private int eType;
	private int register;
	private int billingRegister;

    /** Creates a new instance of TotalRegister */
    public TotalRegister(byte[] data) {
        super(data);
    }

    public String print() {
        return "ETYPE=0x"+Integer.toHexString(getEType())+" ("+EnergyTypeCode.getUnit(getEType(),true)+"), REGISTER="+getRegister()+", BILLINGREGISTER="+getBillingRegister();

    }

    protected void parse() throws java.io.IOException {
        setEType(ProtocolUtils.byte2int(getData()[1]));
        setRegister(ProtocolUtils.getIntLE(getData(),2,3));
        setBillingRegister(ProtocolUtils.getIntLE(getData(),5,3));
    }

    /** Getter for property eType.
     * @return Value of property eType.
     *
     */
    public int getEType() {
        return eType;
    }

    /** Setter for property eType.
     * @param eType New value of property eType.
     *
     */
    public void setEType(int eType) {
        this.eType = eType;
    }

    /** Getter for property register.
     * @return Value of property register.
     *
     */
    public int getRegister() {
        return register;
    }

    /** Setter for property register.
     * @param register New value of property register.
     *
     */
    public void setRegister(int register) {
        this.register = register;
    }

    /** Getter for property billingRegister.
     * @return Value of property billingRegister.
     *
     */
    public int getBillingRegister() {
        return billingRegister;
    }

    /** Setter for property billingRegister.
     * @param billingRegister New value of property billingRegister.
     *
     */
    public void setBillingRegister(int billingRegister) {
        this.billingRegister = billingRegister;
    }

}
