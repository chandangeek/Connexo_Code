/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FirmwareManagementOptionsResourceTest extends BaseFirmwareTest {
    @Mock
    private DeviceType deviceType;
    @Mock
    private FirmwareManagementOptions options;

    private ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER;
    private Set<ProtocolSupportedFirmwareOptions> set = new HashSet<>();
    private String URI = "devicetypes/1/firmwaremanagementoptions/1";

    @Before
    public void setUpStubs() {
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());

        set.add(protocolSupportedFirmwareOptions);
        when(firmwareService.getSupportedFirmwareOptionsFor(deviceType)).thenReturn(set);
        when(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).thenReturn(set);

        when(firmwareService.findFirmwareManagementOptions(deviceType)).thenReturn(Optional.of(options));
        when(firmwareService.findAndLockFirmwareManagementOptionsByIdAndVersion(eq(deviceType), anyLong())).thenReturn(Optional.of(options));
    }

    @Test
    public void testNoFirmwareManagementOptions() {
        when(firmwareService.findFirmwareManagementOptions(deviceType)).thenReturn(Optional.empty());
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.supportedOptions[0].id")).isEqualTo(protocolSupportedFirmwareOptions.getId());
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(0);
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(false);
        Arrays.stream(FirmwareCheckManagementOption.values()).forEach(checkOption -> {
            assertThat(jsonModel.<Boolean>get("$.checkOptions." + checkOption.name() + ".activated")).isFalse();
            assertThat(jsonModel.<Object>get("$.checkOptions." + checkOption.name() + ".statuses")).isNull();
        });
    }

    @Test
    public void testGetFirmwareManagementOptions() {
        when(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).thenReturn(new HashSet<>());
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.supportedOptions[0].id")).isEqualTo(protocolSupportedFirmwareOptions.getId());
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(0);
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(false);
        Arrays.stream(FirmwareCheckManagementOption.values()).forEach(checkOption -> {
            assertThat(jsonModel.<Boolean>get("$.checkOptions." + checkOption.name() + ".activated")).isFalse();
            assertThat(jsonModel.<Object>get("$.checkOptions." + checkOption.name() + ".statuses")).isNull();
        });
    }

    @Test
    public void testGetFirmwareManagementOptionsAllowed() {
        when(options.getOptions()).thenReturn(set);
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.supportedOptions[0].id")).isEqualTo(protocolSupportedFirmwareOptions.getId());
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.allowedOptions[0].id")).isEqualTo(protocolSupportedFirmwareOptions.getId());
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(true);
        Arrays.stream(FirmwareCheckManagementOption.values()).forEach(checkOption -> {
            assertThat(jsonModel.<Boolean>get("$.checkOptions." + checkOption.name() + ".activated")).isFalse();
            assertThat(jsonModel.<Object>get("$.checkOptions." + checkOption.name() + ".statuses")).isNull();
        });
    }

    @Test
    public void testGetFirmwareManagementOptionsWithActivatedCheck() {
        when(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(true);
        when(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.TEST));
        when(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(EnumSet.noneOf(FirmwareStatus.class));
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Boolean>get("$.checkOptions.MASTER_FIRMWARE_CHECK.activated")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.MASTER_FIRMWARE_CHECK.statuses")).containsOnly(FirmwareStatus.TEST.name());
        assertThat(jsonModel.<Boolean>get("$.checkOptions.TARGET_FIRMWARE_STATUS_CHECK.activated")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.TARGET_FIRMWARE_STATUS_CHECK.statuses")).isEmpty();
        assertThat(jsonModel.<Boolean>get("$.checkOptions.CURRENT_FIRMWARE_CHECK.activated")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.CURRENT_FIRMWARE_CHECK.statuses")).isNull();
    }

    @Test
    public void testGetFirmwareManagementOptionsWithAllActivatedChecks() {
        when(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).thenReturn(true);
        when(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(true);
        when(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.TEST));
        when(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(true);
        when(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.FINAL));
        String json = target(URI).request().get(String.class);

        System.out.println(json);
        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<Boolean>get("$.checkOptions.MASTER_FIRMWARE_CHECK.activated")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.MASTER_FIRMWARE_CHECK.statuses")).containsOnly(FirmwareStatus.FINAL.name());
        assertThat(jsonModel.<Boolean>get("$.checkOptions.TARGET_FIRMWARE_STATUS_CHECK.activated")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.TARGET_FIRMWARE_STATUS_CHECK.statuses")).containsOnly(FirmwareStatus.TEST.name());
        assertThat(jsonModel.<Boolean>get("$.checkOptions.CURRENT_FIRMWARE_CHECK.activated")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.CURRENT_FIRMWARE_CHECK.statuses")).isNull();
    }

    @Test
    public void testTurnOffFirmwareManagementOptions() {
        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        firmwareManagementOptionsInfo.isAllowed = false;

        target(URI).request().put(Entity.json(firmwareManagementOptionsInfo));

        verify(options).delete();
    }

    @Test
    public void testPutFirmwareManagementOptions() {
        when(options.getOptions()).thenReturn(set);
        when(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).thenReturn(true);
        when(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(true);
        when(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).thenReturn(EnumSet.of(FirmwareStatus.FINAL));
        when(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).thenReturn(EnumSet.noneOf(FirmwareStatus.class));

        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        firmwareManagementOptionsInfo.isAllowed = true;
        firmwareManagementOptionsInfo.allowedOptions = Collections.singletonList(new ManagementOptionInfo(protocolSupportedFirmwareOptions.getId(), null));
        firmwareManagementOptionsInfo.checkOptions.put(
                FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK, new CheckManagementOptionInfo(true, EnumSet.of(FirmwareStatus.FINAL)));
        firmwareManagementOptionsInfo.checkOptions.put(
                FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, new CheckManagementOptionInfo(true, null));
        firmwareManagementOptionsInfo.checkOptions.put(
                FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK, new CheckManagementOptionInfo(false, null));

        String json = target(URI).request().put(Entity.json(firmwareManagementOptionsInfo), String.class);

        verify(options).setOptions(EnumSet.of(protocolSupportedFirmwareOptions));
        verify(options).activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK, EnumSet.of(FirmwareStatus.FINAL));
        verify(options).activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, null);
        verify(options).deactivate(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK);
        verify(options).save();

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<?>>get("$.supportedOptions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.supportedOptions[0].id")).isEqualTo(protocolSupportedFirmwareOptions.getId());
        assertThat(jsonModel.<List<?>>get("$.allowedOptions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.allowedOptions[0].id")).isEqualTo(protocolSupportedFirmwareOptions.getId());
        assertThat(jsonModel.<Boolean>get("$.isAllowed")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.checkOptions.MASTER_FIRMWARE_CHECK.activated")).isFalse();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.MASTER_FIRMWARE_CHECK.statuses")).isEmpty();
        assertThat(jsonModel.<Boolean>get("$.checkOptions.TARGET_FIRMWARE_STATUS_CHECK.activated")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.TARGET_FIRMWARE_STATUS_CHECK.statuses")).containsOnly(FirmwareStatus.FINAL.name());
        assertThat(jsonModel.<Boolean>get("$.checkOptions.CURRENT_FIRMWARE_CHECK.activated")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.checkOptions.CURRENT_FIRMWARE_CHECK.statuses")).isNull();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UPGRADE_OPTIONS_REQUIRED + "}", property = "key", strict = false)
    public void testPutFirmwareManagementOptionsException() {
        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        firmwareManagementOptionsInfo.isAllowed = true;

        target(URI).request().put(Entity.json(firmwareManagementOptionsInfo));
    }
}
