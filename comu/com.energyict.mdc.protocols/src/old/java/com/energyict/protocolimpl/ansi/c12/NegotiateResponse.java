/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * NegotiateResponse.java
 *
 * Created on 17 oktober 2005, 16:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class NegotiateResponse extends AbstractResponse {

    private int packetSize;
    private int nrOfPackets;

    private final String[] baudStr={"300","600","1200","2400","4800","9600","14400","19200","28800","57600"};
    private int baudRateIndex;


    /** Creates a new instance of NegotiateResponse */
    public NegotiateResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    public String toString() {
        return "NegotiateResponse: packetSize="+getPacketSize()+", nrOfPackets="+getNrOfPackets()+""+(getBaudRateIndex()==0?", Baudrate externally defined":(", baudRateIndex="+getBaudRateIndex()+" ("+baudStr[getBaudRateIndex()-1]+")\n"));
    }

    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        byte[] data = responseData.getData();
        setPacketSize(ProtocolUtils.getInt(data,1, 2));
        setNrOfPackets(ProtocolUtils.getInt(data,3,1));
        setBaudRateIndex(ProtocolUtils.getInt(data,4,1));
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getNrOfPackets() {
        return nrOfPackets;
    }

    public void setNrOfPackets(int nrOfPackets) {
        this.nrOfPackets = nrOfPackets;
    }

    public int getBaudRateIndex() {
        return baudRateIndex;
    }

    public void setBaudRateIndex(int baudRateIndex) {
        this.baudRateIndex = baudRateIndex;
    }

}
