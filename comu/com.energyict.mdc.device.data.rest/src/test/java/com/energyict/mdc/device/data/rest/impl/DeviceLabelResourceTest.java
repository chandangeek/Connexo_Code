package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.LabelCategory;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceLabelResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    Device device;
    @Mock
    DeviceConfiguration deviceConfiguration;
    @Mock
    LabelCategory category;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(device.getId()).thenReturn(100L);
        when(device.getmRID()).thenReturn("1");
        when(device.getVersion()).thenReturn(1L);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceService.findByUniqueMrid(device.getmRID())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceBymRIDAndVersion(device.getmRID(), device.getVersion())).thenReturn(Optional.of(device));

        when(deviceConfiguration.getId()).thenReturn(1L);
        when(deviceConfiguration.getVersion()).thenReturn(1L);
        when(deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId())).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfiguration.getId(), deviceConfiguration.getVersion())).thenReturn(Optional.of(deviceConfiguration));

        when(category.getName()).thenReturn("mycategory");
        doReturn("My category").when(thesaurus).getString("mycategory", "mycategory");
        when(favoritesService.findLabelCategory("mycategory")).thenReturn(Optional.of(category));

    }

    @Test
    public void testNoDeviceLabels() {
        List<DeviceLabel> deviceLabels = new ArrayList<>();
        when(favoritesService.getDeviceLabels(device, null)).thenReturn(deviceLabels);

        String response = target("/devices/1/devicelabels").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);
    }

    @Test
    public void testGetDeviceLabels() {
        Instant now = Instant.now();

        List<DeviceLabel> deviceLabels = new ArrayList<>();
        when(favoritesService.getDeviceLabels(device, null)).thenReturn(deviceLabels);
        DeviceLabel label1 = mock(DeviceLabel.class);
        DeviceLabel label2 = mock(DeviceLabel.class);
        when(label1.getComment()).thenReturn("Comment1");
        when(label1.getCreationDate()).thenReturn(now);
        when(label1.getLabelCategory()).thenReturn(category);
        when(label1.getDevice()).thenReturn(device);

        when(label2.getComment()).thenReturn("Comment2");
        when(label2.getCreationDate()).thenReturn(now.minusMillis(100));
        when(label2.getLabelCategory()).thenReturn(category);
        when(label2.getDevice()).thenReturn(device);

        deviceLabels.add(label1);
        deviceLabels.add(label2);

        String response = target("/devices/1/devicelabels").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<DeviceLabelInfo>>get("$.deviceLabels")).hasSize(2);
        assertThat(jsonModel.<String>get("$.deviceLabels[0].category.id")).isEqualTo("mycategory");
        assertThat(jsonModel.<String>get("$.deviceLabels[0].category.name")).isEqualTo("My category");
        assertThat(jsonModel.<String>get("$.deviceLabels[0].comment")).isEqualTo("Comment1");
        assertThat(jsonModel.<Long>get("$.deviceLabels[0].creationDate")).isEqualTo(now.toEpochMilli());

        assertThat(jsonModel.<String>get("$.deviceLabels[1].category.id")).isEqualTo("mycategory");
        assertThat(jsonModel.<String>get("$.deviceLabels[1].category.name")).isEqualTo("My category");
        assertThat(jsonModel.<String>get("$.deviceLabels[1].comment")).isEqualTo("Comment2");
        assertThat(jsonModel.<Long>get("$.deviceLabels[1].creationDate")).isEqualTo(now.minusMillis(100).toEpochMilli());
    }

    @Test
    public void testCreateDeviceLabel() {
        Instant now = Instant.now();
        DeviceLabel label = mock(DeviceLabel.class);
        when(label.getComment()).thenReturn("My comment");
        when(label.getCreationDate()).thenReturn(now);
        when(label.getLabelCategory()).thenReturn(category);
        when(label.getDevice()).thenReturn(device);
        when(favoritesService.findOrCreateDeviceLabel(device, null, category, "My comment")).thenReturn(label);

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.category = new IdWithNameInfo("mycategory", null);
        info.comment = "My comment";
        Response response = target("/devices/1/devicelabels").request().post(Entity.entity(info, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        DeviceLabelInfo createdLabelInfo = response.readEntity(DeviceLabelInfo.class);
        assertThat(createdLabelInfo.comment).isEqualTo("My comment");
        assertThat(createdLabelInfo.creationDate).isEqualTo(now);
        assertThat(createdLabelInfo.category.id).isEqualTo("mycategory");
    }

    @Test
    public void testCreateDeviceCategoryNotFound() {
        when(favoritesService.findLabelCategory("mycategory_XXx")).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.category = new IdWithNameInfo("mycategory_XXx", null);
        Response response = target("/devices/1/devicelabels").request().post(Entity.entity(info, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeleteDeviceLabel() {
        DeviceLabel label = mock(DeviceLabel.class);
        when(favoritesService.findDeviceLabel(device, null, category)).thenReturn(Optional.of(label));

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.mRID = device.getmRID();
        info.version = device.getVersion();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());

        Response response = target("/devices/1/devicelabels/mycategory").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(favoritesService).removeDeviceLabel(label);
    }

    @Test
    public void testDeleteDeviceLabelBadVersion() {
        long badVersion = device.getVersion() + 1;
        DeviceLabel label = mock(DeviceLabel.class);
        when(favoritesService.findDeviceLabel(device, null, category)).thenReturn(Optional.of(label));
        when(deviceService.findAndLockDeviceBymRIDAndVersion(device.getmRID(), badVersion)).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.mRID = device.getmRID();
        info.version = badVersion;
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());

        Response response = target("/devices/1/devicelabels/mycategory").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(favoritesService, never()).removeDeviceLabel(label);
    }

    @Test
    public void testDeleteDeviceLabelNotFound() {
        when(favoritesService.findDeviceLabel(device, null, category)).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.mRID = device.getmRID();
        info.version = device.getVersion();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());

        Response response = target("/devices/1/devicelabels/mycategory").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
