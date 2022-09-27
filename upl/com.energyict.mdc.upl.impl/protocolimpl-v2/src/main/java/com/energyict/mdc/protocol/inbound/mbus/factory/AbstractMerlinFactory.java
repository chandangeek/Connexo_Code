package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.obis.ObisCode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * General factory for objects available within the whole frame,
 * common to registers and profiles: device identified, telegram date and time
 */
public abstract class AbstractMerlinFactory {
    private final Telegram telegram;
    private final InboundContext inboundContext;
    private final CollectedDataFactory collectedDataFactory;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private Instant telegramDateTime;


    public AbstractMerlinFactory(Telegram telegram, InboundContext inboundContext) {
        this.telegram = telegram;
        this.inboundContext = inboundContext;
        this.collectedDataFactory = inboundContext.getInboundDiscoveryContext().getCollectedDataFactory();
    }


    // each implementation must return its own OBIS-CODE
    public abstract ObisCode getObisCode();

    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(telegram.getSerialNr());
        }
        return this.deviceIdentifier;
    }

    public Instant getTelegramDateTime() {
        return telegramDateTime;
    }


    protected void extractTelegramDateTime() {
        this.telegramDateTime = TelegramDateTimeFactory.from(telegram, inboundContext);
    }


    public Telegram getTelegram() {
        return telegram;
    }

    public InboundContext getInboundContext() {
        return inboundContext;
    }

    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    /**
     * Generic converter from a UTC instant to a specific time-zone
     */
    public static ZonedDateTime toDeviceTimeZone(final Instant someInstant, ZoneId timeZone) {
        return someInstant.atZone(timeZone);
    }


    /**
     * Converts a UTC instant to midnight with time zone applied
     */
    public static Instant toMidnightWithTimeZone(final Instant randomInstant, ZoneId timeZone) {
        return toDeviceTimeZone(randomInstant, timeZone)
                .withNano(0)
                .withSecond(0)
                .withMinute(0)
                .withHour(0)
                .toInstant();
    }

    /**
     * Adjusts the telegram date/time (in UTC) to the (configured/default) device time zone
     */
    public Date getDateOnDeviceTimeZoneFromTelegramTime() {
        return new Date(toDeviceTimeZone(getTelegramDateTime(), getInboundContext().getTimeZone()).toEpochSecond());
    }

}
