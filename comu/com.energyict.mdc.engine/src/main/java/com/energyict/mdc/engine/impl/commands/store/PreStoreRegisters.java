package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 8/4/14
 * Time: 8:24 AM
 */
public class PreStoreRegisters {

    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final ComServerDAO comServerDAO;
    private List<RegisterIdentifier> unknownRegisters;

    public PreStoreRegisters(MdcReadingTypeUtilService mdcReadingTypeUtilService, ComServerDAO comServerDAO) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        this.comServerDAO = comServerDAO;
    }

    /**
     * Tasks:
     * <ul>
     * <li>Scale value according to unit</li>
     * <li>OverFlow calculation</li>
     * </ul>
     *
     * @param collectedRegisterList the collected data from a registers to (pre)Store
     * @return the preStored registers
     */
    public Map<DeviceIdentifier, List<Reading>> preStore(CollectedRegisterList collectedRegisterList) {
        this.unknownRegisters = new ArrayList<>();
        Map<DeviceIdentifier, List<Reading>> processedReadings = new HashMap<>();
        for (CollectedRegister collectedRegister : collectedRegisterList.getCollectedRegisters()) {
            Optional<OfflineRegister> offlineRegister = this.comServerDAO.findOfflineRegister(collectedRegister.getRegisterIdentifier());
            DeviceIdentifier deviceIdentifier = collectedRegister.getRegisterIdentifier().getDeviceIdentifier();
            if (offlineRegister.isPresent()) {
                Reading reading = MeterDataFactory.createReadingForDeviceRegisterAndObisCode(collectedRegister);
                if (!collectedRegister.isTextRegister() && collectedRegister.getCollectedQuantity() != null) {
                    Unit configuredUnit = this.mdcReadingTypeUtilService.getMdcUnitFor(collectedRegister.getReadingType().getMRID());
                    int scaler = getScaler(collectedRegister.getCollectedQuantity().getUnit(), configuredUnit);
                    BigDecimal overflow = offlineRegister.get().getOverFlowValue();
                    Reading scaledReading = getScaledReading(scaler, reading);
                    Reading overflowCheckedReading = getOverflowCheckedReading(overflow, scaledReading);
                    addProcessedReadingFor(processedReadings, deviceIdentifier, overflowCheckedReading);
                } else {
                    addProcessedReadingFor(processedReadings, deviceIdentifier, reading);
                }
            }
            else {
                this.unknownRegisters.add(collectedRegister.getRegisterIdentifier());
            }
        }
        return processedReadings;
    }

    public List<RegisterIdentifier> getUnknownRegisters() {
        return Collections.unmodifiableList(this.unknownRegisters);
    }

    private boolean addProcessedReadingFor(Map<DeviceIdentifier, List<Reading>> allProcessedReadings, DeviceIdentifier deviceIdentifier, Reading newReading) {
        if(!allProcessedReadings.containsKey(deviceIdentifier)){
            allProcessedReadings.put(deviceIdentifier, new ArrayList<>());
        }
        return allProcessedReadings.get(deviceIdentifier).add(newReading);
    }

    private Reading getOverflowCheckedReading(BigDecimal overflow, Reading scaledReading) {
        if (scaledReading.getValue().compareTo(overflow) > 0) {
            return ReadingImpl.of(scaledReading.getReadingTypeCode(), scaledReading.getValue().subtract(overflow), scaledReading.getTimeStamp());
        }
        return scaledReading;
    }

    private Reading getScaledReading(int scaler, Reading reading) {
        if (scaler == 0) {
            return reading;
        } else {
            BigDecimal scaledValue = reading.getValue().scaleByPowerOfTen(scaler);
            return ReadingImpl.of(reading.getReadingTypeCode(), scaledValue, reading.getTimeStamp());
        }
    }

    private int getScaler(Unit fromUnit, Unit toUnit) {
        if (fromUnit != null && toUnit != null && fromUnit.equalBaseUnit(toUnit)) {
            return fromUnit.getScale() - toUnit.getScale();
        } else {
            return 0;
        }
    }
}
