package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.events.ErrorFlagsEventsFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.events.StatusEventsFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.profiles.DailyProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.profiles.HourlyProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.registers.RegisterFactory;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class MerlinCollectedDataFactory {
    private final Telegram telegram;
    private final InboundContext inboundContext;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private CollectedRegisterList collectedRegisterList;
    private CollectedLoadProfile hourlyLoadProfile;
    private List<CollectedData> collectedDataList;
    private FrameType frameType;
    private CollectedLoadProfile dailyLoadProfile;
    private ZoneId timeZone;
    public static final String PROPERTY_DEVICE_TIME_ZONE = "deviceTimeZone";
    private CollectedLogBook eventsStatus;

    public MerlinCollectedDataFactory(Telegram telegram, InboundContext inboundContext) {
        this.telegram = telegram;
        this.inboundContext = inboundContext;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(telegram.getSerialNr());
        }
        return this.deviceIdentifier;
    }

    public List<CollectedData> getCollectedData() {
        if (this.collectedDataList != null) {
            return collectedDataList;
        }

        this.frameType = FrameType.of(telegram);

        lookupDeviceProperties();

        if (FrameType.UNKNOWN.equals(frameType)) {
            inboundContext.getLogger().logW("Could not detect the frame type!");
        } else {
            inboundContext.getLogger().log("Frame type detected: " + frameType);
        }

        collectedDataList = new ArrayList<>();

        try {
            extractRegisters();
        } catch (Exception ex) {
            inboundContext.getLogger().logE("Exception while parsing registers", ex);
        }

        try {
            extractHourlyProfile();
        } catch (Exception ex) {
            inboundContext.getLogger().logE("Exception while parsing hourly profile", ex);
        }

        try {
            extractDailyProfile();
        } catch (Exception ex) {
            inboundContext.getLogger().logE("Exception while parsing daily profile", ex);
        }

        try {
            extractStatusEvents();
        } catch (Exception ex) {
            inboundContext.getLogger().logE("Exception while parsing status events", ex);
        }

        if (collectedRegisterList != null ) {
            collectedDataList.add(collectedRegisterList);
        }

        if (hourlyLoadProfile != null) {
            collectedDataList.add(hourlyLoadProfile);
        }

        if (dailyLoadProfile != null) {
            collectedDataList.add(dailyLoadProfile);
        }

        if (eventsStatus != null) {
            collectedDataList.add(eventsStatus);
        }

        return collectedDataList;
    }

    private void extractStatusEvents() {
        StatusEventsFactory eventsFactory = new StatusEventsFactory(telegram, inboundContext);
        this.eventsStatus = eventsFactory.extractEventsFromStatus();
    }

    private void extractErrorFlags() {
        ErrorFlagsEventsFactory errorFlagsEventsFactory = new ErrorFlagsEventsFactory(telegram, inboundContext);

        TelegramVariableDataRecord eventsRecord = telegram.getBody().getBodyPayload().getRecords().get(8);

        if (errorFlagsEventsFactory.appliesFor(eventsRecord)) {
            errorFlagsEventsFactory.extractEventsFromErrorFlags(eventsRecord);
        }
    }

    protected void lookupDeviceProperties() {
        TypedProperties protocolProperties = inboundContext.getInboundDiscoveryContext().getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());

        if (protocolProperties.hasLocalValueFor(PROPERTY_DEVICE_TIME_ZONE)){
            String configuredTimeZoneId = protocolProperties.getTypedProperty(PROPERTY_DEVICE_TIME_ZONE, TimeZone.getDefault()).getID();
            timeZone = ZoneId.of(configuredTimeZoneId);
            inboundContext.getLogger().log("Using configured time zone of the device: " + timeZone);
        } else {
            timeZone = ZoneId.systemDefault();
            inboundContext.getLogger().log("Using system default time zone : " + timeZone);
        }

        inboundContext.setTimeZone(timeZone);
    }

    private void extractRegisters() {
        RegisterFactory registerFactory = new RegisterFactory(telegram, inboundContext);
        this.collectedRegisterList = registerFactory.extractRegisters();
    }

    private void extractHourlyProfile() {
        if (!FrameType.DAILY_FRAME.equals(frameType)) {
            return;
        }
        HourlyProfileFactory profileFactory = new HourlyProfileFactory(telegram, inboundContext);

        // for the known frames (weekly, daily) the ENCODING_VARIABLE_LENGTH field is always 4 and 3 is always the start index
        TelegramVariableDataRecord indexRecord = telegram.getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = telegram.getBody().getBodyPayload().getRecords().get(4);

        this.hourlyLoadProfile = profileFactory.extractLoadProfile(hourlyRecord, indexRecord);
    }

    private void extractDailyProfile() {
        if (!FrameType.WEEKLY_FRAME.equals(frameType)) {
            return;
        }
        DailyProfileFactory profileFactory = new DailyProfileFactory(telegram, inboundContext);

        // for the known frames (weekly, daily) the ENCODING_VARIABLE_LENGTH field is always 4 and 3 is always the start index
        TelegramVariableDataRecord indexRecord = telegram.getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = telegram.getBody().getBodyPayload().getRecords().get(4);

        this.dailyLoadProfile = profileFactory.extractLoadProfile(hourlyRecord, indexRecord);
    }

}
