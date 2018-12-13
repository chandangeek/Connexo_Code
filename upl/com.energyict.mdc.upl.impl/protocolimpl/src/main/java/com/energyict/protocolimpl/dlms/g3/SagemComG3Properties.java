package com.energyict.protocolimpl.dlms.g3;

import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 * Date: 18/12/12
 * Time: 12:56
 * Author: khe
 */
class SagemComG3Properties extends G3Properties {

    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = false;

    SagemComG3Properties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

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

    @Override
    protected boolean validateInvokeId() {
        return getBooleanProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

}