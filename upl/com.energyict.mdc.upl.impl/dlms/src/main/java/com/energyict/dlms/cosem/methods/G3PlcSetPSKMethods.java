package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum G3PlcSetPSKMethods implements DLMSClassMethods {

    SET_PSK(1);

    private final int methodId;

    /**
     * Create a new instance.
     *
     * @param methodId The ID of the method.
     */
    private G3PlcSetPSKMethods(final int methodId) {
        this.methodId = methodId;
    }

    /**
     * {@inheritDoc}
     */
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.MANUFACTURER_SPECIFIC_8194;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public final int getMethodNumber() {
        return this.methodId;
    }
}
