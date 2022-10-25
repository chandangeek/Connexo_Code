/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Effective;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class MasterHasLatestFirmwareCheckTest extends AbstractFirmwareCheckTest {

    private QueryStream<ActivatedFirmwareVersion> activatedFirmwareVersionStream;
    @Captor
    private ArgumentCaptor<Condition> conditionCaptor;
    @Mock
    private DeviceType masterDeviceType;
    @Mock
    private Device master;
    @Mock
    private FirmwareManagementDeviceUtils masterFirmwareDeviceUtils;

    public MasterHasLatestFirmwareCheckTest() {
        super(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK, MasterHasLatestFirmwareCheck.class);
    }

    @Override
    public void setUp() {
        super.setUp();
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(master));
        when(master.getDeviceType()).thenReturn(masterDeviceType);
        when(firmwareService.getFirmwareManagementDeviceUtilsFor(master)).thenReturn(masterFirmwareDeviceUtils);
        when(masterFirmwareDeviceUtils.getDevice()).thenReturn(master);
        when(masterFirmwareDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(true);
        activatedFirmwareVersionStream = FakeBuilder.initBuilderStub(false, QueryStream.class);
        when(dataModel.stream(ActivatedFirmwareVersion.class)).thenAnswer(invocation -> activatedFirmwareVersionStream);
        when(firmwareService.isFirmwareTypeSupported(masterDeviceType, FirmwareType.METER)).thenReturn(true);
        when(firmwareService.isFirmwareTypeSupported(masterDeviceType, FirmwareType.COMMUNICATION)).thenReturn(true);
        when(firmwareService.isFirmwareTypeSupported(masterDeviceType, FirmwareType.AUXILIARY)).thenReturn(true);
        when(firmwareService.getActiveFirmwareVersion(master, FirmwareType.METER)).thenReturn(Optional.of(activatedMeterFirmware));
        when(firmwareService.getActiveFirmwareVersion(master, FirmwareType.COMMUNICATION)).thenReturn(Optional.of(activatedCommunicationFirmware));
        when(firmwareService.getActiveFirmwareVersion(master, FirmwareType.AUXILIARY)).thenReturn(Optional.of(activatedAuxiliaryFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.METER)), anySetOf(FirmwareStatus.class))).thenReturn(Optional.of(activeMeterFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), anySetOf(FirmwareStatus.class))).thenReturn(Optional.of(activeCommunicationFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), anySetOf(FirmwareStatus.class))).thenReturn(Optional.of(activeAuxiliaryFirmware));
    }

    @Test
    public void testMaximumFirmwareVersionsOnMaster() {
        expectSuccess();

        verify(firmwareService, times(3)).getMaximumFirmware(eq(masterDeviceType), anySetOf(FirmwareType.class), eq(EnumSet.of(FirmwareStatus.FINAL, FirmwareStatus.TEST)));
        verify(activatedFirmwareVersionStream, times(3)).filter(conditionCaptor.capture());
        List<Condition> filterConditions = conditionCaptor.getAllValues();
        Optional<Comparison> deviceConditionOptional = filterConditions.stream()
                .filter(Comparison.class::isInstance)
                .findAny()
                .map(Comparison.class::cast);
        assertThat(deviceConditionOptional).isPresent();
        assertThat(deviceConditionOptional.map(Comparison::getFieldName)).isPresent();
        assertThat(deviceConditionOptional.map(Comparison::getFieldName).get()).contains("device");
        assertThat(deviceConditionOptional.map(Comparison::getValues)).contains(new Device[]{master});
        Optional<Contains> firmwareTypeConditionOptional = filterConditions.stream()
                .filter(Contains.class::isInstance)
                .findAny()
                .map(Contains.class::cast);
        assertThat(firmwareTypeConditionOptional).isPresent();
        assertThat(firmwareTypeConditionOptional.map(Contains::getFieldName)).isPresent();
        assertThat(firmwareTypeConditionOptional.map(Contains::getFieldName).get()).contains("firmwareType");
        assertThat(firmwareTypeConditionOptional.map(Contains::getCollection)).contains(Arrays.asList(FirmwareType.METER, FirmwareType.COMMUNICATION, FirmwareType.AUXILIARY));
        Optional<Effective> effectiveConditionOptional = filterConditions.stream()
                .filter(Effective.class::isInstance)
                .findAny()
                .map(Effective.class::cast);
        assertThat(effectiveConditionOptional).isPresent();
        assertThat(effectiveConditionOptional.map(Effective::getFieldName)).isPresent();
        assertThat(effectiveConditionOptional.map(Effective::getFieldName).get()).contains("interval");

        verify(activatedFirmwareVersionStream).anyMatch(conditionCaptor.capture());
        Condition condition = conditionCaptor.getValue();
        assertThat(condition).isInstanceOf(Comparison.class);
        Comparison firmwareStatusCondition = (Comparison) condition;
        assertThat(firmwareStatusCondition.getFieldName()).contains("firmwareStatus");
        assertThat(firmwareStatusCondition.getValues()).containsOnly(FirmwareStatus.GHOST);
    }

    @Test
    public void testOldMeterFWOnMaster() {
        FirmwareVersion oldVersion = mock(FirmwareVersion.class);
        when(activatedMeterFirmware.getFirmwareVersion()).thenReturn(oldVersion);

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testOldCommFWOnMaster() {
        FirmwareVersion oldVersion = mock(FirmwareVersion.class);
        when(activatedCommunicationFirmware.getFirmwareVersion()).thenReturn(oldVersion);

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testOldAuxFWOnMaster() {
        FirmwareVersion oldVersion = mock(FirmwareVersion.class);
        when(activatedAuxiliaryFirmware.getFirmwareVersion()).thenReturn(oldVersion);

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testNoActiveMeterFWOnMaster() {
        when(firmwareService.getActiveFirmwareVersion(master, FirmwareType.METER)).thenReturn(Optional.empty());

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testNoActiveCommFWOnMaster() {
        when(firmwareService.getActiveFirmwareVersion(master, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testNoActiveAuxFWOnMaster() {
        when(firmwareService.getActiveFirmwareVersion(master, FirmwareType.AUXILIARY)).thenReturn(Optional.empty());

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testNoMeterFWOnMaster() {
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.METER)), anySetOf(FirmwareStatus.class))).thenReturn(Optional.empty());

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testNoCommFWOnMaster() {
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), anySetOf(FirmwareStatus.class))).thenReturn(Optional.empty());

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testNoAuxFWOnMaster() {
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), anySetOf(FirmwareStatus.class))).thenReturn(Optional.empty());

        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testCommunicationFWNotSupported() {
        when(firmwareService.isFirmwareTypeSupported(masterDeviceType, FirmwareType.COMMUNICATION)).thenReturn(false);

        expectSuccess();
        verify(firmwareService, never()).getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), anySetOf(FirmwareStatus.class));
        verify(firmwareService, never()).getActiveFirmwareVersion(master, FirmwareType.COMMUNICATION);
    }

    @Test
    public void testAuxiliaryFWNotSupported() {
        when(firmwareService.isFirmwareTypeSupported(masterDeviceType, FirmwareType.AUXILIARY)).thenReturn(false);

        expectSuccess();
        verify(firmwareService, never()).getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), anySetOf(FirmwareStatus.class));
        verify(firmwareService, never()).getActiveFirmwareVersion(master, FirmwareType.AUXILIARY);
    }

    @Test
    public void testMasterHasLatestFinalFirmwareVersions() {
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.METER)), eq(EnumSet.of(FirmwareStatus.TEST)))).thenReturn(Optional.empty());
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), eq(EnumSet.of(FirmwareStatus.TEST)))).thenReturn(Optional.empty());
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), eq(EnumSet.of(FirmwareStatus.TEST)))).thenReturn(Optional.empty());
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.METER)), eq(EnumSet.of(FirmwareStatus.FINAL)))).thenReturn(Optional.of(activeMeterFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), eq(EnumSet.of(FirmwareStatus.FINAL)))).thenReturn(Optional.of(activeCommunicationFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), eq(EnumSet.of(FirmwareStatus.FINAL)))).thenReturn(Optional.of(activeAuxiliaryFirmware));

        when(firmwareCampaignManagementOptions.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.FINAL));
        expectSuccess();
        verify(firmwareService, times(3)).getMaximumFirmware(eq(masterDeviceType), anySetOf(FirmwareType.class), eq(EnumSet.of(FirmwareStatus.FINAL)));

        when(firmwareCampaignManagementOptions.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.TEST));
        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testMasterHasLatestTestFirmwareVersions() {
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.METER)), eq(EnumSet.of(FirmwareStatus.TEST)))).thenReturn(Optional.of(activeMeterFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), eq(EnumSet.of(FirmwareStatus.TEST)))).thenReturn(Optional.of(activeCommunicationFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), eq(EnumSet.of(FirmwareStatus.TEST)))).thenReturn(Optional.of(activeAuxiliaryFirmware));
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.METER)), eq(EnumSet.of(FirmwareStatus.FINAL)))).thenReturn(Optional.empty());
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.COMMUNICATION)), eq(EnumSet.of(FirmwareStatus.FINAL)))).thenReturn(Optional.empty());
        when(firmwareService.getMaximumFirmware(eq(masterDeviceType), eq(EnumSet.of(FirmwareType.AUXILIARY)), eq(EnumSet.of(FirmwareStatus.FINAL)))).thenReturn(Optional.empty());

        when(firmwareCampaignManagementOptions.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.TEST));
        expectSuccess();
        verify(firmwareService, times(3)).getMaximumFirmware(eq(masterDeviceType), anySetOf(FirmwareType.class), eq(EnumSet.of(FirmwareStatus.TEST)));

        when(firmwareCampaignManagementOptions.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.FINAL));
        expectError("Firmware types on the master don't have the highest level (among firmware types with the acceptable status).");
    }

    @Test
    public void testFirmwareNotReadOut() {
        when(masterFirmwareDeviceUtils.isReadOutAfterLastFirmwareUpgrade()).thenReturn(false);

        expectError("Firmware on the master hasn't been read out after the last upload.");
    }

    @Test
    public void testGhostFound() {
        activatedFirmwareVersionStream = FakeBuilder.initBuilderStub(true, QueryStream.class);

        expectError("There is firmware with \"Ghost\" status on the master device.");
    }

    @Test
    public void testDeviceHasNoMaster() {
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());

        expectSuccess();
        verifyZeroInteractions(master, masterDeviceType, masterFirmwareDeviceUtils);
        verify(firmwareService, never()).getMaximumFirmware(eq(masterDeviceType), anySetOf(FirmwareType.class), anySetOf(FirmwareStatus.class));
        verify(firmwareService, never()).getActiveFirmwareVersion(eq(master), any(FirmwareType.class));
        verify(firmwareService, never()).isFirmwareTypeSupported(eq(masterDeviceType), any(FirmwareType.class));
    }

    @Test
    public void testCheckNotActivated() {
        when(firmwareService.getActiveFirmwareVersion(eq(master), any(FirmwareType.class))).thenReturn(Optional.empty());
        when(firmwareCampaignManagementOptions.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(false);
        FirmwareManagementOptions masterFirmwareManagementOptions = mock(FirmwareManagementOptions.class);
        when(firmwareService.findFirmwareManagementOptions(masterDeviceType)).thenReturn(Optional.of(masterFirmwareManagementOptions));
        when(masterFirmwareManagementOptions.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(true);

        expectSuccess();
    }

}
