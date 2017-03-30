/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClassReadCommand.java
 *
 * Created on 8 juli 2005, 11:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ClassWriteCommand extends CommandBuilder {

    private static final int COMMANDBYTE = 0x11;
    int timeout;
    int expectedFrameType;

    /** Creates a new instance of ClassWriteCommand */
    public ClassWriteCommand(AlphaConnection alphaConnection) {
        super(alphaConnection);
    }
    public ClassWriteCommand(AlphaConnection alphaConnection, int timeout) {
        super(alphaConnection);
        this.timeout = timeout;
    }

    protected int getExpectedFrameType() {
        return AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
    }

    public void writeClass(int classId,int classLength, byte[] classData) throws IOException {

        byte[] data = new byte[7+classData.length+1];
        data[0] = COMMANDBYTE;
        data[1] = (byte)timeout; // pad
        data[2] = (byte)((classLength>>8)&0xFF);
        data[3] = (byte)(classLength&0xFF);
        data[4] = 0;
        data[5] = 0;
        data[6] = (byte)classId;
        System.arraycopy(classData, 0, data, 7, classData.length);
        data[data.length-1] = (byte)calcChecksum(classData);

        sendCommandWithResponse(data);
    } // public ResponseFrame readClass(int classId,int classLength, boolean multiple) throws IOException


    protected int calcChecksum(byte[] data) throws IOException {
       int checksum = 0;
       for (int i=0;i<data.length;i++)
           checksum += data[i];
       checksum= ((checksum&0xFF)^0xFF);
       return checksum;
    }

}
