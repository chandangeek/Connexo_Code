/*
 * ResponseFrame.java
 *
 * Created on 11 juli 2005, 9:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

/**
 *
 * @author Koen
 */
public class ResponseFrame {

    private byte[] data;
    private int stat;
    //private int len;
    private boolean ack;
    private String nakReason;
    private int commandByte;
    private int expectedFrameType;

    /** Creates a new instance of ResponseFrame */
    public ResponseFrame() {
        ack=false;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

//    public int getLen() {
//        return len;
//    }
//
//    public void setLen(int len) {
//        this.len = len;
//    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public String getNakReason() {
        return nakReason;
    }

    public void setNakReason(String nakReason) {
        this.nakReason = nakReason;
    }

    public int getCommandByte() {
        return commandByte;
    }

    public void setCommandByte(int commandByte) {
        this.commandByte = commandByte;
    }

    public int getExpectedFrameType() {
        return expectedFrameType;
    }

    public void setExpectedFrameType(int expectedFrameType) {
        this.expectedFrameType = expectedFrameType;
    }

}
