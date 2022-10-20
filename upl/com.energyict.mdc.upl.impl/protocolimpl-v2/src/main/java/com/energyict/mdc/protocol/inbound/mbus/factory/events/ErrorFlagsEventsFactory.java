package com.energyict.mdc.protocol.inbound.mbus.factory.events;

import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.ErrorFlagsMapping;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramFunctionType;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ErrorFlagsEventsFactory extends AbstractMerlinFactory {

    public static final String VIF_SECOND_EXTENSION_TABLE = "fd";
    public static final int EVENT_FLAG_DIF = 0x03;

    public ErrorFlagsEventsFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    @Override
    public ObisCode getObisCode() {
        return ObisCode.fromString(STANDARD_LOGBOOK);
    }

    public TelegramEncoding applicableDataFieldEncoding() {
        return TelegramEncoding.ENCODING_INTEGER;
    }

    public TelegramFunctionType applicableFunctionType() {
        return TelegramFunctionType.INSTANTANEOUS_VALUE;
    }

    private int applicableDIF() {
        return EVENT_FLAG_DIF;
    }

    public boolean appliesFor(TelegramVariableDataRecord record) {
        try {
            return (applicableDataFieldEncoding().equals(record.getDif().getDataFieldEncoding())
                    && applicableFunctionType().equals(record.getDif().getFunctionType())
                    && VIF_SECOND_EXTENSION_TABLE.equalsIgnoreCase(record.getVif().getFieldPartsAsString())
                    && applicableDIF() == record.getDif().getFieldAsByteArray()[0]
            );
        } catch (Exception ex) {
            getInboundContext().getLogger().error("Error while checking applicability of error flags record", ex);
            ex.printStackTrace();
            return false;
        }

    }

    public CollectedLogBook extractEventsFromErrorFlags(TelegramVariableDataRecord eventsRecord) {
        List<String> parts = eventsRecord.getDataField().getFieldParts();

        if (parts == null || parts.size() != 3) {
            getInboundContext().getLogger().info("Invalid number of parts received for error flags extraction");
            return null;
        }

        LogBookIdentifier standardLogBook = new LogBookIdentifierByDeviceAndObisCode(getDeviceIdentifier(), getObisCode());
        CollectedLogBook errorFlagsEvents = getCollectedDataFactory().createCollectedLogBook(standardLogBook);

        extractTelegramDateTime();

        for (int efIndex = 0; efIndex < parts.size(); efIndex++) {
            byte efByte = Byte.parseByte(parts.get(efIndex), 16);
            final int finalEfIndex = efIndex + 1;
            List<MeterProtocolEvent> meterEvents = Arrays.stream(ErrorFlagsMapping.values())
                    .map(mapping -> extractMapping(finalEfIndex, mapping, efByte))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            errorFlagsEvents.addCollectedMeterEvents(meterEvents);

        }

        return errorFlagsEvents;
    }

    private MeterProtocolEvent extractMapping(int efIndex, ErrorFlagsMapping mapping, byte efByte) {
        if (efIndex != mapping.getEf()) {
            return null; // not our byte
        }

        byte mask = (byte) (1 << mapping.getBit());

        if ((efByte & mask) != mask) {
            return null; // bit not set
        }

        getInboundContext().getLogger().info("Error flag: " + mapping.getMessage());
        return createEvent(mapping.getEventCode(), mapping.getMessage());
    }


}
