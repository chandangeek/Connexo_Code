package com.elster.insight.usagepoint.data.exceptions;

import java.time.Instant;
import java.util.Date;

import aQute.bnd.annotation.ProviderType;
import org.osgi.service.device.Device;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when validation
 * is activated on a {@link UsagePoint} but the specified last checked
 * timestamp is invalid.
 *
 */
@ProviderType
public class InvalidLastCheckedException extends RuntimeException {

    public static InvalidLastCheckedException lastCheckedCannotBeNull(UsagePoint usagePoint, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new InvalidLastCheckedException(thesaurus, messageSeed, usagePoint);
    }

    public static InvalidLastCheckedException lastCheckedAfterCurrentLastChecked(UsagePoint usagePoint, Instant oldLastChecked, Instant newLastChecked, Thesaurus thesaurus, MessageSeed messageSeed) {
        InvalidLastCheckedException e = new InvalidLastCheckedException(thesaurus, messageSeed, usagePoint);
        e.oldLastChecked = Date.from(oldLastChecked);
        e.newLastChecked = Date.from(newLastChecked);
        return e;
    }

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final UsagePoint usagePoint;
    private Date oldLastChecked;
    private Date newLastChecked;

    private InvalidLastCheckedException(Thesaurus thesaurus, MessageSeed messageSeed, UsagePoint usagePoint) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.usagePoint = usagePoint;
    }

    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    public Date getNewLastChecked() {
        return newLastChecked;
    }

    public Date getOldLastChecked() {
        return oldLastChecked;
    }

    public MessageSeed getMessageSeed() {
        return messageSeed;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.usagePoint.getMRID(), this.oldLastChecked, this.newLastChecked);
    }

}