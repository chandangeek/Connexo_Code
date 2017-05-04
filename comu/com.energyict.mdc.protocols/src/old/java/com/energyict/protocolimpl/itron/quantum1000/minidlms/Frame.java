/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Frame.java
 *
 * Created on 30 november 2006, 17:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class Frame {

    private int sourceAddress;
    private int destinationAddress;
    private int packetType;
    private byte[] data;
    private int sendSequence;
    private int receivedSequence;
    private boolean lastFrame;

    public String toString() {
        return "SA=0x"+Integer.toHexString(sourceAddress)+", DA=0x"+Integer.toHexString(destinationAddress)+", PacketType="+Integer.toHexString(packetType)+", PN(s)="+sendSequence+", PN(r)="+receivedSequence+", P="+lastFrame;
    }

    /** Creates a new instance of Frame */
    public Frame(byte[] data) {
        setSourceAddress((int)data[0]&0xff);
        setDestinationAddress((int)data[1]&0xff);
        setPacketType((int)data[2]&0xff);

        if (data.length > 3) {
            setData(ProtocolUtils.getSubArray2(data,3, data.length-5));
        }
        setSendSequence(-1);
        setReceivedSequence(-1);

        if ((getPacketType()&0x80) == 0) {
            // I-frame
            setSendSequence((((int) getPacketType() & 0xff) & 0x70) >> 4);
            setReceivedSequence((((int) getPacketType() & 0xff) & 0x07) >> 0);
            setLastFrame((getPacketType()&0x08) == 0x08);
            setPacketType(MiniDLMSConnection.getPACKET_TYPE_I());
        }
        else if (getPacketType() == MiniDLMSConnection.getPACKET_TYPE_SABM()) {
            setPacketType(MiniDLMSConnection.getPACKET_TYPE_SABM());
        }
        else if (getPacketType() == MiniDLMSConnection.getPACKET_TYPE_UA()) {
            setPacketType(MiniDLMSConnection.getPACKET_TYPE_UA());
        }
        else if (getPacketType() == MiniDLMSConnection.getPACKET_TYPE_UI()) {
            setPacketType(MiniDLMSConnection.getPACKET_TYPE_UI());
        }
        else if ((getPacketType() & 0xf8) == MiniDLMSConnection.getPACKET_TYPE_RR()) {
            setReceivedSequence((((int) getPacketType() & 0xff) & 0x07) >> 0);
            setPacketType(MiniDLMSConnection.getPACKET_TYPE_RR());
        }
        else if ((getPacketType() & 0xf8) == MiniDLMSConnection.getPACKET_TYPE_REJ()) {
            setReceivedSequence((((int) getPacketType() & 0xff) & 0x07) >> 0);
            setPacketType(MiniDLMSConnection.getPACKET_TYPE_REJ());
        }

    }

    public boolean isPacketSABM() {
        return getPacketType() == MiniDLMSConnection.getPACKET_TYPE_SABM();
    }
    public boolean isPacketUA() {
        return getPacketType() == MiniDLMSConnection.getPACKET_TYPE_UA();
    }
    public boolean isPacketUI() {
        return getPacketType() == MiniDLMSConnection.getPACKET_TYPE_UA();
    }
    public boolean isPacketI() {
        return (getPacketType() == MiniDLMSConnection.getPACKET_TYPE_I());
    }
    public boolean isPacketRR() {
        return (getPacketType() == MiniDLMSConnection.getPACKET_TYPE_RR());
    }
    public boolean isPacketREJ() {
        return (getPacketType() == MiniDLMSConnection.getPACKET_TYPE_REJ());
    }


    public int getSourceAddress() {
        return sourceAddress;
    }

    private void setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getDestinationAddress() {
        return destinationAddress;
    }

    private void setDestinationAddress(int destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getPacketType() {
        return packetType;
    }

    private void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSendSequence() {
        return sendSequence;
    }

    private void setSendSequence(int sendSequence) {
        this.sendSequence = sendSequence;
    }

    public int getReceivedSequence() {
        return receivedSequence;
    }

    private void setReceivedSequence(int receivedSequence) {
        this.receivedSequence = receivedSequence;
    }

    public int getReceiveSequencePlus1() {
        int temp = receivedSequence;
        if (temp++ >=7) temp=0;
        return temp;
    }
    public int getSendSequencePlus1() {
        int temp = sendSequence;
        if (temp++ >=7) temp=0;
        return temp;
    }


    public boolean isLastFrame() {
        return lastFrame;
    }

    private void setLastFrame(boolean lastFrame) {
        this.lastFrame = lastFrame;
    }

}
