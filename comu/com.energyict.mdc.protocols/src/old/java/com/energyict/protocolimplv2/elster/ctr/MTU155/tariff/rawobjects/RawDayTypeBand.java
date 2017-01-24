package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeDayTypeDefObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeDayTypeObject;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 13/04/11
 * Time: 13:50
 */
public class RawDayTypeBand extends AbstractField<RawDayTypeBand> {

    private static final int LENGTH = 5;

    private long[] code;
    private int[] time;

    public RawDayTypeBand() {
        code = new long[LENGTH];
        time = new int[LENGTH];
    }

    public RawDayTypeBand(CodeDayTypeObject dayType) {
        this();
        List<CodeDayTypeDefObject> bands = dayType.getDayTypeDefs();
        Collections.sort(bands);

        if (bands.size() == 5) {
            for (int i = 0; i < bands.size(); i++) {
                code[i] = bands.get(i == 0 ? 4 : i - 1).getCodeValue();
                time[i] = bands.get(i).getFrom() / 10000;
            }
        } else if (bands.size() == 6) {
            for (int i = 0; i < (bands.size() - 1); i++) {
                code[i] = bands.get(i).getCodeValue();
                time[i] = bands.get(i + 1).getFrom() / 10000;
            }
        } else {
            throw new IllegalArgumentException("Expected 5 or 6 bands from EIServer for day type [" + dayType.getName() + "] but received [" + bands.size() + "]!");
        }

    }

    public byte[] getBytes() {
        byte[] rawData = new byte[LENGTH];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = 0;
            rawData[i] |= ((code[i] << 6) & 0xC0);
            rawData[i] |= (time[i] & 0x1F);
        }
        return rawData;
    }

    public RawDayTypeBand parse(byte[] rawData, int offset) throws CTRParsingException {
        for (int i = 0; i < code.length; i++) {
            code[i] = (rawData[offset + i] >> 6) & 0x03;
            time[i] = rawData[offset + i] & 0x1F;
        }
        return this;
    }

    public int getLength() {
        return getBytes().length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("rawData = ").append(ProtocolTools.getHexStringFromBytes(getBytes())).append('\n');
        for (int i = 0; i < code.length; i++) {
            sb.append("time = ").append(time[i]).append(":00").append(", ");
            sb.append("code = ").append(code[i]).append('\n');

        }
        return sb.toString();
    }
}
