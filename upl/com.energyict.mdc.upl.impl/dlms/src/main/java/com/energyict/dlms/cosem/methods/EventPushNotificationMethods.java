package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Push setup IC (class_id = 40, version = 0, logical_name = 0.0.25.9.0.255)
 */
public enum EventPushNotificationMethods implements DLMSClassMethods {
    SEND_TEST_NOTIFICATION_METHOD(-1, 32);

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
    private EventPushNotificationMethods(final int methodNumber, final int shortAddress) {
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
