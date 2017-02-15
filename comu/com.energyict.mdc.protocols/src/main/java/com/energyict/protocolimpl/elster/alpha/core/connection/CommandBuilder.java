/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandBuilder.java
 *
 * Created on 8 juli 2005, 11:31
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
abstract public class CommandBuilder {

    AlphaConnection alphaConnection;

    abstract int getExpectedFrameType();

    /** Creates a new instance of CommandBuilder */
    public CommandBuilder(AlphaConnection alphaConnection) {
        this.alphaConnection=alphaConnection;
    }


    /*******************************************************************************************
     * PROTECTED METHODS delegete to AlphaConnection Class
     ******************************************************************************************/
    protected void sendCommandWithoutResponse(int data) throws IOException {
        sendCommand(new byte[]{(byte)data}, false);
    }
    protected void sendCommandWithoutResponse(byte[] data) throws IOException {
        sendCommand(data, false);
    }
    protected ResponseFrame sendCommandWithResponse(int data) throws IOException {
        return sendCommand(new byte[]{(byte)data}, true);
    }
    protected ResponseFrame sendCommandWithResponse(byte[] data) throws IOException {
        return sendCommand(data, true);
    }
    protected ResponseFrame sendCommand(byte[] data, boolean response) throws IOException {
        ResponseFrame responseFrame = alphaConnection.sendCommand(data,getExpectedFrameType(),response);

        if (response) {
            if (!responseFrame.isAck())
                throw new IOException("sendCommand(), ERROR, reason "+responseFrame.getNakReason());
            return responseFrame;
        }
        else {
            return null;
        }
    }

}
