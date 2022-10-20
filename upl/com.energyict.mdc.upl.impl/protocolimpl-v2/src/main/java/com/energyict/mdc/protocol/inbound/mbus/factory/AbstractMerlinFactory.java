package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cim.EndDeviceEventType;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;

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


    public static final String DAILY_PROFILE_OBISCODE = "8.0.99.2.0.255";
    public static final String DAILY_PROFILE_CIM = "11.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0";    //[60-minutes] Bulk Water volume (m³)

    public static final String HOURLY_PROFILE_OBISCODE = "8.0.99.1.0.255";
    public static final String HOURLY_PROFILE_CIM = "0.0.7.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0";    //[60-minutes] Bulk Water volume (m³)

    public static final String NIGHTLINE_PROFILE_OBISCODE = "8.0.99.3.0.255";
    public static final String NIGHTLINE_PROFILE_CIM = "0.0.7.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0";    //[60-minutes] Bulk Water volume (m³)

    public static final String STANDARD_LOGBOOK = "0.0.99.98.0.255";


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
        return Date.from(toDeviceTimeZone(getTelegramDateTime(), getInboundContext().getTimeZone()).toInstant());
    }


    protected MeterProtocolEvent createEvent(int eiCode, String message) {
        Date eventTime = getDateOnDeviceTimeZoneFromTelegramTime();
        int protocolCode = 0;
        EndDeviceEventType eventType = EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(eiCode);
        int meterEventLogId = 0;
        int deviceEventId = 0;

        return new MeterProtocolEvent(eventTime, eiCode, protocolCode, eventType, message, meterEventLogId, deviceEventId);

    }
}
