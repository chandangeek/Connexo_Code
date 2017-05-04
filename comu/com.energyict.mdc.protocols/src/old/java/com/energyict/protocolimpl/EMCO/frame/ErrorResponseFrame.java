/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.EMCO.frame;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.HashMap;

public class ErrorResponseFrame implements ResponseFrame {

    public static HashMap<String, String> errorMessages = new HashMap<String, String>(6);

    static {
        errorMessages.put("0", "Data communication error (parity, overrun, framing or noise)");
        errorMessages.put("1", "Syntax error (invalid character or message format)");
        errorMessages.put("2", "Non-existent register number");
        errorMessages.put("5", "Message checksum error");
        errorMessages.put("6", "Data block CRC checksum error");
        errorMessages.put("7", "Incorrect number of bytes in data block");
    }


    private int unitNumber;
    private int errorCode;
    private boolean faultsPresent;
    private byte[] responseFrame;

    /**
     * Method will return a byte array containing the complete response.
     */
    public byte[] getBytes() {
        return responseFrame;
    }

    /**
     * Getter for the type of the response
     */
    public char getResponseType() {
        return ERROR_RESPONSE;
    }

    /**
     * Parse all info out the given byte array into proper variables
     */
    public void parseBytes(byte[] bytes) throws ProtocolConnectionException {
        responseFrame = bytes;
        int pos = 0;

        if (bytes[pos] != START_OF_FRAME[0]) {
            throw new ProtocolConnectionException("The response frame doesn't start with the ASCII character ':' - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        pos += 1;

        try {
            String unitNumberString = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(bytes, pos, pos + 5));
            unitNumber = Integer.parseInt(unitNumberString);
        } catch (NumberFormatException e) {
            throw new ProtocolConnectionException("Failed to parse the unit number out of the response frame - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        pos += 5;

        if (bytes[pos] != ERROR_RESPONSE) {
            throw new ProtocolConnectionException("Received an invalid error frame - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        pos += 1;

       try {
            String errorCodeString = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(bytes, pos, pos + 1));
            errorCode = Integer.parseInt(errorCodeString);
        } catch (NumberFormatException e) {
            throw new ProtocolConnectionException("Failed to parse the error code out of the response frame - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        pos += 1;

        if (bytes[pos] == ',') {
            faultsPresent = false;
        } else if (bytes[pos] == '!') {
            faultsPresent = true;
        } else {
            throw new ProtocolConnectionException("Could not determine the fault status of the response - " + ProtocolTools.getAsciiFromBytes(bytes));

        }
    }

    /**
     * Checks if this response frame correlates to the request frame.
     */
    public void checkMatchingRequest(RequestFrame requestFrame) throws ProtocolConnectionException {
        // Not used
    }

    /**
     * Getter for the unique device unit number
     */
    public int getUnitNumber() {
        return unitNumber;
    }


    public int getErrorCode() {
        return errorCode;
    }


    public boolean isFaultsPresent() {
        return faultsPresent;
    }

    public String getErrorMessage() {
        String message = errorMessages.get(Integer.toString(errorCode));
        return (message != null) ? message
                : "Unknown error code: " + errorCode;
    }
}