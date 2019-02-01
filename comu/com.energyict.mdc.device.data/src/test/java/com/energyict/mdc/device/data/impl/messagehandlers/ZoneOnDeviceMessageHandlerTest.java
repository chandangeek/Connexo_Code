package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.EndDeviceZoneBuilder;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneAction;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ZoneOnDeviceQueueMessage;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Jozsef Szekrenyes on 31/01/2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class ZoneOnDeviceMessageHandlerTest {
    @Mock
    private TransactionService transactionService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private MeteringZoneService meteringZoneService;
    @Mock
    private JsonService jsonService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Message message;
    @Mock
    private Zone zone;
    @Mock
    private ZoneType zoneType;
    @Mock
    private Device device;
    @Mock
    private EndDevice endDevice;
    @Mock
    private EndDeviceZone endDeviceZone;
    @Mock
    private EndDeviceZoneBuilder endDeviceZoneBuilder;
    @Mock
    private Finder<EndDeviceZone> finder;
    @Mock
    private NlsMessageFormat nlsMessageFormat;

    private ZoneOnDeviceMessageHandler messageHandler;

    @Before
    public void setUp() {
        when(thesaurus.getFormat((MessageSeeds) any())).thenReturn(nlsMessageFormat);
        when(meteringService.findEndDeviceByName(anyString())).thenReturn(Optional.of(endDevice));
        when(meteringZoneService.getZone(anyLong())).thenReturn(Optional.of(zone));
        when(meteringZoneService.newEndDeviceZoneBuilder()).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.withEndDevice(endDevice)).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.withZone(zone)).thenReturn(endDeviceZoneBuilder);
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.of(device));
        when(meteringZoneService.getByEndDevice((EndDevice) any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Collections.singletonList(endDeviceZone).stream());
        when(endDeviceZone.getZone()).thenReturn(zone);
        when(zone.getZoneType()).thenReturn(zoneType);
        when(zoneType.getId()).thenReturn(1L);
        messageHandler = createMessageHandler();
    }

    @Test
    public void testProcessAddExistingZone() {
        createAddZoneMessage();

        messageHandler.process(message);

        Mockito.verify(endDeviceZone, times(1)).setZone(zone);
        Mockito.verify(endDeviceZone, times(1)).save();
    }

    @Test
    public void testProcessAddNewZone() {
        when(finder.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        createAddZoneMessage();

        messageHandler.process(message);

        verify(endDeviceZoneBuilder, times(1)).create();
        verify(endDeviceZone, never()).setZone(zone);
        verify(endDeviceZone, never()).save();
        verify(meteringZoneService, times(1)).newEndDeviceZoneBuilder();
    }

    @Test
    public void testAddZoneToInexistentEndDevice() {
        when(meteringService.findEndDeviceByName(anyString())).thenReturn(Optional.empty());
        createAddZoneMessage();

        messageHandler.process(message);

        verify(endDeviceZoneBuilder, never()).create();
        verify(endDeviceZone, never()).setZone(zone);
        verify(endDeviceZone, never()).save();
    }

    @Test
    public void testProcessRemoveZone() {
        createRemoveZoneMessage();

        messageHandler.process(message);

        verify(endDeviceZone, times(1)).delete();
    }

    @Test
    public void testExceptionOnAddZoneToDevice() {
        when(finder.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        when(meteringService.findEndDeviceByName(anyString())).thenThrow(NotUniqueException.class);
        createAddZoneMessage();

        messageHandler.process(message);

        verify(endDeviceZoneBuilder, never()).create();
    }

    @Test
    public void testExceptionOnRemoveZoneFromDevice() {
        when(meteringService.findEndDeviceByName(anyString())).thenThrow(NotUniqueException.class);
        createRemoveZoneMessage();

        messageHandler.process(message);

        verify(endDeviceZone, never()).delete();
    }

    @Test
    public void testProcessUnknownCommand() {
        messageHandler = new ZoneOnDeviceMessageHandler();
        JsonService js = new JsonServiceImpl();
        messageHandler.init(meteringService, deviceService, meteringZoneService, js, transactionService, thesaurus);
        createUnknownMessage();

        messageHandler.process(message);

        verify(message, times(2)).getPayload();
        verify(meteringZoneService, never()).getZone(anyLong());
    }

    @Test
    public void testProcessInvalidZone() {
        when(meteringZoneService.getZone(anyLong())).thenReturn(Optional.empty());
        createAddZoneMessage();

        messageHandler.process(message);

        verify(thesaurus, times(1)).getFormat(MessageSeeds.NO_SUCH_ZONE);
        verify(deviceService, never()).findDeviceById(anyLong());
        verify(endDeviceZone, never()).setZone(zone);
        verify(endDeviceZone, never()).save();
    }

    @Test
    public void testProcessInvalidDevice() {
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        createAddZoneMessage();

        messageHandler.process(message);

        verify(meteringZoneService, times(1)).getZone(anyLong());
        verify(deviceService, times(1)).findDeviceById(anyLong());
        verify(thesaurus, times(1)).getFormat(MessageSeeds.NO_SUCH_DEVICE);
        verify(endDeviceZone, never()).setZone(zone);
        verify(endDeviceZone, never()).save();
    }

    @Test
    public void testOnMessageDelete() {
        messageHandler.onMessageDelete(message);

        verify(meteringService, never()).findEndDeviceByName(anyString());
        verify(meteringZoneService, never()).getByEndDevice((EndDevice) any());
        verify(deviceService, never()).findDeviceById(anyLong());
        verify(endDeviceZone, never()).setZone(zone);
        verify(endDeviceZone, never()).save();
    }

    private void createAddZoneMessage() {
        ZoneOnDeviceQueueMessage zoneOnDeviceQueueMessage = new ZoneOnDeviceQueueMessage(1, 1, 1, ZoneAction.Add);
        createMessage(zoneOnDeviceQueueMessage);
    }

    private void createRemoveZoneMessage() {
        ZoneOnDeviceQueueMessage zoneOnDeviceQueueMessage = new ZoneOnDeviceQueueMessage(1, 1, 1, ZoneAction.Remove);
        createMessage(zoneOnDeviceQueueMessage);
    }

    private void createUnknownMessage() {
        String serializedMessage = "{\"zoneId\":1,\"deviceId\":1,\"zoneTypeId\":1,\"action\":\"Unknown\"}";
        when(message.getPayload()).thenReturn(serializedMessage.getBytes());
    }

    private void createMessage(ZoneOnDeviceQueueMessage zoneOnDeviceQueueMessage) {
        JsonService js = new JsonServiceImpl();
        String serializedMessage = js.serialize(zoneOnDeviceQueueMessage);
        when(message.getPayload()).thenReturn(serializedMessage.getBytes());
        when(jsonService.deserialize((byte[]) any(), eq(ZoneOnDeviceQueueMessage.class))).thenReturn(zoneOnDeviceQueueMessage);
    }

    private ZoneOnDeviceMessageHandler createMessageHandler() {
        ZoneOnDeviceMessageHandler messageHandler = new ZoneOnDeviceMessageHandler();
        messageHandler.init(meteringService, deviceService, meteringZoneService, jsonService, transactionService, thesaurus);

        return messageHandler;
    }
}
