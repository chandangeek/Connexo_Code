package com.energyict.dlms.cosem.attributeobjects;

/**
 * Copyrights EnergyICT
 * Date: 10/15/12
 * Time: 10:01 AM
 */
public enum ImageTransferStatus {

    TRANSFER_NOT_INITIATED(0),
    TRANSFER_INITIATED(1),
    VERIFICATION_INITIATED(2),
    VERIFICATION_SUCCESSFUL(3),
    VERIFICATION_FAILED(4),
    ACTIVATION_INITIATED(5),
    ACTIVATION_SUCCESSFUL(6),
    ACTIVATION_FAILED(7);

    private final int value;

    ImageTransferStatus(final int value) {
        this.value = value;
    }

    /**
     * @return The enum value used in the dlms object
     */
    public final int getValue() {
        return value;
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
