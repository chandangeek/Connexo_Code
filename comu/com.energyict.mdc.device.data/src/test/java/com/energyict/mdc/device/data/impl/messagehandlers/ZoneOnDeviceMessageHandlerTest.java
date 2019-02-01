package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneAction;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ZoneOnDeviceQueueMessage;

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
import static org.mockito.Mockito.times;
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
    private Finder<EndDeviceZone> finder;

    private ZoneOnDeviceMessageHandler messageHandler;

    @Before
    public void setUp() {
        when(meteringZoneService.getZone(anyLong())).thenReturn(Optional.of(zone));
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.of(device));
        when(meteringZoneService.getByEndDevice((EndDevice) any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Collections.singletonList(endDeviceZone).stream());
        when(meteringService.findEndDeviceByName(anyString())).thenReturn(Optional.of(endDevice));
        when(endDeviceZone.getZone()).thenReturn(zone);
        when(zone.getZoneType()).thenReturn(zoneType);
        when(zoneType.getId()).thenReturn(1L);
        messageHandler = createMessageHandler();
    }

    private void createAddZoneMessage() {
        ZoneOnDeviceQueueMessage zoneOnDeviceQueueMessage = new ZoneOnDeviceQueueMessage(1, 1, 1, ZoneAction.Add);
        createAddZoneMessage(zoneOnDeviceQueueMessage);
    }

    private void createAddZoneMessage(ZoneOnDeviceQueueMessage zoneOnDeviceQueueMessage) {
        JsonService js = new JsonServiceImpl();
        String serializedMessage = js.serialize(zoneOnDeviceQueueMessage);
        when(message.getPayload()).thenReturn(serializedMessage.getBytes());
        when(jsonService.deserialize((byte[]) any(), eq(ZoneOnDeviceQueueMessage.class))).thenReturn(zoneOnDeviceQueueMessage);
    }

    @Test
    public void testProcessAddZone() {
        createAddZoneMessage();

        messageHandler.process(message);

        Mockito.verify(endDeviceZone, times(1)).setZone(zone);
        Mockito.verify(endDeviceZone, times(1)).save();
    }

    @Test
    public void testProcessRemoveZone() {
        messageHandler.process(message);

        Mockito.verify(endDeviceZone, times(1)).setZone(zone);
        Mockito.verify(endDeviceZone, times(1)).save();
    }

    private ZoneOnDeviceMessageHandler createMessageHandler() {
        ZoneOnDeviceMessageHandler messageHandler = new ZoneOnDeviceMessageHandler();
        messageHandler.init(meteringService, deviceService, meteringZoneService, jsonService, transactionService, thesaurus);

        return messageHandler;
    }
}
