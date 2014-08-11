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

    private static final int NAME_INDEX = 0;
    private static final int UNIT_INDEX = 1;
    private static final Logger LOGGER = Logger.getLogger(MasterDataGenerator.class.getName());
    private static final String[] scalers = new String[]{"m", "", "k", "M", "G"};
    private static final String[][] phenomenaListWithScalers = new String[][]{
            {"Active Energy", "Wh"},
            {"Active Power", "W"},
            {"Apparent Energy", "VAh"},
            {"Apparent Power", "VA"},
            {"Capacitive Power", "var"},
            {"Capacitive Energy", "varh"},
            {"Capacitive Power", "var"},
            {"Current", "A"},
            {"Energy", "Wh"},
            {"Energy", "J"},
            {"Gas Consumption", "Wh"},
            {"Heat", "Wh"},
            {"Inductive Energy", "varh"},
            {"Inductive Power", "var"},
            {"Reactive Energy", "varh"},
            {"Reactive Power", "var"},
            {"Voltage", "V"}
    };
    private static final String[][] phenomenaListWithoutScalers = new String[][]{
            {"Temperature","\u00B0C"},
            {"Temperature", "K"},
            {"Pressure", "bar"},
            {"Gas Flow", "m3/h"},
            {"Gas Normalized Flow", "Nm3/h"},
            {"Gas Normalized Volume", "Nm3"},
            {"Gas Volume", "m3"},
            {"Water Volume", "m3"}
    };

    static List<Phenomenon> generatePhenomena(MasterDataService masterDataService) {
        List<Phenomenon> phenomena = new ArrayList<>();
        Set<Unit> unitsToKeepTrack = new HashSet<>();
        for (String[] phenomenaEntry : phenomenaListWithScalers) {
            try {
                String acronym = phenomenaEntry[UNIT_INDEX];
                for (String scaler : scalers) {
                    Unit unit = Unit.get(scaler + acronym);
                    if (!unitsToKeepTrack.contains(unit)) {
                        unitsToKeepTrack.add(unit);
                        Phenomenon phenomenon = masterDataService.newPhenomenon(phenomenaEntry[NAME_INDEX] + " (" + unit.toString() + ")", unit);
                        phenomenon.save();
                        phenomena.add(phenomenon);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        for (String[] phenomenaListWithoutScaler : phenomenaListWithoutScalers) {
            try {
                String acronym = phenomenaListWithoutScaler[UNIT_INDEX];
                    Unit unit = Unit.get(acronym);
                    if (!unitsToKeepTrack.contains(unit)) {
                        unitsToKeepTrack.add(unit);
                        Phenomenon phenomenon = masterDataService.newPhenomenon(phenomenaListWithoutScaler[NAME_INDEX] + " (" + acronym + ")", unit);
                        phenomenon.save();
                        phenomena.add(phenomenon);
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