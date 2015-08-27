package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.DeviceCreator;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceImpl;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the execution of the CollectedRegisterListDeviceCommand
 *
 * @author sva
 * @since 21/01/13 - 11:21
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterListDeviceCommandTest {

    private static final long DEVICE_ID = 1;
    private static final long REGISTER_ID = 2;
    private static ObisCode REGISTER_OBIS = ObisCode.fromString("0.0.96.10.2.255");

    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private CollectedRegister collectedRegister;
    @Mock
    private RegisterIdentifier collectedRegisterIdentifier;
    @Mock
    private DeviceFactory deviceFactory;
    @Mock
    private Device device;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    private MeteringService meteringService;

    @Before
    public void initializeMocksAndFactories() {
        when(this.offlineDevice.getId()).thenReturn(DEVICE_ID);
        when(comServerDAO.findOfflineRegister(any(RegisterIdentifier.class))).thenReturn(offlineRegister);
        when(offlineRegister.getRegisterId()).thenReturn(REGISTER_ID);
        when(offlineRegister.getObisCode()).thenReturn(REGISTER_OBIS);
        when(offlineRegister.getOverFlowValue()).thenReturn(new BigDecimal(DeviceCreator.CHANNEL_OVERFLOW_VALUE));

        when(this.collectedRegister.getCollectedQuantity()).thenReturn(new Quantity("2", Unit.getUndefined()));
        when(this.collectedRegister.getEventTime()).thenReturn(Instant.ofEpochMilli(1358757000000L)); // 21 januari 2013 9:30:00
        when(this.collectedRegister.getFromTime()).thenReturn(Instant.ofEpochMilli(1358755200000L));  // 21 januari 2013 9:00:00
        when(this.collectedRegister.getToTime()).thenReturn(Instant.ofEpochMilli(1358758800000L));    // 21 januari 2013 10:00:00
        when(this.collectedRegister.getReadTime()).thenReturn(Instant.ofEpochMilli(1358758920000L));  // 21 januari 2013 10:02:00
        when(this.collectedRegister.getText()).thenReturn("CollectedRegister text");
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.3.72.0");
        when(this.collectedRegister.getReadingType()).thenReturn(readingType);
        when(this.collectedRegisterIdentifier.getObisCode()).thenReturn(REGISTER_OBIS);
        when(this.collectedRegister.getRegisterIdentifier()).thenReturn(this.collectedRegisterIdentifier);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.meteringService.getReadingType(Matchers.<String>any())).thenReturn(Optional.ofNullable(null));
        when(serviceProvider.mdcReadingTypeUtilService()).thenReturn(new MdcReadingTypeUtilServiceImpl(this.meteringService));
    }

    @Test
    public void testExecutionOfDeviceCommand() {
        MeterDataStoreCommand meterDataStoreCommand = new MeterDataStoreCommandImpl(this.serviceProvider);
        CollectedRegisterListDeviceCommand command = new CollectedRegisterListDeviceCommand(getDeviceRegisterList(), null, meterDataStoreCommand, this.serviceProvider);
        command.logExecutionWith(this.executionLogger);

        // Business methods
        command.execute(comServerDAO);
        meterDataStoreCommand.execute(comServerDAO);

        // asserts
        ArgumentCaptor<MeterReading> argument = ArgumentCaptor.forClass(MeterReading.class);
        verify(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), argument.capture());
        MeterReading readingData = argument.getValue();

        Assert.assertEquals("Expecting only 1 registerValue", 1, readingData.getReadings().size());
        Reading registerValue = readingData.getReadings().get(0);
        Assert.assertEquals(collectedRegister.getCollectedQuantity().getAmount(), registerValue.getValue());
        Assert.assertEquals(collectedRegister.getEventTime(), registerValue.getTimeStamp());
        Assert.assertEquals(collectedRegister.getFromTime(), registerValue.getTimePeriod().filter(Range::hasLowerBound).map(Range::lowerEndpoint).orElse(null));
        Assert.assertEquals(collectedRegister.getToTime(), registerValue.getTimePeriod().filter(Range::hasUpperBound).map(Range::upperEndpoint).orElse(null));
    }

    @Test
    public void testToJournalMessageDescription() {
        CollectedRegisterListDeviceCommand command = new CollectedRegisterListDeviceCommand(getDeviceRegisterList(), null, new MeterDataStoreCommandImpl(this.serviceProvider), this.serviceProvider);
        command.logExecutionWith(this.executionLogger);

        // Business methods
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // asserts
        assertThat(journalMessage).contains("{deviceIdentifier: id 1; nr of collected registers: 1}");
    }

    private DeviceRegisterList getDeviceRegisterList() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        DeviceRegisterList deviceRegisterList = new DeviceRegisterList(deviceIdentifier);
        when(this.collectedRegisterIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        deviceRegisterList.addCollectedRegister(collectedRegister);
        return deviceRegisterList;
    }
}
