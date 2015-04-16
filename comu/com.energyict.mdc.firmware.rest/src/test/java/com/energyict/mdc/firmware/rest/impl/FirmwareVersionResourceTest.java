package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.jayway.jsonpath.JsonModel;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareVersionResourceTest extends BaseFirmwareTest {
    @Mock
    private DeviceType deviceType;

    @Mock
    private FirmwareVersion firmwareVersion;

    @Mock
    private Condition condition;

    @Mock
    private QueryParameters queryParameters;

    @Before
    public void setUpStubs() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(firmwareService.getFirmwareVersionById(1)).thenReturn(Optional.of(firmwareVersion));
        when(firmwareVersion.getId()).thenReturn(1L);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("firmwareVersion");
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        Finder<FirmwareVersion> firmwareVersionFinder = mockFinder(Arrays.asList(firmwareVersion));
        when(firmwareService.findAllFirmwareVersions(any(FirmwareVersionFilter.class))).thenReturn(firmwareVersionFinder);
        when(firmwareService.newFirmwareVersion(any(DeviceType.class), anyString(), any(), any())).thenReturn(firmwareVersion);
    }

    @Test
    public void testGetFirmwares() {
        String json = target("devicetypes/1/firmwares").request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwares")).hasSize(1);
    }

    @Test
    public void testGetFirmwareById() {
        String json = target("devicetypes/1/firmwares/1").request().get(String.class);

        System.out.println(json);
         JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(1L);
        assertThat(jsonModel.<String>get("$.firmwareVersion")).isEqualTo("firmwareVersion");
        assertThat(jsonModel.<String>get("$.firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwareStatus.id")).isEqualTo("final");
    }

    @Test
    public void testPostValidate() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();

        Response response = target("devicetypes/1/firmwares/validate")
                .request()
                .post(Entity.json(firmwareVersionInfo));

        assertThat(response.getEntity()).isNotNull();
        verify(firmwareVersion).validate();

        firmwareVersionInfo.fileSize = 1;

        response = target("devicetypes/1/firmwares/validate")
                .request()
                .post(Entity.json(firmwareVersionInfo));

        verify(firmwareVersion).setFirmwareFile(new byte[firmwareVersionInfo.fileSize]);
        assertThat(response.getEntity()).isNotNull();
    }

    @Test
    public void testPost() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();

        final FileDataBodyPart filePart = new FileDataBodyPart("firmwareFile", new File("pom.xml"));

        final MultiPart multiPartEntity = new FormDataMultiPart()
                .field("data", firmwareVersionInfo, MediaType.APPLICATION_JSON_TYPE)
                .bodyPart(filePart);


        final WebTarget target = target("devicetypes/1/firmwares");

        final Response response = target.request()
                .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
        verify(firmwareVersion).setFirmwareFile(any());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testPostWithoutAFile() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();

        final MultiPart multiPartEntity = new FormDataMultiPart()
                .field("data", firmwareVersionInfo, MediaType.APPLICATION_JSON_TYPE);


        final WebTarget target = target("devicetypes/1/firmwares");

        final Response response = target.request()
                .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
        verify(firmwareVersion, never()).setFirmwareFile(any());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testPutValidate() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        Response response = target("devicetypes/1/firmwares/1/validate")
                .request()
                .put(Entity.json(firmwareVersionInfo));

        assertThat(response.getEntity()).isNotNull();
        verify(firmwareVersion).validate();

        firmwareVersionInfo.fileSize = 1;

        response = target("devicetypes/1/firmwares/1/validate")
                .request()
                .post(Entity.json(firmwareVersionInfo));

        verify(firmwareVersion).validate();
        assertThat(response.getEntity()).isNotNull();
    }

    @Test
    public void testPut() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();

        final FileDataBodyPart filePart = new FileDataBodyPart("firmwareFile", new File("pom.xml"));

        final MultiPart multiPartEntity = new FormDataMultiPart()
                .field("data", firmwareVersionInfo, MediaType.APPLICATION_JSON_TYPE)
                .bodyPart(filePart);


        final WebTarget target = target("devicetypes/1/firmwares/1");

        final Response response = target.request()
                .put(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
        verify(firmwareVersion).setFirmwareFile(any());

        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPutWithoutFile() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();

        final MultiPart multiPartEntity = new FormDataMultiPart()
                .field("data", firmwareVersionInfo, MediaType.APPLICATION_JSON_TYPE);


        final WebTarget target = target("devicetypes/1/firmwares/1");

        final Response response = target.request()
                .put(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
        verify(firmwareVersion, never()).setFirmwareFile(any());

        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPutDelete() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.firmwareStatus.id = FirmwareStatus.DEPRECATED;

        final MultiPart multiPartEntity = new FormDataMultiPart()
                .field("data", firmwareVersionInfo, MediaType.APPLICATION_JSON_TYPE);


        final WebTarget target = target("devicetypes/1/firmwares/1");

        final Response response = target.request()
                .put(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));

        verify(firmwareService).deprecateFirmwareVersion(firmwareVersion);
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
