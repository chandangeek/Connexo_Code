package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MerlinCollectedDataFactory {
    private final Telegram telegram;
    private final CollectedDataFactory collectedDataFactory;
    private final InboundContext inboundContext;
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private CollectedRegisterList collectedRegisterList;
    private Instant telegramDateTime;

    public MerlinCollectedDataFactory(Telegram telegram, InboundContext inboundContext) {
        this.telegram = telegram;
        this.inboundContext = inboundContext;
        this.collectedDataFactory = inboundContext.getInboundDiscoveryContext().getCollectedDataFactory();
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (this.deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(telegram.getSerialNr());
        }
        return this.deviceIdentifier;
    }

    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDataList = new ArrayList<>();

        this.collectedRegisterList = collectedDataFactory.createCollectedRegisterList(getDeviceIdentifier());

        extractTelegramDateTime();

        telegram.getBody().getBodyPayload().getRecords()
                .forEach(this::extractRegister);

        collectedDataList.add(collectedRegisterList);


        return collectedDataList;
    }

    private void extractTelegramDateTime() {
        if (telegram.getBody().getBodyPayload().getRecords().size() >= 2){
            String dateTime = telegram.getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue();
            this.telegramDateTime = Instant.parse(dateTime);
        } else {
            this.telegramDateTime = Instant.now();
        }
    }


    private void extractRegister(TelegramVariableDataRecord record) {
        if (record.getVif() != null) {
            Optional<DataMapping> dataMapping = DataMapping.getFor(record);
            if (dataMapping != null && dataMapping.isPresent()) {
                ObisCode  obisCode = dataMapping.get().getObisCode();
                RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());

                String valueText = record.getDataField().getParsedValue();
                BigDecimal valueNumeric = BigDecimal.valueOf(Long.parseLong(valueText));

                String unitName = record.getVif().getmUnit().getValue();
                int unitScale = record.getVif().getMultiplier();
                Unit unit = Unit.get(unitName, unitScale) ;

                Quantity quantity = new Quantity(valueNumeric, unit);

                CollectedRegister register = collectedDataFactory.createDefaultCollectedRegister(registerIdentifier);;
                register.setCollectedData(quantity);
                register.setReadTime(Date.from(telegramDateTime));
                collectedRegisterList.addCollectedRegister(register);
            }
        }
    }
}
