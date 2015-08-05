package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.google.common.collect.Range;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.*;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceReadingsImporterIntegrationTest extends PersistenceIntegrationTest {

    @Mock
    private Logger logger;

    @Test
    @Transactional
    public void testImportRegisterAndChannelReadings() {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        ReadingType readingTypeForRegister = inMemoryPersistence.getService(MeteringService.class).getReadingType("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0").get();
        MasterDataService masterDataService = inMemoryPersistence.getService(MasterDataService.class);
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingTypeForRegister).get();
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("Daily Electriciyl", ObisCode.fromString("1.2.3.4.5.6"), TimeDuration.days(1), Arrays.asList(registerType));
        loadProfileType.save();
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getService(DeviceConfigurationService.class);
        DeviceType deviceType = deviceConfigurationService.newDeviceType("Device Type", deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.save();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Default").add();
        deviceConfiguration.createNumericalRegisterSpec(registerType)
                .setNumberOfDigits(8)
                .setNumberOfFractionDigits(2)
                .setOverflowValue(BigDecimal.valueOf(100000000))
                .add().save();
        LoadProfileSpec loadProfileSpec = deviceConfiguration.createLoadProfileSpec(loadProfileType).add();
        loadProfileSpec.save();
        deviceConfiguration.createChannelSpec(loadProfileType.getChannelTypes().get(0), loadProfileSpec)
                .setNbrOfFractionDigits(2)
                .setOverflow(BigDecimal.valueOf(100000000))
                .add().save();
        deviceConfiguration.activate();
        deviceConfiguration.save();

        DeviceService deviceService = inMemoryPersistence.getService(DeviceService.class);
        Device device = deviceService.newDevice(deviceConfiguration, "TestDevice", "TestDevice");
        device.save();

        DeviceReadingsImporterFactory deviceReadingsImporterFactory = inMemoryPersistence.getService(DeviceReadingsImporterFactory.class);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatInfo(FORMAT3));
        FileImporter importer = deviceReadingsImporterFactory.createImporter(properties);

        String csv = "Device MRID;Reading date;Reading type MRID;Reading Value;;\n" +
                "TestDevice;01/08/2015 00:00;0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;100.527\n" +
                "TestDevice;02/08/2015 00:00;0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;101;11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;800.455\n" +
                "TestDevice;03/08/2015 00:00;11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;810";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        importer.process(importOccurrence);

        Thesaurus thesaurus = inMemoryPersistence.getService(Thesaurus.class);

        verify(logger).info(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG.getTranslated(thesaurus, 2, "100.52"));
        verify(logger).info(MessageSeeds.READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG.getTranslated(thesaurus, 3, "800.45"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 4, 1, 2));

        List<NumericalReading> readings = device.getRegisters().get(0).getReadings(Interval.forever());
        assertThat(readings).hasSize(2);
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(ZonedDateTime.of(2015, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(100.52));
        assertThat(readings.get(1).getTimeStamp()).isEqualTo(ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(readings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(101));

        List<LoadProfileReading> channelData = device.getChannels().get(0).getChannelData(Range.openClosed(Instant.EPOCH, Instant.MAX));
        assertThat(channelData).hasSize(2);
        ArrayList<IntervalReadingRecord> channelReadings = new ArrayList<>(channelData.get(0).getChannelValues().values());
        assertThat(channelReadings).hasSize(1);
        assertThat(channelReadings.get(0).getTimeStamp()).isEqualTo(ZonedDateTime.of(2015, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(channelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(810));
        channelReadings = new ArrayList<>(channelData.get(1).getChannelValues().values());
        assertThat(channelReadings).hasSize(1);
        assertThat(channelReadings.get(0).getTimeStamp()).isEqualTo(ZonedDateTime.of(2015, 8, 2, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(channelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(800.45));
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        when(importOccurrence.getLogger()).thenReturn(logger);
        return importOccurrence;
    }
}
