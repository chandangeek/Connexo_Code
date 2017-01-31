/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterData;
import com.energyict.mdc.protocol.api.device.data.MeterDataMessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadProfileToRegisterParser {

    private static final ObisCode MBUS_VALUE_OBISCODE = ObisCode.fromString("0.x.24.2.1.255");

    public MessageResult parse(MessageResult messageResult) {
        List<RegisterValue> normalRegisters = new ArrayList<RegisterValue>();
        Map<String, Map<Date, MBusRegisterPair>> allMBusIntervalPairs = new HashMap<String, Map<Date, MBusRegisterPair>>();

        if (messageResult instanceof MeterDataMessageResult) {
            MeterReadingData meterReadingData = ((MeterDataMessageResult) messageResult).getMeterData().getMeterReadingData();
            for (RegisterValue registerValue : meterReadingData.getRegisterValues()) {
                if (MBUS_VALUE_OBISCODE.equalsIgnoreBChannel(registerValue.getObisCode())) {
                    Map<Date, MBusRegisterPair> intervalPairs = allMBusIntervalPairs.get(registerValue.getSerialNumber());
                    if (intervalPairs == null) {
                        intervalPairs = new HashMap<Date, MBusRegisterPair>();
                    }
                    MBusRegisterPair mBusRegisterPair = intervalPairs.get(registerValue.getToTime());  //Get the pair with the toTime of this interval
                    if (mBusRegisterPair == null) {
                        mBusRegisterPair = new MBusRegisterPair();
                    }
                    mBusRegisterPair.getRegisterValues().add(registerValue);
                    intervalPairs.put(registerValue.getToTime(), mBusRegisterPair);
                    allMBusIntervalPairs.put(registerValue.getSerialNumber(), intervalPairs);
                } else {
                    normalRegisters.add(registerValue);
                }
            }

            for (String serialNumber : allMBusIntervalPairs.keySet()) {
                Map<Date, MBusRegisterPair> intervalPairs = allMBusIntervalPairs.get(serialNumber);
                for (Date intervalTimestamp : intervalPairs.keySet()) {
                    MBusRegisterPair mbusRegisterPair = intervalPairs.get(intervalTimestamp);
                    if (mbusRegisterPair != null) {
                        if (mbusRegisterPair.getRegisterValues().size() == 2) {
                            RegisterValue result = null;
                            for (RegisterValue mbusRegister : mbusRegisterPair.getRegisterValues()) {
                                if (!isCaptureTime(mbusRegister)) {
                                    result = mbusRegister;            //Gas value register
                                    break;
                                }
                            }
                            if (result != null) {
                                for (RegisterValue mbusRegister : mbusRegisterPair.getRegisterValues()) {
                                    if (isCaptureTime(mbusRegister)) {                          //Capture time register
                                        Quantity quantity = mbusRegister.getQuantity();         //The timestamp
                                        Date eventTimestamp = new Date(quantity.longValue() * 1000);
                                        result = new RegisterValue(
                                                result.getObisCode(),
                                                result.getQuantity(),
                                                eventTimestamp,                        //The gas capture time, it was stored in a separate channel in the load profile
                                                result.getFromTime(),
                                                result.getToTime(),
                                                result.getReadTime(),
                                                result.getRegisterSpecId(),
                                                result.getText());
                                        normalRegisters.add(result);
                                        break;
                                    }
                                }
                            } else {
                                normalRegisters.addAll(mbusRegisterPair.getRegisterValues());
                            }
                        } else {
                            normalRegisters.addAll(mbusRegisterPair.getRegisterValues());
                        }
                    }
                }
            }

            MeterReadingData mrd = new MeterReadingData();
            for (RegisterValue register : normalRegisters) {
                mrd.add(register);
            }
            MeterData meterData = new MeterData();
            meterData.setMeterReadingData(mrd);
            if (messageResult.isFailed()) {
                return MeterDataMessageResult.createFailed(messageResult.getMessageEntry(), messageResult.getInfo(), meterData);
            } else if (messageResult.isSuccess()) {
                return MeterDataMessageResult.createSuccess(messageResult.getMessageEntry(), messageResult.getInfo(), meterData);
            } else {
                return MeterDataMessageResult.createUnknown(messageResult.getMessageEntry(), messageResult.getInfo(), meterData);
            }
        } else {
            return messageResult;
        }
    }

    /**
     * If the unit of this register is seconds, its the capture time register.
     * Else, it's a gas value
     */
    private boolean isCaptureTime(RegisterValue mbusRegister) {
        return mbusRegister.getQuantity().getUnit().equals(Unit.get(BaseUnit.SECOND));
    }

    private class MBusRegisterPair {

        private List<RegisterValue> registerValues;

        public MBusRegisterPair() {
            registerValues = new ArrayList<RegisterValue>();
        }

        public MBusRegisterPair(List<RegisterValue> registerValues) {
            this.registerValues = registerValues;
        }

        public List<RegisterValue> getRegisterValues() {
            if (registerValues == null) {
                registerValues = new ArrayList<RegisterValue>();
            }
            return registerValues;
        }
    }
}
