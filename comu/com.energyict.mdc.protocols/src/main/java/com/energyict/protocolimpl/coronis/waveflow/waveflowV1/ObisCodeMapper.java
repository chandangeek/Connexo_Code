/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.waveflowV1;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.ExtendedIndexReading;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    static {
        // specific waveflow registers
        registerMaps.put(ObisCode.fromString("1.1.82.8.0.255"), "Input A index");
        registerMaps.put(ObisCode.fromString("1.2.82.8.0.255"), "Input B index");
        registerMaps.put(ObisCode.fromString("1.3.82.8.0.255"), "Input C index");
        registerMaps.put(ObisCode.fromString("1.4.82.8.0.255"), "Input D index");

        registerMaps.put(ObisCode.fromString("1.1.82.8.0.0"), "Last Billing period index for input A");
        registerMaps.put(ObisCode.fromString("1.2.82.8.0.0"), "Last Billing period index for input B");
        registerMaps.put(ObisCode.fromString("1.3.82.8.0.0"), "Last Billing period index for input C");
        registerMaps.put(ObisCode.fromString("1.4.82.8.0.0"), "Last Billing period index for input D");

        registerMaps.put(CommonObisCodeMapper.OBISCODE_RELAYED_FRAMES, "Number of relayed frames and alarm frames transmitted");     //Waveflow V1 433 MHz repeaters only
    }

    private WaveFlowV1 waveFlowV1;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveFlowV1 the protocol
     */
    public ObisCodeMapper(final WaveFlowV1 waveFlowV1) {
        this.waveFlowV1 = waveFlowV1;
    }

    final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveFlowV1.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        strBuilder.append(waveFlowV1.getCommonObisCodeMapper().getRegisterExtendedLogging());
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

            //Uses the pulse weight to convert the received pulse amount in liters
            if (isCurrentIndexReading(obisCode)) {
                int channel = obisCode.getB() - 1;
                if (channel > (waveFlowV1.getNumberOfChannels() - 1)) {
                    throw new NoSuchRegisterException("This channel is not supported");
                }
                PulseWeight pulseWeight = waveFlowV1.getPulseWeight(channel);
                BigDecimal currentIndexValue = new BigDecimal(pulseWeight.getWeight() * waveFlowV1.getRadioCommandFactory().readCurrentReading().getReadings()[channel]);
                return new RegisterValue(obisCode, new Quantity(currentIndexValue, pulseWeight.getUnit()), new Date());

                // Billing data request for inputs A ... D
            } else if (isLastBillingPeriodIndexReading(obisCode)) {
                int channel = obisCode.getB() - 1;
                PulseWeight pulseWeight = waveFlowV1.getPulseWeight(channel);
                ExtendedIndexReading extendedIndexReadingConfiguration = waveFlowV1.getRadioCommandFactory().readExtendedIndexConfiguration();
                if (channel > (waveFlowV1.getNumberOfChannels() - 1)) {
                    throw new NoSuchRegisterException("No billing data available this channel");
                }
                int value = extendedIndexReadingConfiguration.getIndexOfLastMonth(channel);
                if (value == -1) {
                    waveFlowV1.getLogger().log(Level.WARNING, "No billing data available yet, values are 0xFFFFFFFF");
                    throw new NoSuchRegisterException("No billing data available yet");
                }
                BigDecimal lastMonthsIndexValue = new BigDecimal(pulseWeight.getWeight() * value);
                Date toDate = extendedIndexReadingConfiguration.getDateOfLastMonthsEnd();
                return new RegisterValue(obisCode, new Quantity(lastMonthsIndexValue, pulseWeight.getUnit()), toDate, toDate);
            } else if (CommonObisCodeMapper.OBISCODE_RELAYED_FRAMES.equals(obisCode)) {
                int value = waveFlowV1.getParameterFactory().readNumberOfRelayedFramesV1() * 256;
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            }
            // Other cases
            else {
                return waveFlowV1.getCommonObisCodeMapper().getRegisterValue(obisCode);
            }

        } catch (IOException e) {
            if (!(e instanceof NoSuchRegisterException)) {
                waveFlowV1.getLogger().log(Level.SEVERE, "Error getting [" + obisCode + "]: timeout, " + e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Checks if the obis code is of the form 1.b.82.8.0.f       (indicates an input pulse channel)
     * Where b = 1, 2, 3 or 4 and f = 0 or 255.
     *
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

    private boolean isLastBillingPeriodIndexReading(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 0);
    }
}
