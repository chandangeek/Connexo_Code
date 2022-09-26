package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;

public class RegisterFactory extends AbstractMerlinFactory {

    private CollectedRegisterList collectedRegisterList;

    public RegisterFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    @Override
    public ObisCode getObisCode() {
        return null; // not applicable for registers
    }

    public CollectedRegisterList extractRegisters() {
        this.collectedRegisterList = getCollectedDataFactory().createCollectedRegisterList(getDeviceIdentifier());

        extractTelegramDateTime();

        switch (getTelegram().getBody().getBodyPayload().getRecords().size()){
            case 13:
            case 14:
                // parse as Daily Data Packet Structure


            case 5:
                // parse as Weekly Data Packet Structure

            case 7:
                // parse as NRT Data Packet Structure


            default:
                // extract everything we can:
                getTelegram().getBody().getBodyPayload().getRecords()
                        .forEach(this::extractRegister);
        }


        return collectedRegisterList;
    }


    private void extractRegister(TelegramVariableDataRecord record) {
        if (record.getVif() != null) {
            Optional<DataMapping> dataMapping = DataMapping.getFor(record);
            if (dataMapping != null && dataMapping.isPresent()) {
                ObisCode obisCode = dataMapping.get().getObisCode();
                RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());

                String valueText = record.getDataField().getParsedValue();
                BigDecimal valueNumeric = BigDecimal.valueOf(Long.parseLong(valueText));

                Unit unit = UnitFactory.from(record, getInboundContext());

                Quantity quantity = new Quantity(valueNumeric, unit);

                CollectedRegister register = getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier);;
                register.setCollectedData(quantity);
                register.setReadTime(Date.from(getTelegramDateTime()));
                collectedRegisterList.addCollectedRegister(register);

                getInboundContext().getLogger().log(obisCode.toString() + " = " + valueNumeric + " (" + unit.toString() + ")");
            }
        }
    }
}
