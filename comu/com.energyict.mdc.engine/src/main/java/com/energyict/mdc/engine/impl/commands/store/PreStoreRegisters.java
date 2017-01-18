package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.cbo.Unit;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;

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
     * </ul>
     *
     * @param collectedRegisterList the collected data from a registers to (pre)Store
     * @return the preStored registers
     */
    public Map<DeviceIdentifier, List<Reading>> preStore(CollectedRegisterList collectedRegisterList) {
        this.unknownRegisters = new ArrayList<>();
        Map<DeviceIdentifier, List<Reading>> processedReadings = new HashMap<>();
        for (CollectedRegister collectedRegister : collectedRegisterList.getCollectedRegisters()) {
            if (collectedRegister.getResultType().equals(ResultType.Supported)) {
                Optional<OfflineRegister> offlineRegister = this.comServerDAO.findOfflineRegister(collectedRegister.getRegisterIdentifier(), collectedRegister.getReadTime().toInstant());
                DeviceIdentifier deviceIdentifier = offlineRegister.get().getDeviceIdentifier();
                if (offlineRegister.isPresent()) {
                    String readingTypeMRID = offlineRegister.get().getReadingTypeMRID();
                    Reading reading = MeterDataFactory.createReadingForDeviceRegisterAndObisCode(collectedRegister, readingTypeMRID);
                    if (!collectedRegister.isTextRegister() && collectedRegister.getCollectedQuantity() != null) {
                        Unit configuredUnit = this.mdcReadingTypeUtilService.getMdcUnitFor(readingTypeMRID);
                        int scaler = getScaler(collectedRegister.getCollectedQuantity().getUnit(), configuredUnit);
                        Reading scaledReading = getScaledReading(scaler, reading);
                        addProcessedReadingFor(processedReadings, deviceIdentifier, scaledReading);
                    } else {
                        addProcessedReadingFor(processedReadings, deviceIdentifier, reading);
                    }
                } else {
                    this.unknownRegisters.add(collectedRegister.getRegisterIdentifier());
                }
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
