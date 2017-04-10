package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Push setup IC
 * class id = 40, version = 0, logical name = 0-0:25.9.0.255 (0000190900FF)
 * The push setup COSEM IC allows for configuration of upstream push events.
 */
public enum Beacon3100PushSetupMethods implements DLMSClassMethods {
    /**
     * Start data push.
     * @deprecated Method unused
     */
    @Deprecated
    PUSH(1, 0x10),

    /**
     * Echo request payload to head-end as push event. Returns true if request got queued correctly.
     */
    SEND_TEST_NOTIFICATION_METHOD((byte)-1, 0x18);


    /** The method number. */
    private final int methodNumber;

    /** The short address. */
    private final int shortAddress;

    /**
     * Create a new instance.
     *
     * @param 	methodNumber		The method number.
     * @param 	shortAddress		The short address.
     */
    private Beacon3100PushSetupMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final int getMethodNumber() {
        return this.methodNumber;
    }
}
