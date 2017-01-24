package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jme
 */
public final class EdisObisMapper {

    private static final List<EdisObisPair> MAPPING = new ArrayList<EdisObisPair>();

    static {
        MAPPING.add(new EdisObisPair("0.0.97.97.0.255", "F.F"));

        MAPPING.add(new EdisObisPair("1.0.1.7.0.255", "1.25"));

        MAPPING.add(new EdisObisPair("1.0.31.7.0.255", "31.25"));
        MAPPING.add(new EdisObisPair("1.0.51.7.0.255", "51.25"));
        MAPPING.add(new EdisObisPair("1.0.71.7.0.255", "71.25"));

        MAPPING.add(new EdisObisPair("1.0.32.7.0.255", "32.25"));
        MAPPING.add(new EdisObisPair("1.0.52.7.0.255", "52.25"));
        MAPPING.add(new EdisObisPair("1.0.72.7.0.255", "72.25"));

        MAPPING.add(new EdisObisPair("1.0.14.7.0.255", "14.25"));

        MAPPING.add(new EdisObisPair("1.0.21.7.0.255", "21.25"));
        MAPPING.add(new EdisObisPair("1.0.41.7.0.255", "41.25"));
        MAPPING.add(new EdisObisPair("1.0.61.7.0.255", "61.25"));

        MAPPING.add(new EdisObisPair("1.0.3.7.0.255", "3.25"));

        MAPPING.add(new EdisObisPair("1.0.23.7.0.255", "23.25"));
        MAPPING.add(new EdisObisPair("1.0.43.7.0.255", "43.25"));
        MAPPING.add(new EdisObisPair("1.0.63.7.0.255", "63.25"));

        MAPPING.add(new EdisObisPair("1.0.9.7.0.255", "9.25"));

        MAPPING.add(new EdisObisPair("1.0.29.7.0.255", "29.25"));
        MAPPING.add(new EdisObisPair("1.0.49.7.0.255", "49.25"));
        MAPPING.add(new EdisObisPair("1.0.69.7.0.255", "69.25"));

        MAPPING.add(new EdisObisPair("1.0.13.7.0.255", "13.25"));

        MAPPING.add(new EdisObisPair("1.0.33.7.0.255", "33.25"));
        MAPPING.add(new EdisObisPair("1.0.53.7.0.255", "53.25"));
        MAPPING.add(new EdisObisPair("1.0.73.7.0.255", "73.25"));

        MAPPING.add(new EdisObisPair("1.0.0.4.2.255", "0.4.2"));
        MAPPING.add(new EdisObisPair("1.0.0.4.3.255", "0.4.3"));
        MAPPING.add(new EdisObisPair("1.0.0.4.4.255", "0.4.4"));

        MAPPING.add(new EdisObisPair("1.0.81.7.0.255", "81.7.00"));
        MAPPING.add(new EdisObisPair("1.0.81.7.10.255", "81.7.10"));
        MAPPING.add(new EdisObisPair("1.0.81.7.21.255", "81.7.21"));
        MAPPING.add(new EdisObisPair("1.0.81.7.2.255", "81.7.02"));
        MAPPING.add(new EdisObisPair("1.0.81.7.20.255", "81.7.20"));
        MAPPING.add(new EdisObisPair("1.0.81.7.40.255", "81.7.40"));
        MAPPING.add(new EdisObisPair("1.0.81.7.51.255", "81.7.51"));
        MAPPING.add(new EdisObisPair("1.0.81.7.62.255", "81.7.62"));

        MAPPING.add(new EdisObisPair("0.0.96.7.0.255", "C.7.0"));
        MAPPING.add(new EdisObisPair("0.0.96.7.1.255", "C.7.1"));
        MAPPING.add(new EdisObisPair("0.0.96.7.2.255", "C.7.2"));
        MAPPING.add(new EdisObisPair("0.0.96.7.3.255", "C.7.3"));

    }

    /**
     * Return the Edis code for the given <CODE>ObisCode</CODE>
     *
     * @param obisCode          the given obisCode
     * @param longNameObisCodes indication whether longName ObisCodes are supported
     * @return the Edis code
     */
    public static String getEdisCodeFromObisCode(ObisCode obisCode, boolean longNameObisCodes) {
        for (int i = 0; i < MAPPING.size(); i++) {
            EdisObisPair pair = MAPPING.get(i);
            if (pair.getObisCode().equals(obisCode)) {
                return pair.getEdisCode();
            }
        }
        return buildEdisCode(obisCode, longNameObisCodes);
    }

    /**
     * Build the Edis code for the given ObisCode
     *
     * @param obisCode          the given obisCode
     * @param longNameObisCodes indicate whether longName ObisCodes are supported (used for multiple pulse channels)
     * @return the Edis code
     */
    private static String buildEdisCode(ObisCode obisCode, boolean longNameObisCodes) {
        StringBuilder sb = new StringBuilder();

        if (longNameObisCodes) {
            if (obisCode.getA() == 1) {
                if (obisCode.getB() >= 1 && obisCode.getB() <= 8) {
                    if (obisCode.getC() >= 1 && obisCode.getC() <= 10) {
                        if (obisCode.getD() == 2 || obisCode.getD() == 4 || obisCode.getD() == 5 || obisCode.getD() == 6 ||
                                obisCode.getD() == 8 || obisCode.getD() == 9) {
                            if (obisCode.getE() >= 0 && obisCode.getE() <= 8) {
                                sb.append(obisCode.getA());
                                sb.append("-");
                                sb.append(obisCode.getB());
                                sb.append(":");
                            }
                        }
                    }
                }
            }
        }
        sb.append(obisCode.getC()).append(".");
        sb.append(obisCode.getD()).append(".");
        sb.append(obisCode.getE());
        if (obisCode.getF() != 255) {
            sb.append("*");
            sb.append(ProtocolUtils.buildStringDecimal(Math.abs(obisCode.getF()), 2));
        }
        return sb.toString();
    }

}
