package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.obis.ObisCode;

import java.time.Instant;
import java.time.ZoneId;

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


    public static Instant toMidnight(final Instant randomInstant, ZoneId timeZone) {
        return randomInstant.atZone(timeZone)
                .withNano(0)
                .withSecond(0)
                .withMinute(0)
                .withHour(0)
                .toInstant();
    }

}
