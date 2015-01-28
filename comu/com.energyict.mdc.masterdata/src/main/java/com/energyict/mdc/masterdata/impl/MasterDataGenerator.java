package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;
import java.util.Optional;

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
 * <p>
 * Copyrights EnergyICT
 * Date: 24/02/14
 * Time: 10:18
 */
public class MasterDataGenerator {

    private enum FixedRegisterTypes {
        NOT_PRESENT("", null, null, -1),
        SERIAL_NUMBER("0.0.0.0.0.41.92.0.0.0.0.0.0.0.0.0.114.0", ObisCode.fromString("0.0.96.1.0.255"), Unit.getUndefined(), 0),
        ACTIVE_METER_FIRMWARE_VERSION("0.0.0.12.0.41.109.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("1.0.0.2.8.255"), Unit.getUndefined(), 0),
        ACTIVE_MODEM_FIRMWARE_VERSION("0.0.0.12.0.3.109.0.0.0.0.0.0.0.0.0.114.0", ObisCode.fromString("1.1.0.2.8.255"), Unit.getUndefined(), 0),
        METER_STATUS("0.0.0.12.0.41.139.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("0.0.97.97.0.255"), Unit.getUndefined(), 0),
        METER_ALARM_STATUS("0.0.0.12.0.41.118.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("0.0.97.98.0.255"), Unit.getUndefined(), 0),

        // fixed gas related registertypes
        GAS_VOLUME("0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0", ObisCode.fromString("7.0.3.0.0.255"), Unit.get("m3"), 0),
        GAS_VOLUME_CORRECTED("0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.167.0", ObisCode.fromString("7.0.3.1.0.255"), Unit.get("m3"), 0),
        GAS_FLOW("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.125.0", ObisCode.fromString("7.0.43.0.0.255"), Unit.get("m3/h"), 0),
        GAS_FLOW_CORRECTED("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.126.0", ObisCode.fromString("7.0.43.1.0.255"), Unit.get("m3/h"), 0),
        GAS_TEMP_K("0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.6.0", ObisCode.fromString("7.0.41.0.0.255"), Unit.get(BaseUnit.KELVIN), 0),
        GAS_TEMP_C("0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.23.0", ObisCode.fromString("7.0.41.0.0.255"), Unit.get(BaseUnit.DEGREE_CELSIUS), 0),
        GAS_TEMP_F("0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.279.0", ObisCode.fromString("7.0.41.0.0.255"), Unit.get(BaseUnit.FAHRENHEIT), 0),
        GAS_PRESSURE("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.39.0", ObisCode.fromString("7.0.41.0.0.255"), Unit.get(BaseUnit.PASCAL), 0),
        GAS_CONVERSION_FACTOR("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.107.0", ObisCode.fromString("7.0.52.0.0.255"), Unit.get(BaseUnit.WATTHOURPERCUBICMETER), 0),

        // fixed water related registertypes
        WATER_VOLUME("0.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0", ObisCode.fromString("8.0.1.0.0.255"), Unit.get("m3"), 0),
        WATER_FLOW("0.2.0.6.0.9.58.0.0.0.0.0.0.0.0.0.125.0", ObisCode.fromString("8.0.2.0.0.255"), Unit.get("m3/h"), 0),
        ;

        private final String readingType;
        private final ObisCode obisCode;
        private final Unit unit;
        private final int tou;

        FixedRegisterTypes(String readingType, ObisCode obisCode, Unit unit, int tou) {
            this.readingType = readingType;
            this.obisCode = obisCode;
            this.unit = unit;
            this.tou = tou;
        }

        public static FixedRegisterTypes getFixedReadingTypeInformation(ReadingType readingType) {
            for (FixedRegisterTypes fixedRegisterTypes : values()) {
                if (fixedRegisterTypes.readingType.equals(readingType.getMRID())) {
                    return fixedRegisterTypes;
                }
            }
            return NOT_PRESENT;
        }
    }

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
            {"Temperature", "\u00B0C"},
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
                FixedRegisterTypes fixedReadingTypeInformation = FixedRegisterTypes.getFixedReadingTypeInformation(readingType);
                if (fixedReadingTypeInformation.equals(FixedRegisterTypes.NOT_PRESENT)) {
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
                } else {
                    RegisterType measurementType = masterDataService.newRegisterType(readingType.getName(), fixedReadingTypeInformation.obisCode, fixedReadingTypeInformation.unit, readingType, fixedReadingTypeInformation.tou);
                    measurementType.save();
                    measurementTypes.add(measurementType);
                }

            } catch (Exception e) {
                // eat
            }
        }
        return measurementTypes;
    }

}