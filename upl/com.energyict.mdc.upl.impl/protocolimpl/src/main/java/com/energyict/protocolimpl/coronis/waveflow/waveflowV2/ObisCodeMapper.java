package com.energyict.protocolimpl.coronis.waveflow.waveflowV2;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.DailyConsumption;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.ExtendedIndexReading;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    static {
        // specific waveflow registers
        registerMaps.put(ObisCode.fromString("1.1.82.8.0.255"), "Input A index");
        registerMaps.put(ObisCode.fromString("1.2.82.8.0.255"), "Input B index");
        registerMaps.put(ObisCode.fromString("1.3.82.8.0.255"), "Input C index");
        registerMaps.put(ObisCode.fromString("1.4.82.8.0.255"), "Input D index");

        //Monthly billing registers
        registerMaps.put(ObisCode.fromString("1.1.82.8.0.0"), "Last Billing period (month) index for input A");
        registerMaps.put(ObisCode.fromString("1.2.82.8.0.0"), "Last Billing period (month) index for input B");
        registerMaps.put(ObisCode.fromString("1.3.82.8.0.0"), "Last Billing period (month) index for input C");
        registerMaps.put(ObisCode.fromString("1.4.82.8.0.0"), "Last Billing period (month) index for input D");

        //Daily billing registers
        registerMaps.put(ObisCode.fromString("1.1.82.8.0.1"), "Last Billing period (day) index for input A");
        registerMaps.put(ObisCode.fromString("1.2.82.8.0.1"), "Last Billing period (day) index for input B");
        registerMaps.put(ObisCode.fromString("1.3.82.8.0.1"), "Last Billing period (day) index for input C");
        registerMaps.put(ObisCode.fromString("1.4.82.8.0.1"), "Last Billing period (day) index for input D");
    }

    private WaveFlowV2 waveFlowV2;

    /**
     * Creates a new instance ofObisCodeMapper
     * @param waveFlowV2 the protocol
     */
    public ObisCodeMapper(final WaveFlowV2 waveFlowV2) {
        this.waveFlowV2 = waveFlowV2;
    }

    final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveFlowV2.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        strBuilder.append(waveFlowV2.getCommonObisCodeMapper().getRegisterExtendedLogging());
        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        if (info != null) {
            return new RegisterInfo(info);
        } else {
            return CommonObisCodeMapper.getRegisterInfo(obisCode);
        }
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        try {

             // Index request for inputs A..D  (FieldA = 1 (electricity) because there's no pulse counter yet for water meters in the blue book...)
            if (isCurrentIndexReading(obisCode)) {
                int channel = obisCode.getB() - 1;
                PulseWeight pulseWeight = waveFlowV2.getParameterFactory().readPulseWeight(channel + 1);
                BigDecimal currentIndexValue = new BigDecimal(pulseWeight.getWeight() * waveFlowV2.getRadioCommandFactory().readCurrentReading().getReadings()[channel]);
                return new RegisterValue(obisCode, new Quantity(currentIndexValue, pulseWeight.getUnit()), new Date());

            // Billing data request for inputs A ... D
            } else if (isLastBillingPeriodIndexReadingForMonth(obisCode)) {
                int channel = obisCode.getB() - 1;
                PulseWeight pulseWeight = waveFlowV2.getParameterFactory().readPulseWeight(channel + 1);
                ExtendedIndexReading extendedIndexReadingConfiguration = waveFlowV2.getRadioCommandFactory().readExtendedIndexConfiguration();
                int value = extendedIndexReadingConfiguration.getIndexOfLastMonth(channel);
                if (value == -1) {
                    throw new WaveFlowException("No billing data available yet");
                }
                BigDecimal lastMonthsIndexValue = new BigDecimal(pulseWeight.getWeight() * value);
                Date toDate = extendedIndexReadingConfiguration.getDateOfLastMonthsEnd();
                return new RegisterValue(obisCode, new Quantity(lastMonthsIndexValue, pulseWeight.getUnit()), toDate, toDate);
            } else if (isLastBillingPeriodIndexReadingForDay(obisCode)) {
                int channel = obisCode.getB() - 1;
                PulseWeight pulseWeight = waveFlowV2.getParameterFactory().readPulseWeight(channel + 1);
                DailyConsumption consumption = waveFlowV2.getRadioCommandFactory().readDailyConsumption();
                int value = consumption.getIndexZone().getDailyIndex(channel);
                if (value == -1) {
                    throw new WaveFlowException("No billing data available yet");
                }
                BigDecimal lastMonthsIndexValue = new BigDecimal(pulseWeight.getWeight() * value);
                Date toDate = consumption.getIndexZone().getLastDailyLoggedIndex();
                return new RegisterValue(obisCode, new Quantity(lastMonthsIndexValue, pulseWeight.getUnit()), toDate, toDate);
            }

            // Other cases
            else {
                return waveFlowV2.getCommonObisCodeMapper().getRegisterValue(obisCode);
            }
            
        } catch (IOException e) {
            throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] has an error [" + e.getMessage() + "]!");
        }
    }

    /**
     * Checks if the obis code is of the form 1.b.82.8.0.f       (indicates an input pulse channel)
     * Where b = 1, 2, 3 or 4 and f = 0 or 255.
     * @param obisCode the obis code
     * @return true or false
     */
    private boolean isInputPulseRegister(ObisCode obisCode) {
        return ((obisCode.getA() == 1) &&
                ((obisCode.getB() < 5) && (obisCode.getB()) > 0) &&
                (obisCode.getC() == 82) &&
                (obisCode.getD() == 8) &&
                (obisCode.getE() == 0));
    }

    private boolean isCurrentIndexReading(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 255);
    }

    private boolean isLastBillingPeriodIndexReadingForMonth(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 0);
    }

    private boolean isLastBillingPeriodIndexReadingForDay(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 1);
    }
}
