/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ResponseFrame.java
 *
 * Created on 26 juli 2006, 15:08
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.Connection;

/**
 *
 * @author Koen
 */
public class ResponseFrame {

    private int modeOfOperation; // high nibble of frame index 1
    private boolean ack;
    private boolean dataDump;
    private byte[] unitId = new byte[4];
    private StatusByte status;
    private int frameNr;
    private byte[] data;
    private boolean lastBlock;

    /** Creates a new instance of ResponseFrame */
    public ResponseFrame(byte[] frame) {
       parse(frame);
    }

    // =0x"+Integer.toHexString(
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ResponseFrame:\n");
        strBuff.append("   ack="+isAck()+"\n");
        strBuff.append("   data[]=");
        for (int i=0;i<getData().length;i++) {
            strBuff.append(Integer.toHexString(getData()[i])+" ");
        }
        strBuff.append("\n");
        strBuff.append("   dataDump="+isDataDump()+"\n");
        strBuff.append("   frameNr="+getFrameNr()+"\n");
        strBuff.append("   lastBlock="+isLastBlock()+"\n");
        strBuff.append("   modeOfOperation="+getModeOfOperation()+"\n");
        strBuff.append("   status="+getStatus()+"\n");
        for (int i=0;i<getUnitId().length;i++) {
            strBuff.append("       unitId["+i+"]="+getUnitId()[i]+"\n");
        }
        return strBuff.toString();
    }

    private void parse(byte[] frame) {
        int offset=0;
        setAck(frame[offset] == Connection.ACK);
        setDataDump(frame[offset++] == 0x0C);

        setModeOfOperation((((int)frame[offset]&0xFF)>>4)&0x0F);

        unitId = ProtocolUtils.getSubArray2(frame, offset, 4);
        offset+=4;

        status = new StatusByte((int)frame[offset++]&0xFF);
        setFrameNr((int)frame[offset++]&0xFF);

        if (isDataDump()) {
            data = ProtocolUtils.getSubArray2(frame, offset, 256);
            offset+=256;
        }
        else {
            data = ProtocolUtils.getSubArray2(frame, offset, 6);
            offset+=6;
        }

        offset+=2;
        if (isDataDump())
            setLastBlock(((int)frame[offset]&0xFF) == 0x04);

    } // private void parse(byte[] frame)

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public byte[] getUnitId() {
        return unitId;
    }

    public void setUnitId(byte[] unitId) {
        this.unitId = unitId;
    }

    public StatusByte getStatus() {
        return status;
    }

    public void setStatus(StatusByte status) {
        this.status = status;
    }



    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getFrameNr() {
        return frameNr;
    }

    public void setFrameNr(int frameNr) {
        this.frameNr = frameNr;
    }

    public boolean isDataDump() {
        return dataDump;
    }

    public void setDataDump(boolean dataDump) {
        this.dataDump = dataDump;
    }

    public boolean isLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(boolean lastBlock) {
        this.lastBlock = lastBlock;
    }

    public int getModeOfOperation() {
        return modeOfOperation;
    }

    public void setModeOfOperation(int modeOfOperation) {
        this.modeOfOperation = modeOfOperation;
    }
}
