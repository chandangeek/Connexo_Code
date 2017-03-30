/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.EMCO.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.UnsupportedEncodingException;

public class RegisterRequestFrame implements RequestFrame {

    private int unitNumber;
    private int registerNumber;

    public RegisterRequestFrame(int unitNumber, int registerNumber) {
        this.unitNumber = unitNumber;
        this.registerNumber = registerNumber;
    }

    /**
     * Returns an byte array containing the complete ASCII request.
     */
    public byte[] getBytes() throws UnsupportedEncodingException {
        String request = Integer.toString(unitNumber) + REGISTER_REQUEST + Integer.toString(registerNumber);

        byte[] bytes = ProtocolTools.concatByteArrays(START_OF_FRAME, request.getBytes("US-ASCII"));
        return ProtocolTools.concatByteArrays(bytes, END_OF_FRAME);
    }

    /**
     * Getter for the request type of the request
     */
    public char getRequestType() {
        return REGISTER_REQUEST;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }
}