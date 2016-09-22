package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Date;

/**
 * Created by cisac on 8/2/2016.
 */
public class AlarmBitsRegister3 {
    private ObisCode obisCode;
    private long value;

    public AlarmBitsRegister3(ObisCode obisCode, long value) {
        this.obisCode = obisCode;
        this.value = value;
    }

    private String getSeparator(StringBuilder sb) {
        if (sb.toString().equals("")) {
            return "";
        }
        return ", ";
    }

    public RegisterValue getRegisterValue() {
        return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date(), null, new Date(), new Date(), 0, getAlarmDescription(value));
    }

    private String getAlarmDescription(long value) {
        StringBuilder sb = new StringBuilder();
        if (ProtocolTools.isBitSet(value, 4)) {
            sb.append(getSeparator(sb)).append("M-Bus device uninstalled ch1");
        }
        if (ProtocolTools.isBitSet(value, 5)) {
            sb.append(getSeparator(sb)).append("M-Bus device uninstalled ch2");
        }
        if (ProtocolTools.isBitSet(value, 6)) {
            sb.append(getSeparator(sb)).append("M-Bus device uninstalled ch3");
        }
        if (ProtocolTools.isBitSet(value, 7)) {
            sb.append(getSeparator(sb)).append("M-Bus device uninstalled ch4");
        }
        if (ProtocolTools.isBitSet(value, 10)) {
            sb.append(getSeparator(sb)).append("Temporary error M-Bus ch1");
        }
        if (ProtocolTools.isBitSet(value, 11)) {
            sb.append(getSeparator(sb)).append("Temporary error M-Bus ch2");
        }
        if (ProtocolTools.isBitSet(value, 12)) {
            sb.append(getSeparator(sb)).append("Temporary error M-Bus ch3");
        }
        if (ProtocolTools.isBitSet(value, 13)) {
            sb.append(getSeparator(sb)).append("Temporary error M-Bus ch4");
        }
        if (ProtocolTools.isBitSet(value, 14)) {
            sb.append(getSeparator(sb)).append("End of fraud attempt");
        }
        if (ProtocolTools.isBitSet(value, 15)) {
            sb.append(getSeparator(sb)).append("Certificate almost expired");
        }

        return sb.toString();
    }
}
