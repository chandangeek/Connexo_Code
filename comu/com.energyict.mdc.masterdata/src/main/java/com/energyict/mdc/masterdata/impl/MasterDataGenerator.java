package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates:
 * <ul>
 * <li>{@link Phenomenon Phenomena}</li>
 * <li>{@link com.energyict.mdc.masterdata.MeasurementType}s</li>
 * </ul>
 * based on the existing {@link com.elster.jupiter.metering.ReadingType}s
 * in Kore.
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/02/14
 * Time: 10:18
 */
public class MasterDataGenerator {

    private static final Logger LOGGER = Logger.getLogger(MasterDataGenerator.class.getName());

    static List<Phenomenon> generatePhenomena(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, MasterDataService masterDataService) {
        List<Phenomenon> phenomena = new ArrayList<>();
        Set<Unit> unitsToKeepTrack = new HashSet<>();
        for (ReadingType readingType : meteringService.getAvailableReadingTypes()) {
            try {
                ReadingTypeInformation readingTypeInformation = readingTypeUtilService.getReadingTypeInformationFor(readingType);
                Unit unit = readingTypeInformation.getUnit();
                if (!unitsToKeepTrack.contains(unit)) {
                    unitsToKeepTrack.add(unit);
                    Optional<Phenomenon> existingPhenomenon = masterDataService.findPhenomenonByNameAndUnit(readingType.getUnit().name(), unit);
                    if(existingPhenomenon.isPresent()){
                        phenomena.add(existingPhenomenon.get());
                    } else {
                        Phenomenon phenomenon = masterDataService.newPhenomenon(readingType.getUnit().name(), unit);
                        phenomenon.save();
                        phenomena.add(phenomenon);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return phenomena;
    }

    static List<MeasurementType> generateRegisterTypes(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, MasterDataService masterDataService) {
        List<MeasurementType> measurementTypes = new ArrayList<>();
        for (ReadingType readingType : meteringService.getAvailableReadingTypes()) {
            try {
                if (TimeAttribute.NOTAPPLICABLE.equals(readingType.getMeasuringPeriod()) && MacroPeriod.NOTAPPLICABLE.equals(readingType.getMacroPeriod())) {
                    Optional<MeasurementType> measurementTypeByReadingType = masterDataService.findMeasurementTypeByReadingType(readingType);
                    if (!measurementTypeByReadingType.isPresent()) {
                        ReadingTypeInformation readingTypeInformation = readingTypeUtilService.getReadingTypeInformationFor(readingType);
                        ObisCode obisCode = readingTypeInformation.getObisCode();
                        Unit unit = readingTypeInformation.getUnit();
                        int timeOfUse = readingType.getTou();
                        MeasurementType measurementType = masterDataService.newRegisterType(readingType.getName(), obisCode, unit, readingType, timeOfUse);
                        measurementType.save();
                        measurementTypes.add(measurementType);
                    } else {
                        measurementTypes.add(measurementTypeByReadingType.get());
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return measurementTypes;
    }

}