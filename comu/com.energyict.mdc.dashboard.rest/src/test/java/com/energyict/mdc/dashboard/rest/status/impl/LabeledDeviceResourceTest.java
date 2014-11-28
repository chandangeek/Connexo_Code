package com.energyict.mdc.dashboard.rest.status.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.Mock;

import com.elster.jupiter.favorites.DeviceLabel;
import com.elster.jupiter.favorites.LabelCategory;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.jayway.jsonpath.JsonModel;

public class LabeledDeviceResourceTest extends DashboardApplicationJerseyTest {
    @Mock
    LabelCategory category;
    
    @Test
    public void testGetLabeledDevicesWithoutCategory() {
        Response response = target("/mylabeleddevices").request().get();
        
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void testGetLabeledDevicesCategoryNotFound() {
        when(favoritesService.findLabelCategory("xxx")).thenReturn(Optional.empty());
        
        Response response = target("/mylabeleddevices").queryParam("category", "xxx").request().get();
        
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());        
    }
    
    @Test
    public void testGetLabeledDevices() {
        when(category.getName()).thenReturn("mycategory");
        when(category.getTranlatedName()).thenReturn("My category");
        when(favoritesService.findLabelCategory("mycategory")).thenReturn(Optional.of(category));
        List<DeviceLabel> deviceLabels = new ArrayList<>();
        when(favoritesService.getDeviceLabelsOfCategory(null, category)).thenReturn(deviceLabels);
        Instant now = Instant.now();        
        deviceLabels.add(mockDeviceLabel(1L, "ZABF00100", "100", "Elster AS1440", now.minusMillis(100), "Favorite device 100"));
        deviceLabels.add(mockDeviceLabel(2L, "ZABF00200", "200", "Elster AS700", now, "Favorite device 200"));
        deviceLabels.add(mockDeviceLabel(3L, "ZABF00300", "300", "Elster AS1440", now.minusMillis(300), "Favorite device 300"));        
        
        String response = target("/mylabeleddevices").queryParam("category", "mycategory").request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List<Object>>get("$.myLabeledDevices")).hasSize(3);
        assertThat(model.<List<Long>>get("$.myLabeledDevices[*].deviceLabelInfo.creationDate")).isSortedAccordingTo((d1, d2) -> Long.compare(d2, d1));
        
        assertThat(model.<List<String>>get("$.myLabeledDevices[*].mRID")).containsExactly("ZABF00200", "ZABF00100", "ZABF00300");
        assertThat(model.<List<String>>get("$.myLabeledDevices[*].serialNumber")).containsExactly("200", "100", "300");
        assertThat(model.<List<String>>get("$.myLabeledDevices[*].deviceTypeName")).containsExactly("Elster AS700", "Elster AS1440", "Elster AS1440");
        
        assertThat(model.<List<String>>get("$.myLabeledDevices[*].deviceLabelInfo.comment")).containsExactly("Favorite device 200", "Favorite device 100", "Favorite device 300");
        assertThat(model.<List<String>>get("$.myLabeledDevices[*].deviceLabelInfo.category.id")).containsExactly("mycategory", "mycategory", "mycategory");
        assertThat(model.<List<String>>get("$.myLabeledDevices[*].deviceLabelInfo.category.name")).containsExactly("My category", "My category", "My category");
        assertThat(model.<List<Long>>get("$.myLabeledDevices[*].deviceLabelInfo.creationDate")).containsExactly(now.toEpochMilli(), now.minusMillis(100).toEpochMilli(), now.minusMillis(300).toEpochMilli());
    }
    
    private DeviceLabel mockDeviceLabel(Long deviceId, String deviceMRID, String serialNumber, String deviceTypeName, Instant creationDate, String comment) {
        DeviceLabel deviceLabel = mock(DeviceLabel.class);
        EndDevice endDevice = mock(EndDevice.class);
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(endDevice.getAmrSystem()).thenReturn(amrSystem);
        when(endDevice.getAmrId()).thenReturn(deviceId.toString());
        
        Device device = mock(Device.class);
        when(deviceService.findDeviceById(deviceId)).thenReturn(device);
        when(device.getmRID()).thenReturn(deviceMRID);
        when(device.getSerialNumber()).thenReturn(serialNumber);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(deviceTypeName);
        when(device.getDeviceType()).thenReturn(deviceType);
        
        when(deviceLabel.getEndDevice()).thenReturn(endDevice);
        when(deviceLabel.getLabelCategory()).thenReturn(category);
        when(deviceLabel.getCreationDate()).thenReturn(creationDate);
        when(deviceLabel.getComment()).thenReturn(comment);
        return deviceLabel;
    }
    
}
