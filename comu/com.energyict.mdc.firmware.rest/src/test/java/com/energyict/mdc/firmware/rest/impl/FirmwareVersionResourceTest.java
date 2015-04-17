package com.energyict.mdc.firmware.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.jayway.jsonpath.JsonModel;

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

        verify(firmwareVersion).setFirmwareFile(new byte[firmwareVersionInfo.fileSize]);
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

        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
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

        Response response = target("devicetypes/1/firmwares/1").request().put(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
    
    @Test
    public void testDepricateFirmwareVersion() {
        FirmwareVersionInfo info = new FirmwareVersionInfo();
        info.firmwareStatus = new FirmwareStatusInfo();
        info.firmwareStatus.id = FirmwareStatus.DEPRECATED;
        
        Response response = target("devicetypes/1/firmwares/1").request().put(Entity.json(info));
        
        verify(firmwareService).deprecateFirmwareVersion(firmwareVersion);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
    
    @Test
    public void testSetaAsFinalFirmwareVersion() {
        FirmwareVersionInfo info = new FirmwareVersionInfo();
        info.firmwareStatus = new FirmwareStatusInfo();
        info.firmwareStatus.id = FirmwareStatus.FINAL;
        
        Response response = target("devicetypes/1/firmwares/1").request().put(Entity.json(info));
        
        verify(firmwareVersion).setFirmwareStatus(FirmwareStatus.FINAL);
        verify(firmwareService).saveFirmwareVersion(firmwareVersion);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
