package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 19/11/2014 - 10:15
 */
public class EdisFormatter {

    /**
     * Format the given ObisCode to its long-name EDIS notation
     *
     * @param obisCode the ObisCode to format
     * @return the long-name EDIS notation of the OBIS
     */
    public static String formatObisAsEdis(String obisCode) {
        ObisCode obisCodeObj = ObisCode.fromString(obisCode);
        return formatObisAsEdis(obisCodeObj);
    }

    /**
     * Format the given ObisCode to its long-name EDIS notation
     *
     * @param obisCode the ObisCode to format
     * @return the long-name EDIS notation of the OBIS
     */
    public static String formatObisAsEdis(ObisCode obisCode) {
        StringBuilder builder = new StringBuilder();
        builder.append(getEdisFormattedField(obisCode.getA()));
        builder.append("-");
        builder.append(getEdisFormattedField(obisCode.getB()));
        builder.append(":");
        builder.append(getEdisFormattedField(obisCode.getC()));
        builder.append(".");
        builder.append(getEdisFormattedField(obisCode.getD()));
        builder.append(".");
        builder.append(getEdisFormattedField(obisCode.getE()));
        builder.append("*");
        builder.append(getEdisFormattedField(obisCode.getF()));
        return builder.toString();
    }

    private static String getEdisFormattedField(int field) {
        switch (field) {
            case 96:
                return "C";
            case 97:
                return "F";
            case 98:
                return "L";
            case 99:
                return "P";
            default:
                return Integer.toString(field);
        }
    }

    private EdisFormatter() {
        // Hiding constructor of utility class
    }
}
