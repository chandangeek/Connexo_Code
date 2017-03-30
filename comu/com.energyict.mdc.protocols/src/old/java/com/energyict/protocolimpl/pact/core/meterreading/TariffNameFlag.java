/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TariffNameFlags.java
 *
 * Created on 19 maart 2004, 16:16
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author  Koen
 *
 * Container for MeterReadingBlock 86 and 87
 * typeId for that object will be 86 but fullparse will be done if id 87 is received
 */
public class TariffNameFlag extends MeterReadingsBlockImpl {

	private int channelId;
	private int bpIndex;
	private String tariffName;
	private int options;
	private byte[] data86=null;
	private byte[] data87=null;

    /** Creates a new instance of TariffNameFlags */
    public TariffNameFlag() {
        super(null,true);
        setTypeId(0x86);
    }

    public String print() {
       return "CHN_ID=0x"+Integer.toHexString(getChannelId())+" (BP_INDEX="+getBpIndex()+", CHAN_NUM=unused"+
              "), tariff name="+getTariffName()+", OPTIONS=0x"+Integer.toHexString(getOptions());
    }

    public boolean is86Set() {
        return data86 != null;
    }
    public boolean is87Set() {
        return data87 != null;
    }

    public void parse86(byte[] data) {
    	if(data != null){
    		this.data86=data.clone();
    	}
       setChannelId(ProtocolUtils.byte2int(data86[1]));
       setBpIndex(getChannelId()>>4);
    }

    public void parse87(byte[] data) {
        try {
        	if(data != null){
        		data87=data.clone();
        	}
            setTariffName((new String(ProtocolUtils.getSubArray2(data86,2,6)))+
                          (new String(ProtocolUtils.getSubArray2(data87,2,2))));
            setOptions(ProtocolUtils.getIntLE(data87,4,2));
        }
        catch(IOException e) {
            e.printStackTrace(); // should not happen
        }
    }

    protected void parse() throws java.io.IOException {
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



    /** Getter for property tariffName.
     * @return Value of property tariffName.
     *
     */
    public java.lang.String getTariffName() {
        return tariffName;
    }

    /** Setter for property tariffName.
     * @param tariffName New value of property tariffName.
     *
     */
    public void setTariffName(java.lang.String tariffName) {
        this.tariffName = tariffName;
    }

    /** Getter for property options.
     * @return Value of property options.
     *
     */
    public int getOptions() {
        return options;
    }

    /** Setter for property options.
     * @param options New value of property options.
     *
     */
    public void setOptions(int options) {
        this.options = options;
    }

}
