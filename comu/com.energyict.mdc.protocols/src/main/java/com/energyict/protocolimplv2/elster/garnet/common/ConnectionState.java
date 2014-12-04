package com.energyict.protocolimplv2.elster.garnet.common;

/**
 * @author sva
 * @since 17/06/2014 - 10:55
 */
public enum ConnectionState {

    READ_DESTINATION_ID,
    READ_FUNCTION_CODE,
    READ_SOURCE_ID,
    READ_EXTENDED_FRAME_CODE,
    READ_EXTENDED_FRAME_PART,
    READ_FRAME_DATA,
    READ_CRC,
    FRAME_RECEIVED

}
