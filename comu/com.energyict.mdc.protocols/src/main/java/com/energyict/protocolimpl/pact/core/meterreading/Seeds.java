/*
 * Seeds.java
 *
 * Created on 31 maart 2004, 16:12
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class Seeds extends MeterReadingsBlockImpl {

	private int protcl;
	private int mdSeed;
	private int timeSeed;
	private int dstSeed;

    /** Creates a new instance of Seeds */
    public Seeds(byte[] data) {
        super(data);
    }

    protected void parse() throws IOException {
        setProtcl(ProtocolUtils.byte2int(getData()[1]));
        setMdSeed(ProtocolUtils.getIntLE(getData(),2,2));
        setTimeSeed(ProtocolUtils.getIntLE(getData(),4,2));
        setDstSeed(ProtocolUtils.getIntLE(getData(),6,2));
    }

    protected String print() {
        return "PROTCL="+getProtcl()+", MDSEED=0x"+Integer.toHexString(getMdSeed())+", TIMESEED=0x"+Integer.toHexString(getTimeSeed())+", DSTSEED=0x"+Integer.toHexString(getDstSeed());
    }

    /** Getter for property protcl.
     * @return Value of property protcl.
     *
     */
    public int getProtcl() {
        return protcl;
    }

    /** Setter for property protcl.
     * @param protcl New value of property protcl.
     *
     */
    public void setProtcl(int protcl) {
        this.protcl = protcl;
    }

    /** Getter for property mdSeed.
     * @return Value of property mdSeed.
     *
     */
    public int getMdSeed() {
        return mdSeed;
    }

    /** Setter for property mdSeed.
     * @param mdSeed New value of property mdSeed.
     *
     */
    public void setMdSeed(int mdSeed) {
        this.mdSeed = mdSeed;
    }

    /** Getter for property timeSeed.
     * @return Value of property timeSeed.
     *
     */
    public int getTimeSeed() {
        return timeSeed;
    }

    /** Setter for property timeSeed.
     * @param timeSeed New value of property timeSeed.
     *
     */
    public void setTimeSeed(int timeSeed) {
        this.timeSeed = timeSeed;
    }

    /** Getter for property dstSeed.
     * @return Value of property dstSeed.
     *
     */
    public int getDstSeed() {
        return dstSeed;
    }

    /** Setter for property dstSeed.
     * @param dstSeed New value of property dstSeed.
     *
     */
    public void setDstSeed(int dstSeed) {
        this.dstSeed = dstSeed;
    }

}
