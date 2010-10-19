package com.energyict.genericprotocolimpl.elster.ctr.common;

/**
 * Copyrights EnergyICT
 * Date: 19-okt-2010
 * Time: 16:29:27
 */
public class Diagnostics {

    public static String getDescriptionFromCode(int code) {
        String description = "";

        for (int i = 0; i < 13; i++) {
            switch (i) {
                case 0:
                    if (isBitSet(code, i)) {
                        description = "Mains power not available";
                    }
                case 1:
                    if (isBitSet(code, i)) {
                        description = "Low Battery";
                    }
                case 2:
                    if (isBitSet(code, i)) {
                        description = "Event log at 90%";
                    }
                case 3:
                    if (isBitSet(code, i)) {
                        description = "General alarm";
                    }
                case 4:
                    if (isBitSet(code, i)) {
                        description = "Connection with emitter or converter broken";
                    }
                case 5:
                    if (isBitSet(code, i)) {
                        description = "Event log full";
                    }
                case 6:
                    if (isBitSet(code, i)) {
                        description = "Clock misalignment";
                    }
                case 7:
                    if (isBitSet(code, i)) {
                        description = "Converter alarm";
                    }
                case 8:
                    if (isBitSet(code, i)) {
                        description = "Temperature out of range";
                    }
                case 9:
                    if (isBitSet(code, i)) {
                        description = "Pressure out of range";
                    }
                case 10:
                    if (isBitSet(code, i)) {
                        description = "Flow over limit";
                    }
                case 11:
                    if (isBitSet(code, i)) {
                        description = "Valve closing error";
                    }
                case 12:
                    if (isBitSet(code, i)) {
                        description = "Valve opening error";
                    }
            }
        }
        return description;
    }


    private static boolean isBitSet(int value, int bitNr) {
        return (0 != (value & (0x01 << bitNr)));
    }


}
