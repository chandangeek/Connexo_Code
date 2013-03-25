package com.energyict.protocolimpl.dlms.g3;

/**
 * Copyrights EnergyICT
 * Date: 18/12/12
 * Time: 12:56
 * Author: khe
 */
public class SagemComG3Properties extends G3Properties {

    /**
     * Use other conformance block for the SagemCom meter
     */
    @Override
    public long getConformance() {
        if (isSNReference()) {
            return getLongProperty(CONFORMANCE_BLOCK_VALUE, DEFAULT_CONFORMANCE_BLOCK_VALUE_SN);
        } else if (isLNReference()) {
            return 0x001A1D;
        } else {
            return 0;
        }
    }
}
