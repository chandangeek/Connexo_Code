/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MasterDataGenerator {

    private static final Logger LOGGER = Logger.getLogger(MasterDataGenerator.class.getName());

    public enum FixedRegisterTypes {
        SERIAL_NUMBER("0.0.0.0.0.41.92.0.0.0.0.0.0.0.0.0.114.0", ObisCode.fromString("0.0.96.1.0.255")),
        ACTIVE_METER_FIRMWARE_VERSION("0.0.0.12.0.41.109.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("1.0.0.2.8.255")),
        ACTIVE_MODEM_FIRMWARE_VERSION("0.0.0.12.0.3.109.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("1.1.0.2.8.255")),
        METER_STATUS("0.0.0.12.0.41.139.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("0.0.97.97.0.255")),
        METER_ALARM_STATUS("0.0.0.12.0.41.118.0.0.0.0.0.0.0.0.0.109.0", ObisCode.fromString("0.0.97.98.0.255")),

        // fixed electricity related registertypes
        BULK_A_FORWARD_KWH("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", ObisCode.fromString("1.0.1.8.0.255")),
        BULK_A_FORWARD_WH("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", ObisCode.fromString("1.0.1.8.0.255")),
        BULK_A_REVERSE_KWH("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", ObisCode.fromString("1.0.2.8.0.255")),
        BULK_A_REVERSE_WH("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0", ObisCode.fromString("1.0.2.8.0.255")),
        SUM_A_FORWARD_KWH_TOU_1("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0", ObisCode.fromString("1.0.1.8.1.255")),
        SUM_A_FORWARD_WH_TOU_1("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0", ObisCode.fromString("1.0.1.8.1.255")),
        SUM_A_REVERSE_KWH_TOU_1("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.3.72.0", ObisCode.fromString("1.0.2.8.1.255")),
        SUM_A_REVERSE_WH_TOU_1("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0", ObisCode.fromString("1.0.2.8.1.255")),
        SUM_A_FORWARD_KWH_TOU_2("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.3.72.0", ObisCode.fromString("1.0.1.8.2.255")),
        SUM_A_FORWARD_WH_TOU_2("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0", ObisCode.fromString("1.0.1.8.2.255")),
        SUM_A_REVERSE_KWH_TOU_2("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.3.72.0", ObisCode.fromString("1.0.2.8.2.255")),
        SUM_A_REVERSE_WH_TOU_2("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0", ObisCode.fromString("1.0.2.8.2.255")),

        // fixed gas related registertypes
        GAS_VOLUME("0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.42.0", ObisCode.fromString("7.0.3.0.0.255")),
        GAS_VOLUME_CORRECTED("0.0.0.1.1.7.58.0.0.0.0.0.0.0.0.0.167.0", ObisCode.fromString("7.0.3.1.0.255")),
        GAS_FLOW("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.125.0", ObisCode.fromString("7.0.43.0.0.255")),
        GAS_FLOW_CORRECTED("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.126.0", ObisCode.fromString("7.0.43.1.0.255")),
        GAS_TEMP_K("0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.6.0", ObisCode.fromString("7.0.41.0.0.255")),
        GAS_TEMP_C("0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.23.0", ObisCode.fromString("7.0.41.0.0.255")),
        GAS_TEMP_F("0.0.0.12.0.7.46.0.0.0.0.0.0.0.0.0.279.0", ObisCode.fromString("7.0.41.0.0.255")),
        GAS_PRESSURE("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.39.0", ObisCode.fromString("7.0.41.0.0.255")),
        GAS_CONVERSION_FACTOR("0.2.0.6.0.7.58.0.0.0.0.0.0.0.0.0.107.0", ObisCode.fromString("7.0.52.0.0.255")),

        // fixed water related registertypes
        WATER_VOLUME("0.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0", ObisCode.fromString("8.0.1.0.0.255")),
        WATER_FLOW("0.2.0.6.0.9.58.0.0.0.0.0.0.0.0.0.125.0", ObisCode.fromString("8.0.2.0.0.255")),
        ;

        private final String readingType;
        private final ObisCode obisCode;

        FixedRegisterTypes(String readingType, ObisCode obisCode) {
            this.readingType = readingType;
            this.obisCode = obisCode;
        }

        public String getReadingType() {
            return readingType;
        }
    }

    static List<MeasurementType> generateRegisterTypes(MeteringService meteringService, MasterDataService masterDataService) {
        List<MeasurementType> measurementTypes = new ArrayList<>();

        List<RegisterType> registerTypes = masterDataService.findAllRegisterTypes().find();
        Stream.of(FixedRegisterTypes.values())
                .forEach(fixedRegisterType -> {
                    try{
                        if (registerTypes.stream().noneMatch(registerType -> registerType.getReadingType().getMRID().equals(fixedRegisterType.readingType))) {
                            meteringService.getReadingType(fixedRegisterType.readingType).ifPresent(existingReadingType -> {
                                RegisterType measurementType = masterDataService.newRegisterType(existingReadingType, fixedRegisterType.obisCode);
                                measurementType.save();
                                measurementTypes.add(measurementType);
                            });
                        }
                    } catch (Exception e){
                        // eat
                    }
                });
        return measurementTypes;
    }

}
