/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.registers;

public class CircuitFaultStatus {

    private static final String NEWLINE = "\n\r";

    public static String parse(long status) {
        StringBuilder sb = new StringBuilder();
        if ((status & 0x01) == 0x01) {
            sb.append("No consumption in IP circuit with \"IP control - current status\" connected.").append(NEWLINE);
        }
        if ((status & 0x02) == 0x02) {
            sb.append("Consumption higher then the configured threshold with \"IP control - current status\" disconnected.").append(NEWLINE);
        }
        if ((status & 0x04) == 0x04) {
            sb.append("Consumption in IP circuit under the configured minimum threshold with \"IP control - current status\" connected.").append(NEWLINE);
        }
        if ((status & 0x08) == 0x08) {
            sb.append("Consumption in IP circuit over the configured maximum threshold with \"IP control - current status\" connected.").append(NEWLINE);
        }

        if (sb.toString().length() == 0) {
            sb.append("(no faults)");
        }

        return sb.toString();
    }
}