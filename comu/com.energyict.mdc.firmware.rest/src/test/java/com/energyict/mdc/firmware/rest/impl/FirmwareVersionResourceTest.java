/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionBuilder;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.firmware.impl.FirmwareVersionFilterImpl;

import com.jayway.jsonpath.JsonModel;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareVersionResourceTest extends BaseFirmwareTest {
    @Mock
    private DeviceType deviceType;
    @Mock
    private FirmwareVersion firmwareVersion;
    @Mock
    private FirmwareVersionBuilder firmwareVersionBuilder;
    @Mock
    private Condition condition;
    @Mock
    private JsonQueryParameters queryParameters;

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
        when(firmwareVersionBuilder.create()).thenReturn(firmwareVersion);
        when(firmwareService.findAndLockFirmwareVersionByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(firmwareVersion));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                firmwareVersion.validate();
                return null;
            }
        }).when(firmwareVersionBuilder).validate();
        when(firmwareService.newFirmwareVersion(any(DeviceType.class), anyString(), any(), any())).thenReturn(firmwareVersionBuilder);
        when(firmwareService.filterForFirmwareVersion(any(DeviceType.class))).thenAnswer(new Answer<FirmwareVersionFilter>() {
            @Override
            public FirmwareVersionFilter answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new FirmwareVersionFilterImpl(((DeviceType) invocationOnMock.getArguments()[0]));
            }
        });
    }

    @Test
    public void testGetFirmwares() {
        String json = target("devicetypes/1/firmwares").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>> get("$.firmwares")).hasSize(1);
    }

    @Test
    public void testGetFirmwaresWithFilters() {
        String json = target("devicetypes/1/firmwares")
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"firmwareType\",\"value\":[\"communication\",\"meter\"]}," +
                                      "{\"property\":\"firmwareStatus\",\"value\":[\"test\",\"ghost\",\"final\",\"deprecated\"]}]"))
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>> get("$.firmwares")).hasSize(1);
    }

    @Test
    public void testGetFirmwareById() {
        String json = target("devicetypes/1/firmwares/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Number> get("$.id").longValue()).isEqualTo(1L);
        assertThat(jsonModel.<String> get("$.firmwareVersion")).isEqualTo("firmwareVersion");
        assertThat(jsonModel.<String> get("$.firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String> get("$.firmwareStatus.id")).isEqualTo("final");
    }

    @Test
    public void testValidateCreationOfFirmwareVersion() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();

        Response response = target("devicetypes/1/firmwares/validate").request().post(Entity.json(firmwareVersionInfo));

        verify(firmwareVersion).validate();
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        firmwareVersionInfo.fileSize = 1;
        response = target("devicetypes/1/firmwares/validate").request().post(Entity.json(firmwareVersionInfo));

        verify(firmwareVersionBuilder).setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testCreateFirmwareVersion() throws Exception {
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("firmwareVersion", "METER1")
            .field("firmwareType", FirmwareType.METER.getType())
            .field("firmwareStatus", FirmwareStatus.TEST.getStatus())
            .bodyPart(new FileDataBodyPart("firmwareFile", File.createTempFile("prefix", "suffix"))).close();

        Response response = target("devicetypes/1/firmwares").request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareVersionBuilder).create();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testValidateEditOfFirmwareVersion() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        Response response = target("devicetypes/1/firmwares/1/validate").request().put(Entity.json(firmwareVersionInfo));

        assertThat(response.getEntity()).isNotNull();
        verify(firmwareVersion).validate();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        firmwareVersionInfo.fileSize = 1;

        response = target("devicetypes/1/firmwares/1/validate").request().put(Entity.json(firmwareVersionInfo));

        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEditFirmwareVersion() throws Exception {
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("firmwareVersion", "METER1")
            .field("firmwareStatus", FirmwareStatus.FINAL.getStatus())
            .bodyPart(new FileDataBodyPart("firmwareFile", File.createTempFile("prefix", "suffix"))).close();

        Response response = target("devicetypes/1/firmwares/1").request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareVersion).update();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeprecateFirmwareVersion() {
        FirmwareVersionInfo info = new FirmwareVersionInfo();
        info.firmwareStatus = new FirmwareStatusInfo();
        info.firmwareStatus.id = FirmwareStatus.DEPRECATED;
        info.version = 1L;

        Response response = target("devicetypes/1/firmwares/1").request().put(Entity.json(info));

        verify(firmwareVersion).deprecate();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testSetAsFinalFirmwareVersion() {
        FirmwareVersionInfo info = new FirmwareVersionInfo();
        info.firmwareStatus = new FirmwareStatusInfo();
        info.firmwareStatus.id = FirmwareStatus.FINAL;
        info.version = 1L;

        Response response = target("devicetypes/1/firmwares/1").request().put(Entity.json(info));

        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareVersion).update();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
