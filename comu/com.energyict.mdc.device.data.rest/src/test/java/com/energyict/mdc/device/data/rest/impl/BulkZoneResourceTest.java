package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.EndDeviceZoneBuilder;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.data.Device;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BulkZoneResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String ZONE_TYPE_NAME = "ZoneTypeName";
    private static final String ZONE_NAME = "ZoneName";
    private static final String END_DEVICE_NAME = "DeviceName";
    private static final String NO_END_DEVICE_NAME = "NoDeviceName";

    private static final long ZONE_TYPE_ID = 1L;
    private static final long ZONE_ID = 2L;
    private static final long END_DEVICE_ZONE_ID = 3L;
    private static final long NO_END_DEVICE_ZONE_ID = 33L;

    private static final String APPLICATION = "nameOfApplication";
    private static final long VERSION = 1L;

    private ExceptionFactory exceptionFactory;
    private BulkZoneResource bulkZoneResource;

    @Mock
    private EndDeviceZoneBuilder endDeviceZoneBuilder;
    @Mock
    private DestinationSpec bulkZoneQueueDestination;
    @Mock
    private MessageBuilder msgBuilder;
    @Mock
    private UriInfo uriInfo;


    @Before
    public void setUp1() {
        Device device = mockDevice();
        EndDevice endDevice = mock(EndDevice.class);
        when(meteringService.findEndDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.of(endDevice));
        when(meteringService.findEndDeviceByName(NO_END_DEVICE_NAME)).thenReturn(Optional.empty());

        EndDeviceZone endDeviceZone1 = mockEndDeviceZone(ZONE_TYPE_ID, ZONE_TYPE_NAME, ZONE_ID, ZONE_NAME, END_DEVICE_ZONE_ID);
        Finder<EndDeviceZone> endDeviceZoneFinder = mockFinder(Arrays.asList(endDeviceZone1));
        when(meteringZoneService.getByEndDevice(endDevice)).thenReturn(endDeviceZoneFinder);

        when(meteringZoneService.getEndDeviceZone(END_DEVICE_ZONE_ID)).thenReturn(Optional.of(endDeviceZone1));
        when(meteringZoneService.getEndDeviceZone(NO_END_DEVICE_ZONE_ID)).thenReturn(Optional.empty());

        Zone zone = mockZone(ZONE_ID, ZONE_NAME, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME);
        when(meteringZoneService.getZone(ZONE_ID)).thenReturn(Optional.of(zone));
        when(meteringZoneService.newEndDeviceZoneBuilder()).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.withEndDevice(endDevice)).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.withZone(zone)).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.create()).thenReturn(endDeviceZone1);
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(bulkZoneQueueDestination));
        when(bulkZoneQueueDestination.message(anyString())).thenReturn(msgBuilder);
        when(appServerHelper.verifyActiveAppServerExists(anyString())).thenReturn(Boolean.TRUE);

        mockUriInfo();
        exceptionFactory = new ExceptionFactory(thesaurus);
        bulkZoneResource = new BulkZoneResource(exceptionFactory, appServerHelper, jsonService, messageService, searchService, meteringZoneService,
                deviceService, meteringService, thesaurus);
    }

    private Device mockDevice() {
        Device device = mock(Device.class);
        when(device.getVersion()).thenReturn(1L);
        when(device.getName()).thenReturn(END_DEVICE_NAME);
        when(deviceService.findAndLockDeviceByNameAndVersion(END_DEVICE_NAME, device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device));
        when(deviceService.findDeviceById(0L)).thenReturn(Optional.empty());

        return device;
    }

    private void mockUriInfo() {
        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        parameters.add("deviceIds", "1");
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
    }

    private Zone mockZone(Long zoneId, String zoneName, String application, long version, long zoneTypeId, String zoneTypeName) {
        Zone zone = mock(Zone.class);
        when(zone.getId()).thenReturn(zoneId);
        when(zone.getName()).thenReturn(zoneName);
        when(zone.getApplication()).thenReturn(application);
        when(zone.getVersion()).thenReturn(version);
        ZoneType zoneType = mock(ZoneType.class);
        when(zoneType.getId()).thenReturn(zoneTypeId);
        when(zoneType.getName()).thenReturn(zoneTypeName);
        when(zone.getZoneType()).thenReturn(zoneType);
        return zone;
    }

    private EndDeviceZone mockEndDeviceZone(long zoneTypeId, String zoneTypeName, long zoneId, String zoneName, long endDeviceZoneId) {
        ZoneType zoneType = mock(ZoneType.class);
        when(zoneType.getName()).thenReturn(zoneTypeName);
        when(zoneType.getId()).thenReturn(zoneTypeId);

        Zone zone = mock(Zone.class);
        when(zone.getName()).thenReturn(zoneName);
        when(zone.getZoneType()).thenReturn(zoneType);
        when(zone.getId()).thenReturn(zoneId);

        EndDeviceZone endDeviceZone = mock(EndDeviceZone.class);
        when(endDeviceZone.getId()).thenReturn(endDeviceZoneId);
        when(endDeviceZone.getZone()).thenReturn(zone);
        return endDeviceZone;
    }

    @Test
    public void testAddZoneToDeviceSet() {
        BulkRequestInfo info = new BulkRequestInfo();
        info.action = "addToZone"; // removeFromZone
        info.deviceIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(info);

        Response response = target("/devices/zones").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetDevicesOnZoneTypeWithoutFilter() {
        Response response = bulkZoneResource.getDevicesOnZoneType(uriInfo, ZONE_TYPE_ID, ZONE_ID, null);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
