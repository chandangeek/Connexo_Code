/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.EMCO.frame;

import java.io.UnsupportedEncodingException;

public interface RequestFrame {

    public static char REGISTER_REQUEST = '#';      // Request transmission of a data register
    public static char CONFIG_REQUEDT = '@';        // Request transmission of the configuration data block
    public static char WRITE_CONFIG_TO_EPROM = '*'; // Store data in permanent memory
    public static char WRITE_CONFIG_TO_RAM = '=';   // Store data in RAM
    public static char REQUEST_REPORT = '&';        // Request transmission of a report

    public static byte[] START_OF_FRAME = {(byte) 0x3A};    // Each request should start with char :
    public static byte[] END_OF_FRAME = {(byte) 0x0D};    // Each request should be closed by an Carriage Return

    /**
     * Method will return a byte array containing the complete request.
     * This bye array can then be send out to the device.
     */
    public byte[] getBytes() throws UnsupportedEncodingException;

    /**
     * Getter for the request type of the request
     */
    public char getRequestType();

    /**
     * Getter for the unique device unit number
     */
    public int getUnitNumber();

}