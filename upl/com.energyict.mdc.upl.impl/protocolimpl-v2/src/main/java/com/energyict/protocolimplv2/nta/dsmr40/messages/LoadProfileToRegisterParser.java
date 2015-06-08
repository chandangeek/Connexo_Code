package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedRegisterList;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;

import java.util.*;

/**
 * @author sva
 * @since 7/01/2015 - 11:43
 */
public class LoadProfileToRegisterParser {

    private static final ObisCode MBUS_VALUE_OBISCODE = ObisCode.fromString("0.x.24.2.1.255");

    public CollectedMessage parse(CollectedMessage collectedMessage) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        Map<RegisterIdentifier, Map<Date, MBusRegisterPair>> allMBusIntervalPairs = new HashMap<>();
        if (collectedMessage != null && collectedMessage instanceof CollectedRegisterList) {
            CollectedRegisterList collectedRegisterList = (CollectedRegisterList) collectedMessage;
            for (CollectedRegister collectedRegister : collectedRegisterList.getCollectedRegisters()) {
                RegisterIdentifier registerIdentifier = collectedRegister.getRegisterIdentifier();
                if (MBUS_VALUE_OBISCODE.equalsIgnoreBChannel(registerIdentifier.getRegisterObisCode())) {
                    Map<Date, MBusRegisterPair> intervalPairs = allMBusIntervalPairs.get(registerIdentifier);
                    if (intervalPairs == null) {
                        intervalPairs = new HashMap<Date, MBusRegisterPair>();
                    }
                    MBusRegisterPair mBusRegisterPair = intervalPairs.get(collectedRegister.getToTime());  //Get the pair with the toTime of this interval
                    if (mBusRegisterPair == null) {
                        mBusRegisterPair = new MBusRegisterPair();
                    }
                    mBusRegisterPair.getCollectedRegisters().add(collectedRegister);
                    intervalPairs.put(collectedRegister.getToTime(), mBusRegisterPair);
                    allMBusIntervalPairs.put(registerIdentifier, intervalPairs);
                } else {
                    collectedRegisters.add(collectedRegister); // no conversion needed
                }
            }

            for (RegisterIdentifier registerIdentifier : allMBusIntervalPairs.keySet()) {
                Map<Date, MBusRegisterPair> intervalPairs = allMBusIntervalPairs.get(registerIdentifier);
                for (Date intervalTimestamp : intervalPairs.keySet()) {
                    MBusRegisterPair mbusRegisterPair = intervalPairs.get(intervalTimestamp);
                    if (mbusRegisterPair != null) {
                        if (mbusRegisterPair.getCollectedRegisters().size() == 2) {
                            CollectedRegister result = null;
                            for (CollectedRegister mbusRegister : mbusRegisterPair.getCollectedRegisters()) {
                                if (!isCaptureTime(mbusRegister)) {
                                    result = mbusRegister;            //Gas value register
                                    break;
                                }
                            }
                            if (result != null) {
                                for (CollectedRegister mbusRegister : mbusRegisterPair.getCollectedRegisters()) {
                                    if (isCaptureTime(mbusRegister)) {                              //Capture time register
                                        Quantity quantity = mbusRegister.getCollectedQuantity();    //The timestamp
                                        if (quantity != null) {
                                            Date eventTimestamp = new Date(quantity.longValue() * 1000);
                                            result.setCollectedTimeStamps(
                                                    result.getReadTime(),
                                                    result.getFromTime(),
                                                    result.getToTime(),
                                                    eventTimestamp          //The gas capture time, it was stored in a separate channel in the load profile
                                            );
                                            collectedRegisters.add(result);
                                            break;
                                        } else { // In case quantity of capture time register is null, then don't combine
                                            collectedRegisters.addAll(mbusRegisterPair.getCollectedRegisters());
                                            break;
                                        }
                                    }
                                }
                            } else {
                                collectedRegisters.addAll(mbusRegisterPair.getCollectedRegisters());
                            }
                        } else {
                            collectedRegisters.addAll(mbusRegisterPair.getCollectedRegisters());
                        }
                    }
                }
            }

            collectedRegisterList.getCollectedRegisters().clear();                       // Remove all old CollectedRegister data
            collectedRegisterList.getCollectedRegisters().addAll(collectedRegisters);    // Re-add the updated list of CollectedRegister data
        }

        return collectedMessage;
    }

    /**
     * If the unit of this register is seconds, its the capture time register.
     * Else, it's a gas value
     */
    private boolean isCaptureTime(CollectedRegister mbusRegister) {
        return mbusRegister.getCollectedQuantity().getUnit().equals(Unit.get(BaseUnit.SECOND));
    }

    private class MBusRegisterPair {

        private List<CollectedRegister> collectedRegisters;

        public MBusRegisterPair() {
        }

        public MBusRegisterPair(List<CollectedRegister> collectedRegisters) {
            this.collectedRegisters = collectedRegisters;
        }

        public List<CollectedRegister> getCollectedRegisters() {
            if (collectedRegisters == null) {
                collectedRegisters = new ArrayList<CollectedRegister>();
            }
            return collectedRegisters;
        }
    }
}