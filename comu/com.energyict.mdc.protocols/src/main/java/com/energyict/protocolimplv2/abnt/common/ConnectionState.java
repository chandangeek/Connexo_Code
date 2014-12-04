package com.energyict.protocolimplv2.abnt.common;

/**
 * @author sva
 * @since 17/06/2014 - 10:55
 */
public enum ConnectionState {

    READ_FUNCTION_CODE,
    READ_METER_SERIAL,
    READ_FRAME_DATA,
    READ_CRC,
    FRAME_RECEIVED,

    SPECIAL_COMMAND

}
