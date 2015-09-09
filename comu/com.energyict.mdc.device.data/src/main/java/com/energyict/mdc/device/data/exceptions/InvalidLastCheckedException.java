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

    public static InvalidLastCheckedException lastCheckedCannotBeNull(Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new InvalidLastCheckedException(thesaurus, messageSeed, device);
    }

    public static InvalidLastCheckedException lastCheckedAfterCurrentLastChecked(Device device, Instant oldLastChecked, Instant newLastChecked, Thesaurus thesaurus, MessageSeed messageSeed) {
        InvalidLastCheckedException e = new InvalidLastCheckedException(thesaurus, messageSeed, device);
        e.oldLastChecked = Date.from(oldLastChecked);
        e.newLastChecked = Date.from(newLastChecked);
        return e;
    }

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final Device device;
    private Date oldLastChecked;
    private Date newLastChecked;

    private InvalidLastCheckedException(Thesaurus thesaurus, MessageSeed messageSeed, Device device) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    public MessageSeed getMessageSeed() {
        return messageSeed;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.device.getmRID(), this.oldLastChecked, this.newLastChecked);
    }

}