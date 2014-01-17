package com.energyict.protocolimpl.iec1107.as220;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 21/06/12
 * Time: 15:47
 */
public class AS220DataDumpParser extends com.energyict.protocolimpl.base.DataDumpParser {

    private HashMap<Integer, Integer> billingPointsHashMap;

    /**
     * Creates a new instance of SCTMDumpData
     */
    public AS220DataDumpParser(byte[] frame) throws IOException {
        super(frame);
    }

    public AS220DataDumpParser(byte[] frame, String datetimeSignature) throws IOException {
        super(frame, datetimeSignature);
    }

    public HashMap<Integer, Integer> getBillingPointsMap() {
        if (billingPointsHashMap == null) {
            billingPointsHashMap = new HashMap<Integer, Integer>();
            int index1 = -1;
            int key = 0;
            while (true) {
                index1 = getStrFrame().indexOf("0.1.2*", index1 + 1);
                if (index1 == -1) {
                    break;
                }

                int index2 = getStrFrame().indexOf('(', index1);
                try {
                    billingPointsHashMap.put(key++, Integer.parseInt(getStrFrame().substring(index1 + 6, index2)));
                } catch (NumberFormatException e) {
                    // absorb
                }
            }
        }
        return billingPointsHashMap;
    }

    @Override
    public String getRegisterStrValue(String strReg) throws IOException {
        int splitter = strReg.indexOf('*');
        if (splitter == -1) {   // Actual registers can be read out with conventional method
            return super.getRegisterStrValue(strReg);
        } else {                // For billing registers, a non-standard method is needed.
            String reg = strReg.substring(0, splitter + 1);
            int billingPoint = Integer.parseInt(strReg.substring(splitter + 1));

            String[] split = getStrFrame().split("\n");

            for (String each : split) {
                if (each.contains(reg)) {
                    if (billingPoint == 0) {
                        return each.substring(0, each.indexOf("\r"));
                    } else {
                        billingPoint -= 1;
                    }
                }
            }
        }

        throw new NoSuchRegisterException("DataDumParser, getRegisterStrValue, register not found");
    }

    @Override
    public int getBillingCounter() {
        return getBillingPointsMap().size();
    }
}