package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;
import java.util.Date;

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
        NULL {
            @Override
            public MessageSeed messageSeed() {
                return MessageSeeds.LAST_CHECKED_CANNOT_BE_NULL;
            }
        },

        /**
         * The specified last checked timestamp is after the
         * last checked of the current meter activation.
         */
        AFTER_CURRENT_LAST_CHECKED {
            @Override
            public MessageSeed messageSeed() {
                return MessageSeeds.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED;
            }
        };

        public abstract MessageSeed messageSeed();

    }

    public static InvalidLastCheckedException lastCheckedCannotBeNull(Thesaurus thesaurus, Device device) {
        return new InvalidLastCheckedException(thesaurus, Reason.NULL, device);
    }

    public static InvalidLastCheckedException lastCheckedAfterCurrentLastChecked(Thesaurus thesaurus, Device device, Instant oldLastChecked, Instant newLastChecked) {
        InvalidLastCheckedException e = new InvalidLastCheckedException(thesaurus, Reason.AFTER_CURRENT_LAST_CHECKED, device);
        e.oldLastChecked = Date.from(oldLastChecked);
        e.newLastChecked = Date.from(newLastChecked);
        return e;
    }

    private final Thesaurus thesaurus;
    private final Reason reason;
    private final Device device;
    private Date oldLastChecked;
    private Date newLastChecked;

    private InvalidLastCheckedException(Thesaurus thesaurus, Reason reason, Device device) {
        super();
        this.thesaurus = thesaurus;
        this.reason = reason;
        this.device = device;
    }

    public Reason getReason() {
        return reason;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.reason.messageSeed()).format(this.device.getmRID(), this.oldLastChecked, this.newLastChecked);
    }

}