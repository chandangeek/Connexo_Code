package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Push setup IC
 * The push setup COSEM IC allows for configuration of upstream push events.
 */
public enum  Beacon3100PushSetupAttributes implements DLMSClassAttributes{

    /**
     * Identifies the push setup IC instance (the OBIS code).
     */
    LOGICAL_NAME(1, 0x00),

    /**
     * List of attributes to be pushed.
     * @deprecated Attribute not used.
     */
    @Deprecated
    PUSH_OBJECT_LIST(2, 0x08),

    /**
     * Transport method and message format to be used.
     */
    SEND_DESTINATION_AND_METHOD (3, 0x10),

    /**
     * Time period during which notifications can be pushed.
     * @deprecated Attribute is not used.
     */
    @Deprecated
    COMMUNICATION_WINDOW(4, 0x18),

    /**
     * Random delay before pushing events.
     * @deprecated Attribute is not used.
     */
    @Deprecated
    RANDOMIZATION_START_INTERVAL(5, 0x20),

    /**
     * Maximum number of retries in case of unsuccessful push attempt.
     */
    NUMBER_OF_RETRIES(6, 0x28),

    /**
     * Placeholder for upstream event-notifications.
     * @deprecated Attribute is never accessible
     */
    @Deprecated
    EVENT(-1, 0x30),

    /**
     * Defines the notification type of outgoing push events. Enum of type notification_type
     */
    NOTIFICATION_TYPE(-2, 0x38),

    /**
     * Defines the type of ciphering to be applied to outgoing push events.
     */
    NOTIFICATION_CIPHERING(-3, 0x40),

    /**
     * Contains the alarm service state
     */
    ALARM_SERVICE_ENABLED(-4, 0x48),

    /**
     * The list of propagated event codes by the alarm service; array of double-long unsigned.
     * An event code is a concatenation of the DLMS code and device-code
     */
    ALARM_SERVICE_EVENT_CODES(-5, 0x50);



    private final int attributeNumber;
    private final int shortName;

    Beacon3100PushSetupAttributes(int attributeNumber, int shortName) {
        this.attributeNumber = attributeNumber;
        this.shortName = shortName;
    }

    @Override
    public int getAttributeNumber() {
        return attributeNumber;
    }

    @Override
    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP;
    }

    @Override
    public int getShortName() {
        return shortName;
    }
}
