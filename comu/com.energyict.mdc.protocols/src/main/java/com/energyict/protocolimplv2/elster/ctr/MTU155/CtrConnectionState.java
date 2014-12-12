package com.energyict.protocolimplv2.elster.ctr.MTU155;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 9:14:03
 */
public enum CtrConnectionState {

    WAIT_FOR_STX,
    READ_MIN_LENGTH,
    READ_EXTENDED_LENGTH,
    READ_FRAME,
    FRAME_RECEIVED

}
