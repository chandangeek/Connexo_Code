package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class ObisCodeMapperHydreka {

    private static final ObisCode OBISCODE_RTC_RESYNCH_PERIOD = ObisCode.fromString("0.0.96.0.112.255");
    private static final ObisCode OBISCODE_READING_HOUR_LEAKAGE_STATUS = ObisCode.fromString("0.0.96.0.113.255");
    private static final ObisCode OBISCODE_READING_HOUR_HISTOGRAM = ObisCode.fromString("0.0.96.0.114.255");
    public static final ObisCode OBISCODE_DAILY_HYDREKA_DATA = ObisCode.fromString("0.0.96.0.115.255");
    private Hydreka waveFlow;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveFlow the protocol
     */
    public ObisCodeMapperHydreka(final Hydreka waveFlow) {
        this.waveFlow = waveFlow;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (obisCode.equals(OBISCODE_RTC_RESYNCH_PERIOD)) {
            return new RegisterValue(obisCode, new Quantity(waveFlow.getParameterFactory().readRTCResynchPeriod(), Unit.get(BaseUnit.HOUR)));
        } else if (obisCode.equals(OBISCODE_READING_HOUR_LEAKAGE_STATUS)) {
            return new RegisterValue(obisCode, new Quantity(waveFlow.getParameterFactory().readReadingHourLeakageStatus(), Unit.get(BaseUnit.HOUR)));
        } else if (obisCode.equals(OBISCODE_READING_HOUR_HISTOGRAM)) {
            return new RegisterValue(obisCode, new Quantity(waveFlow.getParameterFactory().readReadingHourHistogram(), Unit.get(BaseUnit.HOUR)));
        } else if (obisCode.equals(OBISCODE_DAILY_HYDREKA_DATA)) {
            byte[] rawData = waveFlow.getRadioCommandFactory().readDailyHydrekaDataReading().getRawData();
            return new RegisterValue(obisCode, ProtocolTools.getHexStringFromBytes(rawData, ""));
        }
        return waveFlow.getCommonObisCodeMapper().getRegisterValue(obisCode);
    }
}