package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MerlinCollectedDataFactory {
    private final Telegram telegram;
    private final InboundContext inboundContext;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private CollectedRegisterList collectedRegisterList;
    private CollectedLoadProfile hourlyLoadProfile;
    private List<CollectedData> collectedDataList;

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
            inboundContext.getLogger().logE("Exception while parsing registers", ex);
        }

        try {
            extractHourlyProfile();
        } catch (Exception ex) {
            inboundContext.getLogger().logE("Exception while parsing hourly profile", ex);
        }

        if (collectedRegisterList != null ) {
            collectedDataList.add(collectedRegisterList);
        }

        if (hourlyLoadProfile != null) {
            collectedDataList.add(hourlyLoadProfile);
        }

        return collectedDataList;
    }

    private void extractRegisters() {
        RegisterFactory registerFactory = new RegisterFactory(telegram, inboundContext);
        this.collectedRegisterList = registerFactory.extractRegisters();
    }

    private void extractHourlyProfile() {
        HourlyProfileFactory hourlyProfileFactory = new HourlyProfileFactory(telegram, inboundContext);

        if (telegram.getBody().getBodyPayload().getRecords().size() >= 13) { // TODO -> find a proper method
            TelegramVariableDataRecord indexRecord = telegram.getBody().getBodyPayload().getRecords().get(3);
            TelegramVariableDataRecord hourlyRecord = telegram.getBody().getBodyPayload().getRecords().get(4);

            this.hourlyLoadProfile = hourlyProfileFactory.extractLoadProfile(hourlyRecord, indexRecord);

        }

    }


}
