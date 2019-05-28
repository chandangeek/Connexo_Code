/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.BadFilterException;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.impl.campaign.DevicesInFirmwareCampaignFilterImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DevicesInFirmwareCampaignFilterTest {
    @Mock
    private FirmwareCampaignService firmwareService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private FirmwareCampaign firmwareCampaign;
    @Mock
    private Device device1, device2;

    @Test
    public void testForCampaign() {
        when(firmwareService.getFirmwareCampaignById(1L)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaign.getId()).thenReturn(1L);

        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId())
                .getCondition();

//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(firmwareCampaign);
    }

    @Test(expected = BadFilterException.class)
    public void testForCampaignNotFound() {
        when(firmwareService.getFirmwareCampaignById(1L)).thenReturn(Optional.empty());
        when(firmwareCampaign.getId()).thenReturn(1L);

        new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId())
                .getCondition();
    }

//    @Test
//    public void testStateSuccess() {
//        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
//                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.SUCCESS))
//                .getCondition();
//
//        assertThat(condition.getClass()).isEqualTo(Contains.class);
//        assertThat(((Contains) condition).getFieldName()).isEqualTo(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName());
//        assertThat(((Contains) condition).getCollection()).containsOnly(
//                DefaultState.UPLOAD_SUCCESS, FirmwareManagementDeviceStatus.ACTIVATION_PENDING,
//                FirmwareManagementDeviceStatus.ACTIVATION_ONGOING, FirmwareManagementDeviceStatus.ACTIVATION_FAILED,
//                FirmwareManagementDeviceStatus.ACTIVATION_SUCCESS, FirmwareManagementDeviceStatus.VERIFICATION_ONGOING,
//                FirmwareManagementDeviceStatus.VERIFICATION_TASK_FAILED, FirmwareManagementDeviceStatus.VERIFICATION_SUCCESS,
//                FirmwareManagementDeviceStatus.VERIFICATION_FAILED);
//    }

    @Test
    public void testStateFailed() {
        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.FAILED))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Comparison.class);
//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(FirmwareManagementDeviceStatus.UPLOAD_FAILED);
    }

    @Test
    public void testStateOngoing() {
        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.ONGOING))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Comparison.class);
//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(FirmwareManagementDeviceStatus.UPLOAD_ONGOING);
    }

    @Test
    public void testStatePending() {
        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.PENDING))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Comparison.class);
//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(FirmwareManagementDeviceStatus.UPLOAD_PENDING);
    }

    @Test
    public void testStateConfigurationError() {
        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.CONFIGURATION_ERROR))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Comparison.class);
//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(FirmwareManagementDeviceStatus.CONFIGURATION_ERROR);
    }

    @Test
    public void testStateCancelled() {
        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.CANCELLED))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Comparison.class);
//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(FirmwareManagementDeviceStatus.CANCELLED);
    }

    @Test
    public void testSingleDevice() {
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device1));
        when(device1.getId()).thenReturn(1L);

        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withDeviceIds(Collections.singletonList(device1.getId()))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Comparison.class);
//        assertThat(((Comparison) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName());
        assertThat(((Comparison) condition).getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(((Comparison) condition).getValues()).containsOnly(device1);
    }

    @Test
    public void testMultipleDevice() {
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device1));
        when(deviceService.findDeviceById(2L)).thenReturn(Optional.of(device2));
        when(device1.getId()).thenReturn(1L);
        when(device2.getId()).thenReturn(2L);

        Condition condition = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withDeviceIds(Arrays.asList(device1.getId(), device2.getId()))
                .getCondition();

        assertThat(condition.getClass()).isEqualTo(Contains.class);
//        assertThat(((Contains) condition).getFieldName()).isEqualToIgnoringCase(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName());
        //assertThat(((Contains) condition).getCollection()).containsOnly(device1, device2);
    }

    @Test
    public void testForCampaignAndStatus() {
        when(firmwareCampaign.getId()).thenReturn(1L);
        when(firmwareService.getFirmwareCampaignById(1)).thenReturn(Optional.of(firmwareCampaign));

        DevicesInFirmwareCampaignFilterImpl filter = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId())
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.SUCCESS));

        DevicesInFirmwareCampaignFilterImpl filter1 = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId());

        DevicesInFirmwareCampaignFilterImpl filter2 = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.SUCCESS));

        Condition condition = filter.getCondition();

        assertThat(condition.implies(filter1.getCondition())).isTrue();
        assertThat(condition.implies(filter2.getCondition())).isTrue();
    }

    @Test
    public void testForCampaignAndStatusFailed() {
        when(firmwareService.getFirmwareCampaignById(1L)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaign.getId()).thenReturn(1L);

        DevicesInFirmwareCampaignFilterImpl filter = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId())
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.FAILED));

        DevicesInFirmwareCampaignFilterImpl filter1 = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId());

        DevicesInFirmwareCampaignFilterImpl filter2 = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withStatus(Collections.singletonList(FirmwareManagementDeviceStatus.Constants.FAILED));

        Condition condition = filter.getCondition();

        assertThat(condition.toString()).isEqualToIgnoringWhitespace("(" + filter1.getCondition().toString() + " AND " + filter2.getCondition().toString() + ")");
    }

    @Test
    public void testForCampaignAndDevice() {
        when(firmwareService.getFirmwareCampaignById(1L)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaign.getId()).thenReturn(1L);
        when(deviceService.findDeviceById(11L)).thenReturn(Optional.of(device1));
        when(device1.getId()).thenReturn(11L);

        DevicesInFirmwareCampaignFilterImpl filter = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId())
                .withDeviceIds(Collections.singletonList(device1.getId()));

        DevicesInFirmwareCampaignFilterImpl filter1 = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withFirmwareCampaignId(this.firmwareCampaign.getId());

        DevicesInFirmwareCampaignFilterImpl filter2 = new DevicesInFirmwareCampaignFilterImpl(firmwareService, deviceService)
                .withDeviceIds(Collections.singletonList(device1.getId()));

        Condition condition = filter.getCondition();

        assertThat(condition.toString()).isEqualToIgnoringWhitespace("(" + filter1.getCondition().toString() + " AND " + filter2.getCondition().toString() + ")");
    }
}
