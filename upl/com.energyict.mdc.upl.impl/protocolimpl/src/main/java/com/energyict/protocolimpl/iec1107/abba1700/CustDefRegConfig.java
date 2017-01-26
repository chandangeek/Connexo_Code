/*
 * CustDefRegConfig.java
 *
 * Created on 16 juni 2004, 13:33
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 * @author Koen
 */
public class CustDefRegConfig {

    private final int[][] custRegSource;
    private final boolean extended;

    private static final int NUMBER_OF_CDRS = 3;
    private static final int NORMAL_CDR_COMBINATIONS = 2;
    private static final int EXTENDED_CDR_COMBINATIONS = 5;

    /**
     * Creates a new instance of CustDefRegConfig
     *
     * @param data
     * @throws ProtocolException
     */
    public CustDefRegConfig(byte[] data) throws ProtocolException {
        if (data.length < (NUMBER_OF_CDRS * NORMAL_CDR_COMBINATIONS)) {
            throw new ProtocolException("Invalid content of the CustDefRegConfig register: " + ProtocolTools.getHexStringFromBytes(data));
        }
        extended = (data.length >= (EXTENDED_CDR_COMBINATIONS * NUMBER_OF_CDRS));
        custRegSource = new int[NUMBER_OF_CDRS][extended ? EXTENDED_CDR_COMBINATIONS : NORMAL_CDR_COMBINATIONS];
        for (int cdrNumber = 0; cdrNumber < custRegSource.length; cdrNumber++) {
            for (int cdrSource = 0; cdrSource < custRegSource[cdrNumber].length; cdrSource++) {
                int ptr = cdrNumber * custRegSource[cdrNumber].length + cdrSource;
                custRegSource[cdrNumber][cdrSource] = ProtocolUtils.getIntLE(data, ptr, 1);
            }
        }
    }

    /**
     * Getter for property custRegSource.
     *
     * @return Value of property custRegSource.
     */
    public int[][] getCustRegSource() {
        return this.custRegSource;
    }

    /**
     * Get the register source for a given value. Skips '0' values
     *
     * @param custReg
     * @return
     */
    public int getRegSource(int custReg) {
        int[] customerSources = custRegSource[custReg];
        for (int i = 0; i < customerSources.length; i++) {
            int registerType = customerSources[i];
            if (registerType != 0) {
                return registerType;
            }
        }
        return 0;
    }

    /**
     * Check if the CustDefRegConfig can contain 2 combinations (normal) or 5 combinations (EXTENDED)
     *
     * @return
     */
    public boolean isExtended() {
        return extended;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CustDefRegConfig{");
        for (int cdrNumber = 0; cdrNumber < custRegSource.length; cdrNumber++) {
            int[] combinations = custRegSource[cdrNumber];
            sb.append("CDR").append(cdrNumber + 1).append("=");
            for (int sourceNumber = 0; sourceNumber < combinations.length; sourceNumber++) {
                int source = combinations[sourceNumber];
                sb.append(" ").append(source);
            }
            sb.append(" ] ");
        }
        sb.append("Extended=").append(extended);
        sb.append(" }");
        return sb.toString();
    }
}
