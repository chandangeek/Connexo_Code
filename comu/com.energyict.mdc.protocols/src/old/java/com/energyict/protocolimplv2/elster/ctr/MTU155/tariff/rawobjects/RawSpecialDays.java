package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeCalendarObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimpl.utils.ProtocolTools.getHexStringFromBytes;

/**
 * Copyrights EnergyICT
 * Date: 12/04/11
 * Time: 16:41
 */
public class RawSpecialDays extends AbstractField<RawSpecialDays> {

    private static final int SPECIAL_DAYS_COUNT = 15;
    private static final int LENGTH = SPECIAL_DAYS_COUNT * 2;

    private RawSpecialDay[] specialDays;

    public RawSpecialDays() {
        specialDays = new RawSpecialDay[SPECIAL_DAYS_COUNT];
        Arrays.fill(specialDays, new RawSpecialDay());
    }

    public RawSpecialDays(CodeObject codeObject) {
        this();
        List<CodeCalendarObject> customDays = codeObject.getCustomDayCalendars();
        for (int i = 0; i < customDays.size(); i++) {
            CodeCalendarObject customDay = customDays.get(i);
            specialDays[i] = new RawSpecialDay(customDay);
        }
    }

    public byte[] getBytes() {
        byte[] rawData = new byte[LENGTH];
        int ptr = 0;
        for (int i = 0; i < specialDays.length; i++) {
            RawSpecialDay specialDay = specialDays[i];
            rawData[ptr++] = specialDay.getBytes()[0];
            rawData[ptr++] = specialDay.getBytes()[1];
        }
        return rawData;
    }

    public RawSpecialDays parse(byte[] rawData, int offset) throws CTRParsingException {
        return this;
    }

    public RawSpecialDay[] getSpecialDays() {
        return specialDays;
    }

    public int getLength() {
        return getBytes().length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("rawData = ").append(getHexStringFromBytes(getBytes())).append('\n');
        for (int i = 0; i < specialDays.length; i++) {
            RawSpecialDay specialDay = specialDays[i];
            sb.append("specialDay_").append(i).append(" = ").append(specialDay).append('\n');
        }
        return sb.toString();
    }
}
