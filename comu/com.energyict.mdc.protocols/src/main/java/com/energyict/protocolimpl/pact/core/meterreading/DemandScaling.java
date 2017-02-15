/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DemandScaling.java
 *
 * Created on 19 maart 2004, 16:03
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class DemandScaling extends MeterReadingsBlockImpl {
	private int channelId;
	private int bpIndex;
	private int channelNumber;
	private int mdDivisor;
	private int cmdDivisor;

    /** Creates a new instance of DemandScaling */
    public DemandScaling(byte[] data) {
        super(data,true);
    }

    public String print() {
       return "CHN_ID=0x"+Integer.toHexString(getChannelId())+" (BP_INDEX="+getBpIndex()+", CHAN_NUM="+getChannelNumber()+
              "), MD_DIVISOR="+getMdDivisor()+", CMD_DIVISOR="+getCmdDivisor();
    }

    protected void parse() throws java.io.IOException {
       setChannelId(ProtocolUtils.byte2int(getData()[1]));
       setBpIndex(getChannelId()>>4);
       setChannelNumber(getChannelId()&0x0F);
       setMdDivisor(ProtocolUtils.getIntLE(getData(),3,2));
       setCmdDivisor(ProtocolUtils.getIntLE(getData(),5,2));
    }

    /** Getter for property channelId.
     * @return Value of property channelId.
     *
     */
    public int getChannelId() {
        return channelId;
    }

    /** Setter for property channelId.
     * @param channelId New value of property channelId.
     *
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /** Getter for property bpIndex.
     * @return Value of property bpIndex.
     *
     */
    public int getBpIndex() {
        return bpIndex;
    }

    /** Setter for property bpIndex.
     * @param bpIndex New value of property bpIndex.
     *
     */
    public void setBpIndex(int bpIndex) {
        this.bpIndex = bpIndex;
    }

    /** Getter for property channelNumber.
     * @return Value of property channelNumber.
     *
     */
    public int getChannelNumber() {
        return channelNumber;
    }

    /** Setter for property channelNumber.
     * @param channelNumber New value of property channelNumber.
     *
     */
    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
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

}
