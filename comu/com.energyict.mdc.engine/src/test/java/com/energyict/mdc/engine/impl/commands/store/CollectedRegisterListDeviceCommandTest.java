package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.meterdata.DeviceRegisterList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdw.core.DeviceFactory;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    private static int DEVICE_ID = 1;
    private static int REGISTER_ID = 2;
    private static ObisCode REGISTER_OBIS = ObisCode.fromString("0.0.96.10.2.255");

    @Mock
    private MdwInterface mdwInterface;
    @Mock
    private ServerManager manager;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private CollectedRegister collectedRegister;
    @Mock
    private DeviceFactory deviceFactory;
    @Mock
    private Device device;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;

    @Before
    public void initializeMocksAndFactories() {
        when(this.offlineDevice.getId()).thenReturn(DEVICE_ID);
        when(comServerDAO.findRegister(any(RegisterIdentifier.class))).thenReturn(offlineRegister);
        when(offlineRegister.getRegisterId()).thenReturn(REGISTER_ID);
        when(offlineRegister.getObisCode()).thenReturn(REGISTER_OBIS);

        when(this.collectedRegister.getCollectedQuantity()).thenReturn(new Quantity("2", Unit.getUndefined()));
        when(this.collectedRegister.getEventTime()).thenReturn(new Date(1358757000000L)); // 21 januari 2013 9:30:00
        when(this.collectedRegister.getFromTime()).thenReturn(new Date(1358755200000L));  // 21 januari 2013 9:00:00
        when(this.collectedRegister.getToTime()).thenReturn(new Date(1358758800000L));    // 21 januari 2013 10:00:00
        when(this.collectedRegister.getReadTime()).thenReturn(new Date(1358758920000L));  // 21 januari 2013 10:02:00
        when(this.collectedRegister.getText()).thenReturn("CollectedRegister text");
        ManagerFactory.setCurrent(manager);
        when(this.manager.getMdwInterface()).thenReturn(mdwInterface);
        when(this.mdwInterface.getDeviceFactory()).thenReturn(this.deviceFactory);
        when(this.deviceFactory.find(DEVICE_ID)).thenReturn(device);
        when(this.device.getId()).thenReturn(DEVICE_ID);
    }

    @Test
    public void testExecutionOfDeviceCommand() {
        CollectedRegisterListDeviceCommand command = new CollectedRegisterListDeviceCommand(getDeviceRegisterList(), issueService);
        command.logExecutionWith(this.executionLogger);

        // Business methods
        command.execute(comServerDAO);

        // asserts
        ArgumentCaptor<MeterReadingData> argument = ArgumentCaptor.forClass(MeterReadingData.class);
        //TODO use the storeMeterReadings instead
//        verify(comServerDAO).storeMeterReadingData(eq(new DeviceIdentifierById(DEVICE_ID)), argument.capture());
        MeterReadingData readingData = argument.getValue();

        Assert.assertEquals("Expecting only 1 registerValue", 1, readingData.getRegisterValues().size());
        RegisterValue registerValue = readingData.getRegisterValues().get(0);
        Assert.assertEquals(REGISTER_OBIS, registerValue.getObisCode());
        Assert.assertEquals(collectedRegister.getCollectedQuantity(), registerValue.getQuantity());
        Assert.assertEquals(collectedRegister.getEventTime(), registerValue.getEventTime());
        Assert.assertEquals(collectedRegister.getFromTime(), registerValue.getFromTime());
        Assert.assertEquals(collectedRegister.getToTime(), registerValue.getToTime());
        Assert.assertEquals(collectedRegister.getReadTime(), registerValue.getReadTime());
        Assert.assertEquals(REGISTER_ID, registerValue.getRtuRegisterId());
        Assert.assertEquals(collectedRegister.getText(), registerValue.getText());
    }

    @Test
    public void testToJournalMessageDescription() {
        CollectedRegisterListDeviceCommand command = new CollectedRegisterListDeviceCommand(getDeviceRegisterList(), issueService);
        command.logExecutionWith(this.executionLogger);

        // Business methods
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedRegisterListDeviceCommand.class.getSimpleName()
                + " {deviceIdentifier: id 1; nr of collected registers: 1}");
    }

    private DeviceRegisterList getDeviceRegisterList() {
        DeviceRegisterList deviceRegisterList = new DeviceRegisterList(new DeviceIdentifierById(DEVICE_ID));
        deviceRegisterList.addCollectedRegister(collectedRegister);
        return deviceRegisterList;
    }
}
