package com.energyict.dlms.cosem.attributeobjects;

/**
 * Copyrights EnergyICT
 * Date: 10/15/12
 * Time: 10:01 AM
 */
public enum ImageTransferStatus {

    TRANSFER_NOT_INITIATED(0, "Image transfer not initiated"),
    TRANSFER_INITIATED(1, "Image transfer initiated"),
    VERIFICATION_INITIATED(2, "Image verification initiated"),
    VERIFICATION_SUCCESSFUL(3, "Image verification successful"),
    VERIFICATION_FAILED(4, "Image verification failed"),
    ACTIVATION_INITIATED(5, "Image activation initiated"),
    ACTIVATION_SUCCESSFUL(6, "Image activation successful"),
    ACTIVATION_FAILED(7, "Image activation failed");

    private final int value;
    private final String info;

    ImageTransferStatus(final int value, final String info) {
        this.value = value;
        this.info = info;
    }

    /**
     * @return The enum value used in the dlms object
     */
    public final int getValue() {
        return value;
    }

    public String getInfo() {
        return info;
    }

    /**
     * Find an ImageTransferStatus by the dlms enum value
     *
     * @param value The enum value used in the dlms object
     * @return The matching ImageTransferStatus or 'null' if not found
     */
    public static final ImageTransferStatus fromValue(final int value) {
        final ImageTransferStatus[] values = values();
        for (ImageTransferStatus imageTransferStatus : values) {
            if (imageTransferStatus.getValue() == value) {
                return imageTransferStatus;
            }
        }
        return null;
    }

}
