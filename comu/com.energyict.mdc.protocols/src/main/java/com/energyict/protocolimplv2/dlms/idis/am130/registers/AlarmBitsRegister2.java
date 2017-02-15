/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am130.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.util.Date;

public class AlarmBitsRegister2 {

    private ObisCode obisCode;
    private long value;

    public AlarmBitsRegister2(ObisCode obisCode, long value) {
        this.obisCode = obisCode;
        this.value = value;
    }

    private String getSeparator(StringBuilder sb) {
        if (sb.toString().equals("")) {
            return "";
        }
        return ", ";
    }

    /**
     * Checks if a certain bit in the status flag is set.
     *
     * @param value the status flag
     * @param i     number of the bit
     * @return true if set
     */
    private boolean isBitSet(long value, int i) {
        return (value & ((int) (Math.pow(2, i)))) > 0;
    }

    public RegisterValue getRegisterValue() {
        return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date(), null, new Date(), new Date(), 0, getAlarmDescription(value));
    }

    private String getAlarmDescription(long value) {
        StringBuilder sb = new StringBuilder();
        if (isBitSet(value, 0)) {
            sb.append(getSeparator(sb)).append("Total Power Failure");
        }
        if (isBitSet(value, 1)) {
            sb.append(getSeparator(sb)).append("Power Resume");
        }
        if (isBitSet(value, 2)) {
            sb.append(getSeparator(sb)).append("Voltage Missing Phase L1");
        }
        if (isBitSet(value, 3)) {
            sb.append(getSeparator(sb)).append("Voltage Missing Phase L2");
        }
        if (isBitSet(value, 4)) {
            sb.append(getSeparator(sb)).append("Voltage Missing Phase L3");
        }
        if (isBitSet(value, 5)) {
            sb.append(getSeparator(sb)).append("Voltage Normal Phase L1");
        }
        if (isBitSet(value, 6)) {
            sb.append(getSeparator(sb)).append("Voltage Normal Phase L2");
        }
        if (isBitSet(value, 7)) {
            sb.append(getSeparator(sb)).append("Voltage Normal Phase L3");
        }
        if (isBitSet(value, 8)) {
            sb.append(getSeparator(sb)).append("Missing Neutral");
        }
        if (isBitSet(value, 9)) {
            sb.append(getSeparator(sb)).append("Phase Asymmetry");
        }
        if (isBitSet(value, 10)) {
            sb.append(getSeparator(sb)).append("Current Reversal");
        }
        if (isBitSet(value, 11)) {
            sb.append(getSeparator(sb)).append("Wrong Phase Sequence");
        }
        if (isBitSet(value, 12)) {
            sb.append(getSeparator(sb)).append("Unexpected Consumption");
        }
        if (isBitSet(value, 13)) {
            sb.append(getSeparator(sb)).append("Key Exchanged");
        }
        if (isBitSet(value, 14)) {
            sb.append(getSeparator(sb)).append("Bad Voltage Quality L1");
        }
        if (isBitSet(value, 15)) {
            sb.append(getSeparator(sb)).append("Bad Voltage Quality L2");
        }
        if (isBitSet(value, 16)) {
            sb.append(getSeparator(sb)).append("Bad Voltage Quality L3");
        }
        if (isBitSet(value, 17)) {
            sb.append(getSeparator(sb)).append("External Alert");
        }
        if (isBitSet(value, 18)) {
            sb.append(getSeparator(sb)).append("Local communication attempt");
        }
        if (isBitSet(value, 19)) {
            sb.append(getSeparator(sb)).append("New M-Bus Device Installed Ch1");
        }
        if (isBitSet(value, 20)) {
            sb.append(getSeparator(sb)).append("New M-Bus Device Installed Ch2");
        }
        if (isBitSet(value, 21)) {
            sb.append(getSeparator(sb)).append("New M-Bus Device Installed Ch3");
        }
        if (isBitSet(value, 22)) {
            sb.append(getSeparator(sb)).append("New M-Bus Device Installed Ch4");
        }
        if (isBitSet(value, 27)) {
            sb.append(getSeparator(sb)).append("M-Bus valve alarm Ch1");
        }
        if (isBitSet(value, 28)) {
            sb.append(getSeparator(sb)).append("M-Bus valve alarm Ch2");
        }
        if (isBitSet(value, 29)) {
            sb.append(getSeparator(sb)).append("M-Bus valve alarm Ch3");
        }
        if (isBitSet(value, 30)) {
            sb.append(getSeparator(sb)).append("M-Bus valve alarm Ch4");
        }
        if (isBitSet(value, 31)) {
            sb.append(getSeparator(sb)).append("Disconnect/Reconnect Failure");
        }
        return sb.toString();
    }
}