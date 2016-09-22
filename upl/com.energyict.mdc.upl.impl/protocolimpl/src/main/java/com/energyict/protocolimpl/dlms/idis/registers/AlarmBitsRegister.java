package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 16:33
 */
public class AlarmBitsRegister {

    private ObisCode obisCode;
    private long value;

    public AlarmBitsRegister(ObisCode obisCode, long value) {
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
            sb.append(getSeparator(sb)).append("Clock invalid");
        }
        if (isBitSet(value, 1)) {
            sb.append(getSeparator(sb)).append("Battery replace");
        }
        if (isBitSet(value, 8)) {
            sb.append(getSeparator(sb)).append("Program memory error");
        }
        if (isBitSet(value, 9)) {
            sb.append(getSeparator(sb)).append("RAM error");
        }
        if (isBitSet(value, 10)) {
            sb.append(getSeparator(sb)).append("NV memory error");
        }
        if (isBitSet(value, 11)) {
            sb.append(getSeparator(sb)).append("Measurement system error");
        }
        if (isBitSet(value, 12)) {
            sb.append(getSeparator(sb)).append("Watchdog error");
        }
        if (isBitSet(value, 13)) {
            sb.append(getSeparator(sb)).append("Fraud attempt");
        }
        if (isBitSet(value, 16)) {
            sb.append(getSeparator(sb)).append("M-Bus communication error ch1");
        }
        if (isBitSet(value, 17)) {
            sb.append(getSeparator(sb)).append("M-Bus communication error ch2");
        }
        if (isBitSet(value, 18)) {
            sb.append(getSeparator(sb)).append("M-Bus communication error ch3");
        }
        if (isBitSet(value, 19)) {
            sb.append(getSeparator(sb)).append("M-Bus communication error ch4");
        }
        if (isBitSet(value, 20)) {
            sb.append(getSeparator(sb)).append("M-Bus fraud attempt ch1");
        }
        if (isBitSet(value, 21)) {
            sb.append(getSeparator(sb)).append("M-Bus fraud attempt ch2");
        }
        if (isBitSet(value, 22)) {
            sb.append(getSeparator(sb)).append("M-Bus fraud attempt ch3");
        }
        if (isBitSet(value, 23)) {
            sb.append(getSeparator(sb)).append("M-Bus fraud attempt ch4");
        }
        if (isBitSet(value, 24)) {
            sb.append(getSeparator(sb)).append("Permanent error M-Bus ch1");
        }
        if (isBitSet(value, 25)) {
            sb.append(getSeparator(sb)).append("Permanent error M-Bus ch2");
        }
        if (isBitSet(value, 26)) {
            sb.append(getSeparator(sb)).append("Permanent error M-Bus ch3");
        }
        if (isBitSet(value, 27)) {
            sb.append(getSeparator(sb)).append("Permanent error M-Bus ch4");
        }
        if (isBitSet(value, 28)) {
            sb.append(getSeparator(sb)).append("Battery low on M-Bus ch1");
        }
        if (isBitSet(value, 29)) {
            sb.append(getSeparator(sb)).append("Battery low on M-Bus ch2");
        }
        if (isBitSet(value, 30)) {
            sb.append(getSeparator(sb)).append("Battery low on M-Bus ch3");
        }
        if (isBitSet(value, 31)) {
            sb.append(getSeparator(sb)).append("Battery low on M-Bus ch4");
        }
        return sb.toString();
    }
}