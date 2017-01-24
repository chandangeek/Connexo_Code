package com.energyict.protocolimpl.EMCO.frame;

import com.energyict.protocolimpl.EMCO.FP93Connection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.util.StringTokenizer;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/02/12
 * Time: 10:16
 */
public class RegisterResponseFrame implements ResponseFrame {

    public static final char TYPE_FLOATING_POINT = 'a';
    public static final char TYPE_LONG = 'e';
    public static final char TYPE_HEX = 'h';
    public static final char TYPE_STRING = 'x';

    private int unitNumber;
    private int registerNumber;
    private boolean faultsPresent;
    private char dataType;
    private BigDecimal value;
    private int bitMask;
    private String text;

    private byte[] responseFrame;


    public RegisterResponseFrame() {
    }

    /**
     * Parse all info out the given byte array into proper variables
     */
    public void parseBytes(byte[] bytes) throws ProtocolConnectionException {
        responseFrame = bytes;
        int pos = 0;

        FP93Connection.verifyCheckSum(bytes);

        if (bytes[pos] != START_OF_FRAME[0]) {
            throw new ProtocolConnectionException("The response frame doesn't start with the ASCII character ':' - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        pos += 1;

        // Extract the device unit number
        pos += extractUnitNumber(bytes);

        if (bytes[pos] != REGISTER_RESPONSE) {
            throw new ProtocolConnectionException("Expected a register response, but was of type " + (char) bytes[pos]);
        }
        pos += 1;

        try {
            String registerNumberString = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(bytes, pos, pos + 2));
            registerNumber = Integer.parseInt(registerNumberString);
        } catch (NumberFormatException e) {
            throw new ProtocolConnectionException("Failed to parse the register number out of the response frame - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        pos += 2;

        dataType = (char) bytes[pos];
        if (dataType != TYPE_FLOATING_POINT && dataType != TYPE_HEX && dataType != TYPE_LONG && dataType != TYPE_STRING)  {
            throw new ProtocolConnectionException("The data type (" + (char) dataType + ") is not supported.");
        }
        pos += 1;

        StringTokenizer tokenizer;
        if (ProtocolTools.getAsciiFromBytes(bytes).contains(",")) {
            faultsPresent = false;
            tokenizer = new StringTokenizer(ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(bytes, pos)), ",");
        } else if (ProtocolTools.getAsciiFromBytes(bytes).contains("!")) {
            faultsPresent = true;
            tokenizer = new StringTokenizer(ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(bytes, pos)), "!");
        } else {
            throw new ProtocolConnectionException("Could not determine the fault status of the response - " + ProtocolTools.getAsciiFromBytes(bytes));
        }

        if (tokenizer.countTokens() < 2)  {
            throw new ProtocolConnectionException("Failed to extract the data from the response frame - " + ProtocolTools.getAsciiFromBytes(bytes));
        }
        String data = tokenizer.nextToken();

        switch (dataType) {
            case TYPE_FLOATING_POINT:
                value = new BigDecimal(data);
                break;
            case TYPE_HEX:
                bitMask = Integer.parseInt(data, 16);
                break;
            case TYPE_LONG:
                value = new BigDecimal(data);
                break;
            case TYPE_STRING:
                text = data;
                break;
            default:
                throw new ProtocolConnectionException();
        }
    }

    public void checkMatchingRequest(RequestFrame requestFrame) throws ProtocolConnectionException {
        if (requestFrame.getUnitNumber() != unitNumber) {
            throw new ProtocolConnectionException("The unit number of the response [" + unitNumber + "] doesn't match the request unit number [" + requestFrame.getUnitNumber() + "].");
        } else if (((RegisterRequestFrame) requestFrame).getRegisterNumber() != registerNumber) {
            throw new ProtocolConnectionException("The register number of the response [" + registerNumber + "] doesn't match the request register number [" + ((RegisterRequestFrame) requestFrame).getRegisterNumber() + "].");
        }
    }

    private int extractUnitNumber(byte[] response) throws ProtocolConnectionException {
        final String asciiString = ProtocolTools.getAsciiFromBytes(response);
        String unitNumberString = "";
        int byteCount = 0;

        for (int i = 1; i < 6; i++) {       // The unit number is at max 5 digits.
            if (Character.isDigit(asciiString.charAt(i))) {
                unitNumberString += asciiString.charAt(i);
                byteCount += 1;
            } else {
                break;
            }
        }

        try {
            unitNumber = Integer.parseInt(unitNumberString);
        } catch (NumberFormatException e) {
            throw new ProtocolConnectionException("Could not extract the unit number from the response.");
        }
        return byteCount;
    }

    /**
     * Returns an byte array containing the complete ASCII response.
     */
    public byte[] getBytes() {
        return responseFrame;
    }

    /**
     * Getter for the type of the response
     */
    public char getResponseType() {
        return REGISTER_RESPONSE;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public char getDataType() {
        return dataType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getBitMask() {
        return bitMask;
    }

    public String getText() {
        return text;
    }

    /**
     * Are there fault flags set in the device
     */
    public boolean isFaultsPresent() {
        return faultsPresent;
    }
}