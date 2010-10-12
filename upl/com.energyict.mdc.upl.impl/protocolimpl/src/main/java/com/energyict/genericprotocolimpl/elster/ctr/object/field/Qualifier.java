package com.energyict.genericprotocolimpl.elster.ctr.object.field;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 13:12:44
 */
public class Qualifier {

    private int qlf;
    public final int LENGTH = 1;

    public Qualifier(int qlf) {
        this.qlf = qlf;
    }

    public int getQlf() {
        return qlf;
    }

    public byte[] getBytes() {
        return new byte[]{(byte) qlf};
    }

    public void setQlf(int qlf) {
        this.qlf = qlf;
    }

    public String getTarif() {
        int IF = qlf & (0x0C0) >> 6;
        String tarif = "";
        switch (IF) {
            case 0:
                tarif = "Tariff scheme not active";
                break;
            case 1:
                tarif = "Data recorded against price band 1";
                break;
            case 2:
                tarif = "Data recorded against price band 2";
                break;
            case 3:
                tarif = "Data recorded against price band 3";
                break;
        }
        return tarif;
    }

    public String getValueDescription() {
        int VAL = qlf & (0x018) >> 3;
        String tarif = "";
        switch (VAL) {
            case 0:
                tarif = "Valid effective value";
                break;
            case 1:
                tarif = "Value when subject to maintenance";
                break;
            case 2:
                tarif = "Invalid measurement";
                break;
            case 3:
                tarif = "Reserved";
                break;
        }
        return tarif;
    }

    public double getKmoltFactor() {
        int kmolt = qlf & (0x07);
        if (kmolt < 7) {
            return Math.pow(10, (kmolt*(-1)));
        } else {
            return 1;
        }
    }

    public String getValueTime() {
        int SL = qlf & (0x020) >> 5;
        String valueTime = "";
        switch (SL) {
            case 0:
                valueTime = "Value during daylight saving time";
                break;
            case 1:
                valueTime = "Standard time";
                break;
        }
        return valueTime;
    }
}
