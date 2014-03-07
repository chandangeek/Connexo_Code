package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;
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
 * <li>{@link RegisterMapping RegisterMappings}</li>
 * </ul>
 * based on the existing {@link com.elster.jupiter.metering.ReadingType ReadingTypes}
 * in Kore.
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/02/14
 * Time: 10:18
 */
public class MasterDataGenerator {

    private static final Logger LOGGER = Logger.getLogger(MasterDataGenerator.class.getName());

    static List<Phenomenon> generatePhenomena(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, DeviceConfigurationService deviceConfigurationService) {
        List<Phenomenon> phenomena = new ArrayList<>();
        Set<Unit> unitsToKeepTrack = new HashSet<>();
        for (ReadingType readingType : meteringService.getAvailableReadingTypes()) {
            try {
                ReadingTypeInformation readingTypeInformation = readingTypeUtilService.getReadingTypeInformationFor(readingType);
                Unit unit = readingTypeInformation.getUnit();
                if (!unitsToKeepTrack.contains(unit)) {
                    unitsToKeepTrack.add(unit);
                    Phenomenon phenomenon = deviceConfigurationService.newPhenomenon(readingType.getUnit().name(), unit);
                    phenomenon.save();
                    phenomena.add(phenomenon);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return phenomena;
    }

    static List<RegisterMapping> generateRegisterMappings(MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, DeviceConfigurationService deviceConfigurationService) {
        List<RegisterMapping> registerMappings = new ArrayList<>();
        for (ReadingType readingType : meteringService.getAvailableReadingTypes()) {
            try {
                if (deviceConfigurationService.findRegisterMappingByReadingType(readingType)==null) {
                    ReadingTypeInformation readingTypeInformation = readingTypeUtilService.getReadingTypeInformationFor(readingType);
                    RegisterMapping registerMapping = deviceConfigurationService.newRegisterMapping(readingType.getName(), readingTypeInformation.getObisCode(), readingTypeInformation.getUnit(), readingType , readingType.getTou());
                    registerMapping.save();
                    registerMappings.add(registerMapping);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return registerMappings;
    }
}
