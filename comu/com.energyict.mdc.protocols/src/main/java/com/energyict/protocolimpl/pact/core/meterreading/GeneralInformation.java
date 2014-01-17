/*
 * GeneralInformation.java
 *
 * Created on 19 maart 2004, 11:46
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class GeneralInformation extends MeterReadingsBlockImpl {

	private int readCount;
	private int kwhRegister;
	private int tariffFlags;

    /** Creates a new instance of GeneralInformation */
    public GeneralInformation(byte[] data) {
        super(data);
    }

    public String print() {
        return "READCOUNT="+getReadCount()+", KWH_REGISTER="+getKwhRegister()+", TARIFFFLAGS="+getTariffFlags();
    }

    protected void parse() throws IOException {
        setReadCount(ProtocolUtils.getIntLE(getData(),1,2));
        setKwhRegister(ProtocolUtils.getIntLE(getData(),3,3));
        setTariffFlags(ProtocolUtils.getIntLE(getData(),6,2));
    }

    /** Getter for property readCount.
     * @return Value of property readCount.
     *
     */
    public int getReadCount() {
        return readCount;
    }

    /** Setter for property readCount.
     * @param readCount New value of property readCount.
     *
     */
    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    /** Getter for property kwhRegister.
     * @return Value of property kwhRegister.
     *
     */
    public int getKwhRegister() {
        return kwhRegister;
    }

    /** Setter for property kwhRegister.
     * @param kwhRegister New value of property kwhRegister.
     *
     */
    public void setKwhRegister(int kwhRegister) {
        this.kwhRegister = kwhRegister;
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

}
