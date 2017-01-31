/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.inbound;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.SerialNumber;

import java.io.IOException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-02 (13:42)
 */
public interface MeterType {

    int getBaudrateIndex();

    char getZ();

    SerialNumber getProtocolSerialNumberInstance() throws NestedIOException;

    // KV 19012004
    MeterExceptionInfo getProtocolMeterExceptionInfoInstance() throws NestedIOException;

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