package com.energyict.mdc.protocol.inbound.mbus.factory.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.RegisterMapping;
import com.energyict.mdc.protocol.inbound.mbus.factory.UnitFactory;
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

    public RegisterFactory(Telegram telegram, InboundContext inboundContext, CollectedRegisterList collectedRegisterList) {
        super(telegram, inboundContext);
        this.collectedRegisterList = collectedRegisterList;
    }

    @Override
    public ObisCode getObisCode() {
        return null; // not applicable for registers
    }

    public void extractRegisters() {
        extractTelegramDateTime();

        getTelegram().getBody().getBodyPayload().getRecords()
                .forEach(this::extractRegister);
    }


    private void extractRegister(TelegramVariableDataRecord record) {
        if (record.getVif() != null) {
            Optional<RegisterMapping> dataMapping = RegisterMapping.getFor(record);
            if (dataMapping != null && dataMapping.isPresent()) {
                ObisCode obisCode = dataMapping.get().getObisCode();
                RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());

                String valueText = record.getDataField().getParsedValue();
                BigDecimal valueNumeric = BigDecimal.valueOf(Long.parseLong(valueText));

                Unit unit = UnitFactory.from(record, getInboundContext());

                Quantity quantity = new Quantity(valueNumeric, unit);

                CollectedRegister register = getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier);
                register.setCollectedData(quantity);
                register.setReadTime(Date.from(getTelegramDateTime()));
                if (!alreadyContainsObis(obisCode)) {
                    collectedRegisterList.addCollectedRegister(register);
                    getInboundContext().getLogger().info("[Rec: " + record + "] " + obisCode.toString() + " = " + valueNumeric + " (" + unit.toString() + ") - " + dataMapping.get().name());
                } else {
                    getInboundContext().getLogger().warn("[Rec: " + record + "] DUPLICATE identifiers matching " + obisCode.toString() + " = " + valueNumeric + " (" + unit.toString() + ")");
                }
            } else {
                getInboundContext().getLogger().info("[Rec: " + record + "] - Not Applicable to any mapping");
            }
        }
    }

    private boolean alreadyContainsObis(ObisCode obisCode) {
        if (collectedRegisterList == null) {
            return false;
        }

        return collectedRegisterList.getCollectedRegisters().stream()
                .filter(r -> r.getRegisterIdentifier() != null)
                .filter(r -> r.getRegisterIdentifier().getRegisterObisCode() != null)
                .anyMatch(r -> obisCode.equals(r.getRegisterIdentifier().getRegisterObisCode()));
    }
}
