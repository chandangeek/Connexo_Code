package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/04/2016 - 15:54
 */
public enum MulticastMeterState {

    /* Image transfer not started */
    Image_transfer_not_initiated(0, "Image transfer not initiated"),
    /* Image transfer enabled and initiated */
    Image_transfer_initiated(1, "Image transfer initiated"),
    /* All blocks received, and image transfer verification initiated */
    Image_verification_initiated(2, "Image verification initiated"),
    /* The meter indicates the image is correct and ready to be activated */
    Image_verification_successful(3, "Image verification successful"),
    /* The meter indicates the image is not correct */
    Image_verification_failed(4, "Image verification failed"),
    /* The meter indicates it received the command to activate the image */
    Image_activation_initiated(5, "Image activation initiated"),
    /* The meter indicates it activated the new image ( in case of AM540 this is BEFORE the reboot , so the meter goes offline afterwards */
    Image_activation_successful(6, "Image activation successful"),
    /* The meter indicates it could not activate the image */
    Image_activation_failed(7, "Image activation failed"),
    /* Unknown image transfer status */
    Unknown(-1, "Unknown");

    private int value;
    private String description;

    MulticastMeterState(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static MulticastMeterState fromValue(int value) {
        for (MulticastMeterState multicastMeterState : values()) {
            if (multicastMeterState.getValue() == value) {
                return multicastMeterState;
            }
        }
        return Unknown;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}