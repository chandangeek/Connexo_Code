package com.energyict.protocolimpl.coronis.waveflowDLMS.a1800;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflowDLMS.AbstractDLMS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 16/05/13
 * Time: 11:50
 * Author: khe
 */
public class ErrorRegisterParser {

    private static final ObisCode errorByte1 = ObisCode.fromString("0.0.97.97.0.255");
    private static final ObisCode errorByte2 = ObisCode.fromString("0.0.97.97.1.255");
    private static final ObisCode errorByte3 = ObisCode.fromString("0.0.97.97.2.255");
    private static final String EMPTY = "000000";

    public static List<MeterEvent> readMeterEvents(AbstractDLMS protocol) throws IOException {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        String binaryCode;
        binaryCode = getBinaryCode(protocol.readRegister(errorByte1));
        if (!EMPTY.equals(binaryCode)) {
            result.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.ERROR_REGISTER1, "E1 conditions and codes [E1] [" + binaryCode + "]"));
        }
        binaryCode = getBinaryCode(protocol.readRegister(errorByte2));
        if (!EMPTY.equals(binaryCode)) {
            result.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.ERROR_REGISTER2, "E2 conditions and codes [E2] [" + binaryCode + "]"));
        }
        binaryCode = getBinaryCode(protocol.readRegister(errorByte3));
        if (!EMPTY.equals(binaryCode)) {
            result.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.ERROR_REGISTER3, "E3 conditions and codes [E3] [" + binaryCode + "]"));
        }
        return result;
    }

    /**
     * Turn a given integer (e.g. 1) into its corresponding bitmask and reverse it, using only the 6 first bits.
     * <p/>
     * E.g.: 0x01 ==> 00|0000001 ==> return 100000
     */
    private static String getBinaryCode(RegisterValue registerValue) {
        int alarmCode = registerValue.getQuantity().getAmount().intValue();
        String binary = Integer.toBinaryString(alarmCode);
        String paddedBinary = pad(binary, 6);
        return new StringBuffer(paddedBinary).reverse().toString().substring(0, 6);
    }

    private static String pad(String binary, int length) {
        while (binary.length() < length) {
            binary = "0" + binary;
        }
        return binary;
    }
}