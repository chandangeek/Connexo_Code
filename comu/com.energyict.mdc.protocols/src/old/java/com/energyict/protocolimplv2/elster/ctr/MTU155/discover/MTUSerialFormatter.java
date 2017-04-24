/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import static com.energyict.protocolimpl.utils.ProtocolTools.addPaddingAndClip;
import static com.energyict.protocolimpl.utils.ProtocolTools.isNumber;

public class MTUSerialFormatter {

    private static final int SERIAL_NUMERICAL_PART_LENGTH = 6;

    public static String formatMTUSerialNumber(String mtuSerial) {
        String serial = mtuSerial != null ? mtuSerial : "";
        while (!isNumber(serial)) {
            serial = serial.substring(1);
        }
        return "ELS" + addPaddingAndClip(serial, '0', SERIAL_NUMERICAL_PART_LENGTH, false);
    }

}
