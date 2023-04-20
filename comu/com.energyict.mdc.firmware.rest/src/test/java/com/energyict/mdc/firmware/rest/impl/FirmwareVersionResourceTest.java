/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.util.collections.KPermutation;
import com.energyict.mdc.common.device.config.DeviceType;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FirmwareVersionResourceTest extends BaseFirmwareTest {
    private static final String FIRMWARE_VERSION = "firmwareVersion";
    private static final String METER_FW_DEPENDENCY = "meterFW";
    private static final String COMM_FW_DEPENDENCY = "commFW";
    private static final String AUX_FW_DEPENDENCY = "auxFW";
    @Mock
    private DeviceType deviceType;
    @Mock
    private FirmwareVersion firmwareVersion, meterFWDependency, commFWDependency, auxFWDependency;
    @Mock
    private FirmwareVersionBuilder firmwareVersionBuilder;

    @Before
    public void setUpStubs() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(firmwareService.getFirmwareVersionById(1)).thenReturn(Optional.of(firmwareVersion));
        when(firmwareVersion.getId()).thenReturn(1L);
        when(firmwareVersion.getRank()).thenReturn(3);
        when(firmwareVersion.getFirmwareVersion()).thenReturn(FIRMWARE_VERSION);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getImageIdentifier()).thenReturn("10.4.0");
        when(firmwareVersion.getMeterFirmwareDependency()).thenReturn(Optional.empty());
        when(firmwareVersion.getCommunicationFirmwareDependency()).thenReturn(Optional.empty());
        when(firmwareVersion.getAuxiliaryFirmwareDependency()).thenReturn(Optional.empty());
        Finder<FirmwareVersion> firmwareVersionFinder = mockFinder(Collections.singletonList(firmwareVersion));
        when(firmwareService.findAllFirmwareVersions(any(FirmwareVersionFilter.class))).thenReturn(firmwareVersionFinder);
        when(firmwareVersionBuilder.create()).thenReturn(firmwareVersion);
        when(firmwareService.findAndLockFirmwareVersionByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(firmwareVersion));
        when(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).thenReturn(true);
        doAnswer(invocationOnMock -> {
            firmwareVersion.validate();
            return null;
        }).when(firmwareVersionBuilder).validate();
        when(firmwareService.newFirmwareVersion(any(DeviceType.class), anyString(), any(), any())).thenReturn(firmwareVersionBuilder);
        when(firmwareService.newFirmwareVersion(any(DeviceType.class), anyString(), any(), any(), any())).thenReturn(firmwareVersionBuilder);
        when(firmwareService.filterForFirmwareVersion(any(DeviceType.class)))
                .thenAnswer(invocationOnMock -> new FirmwareVersionFilterImpl((invocationOnMock.getArgumentAt(0, DeviceType.class))));
        when(firmwareService.findSecurityAccessorForSignatureValidation(deviceType))
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class));

        when(firmwareService.getFirmwareVersionById(25)).thenReturn(Optional.of(meterFWDependency));
        when(meterFWDependency.getId()).thenReturn(25L);
        when(meterFWDependency.getFirmwareVersion()).thenReturn(METER_FW_DEPENDENCY);
        when(firmwareService.getFirmwareVersionById(21)).thenReturn(Optional.of(commFWDependency));
        when(commFWDependency.getId()).thenReturn(21L);
        when(commFWDependency.getFirmwareVersion()).thenReturn(COMM_FW_DEPENDENCY);
        when(firmwareService.getFirmwareVersionById(27)).thenReturn(Optional.of(auxFWDependency));
        when(auxFWDependency.getId()).thenReturn(27L);
        when(auxFWDependency.getFirmwareVersion()).thenReturn(AUX_FW_DEPENDENCY);
    }

    @Test
    public void testGetFirmwares() {
        String json = target("devicetypes/1/firmwares").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwares")).hasSize(1);
    }

    @Test
    public void testGetFirmwaresWithFilters() {
        String json = target("devicetypes/1/firmwares")
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"firmwareType\",\"value\":[\"communication\",\"meter\"]}," +
                        "{\"property\":\"firmwareStatus\",\"value\":[\"test\",\"ghost\",\"final\",\"deprecated\"]}]"))
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwares")).hasSize(1);
    }

    @Test
    public void testGetFirmwareById() {
        String json = target("devicetypes/1/firmwares/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(1L);
        assertThat(jsonModel.<String>get("$.firmwareVersion")).isEqualTo(FIRMWARE_VERSION);
        assertThat(jsonModel.<String>get("$.firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwareStatus.id")).isEqualTo("final");
        assertThat(jsonModel.<Number>get("$.rank")).isEqualTo(3);
        assertThat(jsonModel.<Object>get("$.meterFirmwareDependency")).isNull();
        assertThat(jsonModel.<Object>get("$.communicationFirmwareDependency")).isNull();
    }

    @Test
    public void testGetFirmwareWithDependencies() {
        when(firmwareVersion.getMeterFirmwareDependency()).thenReturn(Optional.of(meterFWDependency));
        when(firmwareVersion.getCommunicationFirmwareDependency()).thenReturn(Optional.of(commFWDependency));
        when(firmwareVersion.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(auxFWDependency));

        String json = target("devicetypes/1/firmwares/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(1L);
        assertThat(jsonModel.<Number>get("$.meterFirmwareDependency.id")).isEqualTo(25);
        assertThat(jsonModel.<String>get("$.meterFirmwareDependency.name")).isEqualTo(METER_FW_DEPENDENCY);
        assertThat(jsonModel.<Number>get("$.communicationFirmwareDependency.id")).isEqualTo(21);
        assertThat(jsonModel.<String>get("$.communicationFirmwareDependency.name")).isEqualTo(COMM_FW_DEPENDENCY);
        assertThat(jsonModel.<Number>get("$.auxiliaryFirmwareDependency.id")).isEqualTo(27);
        assertThat(jsonModel.<String>get("$.auxiliaryFirmwareDependency.name")).isEqualTo(AUX_FW_DEPENDENCY);
    }

    @Test
    public void testValidateCreationOfFirmwareVersion() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareVersion = FIRMWARE_VERSION;
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareType.id = FirmwareType.COMMUNICATION;
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.firmwareStatus.id = FirmwareStatus.TEST;
        firmwareVersionInfo.imageIdentifier = "abc";

        Response response = target("devicetypes/1/firmwares/validate").request().post(Entity.json(firmwareVersionInfo));

        verify(firmwareService).newFirmwareVersion(deviceType, FIRMWARE_VERSION, FirmwareStatus.TEST, FirmwareType.COMMUNICATION, "abc");
        verify(firmwareVersionBuilder).validate();
        verifyNoMoreInteractions(firmwareVersionBuilder);
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testValidateCreationWithFileSize() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareVersion = FIRMWARE_VERSION;
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareType.id = FirmwareType.COMMUNICATION;
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.firmwareStatus.id = FirmwareStatus.TEST;
        firmwareVersionInfo.imageIdentifier = "abc";
        firmwareVersionInfo.fileSize = 1;

        Response response = target("devicetypes/1/firmwares/validate").request().post(Entity.json(firmwareVersionInfo));

        verify(firmwareService).newFirmwareVersion(deviceType, FIRMWARE_VERSION, FirmwareStatus.TEST, FirmwareType.COMMUNICATION, "abc");
        verify(firmwareVersionBuilder).setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
        verify(firmwareVersionBuilder).validate();
        verifyNoMoreInteractions(firmwareVersionBuilder);
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testValidateCreationWithDependencies() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareType = new FirmwareTypeInfo();
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.meterFirmwareDependency = new IdWithNameInfo(25, null);
        firmwareVersionInfo.communicationFirmwareDependency = new IdWithNameInfo(21, null);
        firmwareVersionInfo.auxiliaryFirmwareDependency = new IdWithNameInfo(27, null);
        firmwareVersionInfo.fileSize = 1;

        Response response = target("devicetypes/1/firmwares/validate").request().post(Entity.json(firmwareVersionInfo));

        verify(firmwareVersionBuilder).setMeterFirmwareDependency(meterFWDependency);
        verify(firmwareVersionBuilder).setCommunicationFirmwareDependency(commFWDependency);
        verify(firmwareVersionBuilder).setAuxiliaryFirmwareDependency(auxFWDependency);
        verify(firmwareVersionBuilder).validate();
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testCreateFirmwareVersion() throws Exception {
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("firmwareVersion", "METER1")
                .field("firmwareType", FirmwareType.METER.getType())
                .field("firmwareStatus", FirmwareStatus.TEST.getStatus())
                .field("imageIdentifier", "10.4.0")
                .bodyPart(new FileDataBodyPart("firmwareFile", File.createTempFile("prefix", "suffix")))
                .close();

        Response response = target("devicetypes/1/firmwares").request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareService).newFirmwareVersion(deviceType, "METER1", FirmwareStatus.TEST, FirmwareType.METER, "10.4.0");
        verify(firmwareVersionBuilder).create();
        verifyNoMoreInteractions(firmwareVersionBuilder);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testCreateFirmwareVersionWithDependencies() throws Exception {
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("firmwareVersion", "METER1")
                .field("firmwareType", FirmwareType.METER.getType())
                .field("firmwareStatus", FirmwareStatus.TEST.getStatus())
                .field("imageIdentifier", "10.4.0")
                .field("meterFirmwareDependency", "25")
                .field("communicationFirmwareDependency", "21")
                .field("auxiliaryFirmwareDependency", "27")
                .bodyPart(new FileDataBodyPart("firmwareFile", File.createTempFile("prefix", "suffix")))
                .close();

        Response response = target("devicetypes/1/firmwares").request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareService).newFirmwareVersion(deviceType, "METER1", FirmwareStatus.TEST, FirmwareType.METER, "10.4.0");
        verify(firmwareVersionBuilder).setMeterFirmwareDependency(meterFWDependency);
        verify(firmwareVersionBuilder).setCommunicationFirmwareDependency(commFWDependency);
        verify(firmwareVersionBuilder).setAuxiliaryFirmwareDependency(auxFWDependency);
        verify(firmwareVersionBuilder).create();
        verifyNoMoreInteractions(firmwareVersionBuilder);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testValidateEditOfFirmwareVersion() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareVersion = FIRMWARE_VERSION;
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.firmwareStatus.id = FirmwareStatus.FINAL;
        firmwareVersionInfo.imageIdentifier = "abc";

        Response response = target("devicetypes/1/firmwares/1/validate").request().put(Entity.json(firmwareVersionInfo));

        verify(firmwareVersion).setFirmwareVersion(FIRMWARE_VERSION);
        verify(firmwareVersion).setImageIdentifier("abc");
        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareVersion).setMeterFirmwareDependency(null);
        verify(firmwareVersion).setCommunicationFirmwareDependency(null);
        verify(firmwareVersion).validate();
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testValidateEditWithFileSize() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareVersion = FIRMWARE_VERSION;
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.firmwareStatus.id = FirmwareStatus.FINAL;
        firmwareVersionInfo.imageIdentifier = "abc";
        firmwareVersionInfo.fileSize = 1;

        Response response = target("devicetypes/1/firmwares/1/validate").request().put(Entity.json(firmwareVersionInfo));

        verify(firmwareVersion).setFirmwareVersion(FIRMWARE_VERSION);
        verify(firmwareVersion).setImageIdentifier("abc");
        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareVersion).setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
        verify(firmwareVersion).setMeterFirmwareDependency(null);
        verify(firmwareVersion).setCommunicationFirmwareDependency(null);
        verify(firmwareVersion).validate();
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testValidateEditWithDependencies() {
        FirmwareVersionInfo firmwareVersionInfo = new FirmwareVersionInfo();
        firmwareVersionInfo.firmwareVersion = FIRMWARE_VERSION;
        firmwareVersionInfo.firmwareStatus = new FirmwareStatusInfo();
        firmwareVersionInfo.firmwareStatus.id = FirmwareStatus.FINAL;
        firmwareVersionInfo.imageIdentifier = "abc";
        firmwareVersionInfo.meterFirmwareDependency = new IdWithNameInfo(25, null);
        firmwareVersionInfo.communicationFirmwareDependency = new IdWithNameInfo(21, null);
        firmwareVersionInfo.auxiliaryFirmwareDependency = new IdWithNameInfo(27, null);


        Response response = target("devicetypes/1/firmwares/1/validate").request().put(Entity.json(firmwareVersionInfo));

        verify(firmwareVersion).setFirmwareVersion(FIRMWARE_VERSION);
        verify(firmwareVersion).setImageIdentifier("abc");
        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareVersion).setMeterFirmwareDependency(meterFWDependency);
        verify(firmwareVersion).setCommunicationFirmwareDependency(commFWDependency);
        verify(firmwareVersion).setAuxiliaryFirmwareDependency(auxFWDependency);
        verify(firmwareVersion).validate();
        assertThat(response.getEntity()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEditFirmwareVersion() throws Exception {
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("firmwareVersion", "METER1")
                .field("firmwareStatus", FirmwareStatus.FINAL.getStatus())
                .field("imageIdentifier", "10.4.0")
                .bodyPart(new FileDataBodyPart("firmwareFile", File.createTempFile("prefix", "suffix"))).close();

        Response response = target("devicetypes/1/firmwares/1").request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareVersion).setFirmwareVersion("METER1");
        verify(firmwareVersion).setImageIdentifier("10.4.0");
        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareVersion).setMeterFirmwareDependency(null);
        verify(firmwareVersion).setCommunicationFirmwareDependency(null);
        verify(firmwareVersion).update();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEditFirmwareVersionWithDependencies() throws Exception {
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("firmwareVersion", "METER1")
                .field("firmwareStatus", FirmwareStatus.FINAL.getStatus())
                .field("imageIdentifier", "10.4.0")
                .field("meterFirmwareDependency", "25")
                .field("communicationFirmwareDependency", "21")
                .field("auxiliaryFirmwareDependency", "27")
                .bodyPart(new FileDataBodyPart("firmwareFile", File.createTempFile("prefix", "suffix"))).close();

        Response response = target("devicetypes/1/firmwares/1").request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareVersion).setFirmwareVersion("METER1");
        verify(firmwareVersion).setImageIdentifier("10.4.0");
        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareVersion).setMeterFirmwareDependency(meterFWDependency);
        verify(firmwareVersion).setCommunicationFirmwareDependency(commFWDependency);
        verify(firmwareVersion).setAuxiliaryFirmwareDependency(auxFWDependency);
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

    @Test
    public void testReorderFirmwareVersions() {
        doReturn(Arrays.asList(firmwareVersion, commFWDependency, meterFWDependency))
                .when(firmwareService).getOrderedFirmwareVersions(deviceType);

        List<IdWithNameInfo> payload = Arrays.asList(
                new IdWithNameInfo(25, null),
                new IdWithNameInfo(21, null),
                new IdWithNameInfo(1, null)
        );
        Response response = target("devicetypes/1/firmwares/reorder").request().put(Entity.json(payload));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(firmwareService).reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{1, 21, 25}, new long[]{25, 21, 1}));
    }

    @Test
    public void testNeutralReordering() {
        doReturn(Arrays.asList(firmwareVersion, commFWDependency, meterFWDependency))
                .when(firmwareService).getOrderedFirmwareVersions(deviceType);

        List<IdWithNameInfo> payload = Arrays.asList(
                new IdWithNameInfo(1, null),
                new IdWithNameInfo(21, null),
                new IdWithNameInfo(25, null)
        );
        Response response = target("devicetypes/1/firmwares/reorder").request().put(Entity.json(payload));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(firmwareService, never()).reorderFirmwareVersions(any(DeviceType.class), any(KPermutation.class));
    }
}
