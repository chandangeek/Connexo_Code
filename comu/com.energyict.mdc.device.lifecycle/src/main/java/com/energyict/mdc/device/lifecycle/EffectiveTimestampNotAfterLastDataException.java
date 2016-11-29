package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user and the effective timestamp
 * is not after the last date of data on that same {@link Device}.
 */
public class EffectiveTimestampNotAfterLastDataException extends DeviceLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final String deviceName;
    private final Instant effectiveTimestamp;
    private final Instant lastStateChange;
    private final DateTimeFormatter formatter;

    public EffectiveTimestampNotAfterLastDataException(Thesaurus thesaurus, MessageSeed messageSeed, Device device, Instant effectiveTimestamp, Instant lastData, DateTimeFormatter formatter) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.deviceName = device.getName();
        this.effectiveTimestamp = effectiveTimestamp;
        this.lastStateChange = lastData;
        this.formatter = formatter;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus
                .getFormat(this.messageSeed)
                .format(
                        this.deviceName,
                        getFormattedInstant(this.formatter, this.effectiveTimestamp),
                        getFormattedInstant(this.formatter, this.lastStateChange));
    }

    private String getFormattedInstant(DateTimeFormatter formatter, Instant time){
        return formatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault()));
    }
}