/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.meteridentification;

import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.SerialNumber;

import java.io.IOException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-27 (12:30)
 */
public interface MeterType {

    int getBaudrateIndex();

    char getZ();

    SerialNumber getProtocolSerialNumberInstance() throws IOException;

    // KV 19012004
    MeterExceptionInfo getProtocolMeterExceptionInfoInstance() throws IOException;

    String[] getSerialNumberRegisterNames() throws IOException;

    String getResourceName() throws IOException;

    /**
     * Getter for property meter3letterId.
     *
     * @return Value of property meter3letterId.
     */
    String getMeter3letterId();

    /**
     * Setter for property meter3letterId.
     *
     * @param meter3letterId New value of property meter3letterId.
     */
    void setMeter3letterId(String meter3letterId);

    /**
     * Getter for property receivedIdent.
     *
     * @return Value of property receivedIdent.
     */
    String getReceivedIdent();

}