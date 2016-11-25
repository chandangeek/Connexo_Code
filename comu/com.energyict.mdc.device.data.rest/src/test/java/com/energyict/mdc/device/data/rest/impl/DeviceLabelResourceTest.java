package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.LabelCategory;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceLabelResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    Device device;
    @Mock
    LabelCategory category;

    User user;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(device.getId()).thenReturn(100L);
        when(device.getName()).thenReturn("name");
        when(device.getVersion()).thenReturn(1L);
        when(deviceService.findDeviceByName(device.getName())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByIdAndVersion(device.getId(), device.getVersion())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(device.getName(), device.getVersion())).thenReturn(Optional.of(device));

        when(category.getName()).thenReturn("mycategory");
        doReturn("My category").when(thesaurus).getString("mycategory", "mycategory");
        when(favoritesService.findLabelCategory("mycategory")).thenReturn(Optional.of(category));
    }

    @Test
    public void testNoDeviceLabels() {
        when(favoritesService.getDeviceLabels(device, user)).thenReturn(Collections.emptyList());

        String response = target("/devices/name/devicelabels").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);
    }

    @Test
    public void testGetDeviceLabels() {
        Instant now = Instant.now();

        List<DeviceLabel> deviceLabels = new ArrayList<>();
        when(favoritesService.getDeviceLabels(device, user)).thenReturn(deviceLabels);
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

        String response = target("/devices/name/devicelabels").request().get(String.class);

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
        when(favoritesService.findDeviceLabel(device, user, category)).thenReturn(Optional.empty());
        when(favoritesService.findOrCreateDeviceLabel(device, user, category, "My comment")).thenReturn(label);

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.category = new IdWithNameInfo("mycategory", null);
        info.comment = "My comment";
        info.parent = new VersionInfo<>(device.getId(), device.getVersion());
        Response response = target("/devices/name/devicelabels").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        DeviceLabelInfo createdLabelInfo = response.readEntity(DeviceLabelInfo.class);
        assertThat(createdLabelInfo.comment).isEqualTo("My comment");
        assertThat(createdLabelInfo.creationDate).isEqualTo(now);
        assertThat(createdLabelInfo.category.id).isEqualTo("mycategory");
    }

    @Test
    public void testCreateDeviceLabelCategoryNotFound() {
        when(favoritesService.findLabelCategory("mycategory_XXX")).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.category = new IdWithNameInfo("mycategory_XXX", null);
        info.parent = new VersionInfo<>(device.getId(), device.getVersion());

        Response response = target("/devices/name/devicelabels").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeleteDeviceLabel() {
        Instant now = Instant.now();
        DeviceLabel label = mock(DeviceLabel.class);
        when(label.getCreationDate()).thenReturn(now);
        when(favoritesService.findDeviceLabel(device, user, category)).thenReturn(Optional.of(label));

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.parent = new VersionInfo<>(device.getId(), device.getVersion());
        info.creationDate = now;

        Response response = target("/devices/name/devicelabels/mycategory").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(favoritesService).removeDeviceLabel(label);
    }

    @Test
    public void testCreateDeviceLabelBadDeviceVersion() {
        long badVersion = device.getVersion() - 1;
        when(deviceService.findAndLockDeviceByIdAndVersion(device.getId(), badVersion)).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.parent = new VersionInfo<>(device.getId(), badVersion);

        Response response = target("/devices/name/devicelabels").request().post(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo(thesaurus.getFormat(MessageSeeds.FLAG_DEVICE_CONCURRENT_TITLE).format("name"));
        assertThat(concurrentModificationInfo.messageBody).isEqualTo(thesaurus.getFormat(MessageSeeds.FLAG_DEVICE_CONCURRENT_BODY).format("name"));
        assertThat(concurrentModificationInfo.parent.id).isEqualTo((int)device.getId());
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(device.getVersion());
    }

    @Test
    public void testDeleteDeviceLabelBadDeviceVersion() {
        long badVersion = device.getVersion() - 1;
        when(deviceService.findAndLockDeviceByIdAndVersion(device.getId(), badVersion)).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.parent = new VersionInfo<>(device.getId(), badVersion);

        Response response = target("/devices/name/devicelabels/mycategory").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo(thesaurus.getFormat(MessageSeeds.REMOVE_FLAG_DEVICE_CONCURRENT_TITLE).format("name"));
        assertThat(concurrentModificationInfo.messageBody).isEqualTo(thesaurus.getFormat(MessageSeeds.FLAG_DEVICE_CONCURRENT_BODY).format("name"));
        assertThat(concurrentModificationInfo.parent.id).isEqualTo((int)device.getId());
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(device.getVersion());
    }

    @Test
    public void testDeleteDeviceLabelNotFound() {
        when(favoritesService.findDeviceLabel(device, null, category)).thenReturn(Optional.empty());

        DeviceLabelInfo info = new DeviceLabelInfo();
        info.parent = new VersionInfo<>(device.getId(), device.getVersion());

        Response response = target("/devices/name/devicelabels/mycategory").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
