/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory.status;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.CellInfoMapping;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramFunctionType;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Arrays;

public class CellInfoFactory extends AbstractMerlinFactory {

    public static final int DIF_USER_DEFINED = 0x0F;

    /** Indexes in the User-Defined data identifier structure */
    private static final int IDX_CELL_ID_LOW            = 0;
    private static final int IDX_CELL_ID_HIGH           = 1;
    private static final int IDX_SIGNAL_STRENGTH        = 2;
    private static final int IDX_SIGNAL_QUALITY         = 3;
    private static final int IDX_TRANSMISSION_POWER     = 4;
    private static final int IDX_EXTENDED_CODE_COVERAGE = 5;
    private static final int IDX_CELL_ID_ASCII_1        = 6;
    private static final int IDX_CELL_ID_ASCII_2        = 7;
    private static final int IDX_CELL_ID_ASCII_3        = 8;
    private static final int IDX_CELL_ID_ASCII_4        = 9;
    private static final int IDX_RELEASE_ASSIST_ENABLE  = 10;
    private static final int IDX_PAIRED_METER_ID        = 11;

    public CellInfoFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    @Override
    public ObisCode getObisCode() {
        return null; // not applicable
    }

    public TelegramFunctionType applicableFunctionType() {
        return TelegramFunctionType.USER_DEFINED_CELL_ID;
    }

    public int applicableDIF() {
        return DIF_USER_DEFINED;
    }

    public TelegramEncoding applicableDataFieldEncoding() {
        return TelegramEncoding.ENCODING_USER_DEFINED_CELL_ID;
    }

    public boolean appliesFor(TelegramVariableDataRecord record) {
        try {
            return (applicableDataFieldEncoding().equals(record.getDif().getDataFieldEncoding())
                    && applicableFunctionType().equals(record.getDif().getFunctionType())
                    && applicableDIF() == record.getDif().getFieldAsByteArray()[0]
            );
        } catch (Exception ex) {
            getInboundContext().getLogger().error("Error while checking applicability of cell info record", ex);
            ex.printStackTrace();
            return false;
        }
    }

    public void extractCellInformation(TelegramVariableDataRecord record, CollectedRegisterList collectedRegisterList) {
        byte[] data = record.getDataField().getFieldAsByteArray();

        Arrays.stream(CellInfoMapping.values())
                .forEach(m -> {
                    CollectedRegister r = createRegister(data, m);
                    collectedRegisterList.addCollectedRegister(r);
                } );
    }

    private CollectedRegister createRegister(byte[] data, CellInfoMapping registerMapping) {
        ObisCode obisCode = registerMapping.getObisCode();
        RegisterIdentifier registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier());

        Unit unit = Unit.getUndefined();
        CollectedRegister register;

        Object value = registerMapping.extractValue(data);

        if (value instanceof Integer) {
            register = getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier);
            BigDecimal valueNumeric = BigDecimal.valueOf((Integer) value);
            Quantity quantity = new Quantity(valueNumeric, unit);
            register.setCollectedData(quantity);

            getInboundContext().getLogger().info("CellInfo/" +  registerMapping.name() + "=" + valueNumeric);
        } else if (value instanceof String) {
            register = getCollectedDataFactory().createTextCollectedRegister(registerIdentifier);

            String valueText = (String) value;
            register.setCollectedData(valueText);

            getInboundContext().getLogger().info("CellInfo/" + registerMapping.name() + "=" + valueText);
        } else {
            register = getCollectedDataFactory().createTextCollectedRegister(registerIdentifier);

            String valueText = ProtocolTools.bytesToHex((byte[]) value);
            register.setCollectedData(valueText);

            getInboundContext().getLogger().info("CellInfo/" + registerMapping.name() + "=" + valueText);
        }

        register.setReadTime(Date.from(getTelegramDateTime()));

        return register;
    }

    public static int extractCellId(byte[] data) {
        return (data[IDX_CELL_ID_HIGH] & 0xFF) * 0x100 + (data[IDX_CELL_ID_LOW] & 0xFF);
    }

    public static int extractSignalStrength(byte[] data) {
        return data[IDX_SIGNAL_STRENGTH] & 0xFF;
    }

    public static int extractSignalQuality(byte[] data) {
        return data[IDX_SIGNAL_QUALITY] & 0xFF;
    }

    public static int extractTransmissionPower(byte[] data) {
        return data[IDX_TRANSMISSION_POWER] & 0xFF;
    }

    public static int extractExtendedCodeCoverage(byte[] data) {
        return data[IDX_EXTENDED_CODE_COVERAGE] & 0xFF;
    }

    public static int extractAccumulatedTxTime(byte[] data) {
        return 0; //return data[IDX_ACCUMULATED_TX_TIME] & 0xFF;
    }

    public static int extractAccumulatedRxTime(byte[] data) {
        return 0; //data[IDX_ACCUMULATED_RX_TIME] & 0xFF;
    }

    public static int extractReleaseAssistEnable(byte[] data) {
        return 0; //return data[IDX_RELEASE_ASSIST_ENABLE] & 0xFF;
    }

    public static String extractPairedMeterId(byte[] data) {
        int len = Math.min(14, data.length - IDX_PAIRED_METER_ID ) ;
        byte[] meteId = new byte[len];
        int j = 0;

        for (int i = IDX_PAIRED_METER_ID + len - 1; i >= IDX_PAIRED_METER_ID; i--){
            meteId[j++] = data[i];
        }

        return reverse(ProtocolTools.getAsciiFromBytes(meteId));
    }

    public static String reverse(String original) {
        return new StringBuilder(original).reverse().toString();
    }


}
