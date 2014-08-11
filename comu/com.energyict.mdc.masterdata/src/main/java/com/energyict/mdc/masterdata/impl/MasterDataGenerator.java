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
    private static String[][] phenomenaList = new String[][] {
            {"Active Energy","kWh"},
            {"Active Power","kW"},
            {"Apparent Energy","kVAh"},
            {"Apparent Power","kVA"},
            {"Capacitive Power","kvar"},
            {"Capactive Energy","kvarh"},
            {"Capactive Power","kvar"},
            {"Current","A"},
            {"Energy","kWh"},
            {"Energy","Wh"},
            {"Energy","MJ"},
            {"Gas Consumption","kWh"},
            {"Gas Flow","m3/h"},
            {"Gas Normalized Flow","Nm3/h"},
            {"Gas Normalized Volume","Nm3"},
            {"Gas Volume","m3"},
            {"Heat","kWh"},
            {"Inductive Energy","kvarh"},
            {"Inductive Power","kvar"},
            {"Pressure","bar"},
            {"Reactive Energy","kvarh"},
            {"Reactive Energy","varh"},
            {"Reactive Power","kvar"},
            {"Temperature","\u00B0C"},
            {"Temperature","K"},
            {"Voltage","V"},
            {"Water Volume","m3"}
    };

    static List<Phenomenon> generatePhenomena(MasterDataService masterDataService) {
        List<Phenomenon> phenomena = new ArrayList<>();
        Set<Unit> unitsToKeepTrack = new HashSet<>();
        for (String[] phenomenaEntry: phenomenaList) {
            try {
                Unit unit = Unit.get(phenomenaEntry[1]);
                if (!unitsToKeepTrack.contains(unit)) {
                    unitsToKeepTrack.add(unit);
                    Phenomenon phenomenon = masterDataService.newPhenomenon(phenomenaEntry[0]+" ("+phenomenaEntry[1]+")", unit);
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