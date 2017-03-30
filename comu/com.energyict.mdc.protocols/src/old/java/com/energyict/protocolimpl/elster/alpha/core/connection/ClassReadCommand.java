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
public class ClassReadCommand extends CommandBuilder {

    private static final int COMMANDBYTE = 5;
    int timeout;
    int expectedFrameType;

    /** Creates a new instance of ClassReadCommand */
    public ClassReadCommand(AlphaConnection alphaConnection) {
        super(alphaConnection);
    }
    public ClassReadCommand(AlphaConnection alphaConnection, int timeout) {
        super(alphaConnection);
        this.timeout = timeout;
    }

    protected int getExpectedFrameType() {
        return expectedFrameType;
    }

    public ResponseFrame readClass(int classId,int classLength) throws IOException {
        return readClass(classId,classLength,false);
    } // public ResponseFrame readClassMultiple(int classId,int classLength) throws IOException

    public ResponseFrame readClass(int classId,int classLength, boolean multiple) throws IOException {
        if (multiple)
            expectedFrameType = AlphaConnection.FRAME_RESPONSE_TYPE_DATA_MULTIPLE;
        else
            expectedFrameType = AlphaConnection.FRAME_RESPONSE_TYPE_DATA_SINGLE;

        byte[] data = new byte[7];
        data[0] = COMMANDBYTE;
        data[1] = (byte)timeout; // pad
        data[2] = (byte)((classLength>>8)&0xFF);
        data[3] = (byte)(classLength&0xFF);
        data[4] = 0;
        data[5] = 0;
        data[6] = (byte)classId;
        return sendCommandWithResponse(data);
    } // public ResponseFrame readClass(int classId,int classLength, boolean multiple) throws IOException

} // public class ClassReadCommand extends CommandBuilder
