/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.frame;

import com.energyict.protocolimplv2.abnt.common.exception.CrcMismatchException;
import com.energyict.protocolimplv2.abnt.common.field.Field;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;

import java.util.TimeZone;

/**
 * @author sva
 * @since 130082014 - 11:33
 */
public interface Frame<T extends Frame> extends Field<T> {

    /**
     * Getter for the TimeZone
     */
    public TimeZone getTimeZone();

    /**
     * Getter for the serial
     */
    public Function getFunction();

    /**
     * Getter for the data of the frame
     */
    public Data getData();

    /**
     * Getter for the crc of the frame
     */
    public Crc getCrc();

    /**
     * Generate the CRC16 and apply it to the frame
     */
    public void generateAndSetCRC();

    /**
     * Validate the CRC16 of this frame
     * @throws com.energyict.protocolimplv2.abnt.common.exception.CrcMismatchException in case the CRC16 is not valid
     */
    public void validateCRC() throws CrcMismatchException;
}
