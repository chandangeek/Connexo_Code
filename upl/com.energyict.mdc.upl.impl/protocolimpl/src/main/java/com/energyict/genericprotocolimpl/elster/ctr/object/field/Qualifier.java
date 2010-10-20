package com.energyict.genericprotocolimpl.elster.ctr.object.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

import java.util.Arrays;


/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 13:12:44
 */
public class Qualifier extends AbstractField<Qualifier>{

    private int qlf;
    public static final String TARIF_NOT_ACTIVE = "Tariff scheme not active";
    public static final String TARIF_PRICE_BAND1 = "Data recorded against price band 1";
    public static final String TARIF_PRICE_BAND2 = "Data recorded against price band 2";
    public static final String TARIF_PRICE_BAND3 = "Data recorded against price band 3";

    public static final String VALUE_VALID = "Valid effective value";
    public static final String VALUE_MAINTENANCE = "Value when subject to maintenance";
    public static final String VALUE_INVALID_MEASURED = "Invalid measurement";

    public static final String DAYLIGHT_SAVING_TIME = "Value during daylight saving time";
    public static final String STANDARD_TIME = "Standard time";

    public Qualifier(int qlf) {
        this.qlf = qlf;
    }

    public int getQlf() {
        return qlf;
    }

    public byte[] getBytes() {
        return new byte[]{(byte) qlf};
    }

    public Qualifier parse(byte[] rawData, int offset) throws CTRParsingException {
        qlf = getIntFromBytes(rawData, offset, getLength());
        return this;    
    }

    public int getLength() {
        return 1;
    }

    public void setQlf(int qlf) {
        this.qlf = qlf;
    }

    public String getTarif() {
        int if1 = qlf & (0x0C0) >> 6;
        String tarif = "";
        switch (if1) {
            case 0:
                tarif = TARIF_NOT_ACTIVE;
                break;
            case 1:
                tarif = TARIF_PRICE_BAND1;
                break;
            case 2:
                tarif = TARIF_PRICE_BAND2;
                break;
            case 3:
                tarif = TARIF_PRICE_BAND3;
                break;
        }
        return tarif;
    }

    public String getValueDescription() {
        int val = qlf & (0x018) >> 3;
        String description = "";
        switch (val) {
            case 0:
                description = VALUE_VALID;
                break;
            case 1:
                description = VALUE_MAINTENANCE;
                break;
            case 2:
                description = VALUE_INVALID_MEASURED;
                break;
        }
        return description;
    }

    public boolean isInvalidMeasurement() {
        return VALUE_INVALID_MEASURED.equals(getValueDescription());
    }

    public boolean isSubjectToMaintenance() {
        return VALUE_MAINTENANCE.equals(getValueDescription());
    }

    public boolean isValid() {
        return VALUE_VALID.equals(getValueDescription());
    }

    public double getKmoltAbsoluteFactor() {
        int kmolt = qlf & (0x07);
        if (kmolt < 7) {
            return Math.pow(10, (kmolt*(-1)));
        } else {
            return 1;
        }
    }

    public int getKmoltFactor() {
        int kmolt = qlf & (0x07);
        if (kmolt < 7) {
            return kmolt * -1;
        } else {
            return 1;
        }
    }

    public String getValueTime() {
        int sl = qlf & (0x020) >> 5;
        String valueTime = "";
        switch (sl) {
            case 0:
                valueTime = DAYLIGHT_SAVING_TIME;
                break;
            case 1:
                valueTime = STANDARD_TIME;
                break;
        }
        return valueTime;
    }

    public boolean isInvalid() {
        return (getQlf() == 255);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("qlf=").append(qlf);
        return sb.toString();
    }
}
