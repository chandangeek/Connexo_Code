package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.importers.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.google.common.collect.Range;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceReadingsMultipliedImporterIntegrationTest extends PersistenceIntegrationTest {

    @Mock
    private Logger logger;

    @Test
    @Transactional
    public void testImportRegisterAndChannelReadings() {
        Instant creationDate = LocalDate.of(2015, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationDate);
        DeviceConfiguration deviceConfiguration = setup();
        Device device = createDevice(deviceConfiguration, creationDate);
        device.setMultiplier(BigDecimal.TEN, creationDate);
        device.save();

        DeviceReadingsImporterFactory deviceReadingsImporterFactory = inMemoryPersistence.getService(DeviceReadingsImporterFactory.class);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatInfo(FORMAT3));
        FileImporter importer = deviceReadingsImporterFactory.createImporter(properties);

        String csv = "Device name;Reading date;Reading type MRID;Reading Value;;\n" +
                "TestDevice;01/08/2015 01:00;11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;100.527\n" +
                "TestDevice;02/08/2015 00:00;11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;800.455\n" +
                "TestDevice;03/08/2015 00:00;11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;810.81\n" +
                "TestDevice;04/08/2015 00:00;11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0;900.9";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        importer.process(importOccurrence);

        device = reloadDevice(device);

        List<LoadProfileReading> channelData = device.getChannels().get(0).getChannelData(Range.openClosed(Instant.EPOCH, Instant.MAX));
        assertThat(channelData).hasSize(3);

        ReadingType calculatedReadingType = inMemoryPersistence.getService(MeteringService.class)
                .getReadingType("11.0.0.9.1.2.12.0.0.0.0.1.0.0.0.0.72.0")
                .get();
        List<IntervalReadingRecord> channelReadings = new ArrayList<>(channelData.get(0).getChannelValues().values());
        assertThat(channelReadings).hasSize(1);
        assertThat(channelReadings.get(0).getTimeStamp()).isEqualTo(ZonedDateTime.of(2015, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(channelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(900.90));
        assertThat(channelReadings.get(0).filter(calculatedReadingType).getValue()).isEqualTo(BigDecimal.valueOf(9009.00));
        channelReadings = new ArrayList<>(channelData.get(1).getChannelValues().values());
        assertThat(channelReadings).hasSize(1);
        assertThat(channelReadings.get(0).getTimeStamp()).isEqualTo(ZonedDateTime.of(2015, 8, 3, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(channelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(810.81));
        assertThat(channelReadings.get(0).filter(calculatedReadingType).getValue()).isEqualTo(BigDecimal.valueOf(810810, 2));
    }

    private DeviceConfiguration setup() {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(1L);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        ReadingType readingTypeForRegister = inMemoryPersistence.getService(MeteringService.class).getReadingType("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0").get();
        ReadingType calculatedReadingType = inMemoryPersistence.getService(MeteringService.class).getReadingType("11.0.0.9.1.2.12.0.0.0.0.1.0.0.0.0.72.0").get();
        MasterDataService masterDataService = inMemoryPersistence.getService(MasterDataService.class);
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingTypeForRegister).get();
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("Daily Electricity", ObisCode.fromString("1.2.3.4.5.6"), TimeDuration.days(1), Collections.singletonList(registerType));
        loadProfileType.save();
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getService(DeviceConfigurationService.class);
        DeviceType deviceType = deviceConfigurationService.newDeviceType("Device Type", deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        deviceType.addLoadProfileType(loadProfileType);

        //change initial state of default lifecycle to have newly created devices not in In Stock state
        FiniteStateMachine fsm = deviceType.getDeviceLifeCycle().getFiniteStateMachine();
        Optional<State> newInitialState = fsm.getState(DefaultState.COMMISSIONING.getKey());
        fsm.startUpdate().complete(newInitialState.get()).update();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Default").add();
        LoadProfileSpec loadProfileSpec = deviceConfiguration.createLoadProfileSpec(loadProfileType).add();
        loadProfileSpec.save();
        deviceConfiguration.createChannelSpec(loadProfileType.getChannelTypes().get(0), loadProfileSpec)
                .nbrOfFractionDigits(2)
                .overflow(BigDecimal.valueOf(100000000))
                .useMultiplierWithCalculatedReadingType(calculatedReadingType)
                .add().save();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        return deviceConfiguration;
    }

    private Device createDevice(DeviceConfiguration deviceConfiguration, Instant creationDate) {
        return inMemoryPersistence.getService(DeviceService.class).newDevice(deviceConfiguration, "TestDevice", "TestDevice", creationDate);
    }

    private Device reloadDevice(Device device) {
        DeviceService deviceService = inMemoryPersistence.getService(DeviceService.class);
        return deviceService.findDeviceById(device.getId()).get();
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        when(importOccurrence.getLogger()).thenReturn(logger);
        return importOccurrence;
    }
}
