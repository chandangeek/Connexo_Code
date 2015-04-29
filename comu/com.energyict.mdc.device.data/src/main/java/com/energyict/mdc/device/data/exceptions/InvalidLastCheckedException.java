package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.Device;

/**
 * Models the exceptional situation that occurs when validation
 * is activated on a {@link Device} but the specified last checked
 * timestamp is invalid.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-20 (10:28)
 */
public class InvalidLastCheckedException extends RuntimeException {

    public enum Reason {
        /**
         * The last checked timestamp cannot be <code>null</code>.
         */
        NULL,

        /**
         * The specified last checked timestamp is after the
         * last checked of the current meter activation.
         */
        AFTER_CURRENT_LAST_CHECKED;

    }

    public static InvalidLastCheckedException lastCheckedCannotBeNull(Device device) {
        return new InvalidLastCheckedException(Reason.NULL, device);
    }

    public static InvalidLastCheckedException lastCheckedAfterCurrentLastChecked(Device device) {
        return new InvalidLastCheckedException(Reason.AFTER_CURRENT_LAST_CHECKED, device);
    }

    private final Reason reason;
    private final Device device;

    private InvalidLastCheckedException(Reason reason, Device device) {
        super();
        this.reason = reason;
        this.device = device;
    }

    public Reason getReason() {
        return reason;
    }

    public Device getDevice() {
        return device;
    }

}