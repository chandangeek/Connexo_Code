package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeCalendarObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimpl.utils.ProtocolTools.getHexStringFromBytes;

/**
 * Copyrights EnergyICT
 * Date: 12/04/11
 * Time: 15:56
 */
public class RawTariffScheme extends AbstractField<RawTariffScheme> {

    public static final int LENGTH = 73;
    public static final int HOLIDAYS_COUNT = 11;

    private boolean enabled;
    private int year;
    private int tariffId;
    private Date activationDate;
    private long defaultBand;
    private RawBandsDescriptor bandDescriptor;
    private boolean[] holidays;
    private RawSpecialDays specialDays;

    public RawTariffScheme(CodeObject codeObject, String tariffName, Date activationDate) {
        this.enabled = true;
        this.year = codeObject.getYearFrom();
        this.tariffId = getTariffIdentifier(tariffName);
        this.activationDate = activationDate;
        this.defaultBand = codeObject.getDefaultBand();
        bandDescriptor = new RawBandsDescriptor(codeObject);
        holidays = getHolidaysFromCodeObject(codeObject);
        specialDays = new RawSpecialDays(codeObject);
    }

    private boolean[] getHolidaysFromCodeObject(CodeObject codeObject) {
        boolean[] holidaysValue = new boolean[HOLIDAYS_COUNT];
        Arrays.fill(holidaysValue, false);
        List<CodeCalendarObject> holidayCalendars = codeObject.getHolidayCalendars();
        for (CodeCalendarObject holidayCalendar : holidayCalendars) {
            int holidayID = holidayCalendar.getHolidayID();
            if ((holidayID >= 0) && (holidayID < HOLIDAYS_COUNT)) {
                holidaysValue[holidayID] = true;
            }
        }
        return holidaysValue;
    }

    public int getTariffIdentifier(String tariffName) {
        if (tariffName != null) {
            String[] nameParts = tariffName.split("_");
            if (tariffName.split("_").length > 1) {
                try {
                    return Integer.valueOf(nameParts[nameParts.length - 1]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public RawTariffScheme() {
        enabled = false;
        year = 2000;
        tariffId = 0;
        activationDate = new Date();
        defaultBand = 0;
        bandDescriptor = new RawBandsDescriptor();
        holidays = new boolean[HOLIDAYS_COUNT];
        specialDays = new RawSpecialDays();
    }

    public byte[] getBytes() {
        int ptr = 0;
        byte[] rawData = new byte[LENGTH];

        // APT
        rawData[ptr++] = (byte) (isEnabled() ? 0x01 : 0x00);

        // ID_PT
        rawData[ptr++] = (byte) (getYear() - 2000);
        rawData[ptr++] = (byte) getTariffId();

        // DEV
        rawData[ptr++] = (byte) (getActivationDateAsCalendar().get(Calendar.YEAR) - 2000);
        rawData[ptr++] = (byte) (getActivationDateAsCalendar().get(Calendar.MONTH) + 1);
        rawData[ptr++] = (byte) (getActivationDateAsCalendar().get(Calendar.DAY_OF_MONTH));

        // FTD
        rawData[ptr++] = (byte) getDefaultBand();

        // PT1 + PT2
        byte[] bd1 = getBandDescriptor().getBytes();
        for (int i = 0; i < bd1.length; i++) {
            rawData[ptr++] = bd1[i];
        }

        // AFI
        rawData[ptr++] = (byte) ((getHolidaysAsInt() >> 8) & 0x0FF);
        rawData[ptr++] = (byte) ((getHolidaysAsInt() >> 0) & 0x0FF);

        // GP
        byte[] gp = getSpecialDays().getBytes();
        for (int i = 0; i < gp.length; i++) {
            rawData[ptr++] = gp[i];
        }

        return rawData;
    }

    private int getHolidaysAsInt() {
        int holidaysValue = 0;
        for (int i = 0; i < HOLIDAYS_COUNT; i++) {
            holidaysValue |= getHolidays()[i] ? (0x01 << i) : 0;
        }
        return holidaysValue;
    }

    public RawTariffScheme parse(byte[] rawData, int offset) throws CTRParsingException {
        if (rawData.length > (offset + LENGTH)) {
            throw new CTRParsingException("Expected still [" + LENGTH + "] bytes to read, but reached end of byte array!");
        }
        return this;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public Calendar getActivationDateAsCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getActivationDate());
        return cal;
    }

    public long getDefaultBand() {
        return defaultBand;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTariffId() {
        return tariffId;
    }

    public int getYear() {
        return year;
    }

    public RawBandsDescriptor getBandDescriptor() {
        return bandDescriptor;
    }

    public boolean[] getHolidays() {
        return holidays;
    }

    public RawSpecialDays getSpecialDays() {
        return specialDays;
    }

    public int getLength() {
        return getBytes().length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RawTariffScheme = ").append(getHexStringFromBytes(getBytes())).append('\n');
        sb.append(" > enabled = ").append(enabled).append('\n');
        sb.append(" > year = ").append(year).append('\n');
        sb.append(" > tariffId = ").append(tariffId).append('\n');
        sb.append(" > activationDate = ").append(activationDate).append('\n');
        sb.append(" > defaultBand = ").append(defaultBand).append('\n');

        sb.append(" > holidays = ").append('(').append(getHolidaysAsInt()).append(") ");
        for (boolean holiday : holidays) {
            sb.append("[").append(holiday ? "x" : " ").append("]");
        }
        sb.append('\n');

        sb.append(" > bandDescriptor\n");
        String[] bdStrings = bandDescriptor.toString().split("\n");
        for (String string : bdStrings) {
            sb.append("     ").append(string).append('\n');
        }

        sb.append(" > specialDays\n");
        String[] sdStrings = specialDays.toString().split("\n");
        for (String string : sdStrings) {
            sb.append("     ").append(string).append('\n');
        }

        return sb.toString();
    }
}
