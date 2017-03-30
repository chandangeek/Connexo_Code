/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

/**
 * Provides services that relate to hexadecimal representation of numbers.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (10:03)
 */
public interface HexService {

    /**
     * Converts given byteArray to hexString.
     * E.g. val: {10, 0, 9}, String output: "$0A$00$09"
     *
     * @param data byteArray to convert
     * @return String result
     */
    public String toHexString(byte[] data);

}