package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;


public enum FrameCounterProviderMethods implements DLMSClassMethods {

    GET_FRAME_COUNTER(1);

    private final int methodId;

    /**
     * Create a new instance.
     *
     * @param methodId The ID of the method.
     */
    private FrameCounterProviderMethods(final int methodId) {
        this.methodId = methodId;
    }

    /**
     * {@inheritDoc}
     */
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.FRAME_COUNTER_PROVIDER;
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