/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.EMCO.frame;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

public interface ResponseFrame {

    public static char REGISTER_RESPONSE = '#';  // Transmission of a data register
    public static char CONFIG_RESPONSE = '@';    // transmission of the configuration data block / new configuration block received
    public static char ERROR_RESPONSE = '*';


    public static char WRITE_CONFIG_TO_EPROM = '*'; // Store data in permanent memory
    public static char WRITE_CONFIG_TO_RAM = '=';   // Store data in RAM
    public static char REQUEST_REPORT = '&';        // Request transmission of a report

    public static byte[] START_OF_FRAME = {(byte) 0x3A};                // Each response should start with char :
    public static byte[] END_OF_FRAME = {(byte) 0x0D, (byte) 0x0A};      // Each response should be closed by an Carriage Return followed by a Line Feed.

    /**
     * Method will return a byte array containing the complete response.
     */
    public byte[] getBytes();

    /**
     * Getter for the type of the response
     */
    public char getResponseType();

    /**
     * Parse all info out the given byte array into proper variables
     */
    public void parseBytes(byte[] bytes) throws ProtocolConnectionException;

    /**
     * Checks if this response frame correlates to the request frame.
     */
    public void checkMatchingRequest(RequestFrame requestFrame) throws ProtocolConnectionException;

    /**
     * Getter for the unique device unit number
     */
    public int getUnitNumber();

    /**
     * Are there fault flags set in the device
     */
    public boolean isFaultsPresent();

}