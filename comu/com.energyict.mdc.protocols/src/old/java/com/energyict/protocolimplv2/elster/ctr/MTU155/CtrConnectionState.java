/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155;

public enum CtrConnectionState {

    WAIT_FOR_STX,
    READ_MIN_LENGTH,
    READ_EXTENDED_LENGTH,
    READ_FRAME,
    FRAME_RECEIVED

}
