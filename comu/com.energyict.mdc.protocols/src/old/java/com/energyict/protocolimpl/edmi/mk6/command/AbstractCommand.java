/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractCommand.java
 *
 * Created on 21 maart 2006, 10:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.protocolimpl.edmi.mk6.core.ResponseData;

import java.io.IOException;



/**
 *
 * @author koen
 */
abstract public class AbstractCommand {

    private CommandFactory commandFactory;
    private ResponseData responseData;

    abstract protected byte[] prepareBuild();
    abstract protected void parse(byte[] data) throws IOException;

    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CommandFactory commandFactory) {
        this.commandFactory=commandFactory;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void invoke() throws IOException {
        byte[] cmdData = prepareBuild();
        responseData = getCommandFactory().getMk6().getMk6Connection().sendCommand(cmdData);

        if (responseData.isCAN()) {
            switch (responseData.getCANCode()) {

                case ResponseData.CANNOT_WRITE: { // e.g. serial number already set
                    throw new IOException("AbstractCommand, invoke(), CAN response code CANNOT_WRITE");
                } // CANNOT_WRITE

                case ResponseData.UNIMPLEMENTED_OPERATION: {
                    throw new IOException("AbstractCommand, invoke(), CAN response code UNIMPLEMENTED_OPERATION");
                } // UNIMPLEMENTED_OPERATION

                case ResponseData.REGISTER_NOT_FOUND: {
                    throw new IOException("AbstractCommand, invoke(), CAN response code REGISTER_NOT_FOUND");
                } // REGISTER_NOT_FOUND

                case ResponseData.ACCESS_DENIED: { // Security reasons
                    throw new IOException("AbstractCommand, invoke(), CAN response code ACCESS_DENIED");
                } // ACCESS_DENIED

                case ResponseData.WRONG_LENGTH: { // Number of byte in request was incorrect
                    throw new IOException("AbstractCommand, invoke(), CAN response code WRONG_LENGTH");
                } // WRONG_LENGTH

                case ResponseData.BAD_TYPE_CODE: { // Internal error
                    throw new IOException("AbstractCommand, invoke(), CAN response code BAD_TYPE_CODE");
                } // BAD_TYPE_CODE

                case ResponseData.DATA_NOT_READY_YET: { // Still processing. Try again later.
                    throw new IOException("AbstractCommand, invoke(), CAN response code DATA_NOT_READY_YET");
                } // DATA_NOT_READY_YET

                case ResponseData.OUT_OF_RANGE: { // Written value was out of defined ranges.
                    throw new IOException("AbstractCommand, invoke(), CAN response code OUT_OF_RANGE");
                } // OUT_OF_RANGE

                case ResponseData.NOT_LOGGED_IN: { // Not logged in.
                    throw new IOException("AbstractCommand, invoke(), CAN response code NOT_LOGGED_IN");
                } // NOT_LOGGED_IN

                case -1: {
                    throw new IOException("AbstractCommand, invoke(), CAN response without code (possibly wrong password and/or deviceId (user name))");
                }

                default:
                    throw new IOException("AbstractCommand, invoke(), invalid CAN response code "+responseData.getCANCode());

            } // switch (responseData.getCANCode())

        } // if (responseData.isCAN())
        else if (responseData.isACK()) {
            // absorb OK
        }
        else {
            // absorb, responseData is available here!
            parse(getResponseData().getData());
        }

    } // public void invoke() throws IOException

    public ResponseData getResponseData() {
        return responseData;
    }


}
