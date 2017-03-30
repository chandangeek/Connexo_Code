/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3;

public class SagemComG3Properties extends G3Properties {

    public static final String DEFAULT_VALIDATE_INVOKE_ID = "0";

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
        return getBooleanProperty(super.VALIDATE_INVOKE_ID, this.DEFAULT_VALIDATE_INVOKE_ID);
    }
}
