/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignBuilder;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;
import com.energyict.mdc.firmware.FirmwareCampaignVersionStateShapshot;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.firmware.rest.impl.campaign.DeviceInFirmwareCampaignInfo;
import com.energyict.mdc.firmware.rest.impl.campaign.FirmwareCampaignInfo;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareCampaignResourceTest extends BaseFirmwareTest {

    @Test
    public void testGetFirmwareCampaigns() {
        FirmwareCampaignInfo firmwareCampaign = createCampaignInfo();
        QueryStream queryStream = FakeBuilder.initBuilderStub(Stream.of(firmwareCampaign), QueryStream.class);
        when(firmwareCampaignService.streamAllCampaigns()).thenReturn(queryStream);
        String json = target("campaigns").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].id")).isEqualTo(((Number) firmwareCampaign.id).intValue());
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].version")).isEqualTo(((Number) firmwareCampaign.version).intValue());
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].validationTimeout.count")).isEqualTo(((Number) firmwareCampaign.validationTimeout.count).intValue());
        assertThat(jsonModel.<String>get("$.firmwareCampaigns[0].name")).isEqualTo(firmwareCampaign.name);
        assertThat(jsonModel.<String>get("$.firmwareCampaigns[0].managementOption.localizedValue")).isEqualTo(firmwareCampaign.managementOption.localizedValue);
        assertThat(jsonModel.<String>get("$.firmwareCampaigns[0].deviceGroup")).isEqualTo(firmwareCampaign.deviceGroup);
        assertThat(jsonModel.<String>get("$.firmwareCampaigns[0].firmwareType.localizedValue")).isEqualTo(firmwareCampaign.firmwareType.localizedValue);
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].timeBoundaryStart")).isEqualTo(((Number) firmwareCampaign.timeBoundaryStart.toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].timeBoundaryEnd")).isEqualTo(((Number) firmwareCampaign.timeBoundaryEnd.toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].deviceType.id")).isEqualTo(((Number) firmwareCampaign.deviceType.id).intValue());
        assertThat(jsonModel.<String>get("$.firmwareCampaigns[0].deviceType.localizedValue")).isEqualTo(firmwareCampaign.deviceType.localizedValue);
        assertThat(jsonModel.<Number>get("$.firmwareCampaigns[0].startedOn")).isEqualTo(((Number) firmwareCampaign.startedOn.toEpochMilli()).intValue());
        assertNull(jsonModel.<Number>get("$.firmwareCampaigns[0].finishedOn"));
        assertThat(jsonModel.<String>get("$.firmwareCampaigns[0].status.name")).isEqualTo(firmwareCampaign.status.name);
    }

    @Test
    public void testGetFirmwareCampaign() {
        FirmwareCampaign firmwareCampaign = createCampaignMock();
        when(firmwareCampaignService.getFirmwareCampaignById(firmwareCampaign.getId())).thenReturn(Optional.of(firmwareCampaign));
        String json = target("campaigns/3").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(((Number) firmwareCampaign.getId()).intValue());
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(((Number) firmwareCampaign.getVersion()).intValue());
        assertThat(jsonModel.<Number>get("$.validationTimeout.count")).isEqualTo(((Number) firmwareCampaign.getValidationTimeout().getCount()).intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(firmwareCampaign.getName());
        assertThat(jsonModel.<String>get("$.managementOption.localizedValue")).isEqualTo("Upload firmware/image and activate immediately");
        assertThat(jsonModel.<String>get("$.deviceGroup")).isEqualTo(firmwareCampaign.getDeviceGroup());
        assertThat(jsonModel.<String>get("$.firmwareType.localizedValue")).isEqualTo(firmwareCampaign.getFirmwareType().getType());
        assertThat(jsonModel.<Number>get("$.timeBoundaryStart")).isEqualTo(((Number) firmwareCampaign.getUploadPeriodStart().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.timeBoundaryEnd")).isEqualTo(((Number) firmwareCampaign.getUploadPeriodEnd().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.deviceType.id")).isEqualTo(((Number) firmwareCampaign.getDeviceType().getId()).intValue());
        assertThat(jsonModel.<String>get("$.deviceType.localizedValue")).isEqualTo(firmwareCampaign.getDeviceType().getName());
        assertThat(jsonModel.<Number>get("$.startedOn")).isEqualTo(((Number) firmwareCampaign.getServiceCall().getCreationTime().toEpochMilli()).intValue());
        assertNull(jsonModel.<Number>get("$.finishedOn"));
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo(firmwareCampaign.getServiceCall().getState().name());
    }

    @Test
    public void testGetFirmwareCampaignDevices() {
        FirmwareCampaign firmwareCampaign = createCampaignMock();
        when(firmwareCampaignService.getFirmwareCampaignById(firmwareCampaign.getId())).thenReturn(Optional.of(firmwareCampaign));
        DeviceInFirmwareCampaignInfo deviceInCampaignInfo1 = new DeviceInFirmwareCampaignInfo(1, new IdWithNameInfo(1L, "TestDevice1"),
                new IdWithNameInfo(DefaultState.PENDING,"Pending"), Instant.ofEpochSecond(3600), null);
        DeviceInFirmwareCampaignInfo deviceInCampaignInfo2 = new DeviceInFirmwareCampaignInfo(1, new IdWithNameInfo(2L, "TestDevice2"),
                new IdWithNameInfo(DefaultState.PENDING,"Pending"), Instant.ofEpochSecond(3500), null);
        QueryStream queryStream = FakeBuilder.initBuilderStub(Stream.of(deviceInCampaignInfo1, deviceInCampaignInfo2), QueryStream.class);
        when(firmwareCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        String json = target("campaigns/3/devices").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<DeviceInFirmwareCampaignInfo>>get("$.devicesInCampaign").size() == 2);
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[0].device.id")).isEqualTo(((Number) deviceInCampaignInfo1.device.id).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[0].device.name")).isEqualTo(deviceInCampaignInfo1.device.name);
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[0].startedOn")).isEqualTo(((Number) deviceInCampaignInfo1.startedOn.toEpochMilli()).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[0].status.name")).isEqualTo(deviceInCampaignInfo1.status.name);
        assertNull(jsonModel.<Number>get("$.devicesInCampaign[0].finishedOn"));
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[1].device.id")).isEqualTo(((Number) deviceInCampaignInfo2.device.id).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[1].device.name")).isEqualTo(deviceInCampaignInfo2.device.name);
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[1].startedOn")).isEqualTo(((Number) deviceInCampaignInfo2.startedOn.toEpochMilli()).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[1].status.name")).isEqualTo(deviceInCampaignInfo2.status.name);
        assertNull(jsonModel.<Number>get("$.devicesInCampaign[1].finishedOn"));
    }

    @Test
    public void testGetFirmwareVersionsForFirmwareCampaign(){
        FirmwareCampaign firmwareCampaign = createCampaignMock();
        List<FirmwareCampaignVersionStateShapshot> firmwareCampaignVersionStateShapshots = new ArrayList<>();
        FirmwareCampaignVersionStateShapshot fcvs = mock(FirmwareCampaignVersionStateShapshot.class);
        when(fcvs.getFirmwareVersion()).thenReturn("fvVersion");
        when(fcvs.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(fcvs.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(fcvs.getImageIdentifier()).thenReturn("fvImg");
        when(fcvs.getRank()).thenReturn(1);
        when(fcvs.getMeterFirmwareDependency()).thenReturn("fvMDep");
        when(fcvs.getCommunicationFirmwareDependency()).thenReturn("fvCdep");
        when(fcvs.getAuxiliaryFirmwareDependency()).thenReturn("fvAdep");
        firmwareCampaignVersionStateShapshots.add(fcvs);
        when(firmwareCampaignService.getFirmwareCampaignById(anyLong())).thenReturn(Optional.ofNullable(firmwareCampaign));
        when(firmwareService.findFirmwareCampaignVersionStateSnapshots(firmwareCampaign)).thenReturn(firmwareCampaignVersionStateShapshots);

        String json = target("campaigns/"+firmwareCampaign.getId()+"/firmwareversions").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<DeviceInFirmwareCampaignInfo>>get("$.firmwareCampaignVersionStateInfos").size() == 1);
        assertThat(jsonModel.<String>get("$.firmwareCampaignVersionStateInfos[0].firmwareVersion")).isEqualTo("fvVersion");
    }

    @Test
    public void testCreateFirmwareCampaign() throws Exception {
        FirmwareCampaign firmwareCampaign = createCampaignMock();
        FirmwareCampaignBuilder fakeBuilder = FakeBuilder.initBuilderStub(firmwareCampaign, FirmwareCampaignBuilder.class);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(50));
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        when(deviceConfigurationService.findDeviceType(anyLong())).thenReturn(Optional.ofNullable(deviceType));
        when(firmwareCampaignService.newFirmwareCampaign("TestCampaign")).thenReturn(fakeBuilder);
        FirmwareVersion firmwareVersion = firmwareCampaign.getFirmwareVersion();
        when(firmwareService.getFirmwareVersionById(8)).thenReturn(Optional.ofNullable(firmwareVersion));
        when(firmwareService.bestSuitableFirmwareUpgradeMessageId(any(), any(), any())).thenReturn(Optional.of(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE));
        Optional<DeviceMessageSpec> deviceMessageSpec = firmwareCampaign.getFirmwareMessageSpec();
        when(firmwareService.getFirmwareMessageSpec(any(), any(), any())).thenReturn(deviceMessageSpec);
        Finder<FirmwareVersion> firmwaresFinder = mockFinder(Collections.singletonList(firmwareVersion));
        FirmwareVersionFilter firmwareVersionFilter = mock(FirmwareVersionFilter.class);
        when(firmwareService.filterForFirmwareVersion(deviceType)).thenReturn(firmwareVersionFilter);
        when(firmwareService.findAllFirmwareVersions(firmwareService.filterForFirmwareVersion(deviceType))).thenReturn(firmwaresFinder);
        FirmwareCampaignManagementOptions options = mock(FirmwareCampaignManagementOptions.class);
        when(firmwareService.newFirmwareCampaignCheckManagementOptions(firmwareCampaign)).thenReturn(options);

        Response response = target("campaigns").request().post(Entity.json(createCampaignInfo()));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(((Number) firmwareCampaign.getId()).intValue());
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(((Number) firmwareCampaign.getVersion()).intValue());
        assertThat(jsonModel.<Number>get("$.validationTimeout.count")).isEqualTo(((Number) firmwareCampaign.getValidationTimeout().getCount()).intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(firmwareCampaign.getName());
        assertThat(jsonModel.<String>get("$.managementOption.localizedValue")).isEqualTo("Upload firmware/image and activate immediately");
        assertThat(jsonModel.<String>get("$.deviceGroup")).isEqualTo(firmwareCampaign.getDeviceGroup());
        assertThat(jsonModel.<String>get("$.firmwareType.localizedValue")).isEqualTo(firmwareCampaign.getFirmwareType().getType());
        assertThat(jsonModel.<Number>get("$.timeBoundaryStart")).isEqualTo(((Number) firmwareCampaign.getUploadPeriodStart().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.timeBoundaryEnd")).isEqualTo(((Number) firmwareCampaign.getUploadPeriodEnd().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.deviceType.id")).isEqualTo(((Number) firmwareCampaign.getDeviceType().getId()).intValue());
        assertThat(jsonModel.<String>get("$.deviceType.localizedValue")).isEqualTo(firmwareCampaign.getDeviceType().getName());
        assertThat(jsonModel.<Number>get("$.validationComTask.id")).isEqualTo(((Number)firmwareCampaign.getValidationComTaskId()).intValue());
        assertThat(jsonModel.<Number>get("$.calendarUploadComTask.id")).isEqualTo(((Number)firmwareCampaign.getFirmwareUploadComTaskId()).intValue());
    }

    @Test
    public void testEditCampaign() {
        when(clock.instant()).thenReturn(Instant.now());
        FirmwareCampaignInfo firmwareCampaignInfo = createCampaignInfo();
        FirmwareCampaign firmwareCampaign = createCampaignMock();
        when(firmwareCampaignService.findAndLockFirmwareCampaignByIdAndVersion(firmwareCampaignInfo.id, firmwareCampaignInfo.version))
                .thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaignService.getFirmwareCampaignById(3)).thenReturn(Optional.of(firmwareCampaign));
        Response response = target("campaigns/3").request().put(Entity.json(firmwareCampaignInfo));
        verify(firmwareCampaign).update();
    }

    @Test
    public void testCancelCampaign() {
        FirmwareCampaignInfo firmwareCampaignInfo = createCampaignInfo();
        FirmwareCampaign firmwareCampaign = createCampaignMock();
        when(firmwareCampaignService.findAndLockFirmwareCampaignByIdAndVersion(firmwareCampaignInfo.id, firmwareCampaignInfo.version))
                .thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaignService.getFirmwareCampaignById(3)).thenReturn(Optional.of(firmwareCampaign));
        Response response = target("campaigns/3/cancel").request().put(Entity.json(firmwareCampaignInfo));
        verify(firmwareCampaign).cancel();
    }

    private FirmwareCampaignInfo createCampaignInfo() {
        FirmwareCampaignInfo firmwareCampaignInfo = new FirmwareCampaignInfo();
        firmwareCampaignInfo.id = 3L;
        firmwareCampaignInfo.version = 4L;
        firmwareCampaignInfo.validationTimeout = new TimeDurationInfo(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES));
        firmwareCampaignInfo.name = "TestCampaign";
        firmwareCampaignInfo.managementOption = new ManagementOptionInfo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId(), "Upload firmware/image and activate immediately");
        firmwareCampaignInfo.deviceGroup = "TestGroup";
        firmwareCampaignInfo.firmwareType = new FirmwareTypeInfo(FirmwareType.METER, thesaurus);
        firmwareCampaignInfo.timeBoundaryStart = Instant.ofEpochSecond(100);
        firmwareCampaignInfo.timeBoundaryEnd = Instant.ofEpochSecond(200);
        firmwareCampaignInfo.deviceType = new IdWithLocalizedValue<>(1L, "TestDeviceType");
        firmwareCampaignInfo.startedOn = Instant.ofEpochSecond(111);
        firmwareCampaignInfo.finishedOn = null;
        firmwareCampaignInfo.status = new IdWithNameInfo(DefaultState.ONGOING.name(),"Ongoing");
        firmwareCampaignInfo.validationComTask = new IdWithNameInfo(2L,"comTaskName");
        firmwareCampaignInfo.validationConnectionStrategy = new IdWithNameInfo(0L,"Minimize connections");
        firmwareCampaignInfo.calendarUploadConnectionStrategy = new IdWithNameInfo(0L,"As soon as possible");
        firmwareCampaignInfo.calendarUploadComTask = new IdWithNameInfo(1L,"comTaskName");
        ArrayList<PropertyInfo> propertyInfos = new ArrayList<>();
        propertyInfos.add(new PropertyInfo("Firmware file", "FirmwareDeviceMessage.upgrade.userfile", new PropertyValueInfo<>(8, ""), null, true));
        firmwareCampaignInfo.properties = propertyInfos;
        firmwareCampaignInfo.checkOptions = mock(EnumMap.class);
        firmwareCampaignInfo.withUniqueFirmwareVersion = false;
        return firmwareCampaignInfo;
    }

    private FirmwareCampaign createCampaignMock() {
        FirmwareCampaign firmwareCampaign = mock(FirmwareCampaign.class);
        ComTask comtask = mock(ComTask.class);
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(firmwareCampaign.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(111));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        FirmwareType firmwareType = FirmwareType.METER;
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("FV1");
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(firmwareVersion.getFirmwareType()).thenReturn(firmwareType);
        when(firmwareVersion.getCommunicationFirmwareDependency()).thenReturn(Optional.of(firmwareVersion));
        when(firmwareVersion.getAuxiliaryFirmwareDependency()).thenReturn(Optional.of(firmwareVersion));
        when(firmwareVersion.getMeterFirmwareDependency()).thenReturn(Optional.of(firmwareVersion));
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(firmwareCampaign.getFirmwareType()).thenReturn(firmwareType);
        when(firmwareCampaign.getName()).thenReturn("TestCampaign");
        when(firmwareCampaign.getDeviceType()).thenReturn(deviceType);
        when(firmwareCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(firmwareCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(firmwareCampaign.getFirmwareManagementOption()).thenReturn(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        when(firmwareCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getValidationTimeout()).thenReturn(new TimeDuration(2, TimeDuration.TimeUnit.MINUTES));
        when(firmwareCampaign.getId()).thenReturn(3L);
        when(firmwareCampaign.getVersion()).thenReturn(4L);
        when(firmwareCampaign.getFirmwareMessageSpec()).thenReturn(Optional.ofNullable(deviceMessageSpec));
        when(firmwareCampaign.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(firmwareCampaign.getStartedOn()).thenReturn(Instant.ofEpochSecond(111));
        when(firmwareCampaign.isWithUniqueFirmwareVersion()).thenReturn(false);
        FirmwareCampaignManagementOptions firmwareCampaignMgtOptions = mock(FirmwareCampaignManagementOptions.class);
        when(firmwareService.findFirmwareCampaignCheckManagementOptions(firmwareCampaign)).thenReturn(Optional.of(firmwareCampaignMgtOptions));
        when(firmwareCampaign.getFirmwareUploadComTaskId()).thenReturn(1L);
        when(firmwareCampaign.getFirmwareUploadConnectionStrategy()).thenReturn(Optional.of(ConnectionStrategy.AS_SOON_AS_POSSIBLE));
        when(firmwareCampaign.getValidationComTaskId()).thenReturn(2L);
        when(firmwareCampaign.getValidationConnectionStrategy()).thenReturn(Optional.of(ConnectionStrategy.MINIMIZE_CONNECTIONS));
        when(comtask.getName()).thenReturn("comTaskName");
        when(firmwareCampaignService.getComTaskById(anyLong())).thenReturn(comtask);
        return firmwareCampaign;
    }
}
