/*
 * TimeDateMD.java
 *
 * Created on 19 maart 2004, 13:39
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.PactUtils;

import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class TimeDateMD extends MeterReadingsBlockImpl {

	private int regId;
	private int regIdEnergyIndex;
	private int regIdRegisterNumber;
	private Date currentTime;
	private Date billingTime;

    /** Creates a new instance of TimeDateMD
     * @param data data to construct object
     */
    public TimeDateMD(byte[] data,TimeZone timeZone) {
        super(data,timeZone);
    }

    public String print() {
        return "REG_ID=0x"+Integer.toHexString(getRegId())+
               " (ENERGY_INDEX="+getRegIdEnergyIndex()+
               ", REGISTER_NUMBER="+getRegIdRegisterNumber()+
               "), CURRENT_TIME="+(getCurrentTime()==null?"N/A":getCurrentTime().toString())+
               ", BILLING_TIME="+(getBillingTime()==null?"N/A":getBillingTime().toString());
    }

    protected void parse() throws java.io.IOException {
        setRegId(ProtocolUtils.byte2int(getData()[1]));
        setRegIdEnergyIndex(getRegId()>>5);
        setRegIdRegisterNumber(getRegId()&0x1F);
        long val = (long)ProtocolUtils.getLongLE(getData(),2,3);
        if (val == 0xFFFFFF) {
			setCurrentTime(null);
		} else {
			setCurrentTime(PactUtils.getCalendar(val,5,getTimeZone()).getTime());
		}
        val = ProtocolUtils.getLongLE(getData(),5,3);
        if (val == 0xFFFFFF) {
			setBillingTime(null);
		} else {
			setBillingTime(PactUtils.getCalendar(val,5,getTimeZone()).getTime());
		}
    }

    /** Getter for property regId.
     * @return Value of property regId.
     */
    public int getRegId() {
        return regId;
    }

    /** Setter for property regId.
     * @param regId New value of property regId.
     *
     */
    public void setRegId(int regId) {
        this.regId = regId;
    }

    /** Getter for property regIdEnergyIndex.
     * @return Value of property regIdEnergyIndex.
     *
     */
    public int getRegIdEnergyIndex() {
        return regIdEnergyIndex;
    }

    /** Setter for property regIdEnergyIndex.
     * @param regIdEnergyIndex New value of property regIdEnergyIndex.
     *
     */
    public void setRegIdEnergyIndex(int regIdEnergyIndex) {
        this.regIdEnergyIndex = regIdEnergyIndex;
    }

    /** Getter for property regIdRegisterNumber.
     * @return Value of property regIdRegisterNumber.
     *
     */
    public int getRegIdRegisterNumber() {
        return regIdRegisterNumber;
    }

    /** Setter for property regIdRegisterNumber.
     * @param regIdRegisterNumber New value of property regIdRegisterNumber.
     *
     */
    public void setRegIdRegisterNumber(int regIdRegisterNumber) {
        this.regIdRegisterNumber = regIdRegisterNumber;
    }

    /** Getter for property currentTime.
     * @return Value of property currentTime.
     *
     */
    public Date getCurrentTime() {
        return currentTime;
    }

    /** Setter for property currentTime.
     * @param currentTime New value of property currentTime.
     */
    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    /** Getter for property billingTime.
     * @return Value of property billingTime.
     *
     */
    public Date getBillingTime() {
        return billingTime;
    }

    /** Setter for property billingTime.
     * @param billingTime New value of property billingTime.
     *
     */
    public void setBillingTime(Date billingTime) {
        this.billingTime = billingTime;
    }

}
