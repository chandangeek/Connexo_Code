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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MerlinCollectedDataFactory {
    private final Telegram telegram;
    private final InboundContext inboundContext;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private CollectedRegisterList collectedRegisterList;
    private CollectedLoadProfile hourlyLoadProfile;
    private List<CollectedData> collectedDataList;
    //private FrameType frameType;
    private CollectedLoadProfile dailyLoadProfile;
    private ZoneId timeZone;
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

        collectedDataList = new ArrayList<>();

        try {
            extractRegisters();
        } catch (Exception ex) {
            inboundContext.getLogger().error("Exception while parsing registers", ex);
        }

        try {
            extractAllProfiles();
        } catch (Exception ex) {
            inboundContext.getLogger().error("Exception while parsing profiles", ex);
        }

        try {
            extractStatusEvents();
        } catch (Exception ex) {
            inboundContext.getLogger().error("Exception while parsing status events", ex);
        }

        try {
            extractErrorFlags();
        } catch (Exception ex) {
            inboundContext.getLogger().error("Exception while parsing error flags on daily frame", ex);
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

        telegram.getBody().getBodyPayload().getRecords().stream()
                .filter(errorFlagsEventsFactory::appliesFor)
                .forEach(errorFlagsEventsFactory::extractEventsFromErrorFlags);

    }

    private void extractRegisters() {
        RegisterFactory registerFactory = new RegisterFactory(telegram, inboundContext);
        this.collectedRegisterList = registerFactory.extractRegisters();
    }

    private void extractAllProfiles() {
        HourlyProfileFactory hourlyProfileFactory = new HourlyProfileFactory(telegram, inboundContext);
        DailyProfileFactory dailyProfileFactory = new DailyProfileFactory(telegram, inboundContext);

        // always the index record is before the profile record
        List<TelegramVariableDataRecord> records = telegram.getBody().getBodyPayload().getRecords();
        for(int i = 1; i < records.size() - 1; i++) {
            TelegramVariableDataRecord indexRecord = telegram.getBody().getBodyPayload().getRecords().get(i-1);
            TelegramVariableDataRecord profileRecord = telegram.getBody().getBodyPayload().getRecords().get(i);

            // check if can be decoded as hourly profile, the factory does the cross-checks
            try {
                CollectedLoadProfile tentative = hourlyProfileFactory.extractLoadProfile(profileRecord, indexRecord);
                if (tentative != null ) {
                    this.hourlyLoadProfile = tentative;
                }
            } catch (Exception ex) {
                inboundContext.getLogger().error("Exception while extracting hourly profile", ex);
            }

            // check if can be decoded as weekly profile
            try {
                CollectedLoadProfile tentative = dailyProfileFactory.extractLoadProfile(profileRecord, indexRecord);
                if (tentative != null) {
                    this.dailyLoadProfile = tentative;
                }
            } catch (Exception ex) {
                inboundContext.getLogger().error("Exception while extracting daily profile", ex);
            }
        }
    }


}
