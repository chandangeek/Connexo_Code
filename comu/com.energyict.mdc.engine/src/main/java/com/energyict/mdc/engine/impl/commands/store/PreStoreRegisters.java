package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/4/14
 * Time: 8:24 AM
 */
public class PreStoreRegisters {

    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final ComServerDAO comServerDAO;

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
     * @return the preStored LoadProfile
     */
    public List<Reading> preStore(CollectedRegisterList collectedRegisterList) {
        List<Reading> processedReadings = new ArrayList<>();
        for (CollectedRegister collectedRegister : collectedRegisterList.getCollectedRegisters()) {
            Reading reading = MeterDataFactory.createReadingForDeviceRegisterAndObisCode(collectedRegister);
            if(!collectedRegister.isTextRegister()){
                Unit configuredUnit = this.mdcReadingTypeUtilService.getReadingTypeInformationFor(collectedRegister.getReadingType()).getUnit();
                int scaler = getScaler(collectedRegister.getCollectedQuantity().getUnit(), configuredUnit);
                OfflineRegister offlineRegister = comServerDAO.findOfflineRegister(collectedRegister.getRegisterIdentifier());
                BigDecimal overflow = offlineRegister.getOverFlowValue();
                Reading scaledReading = getScaledReading(scaler, reading);
                Reading overflowCheckedReading = getOverflowCheckedReading(overflow, scaledReading);
                processedReadings.add(overflowCheckedReading);
            } else {
                processedReadings.add(reading);
            }
        }
        return processedReadings;
    }

    private Reading getOverflowCheckedReading(BigDecimal overflow, Reading scaledReading) {
        if (scaledReading.getValue().compareTo(overflow) > 0) {
            return new ReadingImpl(scaledReading.getReadingTypeCode(), scaledReading.getValue().subtract(overflow), scaledReading.getTimeStamp());
        }
        return scaledReading;
    }

    private Reading getScaledReading(int scaler, Reading reading) {
        if (scaler == 0) {
            return reading;
        } else {
            BigDecimal scaledValue = reading.getValue().scaleByPowerOfTen(scaler);
            return new ReadingImpl(reading.getReadingTypeCode(), scaledValue, reading.getTimeStamp());
        }
    }

    private int getScaler(Unit fromUnit, Unit toUnit) {
        if (fromUnit.equalBaseUnit(toUnit)) {
            return fromUnit.getScale() - toUnit.getScale();
        } else {
            return 0;
        }
    }
}
